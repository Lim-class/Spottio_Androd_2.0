package com.example.spottio.posts.create

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.spottio.R
import com.example.spottio.auth.AuthManager
import com.example.spottio.posts.data.MediaItem
import com.example.spottio.posts.data.Post
import com.example.spottio.users.UserCache
import com.example.spottio.utils.CloudinaryHelper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddPostFragment : Fragment() {

    private var selectedMediaUris = mutableListOf<Uri>()
    private var selectedCategories = mutableListOf("Generale")

    private lateinit var rvMediaPreview: RecyclerView
    private lateinit var tvMediaCount: TextView
    private lateinit var autoCompleteCategory: AutoCompleteTextView
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var etText: EditText
    private lateinit var btnPublish: Button

    private val categoryList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var previewAdapter: MediaPreviewAdapter

    private val db = FirebaseFirestore.getInstance()

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val totalFiles = selectedMediaUris.size + uris.size
            if (totalFiles > 10) {
                Toast.makeText(context, "Puoi caricare un massimo di 10 file.", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }

            selectedMediaUris.addAll(uris)
            previewAdapter.notifyDataSetChanged()

            rvMediaPreview.visibility = View.VISIBLE
            tvMediaCount.visibility = View.VISIBLE
            tvMediaCount.text = "${selectedMediaUris.size}/10 file selezionati"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)

        etText = view.findViewById(R.id.etPostText)
        val btnMedia = view.findViewById<Button>(R.id.btnAttachMedia)
        btnPublish = view.findViewById(R.id.btnPublish)
        val btnAddCategory = view.findViewById<View>(R.id.btnAddCategory)
        rvMediaPreview = view.findViewById(R.id.rvMediaPreview)
        tvMediaCount = view.findViewById(R.id.tvMediaCount)
        autoCompleteCategory = view.findViewById(R.id.autoCompleteCategory)
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories)

        previewAdapter = MediaPreviewAdapter(selectedMediaUris)
        rvMediaPreview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvMediaPreview.adapter = previewAdapter

        adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        autoCompleteCategory.setAdapter(adapter)

        fetchCategories()
        refreshCategoryChips()

        btnAddCategory.setOnClickListener {
            val newCat = autoCompleteCategory.text.toString().trim()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            if (newCat.isNotEmpty() && !selectedCategories.contains(newCat)) {
                selectedCategories.add(newCat)
                refreshCategoryChips()
                autoCompleteCategory.setText("")

                if (!categoryList.contains(newCat)) {
                    val catData = hashMapOf(
                        "name" to newCat,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                    db.collection("categories").add(catData)
                    categoryList.add(newCat)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        btnMedia.setOnClickListener { galleryLauncher.launch("*/*") }
        btnPublish.setOnClickListener { handlePublish() }

        return view
    }

    private fun refreshCategoryChips() {
        chipGroupCategories.removeAllViews()
        for (category in selectedCategories) {
            val chip = Chip(requireContext()).apply {
                text = category
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    selectedCategories.remove(category)
                    if (selectedCategories.isEmpty()) selectedCategories.add("Generale")
                    refreshCategoryChips()
                }
            }
            chipGroupCategories.addView(chip)
        }
    }

    private fun fetchCategories() {
        db.collection("categories")
            .orderBy("name")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    categoryList.clear()
                    categoryList.add("Generale")
                    for (document in task.result!!) {
                        document.getString("name")?.let {
                            if(!categoryList.contains(it)) categoryList.add(it)
                        }
                    }
                    if (isAdded) adapter.notifyDataSetChanged()
                }
            }
    }

    private fun handlePublish() {
        val text = etText.text.toString().trim()

        if (text.isEmpty() && selectedMediaUris.isEmpty()) {
            Toast.makeText(context, "Inserisci un testo o allega almeno un file!", Toast.LENGTH_SHORT).show()
            return
        }

        val safeContext = context ?: return
        val currentUid = AuthManager.getCurrentUserUid(safeContext)
        val cachedUser = UserCache.getUser(currentUid)
        val currentUsername = cachedUser?.username ?: "Anonimo"

        if (currentUid.isEmpty()) {
            Toast.makeText(safeContext, "Errore: sessione non valida.", Toast.LENGTH_SHORT).show()
            return
        }

        // Blocca il bottone e mostra il caricamento
        btnPublish.isEnabled = false
        val originalBtnText = btnPublish.text.toString()
        btnPublish.text = "Pubblicazione in corso..."

        // Lancia il processo in background (Coroutines)
        lifecycleScope.launch {
            try {
                val uploadedMediaList = mutableListOf<MediaItem>()

                if (selectedMediaUris.isNotEmpty()) {
                    var uploadedCount = 0
                    for (uri in selectedMediaUris) {
                        val type = safeContext.contentResolver.getType(uri)
                        val isVideo = type != null && type.startsWith("video")

                        uploadedCount++
                        withContext(Dispatchers.Main) {
                            btnPublish.text = "Caricamento file ($uploadedCount/${selectedMediaUris.size})..."
                        }

                        // Carica fisicamente il file su Cloudinary
                        val remoteUrl = CloudinaryHelper.uploadMedia(safeContext, uri, isVideo)

                        if (remoteUrl != null) {
                            uploadedMediaList.add(MediaItem(url = remoteUrl, isVideo = isVideo))
                        } else {
                            throw Exception("Caricamento del file fallito.")
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    btnPublish.text = "Salvataggio post..."
                }

                // Crea il post con gli URL remoti (identico alla web-app)
                val newPost = Post(
                    user = currentUid,
                    author = currentUsername,
                    text = text,
                    mediaUri = uploadedMediaList.firstOrNull()?.url,
                    isVideo = uploadedMediaList.firstOrNull()?.isVideo ?: false,
                    mediaList = uploadedMediaList,
                    categories = selectedCategories.toMutableList()
                )

                // Salva su Firebase
                db.collection("posts")
                    .add(newPost)
                    .addOnSuccessListener {
                        Toast.makeText(safeContext, "Post pubblicato con successo! \uD83C\uDF89", Toast.LENGTH_SHORT).show()
                        resetForm(originalBtnText)
                        activity?.findViewById<ViewPager2>(R.id.viewPager)?.setCurrentItem(0, true)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(safeContext, "Errore durante il salvataggio", Toast.LENGTH_SHORT).show()
                        resetBtnError(originalBtnText)
                        Log.e("Firestore", "Err: ${e.message}")
                    }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(safeContext, "Errore: ${e.message}", Toast.LENGTH_LONG).show()
                    resetBtnError(originalBtnText)
                }
            }
        }
    }

    private fun resetForm(originalText: String) {
        etText.text.clear()
        selectedMediaUris.clear()
        selectedCategories.clear()
        selectedCategories.add("Generale")
        refreshCategoryChips()
        previewAdapter.notifyDataSetChanged()
        rvMediaPreview.visibility = View.GONE
        tvMediaCount.visibility = View.GONE
        btnPublish.isEnabled = true
        btnPublish.text = originalText
    }

    private fun resetBtnError(originalText: String) {
        btnPublish.isEnabled = true
        btnPublish.text = originalText
    }
}