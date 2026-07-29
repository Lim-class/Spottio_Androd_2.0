package com.example.spottio.chat.conversation

import android.content.Context
import android.content.SharedPreferences
import com.example.spottio.chat.ChatMessage
import com.example.spottio.users.UserCache
import com.example.spottio.utils.CryptoHelper
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.util.Date

class ChatRepository(
    context: Context,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val currentUser: String,
    private val targetUser: String,
    private val isGroup: Boolean,
    private val conversationId: String
) {
    private var snapshotListener: ListenerRegistration? = null
    private val prefs: SharedPreferences = context.getSharedPreferences("SpottioPrefs", Context.MODE_PRIVATE)

    private val hiddenMessages: MutableSet<String>
        get() = prefs.getStringSet("hidden_messages_$currentUser", mutableSetOf()) ?: mutableSetOf()

    fun listenForMessages(onMessagesChanged: (List<ChatMessage>) -> Unit) {
        val query = if (isGroup) {
            db.collection("groups").document(targetUser).collection("chats")
        } else {
            db.collection("chats").document(conversationId).collection("messages")
        }

        // L'ID da usare per la decrittografia
        val cryptoId = if (isGroup) targetUser else conversationId

        snapshotListener = query.orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null || value == null) return@addSnapshotListener

                val messages = mutableListOf<ChatMessage>()
                val currentHidden = hiddenMessages

                for (doc in value) {
                    if (currentHidden.contains(doc.id)) continue

                    val sender = doc.getString("sender") ?: "Sistema"
                    val encryptedTxt = doc.getString("text") ?: ""
                    val ts = doc.getTimestamp("timestamp")?.toDate() ?: Date()
                    val isMe = sender == currentUser

                    // 🔓 DECRIPTA IL MESSAGGIO QUI
                    val decryptedTxt = CryptoHelper.decrypt(encryptedTxt, cryptoId)

                    var pfpUri = ""
                    var verified = false

                    val cachedData = UserCache.getUser(sender)
                    if (cachedData != null) {
                        pfpUri = cachedData.pfpUri
                        verified = cachedData.isVerified
                    } else {
                        UserCache.fetchUserAsync(sender) {}
                    }

                    messages.add(
                        ChatMessage(
                            docId = doc.id, text = decryptedTxt, isMe = isMe,
                            timestamp = ts, senderName = sender, isGroup = isGroup,
                            profilePicUri = pfpUri, isVerified = verified
                        )
                    )
                }
                onMessagesChanged(messages)
            }
    }

    fun sendMessage(text: String) {
        val previewId = if (isGroup) targetUser else conversationId
        
        // 🔒 CRIPTA IL MESSAGGIO QUI
        val encryptedText = CryptoHelper.encrypt(text, previewId)

        val msg = mutableMapOf<String, Any>(
            "sender" to currentUser,
            "text" to encryptedText, // Testo criptato
            "timestamp" to FieldValue.serverTimestamp()
        )

        if (isGroup) {
            db.collection("groups").document(targetUser).collection("chats").add(msg)
        } else {
            msg["receiver"] = targetUser
            msg["conversationId"] = conversationId
            db.collection("chats").document(conversationId).collection("messages").add(msg)
        }
        
        aggiornaPreview(previewId, encryptedText)
    }

    private fun aggiornaPreview(previewId: String, encryptedText: String) {
        val previewData = mutableMapOf<String, Any>(
            "lastMessage" to encryptedText, // Salvato criptato
            "lastSender" to currentUser,
            "lastUpdate" to FieldValue.serverTimestamp()
        )
        if (!isGroup) {
            previewData["participants"] = listOf(currentUser, targetUser)
            previewData["isGroup"] = false
        }
        db.collection("chat_previews").document(previewId).set(previewData, SetOptions.merge())
    }

    fun editMessage(docId: String, newText: String) {
        val cryptoId = if (isGroup) targetUser else conversationId
        val encryptedText = CryptoHelper.encrypt(newText, cryptoId)
        
        val collectionPath = if (isGroup) "groups/$targetUser/chats" else "chats/$conversationId/messages"
        db.collection(collectionPath).document(docId).update("text", encryptedText)
    }

    fun deleteMessage(docId: String, deleteForEveryone: Boolean, onLocalHide: () -> Unit) {
        if (deleteForEveryone) {
            val collectionPath = if (isGroup) "groups/$targetUser/chats" else "chats/$conversationId/messages"
            db.collection(collectionPath).document(docId).delete()
        } else {
            val hidden = hiddenMessages
            hidden.add(docId)
            prefs.edit().putStringSet("hidden_messages_$currentUser", hidden).apply()
            onLocalHide()
        }
    }

    fun stopListening() {
        snapshotListener?.remove()
        snapshotListener = null
    }
}
