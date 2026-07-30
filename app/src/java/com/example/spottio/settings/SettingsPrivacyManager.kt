package com.example.spottio.settings

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SettingsPrivacyManager(
    private val mDb: FirebaseFirestore,
    private val context: Context,
    private val currentUid: String
) {
    fun getInitialPrivacyStatus(onResult: (Boolean) -> Unit) {
        if (currentUid == "Guest") {
            onResult(false)
            return
        }
        mDb.collection("users").document(currentUid).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists() && documentSnapshot.contains("isPrivate")) {
                    onResult(documentSnapshot.getBoolean("isPrivate") == true)
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener { onResult(false) }
    }

    fun updatePrivacyStatus(isPrivate: Boolean, onComplete: (Boolean) -> Unit) {
        if (currentUid == "Guest") {
            onComplete(false)
            return
        }
        val data = mapOf("isPrivate" to isPrivate)
        mDb.collection("users").document(currentUid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}