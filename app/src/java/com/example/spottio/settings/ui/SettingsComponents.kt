package com.example.spottio.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- COLORI GLOBALI DELLE IMPOSTAZIONI ---
val PrimaryDarkBlue = Color(0xFF0B3C5D)
val PrimaryLightBlue = Color(0xFF328CC1)
val TextDark = Color(0xFF333333)
val TextGray = Color(0xFF666666)
val DividerColor = Color(0xFFE5E7EB)
val ContentBackground = Color(0xFFFAFAFA)

@Composable
fun StaticInfoContent(text: String) {
    Text(text = text, fontSize = 14.sp, color = TextGray)
}

@Composable
fun PasswordSettingsContent(
    onUpdatePassword: (String, String, (Boolean) -> Unit) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(8.dp)) {
        OutlinedTextField(
            value = oldPassword,
            onValueChange = { oldPassword = it },
            placeholder = { Text("Vecchia Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            placeholder = { Text("Nuova Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                isLoading = true
                onUpdatePassword(oldPassword, newPassword) { success ->
                    isLoading = false
                    if(success) {
                        oldPassword = ""
                        newPassword = ""
                    }
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = !isLoading && oldPassword.isNotBlank() && newPassword.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkBlue)
        ) {
            Text(if (isLoading) "Elaborazione..." else "Aggiorna Password", color = Color.White)
        }
    }
}

@Composable
fun EmailSettingsContent(
    currentUsername: String,
    onUpdateEmail: (String, String, (Boolean) -> Unit) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val isGuest = currentUsername == "Guest"

    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            text = "Per motivi di sicurezza, inserisci la tua password attuale prima di inserire la nuova email.",
            color = TextGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            placeholder = { Text("Password Attuale") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isGuest,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = newEmail,
            onValueChange = { newEmail = it },
            placeholder = { Text("Nuovo Indirizzo Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isGuest,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                isLoading = true
                onUpdateEmail(currentPassword, newEmail) { success ->
                    isLoading = false
                    if(success) {
                        currentPassword = ""
                        newEmail = ""
                    }
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = !isGuest && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkBlue)
        ) {
            Text(if (isGuest) "Non disponibile per Guest" else if (isLoading) "Elaborazione..." else "Aggiorna Email", color = Color.White)
        }
    }
}

@Composable
fun ExportSettingsContent(
    onExportJson: (() -> Unit) -> Unit,
    onExportHtml: (() -> Unit) -> Unit
) {
    var isExporting by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            text = "Scarica un archivio completo dei tuoi dati personali, post pubblicati e preferenze dell'algoritmo strutturati in formato standard.",
            color = TextGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    isExporting = true
                    onExportJson { isExporting = false }
                },
                modifier = Modifier.weight(1f).padding(end = 6.dp),
                enabled = !isExporting,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLightBlue),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(if (isExporting) "Attendi..." else "Esporta in JSON", color = Color.White)
            }
            Button(
                onClick = {
                    isExporting = true
                    onExportHtml { isExporting = false }
                },
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                enabled = !isExporting,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkBlue),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(if (isExporting) "Attendi..." else "Esporta in HTML", color = Color.White)
            }
        }
    }
}

@Composable
fun PrivacySettingsContent(
    currentUsername: String,
    onGetPrivacyStatus: ((Boolean) -> Unit) -> Unit,
    onUpdatePrivacy: (Boolean, (Boolean) -> Unit) -> Unit
) {
    var isPrivate by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var statusColor by remember { mutableStateOf(Color.Black) }
    val isGuest = currentUsername == "Guest"

    LaunchedEffect(Unit) {
        if (isGuest) {
            statusMessage = "Funzionalità non disponibile per gli utenti Guest."
            statusColor = TextGray
        } else {
            onGetPrivacyStatus { initialState ->
                isPrivate = initialState
            }
        }
    }

    Column(modifier = Modifier.padding(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isGuest) {
                    isPrivate = !isPrivate
                    statusMessage = if (isPrivate) "Il tuo account è ora Privato." else "Il tuo account è ora Pubblico."
                    statusColor = if (isPrivate) Color(0xFF388E3C) else Color(0xFF1976D2)

                    onUpdatePrivacy(isPrivate) { success ->
                        if(!success) {
                            statusMessage = "Errore di aggiornamento."
                            statusColor = Color.Red
                            isPrivate = !isPrivate // rollback grafico in caso di errore
                        }
                    }
                }
        ) {
            Checkbox(
                checked = isPrivate,
                onCheckedChange = null,
                enabled = !isGuest
            )
            Text("Imposta il profilo come Privato", fontSize = 15.sp, modifier = Modifier.padding(start = 8.dp))
        }

        if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                color = statusColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp)
            )
        }
    }
}

// ==============================================================
// PREVIEW RIGOROSAMENTE INDIPENDENTI PER ANDROID STUDIO
// ==============================================================

@Preview(showBackground = true)
@Composable
fun PreviewPasswordSettings() {
    MaterialTheme { PasswordSettingsContent { _, _, _ -> } }
}

@Preview(showBackground = true)
@Composable
fun PreviewEmailSettings() {
    MaterialTheme { EmailSettingsContent(currentUsername = "UtenteTest") { _, _, _ -> } }
}

@Preview(showBackground = true)
@Composable
fun PreviewExportSettings() {
    MaterialTheme { ExportSettingsContent({}, {}) }
}

@Preview(showBackground = true)
@Composable
fun PreviewPrivacySettings() {
    MaterialTheme { PrivacySettingsContent(currentUsername = "UtenteTest", {}, { _, _ -> }) }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    MaterialTheme {
        SettingsScreen(
            currentUsername = "UtenteTest",
            onUpdatePassword = { _, _, _ -> },
            onUpdateEmail = { _, _, _ -> },
            onExportJson = { _ -> },
            onExportHtml = { _ -> },
            onGetPrivacyStatus = { _ -> },
            onUpdatePrivacy = { _, _ -> }
        )
    }
}