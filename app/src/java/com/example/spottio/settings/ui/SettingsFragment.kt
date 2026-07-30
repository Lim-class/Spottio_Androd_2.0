package com.example.spottio.settings.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.spottio.settings.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    private lateinit var authManager: SettingsAuthManager
    private lateinit var privacyManager: SettingsPrivacyManager
    private lateinit var exportManager: SettingsExportManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val prefs = requireContext().getSharedPreferences("SpottioPrefs", Context.MODE_PRIVATE)
        val currentUsername = prefs.getString("username_attivo", "Guest") ?: "Guest"
        val currentUid = prefs.getString("uid_attivo", "Guest") ?: "Guest"

        val mDb = FirebaseFirestore.getInstance()
        val mAuth = FirebaseAuth.getInstance()

        authManager = SettingsAuthManager(mAuth, requireContext(), currentUsername)
        privacyManager = SettingsPrivacyManager(mDb, requireContext(), currentUid)
        exportManager = SettingsExportManager(mDb, requireContext(), currentUsername)

        return ComposeView(requireContext()).apply {
            setContent {
                SettingsScreen(
                    currentUsername = currentUsername,
                    // Passiamo le funzioni di Firebase come Callback, scollegando la UI dai Manager
                    onUpdatePassword = { old, new, onComplete ->
                        authManager.updatePassword(old, new, onComplete)
                    },
                    onUpdateEmail = { pass, email, onComplete ->
                        authManager.updateEmail(pass, email, onComplete)
                    },
                    onExportJson = { onComplete ->
                        exportManager.eseguiEstrazioneDati(formatoJson = true) { onComplete() }
                    },
                    onExportHtml = { onComplete ->
                        exportManager.eseguiEstrazioneDati(formatoJson = false) { onComplete() }
                    },
                    onGetPrivacyStatus = { onResult ->
                        privacyManager.getInitialPrivacyStatus(onResult)
                    },
                    onUpdatePrivacy = { isPrivate, onComplete ->
                        privacyManager.updatePrivacyStatus(isPrivate, onComplete)
                    }
                )
            }
        }
    }
}