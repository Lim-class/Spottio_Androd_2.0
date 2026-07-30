package com.example.spottio

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class GroupSettingsActivity : AppCompatActivity() {

    private var currentUser: String? = null
    private var groupId: String? = null

    // Stato reattivo
    private val _groupName = mutableStateOf("")
    private val _groupDesc = mutableStateOf("")
    private val _groupIconUri = mutableStateOf("")
    private val _groupAdminId = mutableStateOf<String?>(null)
    private val _memberIds = mutableStateListOf<String>()

    private lateinit var db: FirebaseFirestore

    private val pickGroupIcon = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadGroupIcon(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("SpottioPrefs", Context.MODE_PRIVATE)
        currentUser = prefs.getString("username_attivo", null)
        groupId = intent.getStringExtra("group_id")
        val passedGroupName = intent.getStringExtra("group_name") ?: "Gruppo"

        if (groupId == null || currentUser == null) {
            finish()
            return
        }

        _groupName.value = passedGroupName
        db = FirebaseFirestore.getInstance()

        loadGroupData()

        setContent {
            MaterialTheme {
                GroupSettingsScreen(
                    groupName = _groupName.value,
                    groupDesc = _groupDesc.value,
                    groupIconUri = _groupIconUri.value,
                    memberIds = _memberIds,
                    adminId = _groupAdminId.value,
                    currentUser = currentUser,
                    onBackClick = { finish() },
                    onEditNameClick = { showEditDialog("Nome", "name", _groupName.value) },
                    onEditDescClick = { showEditDialog("Descrizione", "groupDescription", _groupDesc.value) },
                    onIconClick = { pickGroupIcon.launch("image/*") },
                    onAddMemberClick = { showAddMemberDialog() },
                    onLeaveGroupClick = { leaveGroup() },
                    onRemoveMemberRequest = { id ->
                        AlertDialog.Builder(this@GroupSettingsActivity)
                            .setTitle("Rimuovi partecipante")
                            .setMessage("Vuoi rimuovere $id dal gruppo?")
                            .setPositiveButton("Rimuovi") { _, _ -> removeMemberFromGroup(id) }
                            .setNegativeButton("Annulla", null)
                            .show()
                    }
                )
            }
        }
    }

    private fun loadGroupData() {
        db.collection("groups").document(groupId!!).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

            _groupName.value = snapshot.getString("name") ?: _groupName.value
            _groupDesc.value = snapshot.getString("groupDescription") ?: "Nessuna descrizione"
            _groupIconUri.value = snapshot.getString("groupIconUri") ?: ""
            _groupAdminId.value = snapshot.getString("createdBy")

            @Suppress("UNCHECKED_CAST")
            val members = snapshot.get("members") as? List<String>
            if (members != null) {
                _memberIds.clear()
                _memberIds.addAll(members)
            }
        }
    }

    private fun showEditDialog(titolo: String, fieldName: String, testoAttuale: String) {
        val input = EditText(this)
        input.setText(if (testoAttuale == "Nessuna descrizione") "" else testoAttuale)
        input.setSelection(input.text.length)

        AlertDialog.Builder(this)
            .setTitle("Modifica $titolo")
            .setView(input)
            .setPositiveButton("Salva") { _, _ ->
                val newText = input.text.toString().trim()
                val data = hashMapOf<String, Any>(fieldName to newText)
                db.collection("groups").document(groupId!!).set(data, SetOptions.merge())

                if (fieldName == "name") {
                    val previewData = hashMapOf<String, Any>("name" to newText)
                    db.collection("chat_previews").document(groupId!!).set(previewData, SetOptions.merge())
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun showAddMemberDialog() {
        val input = EditText(this)
        input.hint = "Es: MarioRossi"
        input.inputType = InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(this)
            .setTitle("Aggiungi Partecipante")
            .setMessage("Inserisci l'username esatto dell'utente:")
            .setView(input)
            .setPositiveButton("Aggiungi") { _, _ ->
                val username = input.text.toString().trim()
                if (username.isNotEmpty() && !_memberIds.contains(username)) {
                    verificaEAggiungiUtente(username)
                } else if (_memberIds.contains(username)) {
                    Toast.makeText(this, "L'utente è già nel gruppo!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun verificaEAggiungiUtente(username: String) {
        // Facciamo una query per cercare l'utente tramite il campo "username"
        db.collection("users").whereEqualTo("username", username).limit(1).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Utente trovato! Estraiamo il suo vero UID (l'ID del documento)
                    val userDoc = querySnapshot.documents[0]
                    val userUid = userDoc.id

                    // Aggiungiamo il vero UID all'array dei membri del gruppo
                    db.collection("groups").document(groupId!!).update("members", FieldValue.arrayUnion(userUid))
                        .addOnSuccessListener {
                            Toast.makeText(this, "$username aggiunto al gruppo!", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Utente '$username' non trovato.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Errore durante la ricerca.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun leaveGroup() {
        AlertDialog.Builder(this)
            .setTitle("Abbandona Gruppo")
            .setMessage("Sei sicuro di voler uscire da questo gruppo?")
            .setPositiveButton("Esci") { _, _ ->
                val isAdmin = _groupAdminId.value == null || currentUser == _groupAdminId.value
                if (isAdmin && _memberIds.size > 1) {
                    val nuovoAdmin = if (_memberIds[0] == currentUser) _memberIds[1] else _memberIds[0]
                    db.collection("groups").document(groupId!!).update(
                        "members", FieldValue.arrayRemove(currentUser),
                        "createdBy", nuovoAdmin
                    ).addOnSuccessListener { chiudiETornaAllaHome() }
                } else {
                    db.collection("groups").document(groupId!!).update("members", FieldValue.arrayRemove(currentUser))
                        .addOnSuccessListener { chiudiETornaAllaHome() }
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun chiudiETornaAllaHome() {
        Toast.makeText(this, "Hai abbandonato il gruppo", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
    }

    private fun removeMemberFromGroup(memberId: String) {
        db.collection("groups").document(groupId!!)
            .update("members", FieldValue.arrayRemove(memberId))
            .addOnSuccessListener { Toast.makeText(this, "Membro rimosso", Toast.LENGTH_SHORT).show() }
    }

    private fun uploadGroupIcon(uri: Uri) {
        Toast.makeText(this, "Aggiornamento icona...", Toast.LENGTH_SHORT).show()
        val uriString = uri.toString()
        val iconData = hashMapOf<String, Any>("groupIconUri" to uriString)

        db.collection("groups").document(groupId!!).set(iconData, SetOptions.merge())
            .addOnSuccessListener {
                val previewData = hashMapOf<String, Any>("profilePicUri" to uriString)
                db.collection("chat_previews").document(groupId!!).set(previewData, SetOptions.merge())
                Toast.makeText(this, "Icona aggiornata", Toast.LENGTH_SHORT).show()
            }
    }
}
