package com.example.spottio.settings

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class SettingsAuthManager(
    private val mAuth: FirebaseAuth,
    private val context: Context,
    private val currentUsername: String
) {
    fun updatePassword(strOld: String, strNew: String, onComplete: (success: Boolean) -> Unit) {
        val user = mAuth.currentUser
        if (user?.email == null) {
            Toast.makeText(context, "Nessun utente Firebase autenticato.", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }
        if (strOld.isEmpty() || strNew.isEmpty()) {
            Toast.makeText(context, "Compila tutti i campi della password.", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, strOld)
        user.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.updatePassword(strNew).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        Toast.makeText(context, "Password aggiornata con successo!", Toast.LENGTH_SHORT).show()
                        onComplete(true)
                    } else {
                        Toast.makeText(context, "Errore: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                        onComplete(false)
                    }
                }
            } else {
                Toast.makeText(context, "Errore: Vecchia password non corretta.", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
    }

    fun updateEmail(strPass: String, strEmail: String, onComplete: (success: Boolean) -> Unit) {
        if (currentUsername == "Guest") {
            onComplete(false)
            return
        }
        val user = mAuth.currentUser
        if (user?.email == null) {
            Toast.makeText(context, "Nessun utente Firebase autenticato.", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }
        if (strPass.isEmpty() || strEmail.isEmpty()) {
            Toast.makeText(context, "Compila tutti i campi.", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, strPass)
        user.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.verifyBeforeUpdateEmail(strEmail).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        Toast.makeText(context, "Link inviato alla nuova email! Controlla la casella.", Toast.LENGTH_LONG).show()
                        onComplete(true)
                    } else {
                        Toast.makeText(context, "Errore: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                        onComplete(false)
                    }
                }
            } else {
                Toast.makeText(context, "Errore: Password attuale non corretta.", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
    }
}