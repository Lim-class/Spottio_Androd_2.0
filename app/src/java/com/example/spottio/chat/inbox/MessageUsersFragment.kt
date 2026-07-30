package com.example.spottio.chat.inbox

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.spottio.utils.DebouncingTextWatcher
import com.example.spottio.R
import com.example.spottio.chat.ChatPreview
import com.example.spottio.chat.conversation.ChatFragment
import com.example.spottio.users.data.local.UserCache
import com.example.spottio.utils.CryptoHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.Date

class MessageUsersFragment : Fragment() {

    private var currentUser: String = ""

    private lateinit var lvChats: ListView
    private lateinit var etSearch: EditText
    private lateinit var btnNewChat: ImageButton

    private lateinit var adapter: ChatPreviewAdapter
    private val masterList = ArrayList<ChatPreview>()
    private val displayedList = ArrayList<ChatPreview>()

    private lateinit var dialogHelper: MessagesDialogHelper
    private var snapshotListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)

        val db = FirebaseFirestore.getInstance()
        val prefs = requireActivity().getSharedPreferences("SpottioPrefs", Context.MODE_PRIVATE)

        currentUser = prefs.getString("uid_attivo", "") ?: ""

        lvChats = view.findViewById(R.id.lvChats)
        etSearch = view.findViewById(R.id.etSearchUser)
        btnNewChat = view.findViewById(R.id.btnNewChat)

        adapter = ChatPreviewAdapter(requireContext(), displayedList)
        lvChats.adapter = adapter

        dialogHelper = MessagesDialogHelper(requireContext(), parentFragmentManager, currentUser)

        btnNewChat.setOnClickListener { dialogHelper.mostraDialogoNuovaChat() }

        lvChats.setOnItemClickListener { _, _, position, _ ->
            val selected = displayedList[position]
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatFragment.newInstance(selected.targetId ?: "", selected.isGroup, selected.name))
                .addToBackStack(null)
                .commit()
        }

        etSearch.addTextChangedListener(DebouncingTextWatcher(200) { query -> filterChats(query) })

        loadUnifiedChats(db)

        return view
    }

    private fun loadUnifiedChats(db: FirebaseFirestore) {
        if (currentUser.isEmpty()) return

        snapshotListener = db.collection("chat_previews")
            .whereArrayContains("participants", currentUser)
            .orderBy("lastUpdate", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null || value == null) return@addSnapshotListener

                masterList.clear()

                for (doc in value) {
                    val isGroup = doc.getBoolean("isGroup") ?: false
                    val id = doc.id
                    val encryptedLastMessage = doc.getString("lastMessage") ?: "Nessun messaggio"
                    val lastUpdate = doc.getTimestamp("lastUpdate")?.toDate() ?: Date()

                    var nameToShow: String? = null
                    var targetId: String? = null
                    var cryptoTargetId = id

                    if (isGroup) {
                        nameToShow = doc.getString("groupName")
                        targetId = id
                    } else {
                        val rawParticipants = doc.get("participants") as? List<*>
                        val participants = rawParticipants?.mapNotNull { it as? String }

                        if (participants != null) {
                            for (p in participants) {
                                if (p != currentUser) {
                                    targetId = p
                                    nameToShow = p 
                                    break
                                }
                            }
                            if (targetId != null) {
                                val ids = listOf(currentUser, targetId).sorted()
                                cryptoTargetId = "${ids[0]}_${ids[1]}"
                            }
                        }
                    }

                    if (nameToShow == null) nameToShow = targetId ?: "Chat"
                    if (targetId == null) targetId = ""

                    // 🔓 DECRIPTA L'ANTEPRIMA
                    var decryptedLastMessage = CryptoHelper.decrypt(encryptedLastMessage, cryptoTargetId)

                    if (decryptedLastMessage.startsWith("image:")) {
                        decryptedLastMessage = "📷 Immagine"
                    } else if (decryptedLastMessage.startsWith("video:")) {
                        decryptedLastMessage = "🎥 Video"
                    } else if (decryptedLastMessage.startsWith("audio:")) {
                        decryptedLastMessage = "🎤 Audio"
                    }

                    val preview = ChatPreview(
                        id,
                        nameToShow,
                        decryptedLastMessage,
                        lastUpdate,
                        isGroup,
                        targetId,
                        "",
                        false
                    )
                    masterList.add(preview)

                    if (!isGroup && targetId.isNotEmpty()) {
                        UserCache.fetchUserAsync(targetId) {
                            adapter.notifyDataSetChanged()
                        }
                    }
                }

                filterChats(etSearch.text.toString())
            }
    }

    private fun filterChats(query: String) {
        displayedList.clear()
        val lower = query.lowercase().trim()
        if (lower.isEmpty()) {
            displayedList.addAll(masterList)
        } else {
            for (preview in masterList) {
                if (preview.name?.lowercase()?.contains(lower) == true) {
                    displayedList.add(preview)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
        snapshotListener = null
    }
}
