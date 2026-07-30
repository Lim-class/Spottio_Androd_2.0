package com.example.spottio.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spottio.settings.SettingType
import com.example.spottio.settings.SettingsMenuProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUsername: String,
    onUpdatePassword: (String, String, (Boolean) -> Unit) -> Unit,
    onUpdateEmail: (String, String, (Boolean) -> Unit) -> Unit,
    onExportJson: (() -> Unit) -> Unit,
    onExportHtml: (() -> Unit) -> Unit,
    onGetPrivacyStatus: ((Boolean) -> Unit) -> Unit,
    onUpdatePrivacy: (Boolean, (Boolean) -> Unit) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedType by remember { mutableStateOf<SettingType?>(null) }

    val allItems = remember { SettingsMenuProvider.getMenuItems() }
    val filteredItems = allItems.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Impostazioni",
                color = PrimaryDarkBlue,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Configura le preferenze del tuo account.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    expandedType = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Cerca impostazioni...") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredItems) { item ->
                    SettingAccordionItem(
                        title = item.title,
                        isExpanded = expandedType == item.type,
                        onHeaderClick = {
                            expandedType = if (expandedType == item.type) null else item.type
                        }
                    ) {
                        SettingContent(
                            type = item.type,
                            currentUsername = currentUsername,
                            onUpdatePassword = onUpdatePassword,
                            onUpdateEmail = onUpdateEmail,
                            onExportJson = onExportJson,
                            onExportHtml = onExportHtml,
                            onGetPrivacyStatus = onGetPrivacyStatus,
                            onUpdatePrivacy = onUpdatePrivacy
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingAccordionItem(
    title: String,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 1.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.White)
                .clickable { onHeaderClick() }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = TextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Comprimi" else "Espandi",
                tint = TextDark
            )
        }

        HorizontalDivider(thickness = 1.dp, color = DividerColor)

        AnimatedVisibility(visible = isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ContentBackground)
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingContent(
    type: SettingType,
    currentUsername: String,
    onUpdatePassword: (String, String, (Boolean) -> Unit) -> Unit,
    onUpdateEmail: (String, String, (Boolean) -> Unit) -> Unit,
    onExportJson: (() -> Unit) -> Unit,
    onExportHtml: (() -> Unit) -> Unit,
    onGetPrivacyStatus: ((Boolean) -> Unit) -> Unit,
    onUpdatePrivacy: (Boolean, (Boolean) -> Unit) -> Unit
) {
    when (type) {
        SettingType.PASSWORD -> PasswordSettingsContent(onUpdatePassword)
        SettingType.CAMBIO_EMAIL -> EmailSettingsContent(currentUsername, onUpdateEmail)
        SettingType.PRIVACY_PROFILO -> PrivacySettingsContent(currentUsername, onGetPrivacyStatus, onUpdatePrivacy)
        SettingType.ESPORTA_DATI -> ExportSettingsContent(onExportJson, onExportHtml)

        SettingType.COLORE_SFONDO -> StaticInfoContent("La modifica del colore di sfondo è disponibile esclusivamente nella versione web dell'applicazione.")
        SettingType.ELIMINA_ACCOUNT -> StaticInfoContent(
            if (currentUsername.equals("admin", ignoreCase = true)) {
                "L'account amministratore (admin) non può essere rimosso dal sistema locale o remoto."
            } else {
                "Per eliminare definitivamente il tuo account, invia una richiesta formale al supporto."
            }
        )
        SettingType.SCARICA_APK -> StaticInfoContent("Stai già utilizzando l'applicazione Android nativa.")
        SettingType.STORIA -> StaticInfoContent("Cronologia Account: Nessuna attività recente.")
        SettingType.INFORMATIVA_PRIVACY -> StaticInfoContent("Spottio tutela i tuoi dati personali ai sensi del GDPR.")
        SettingType.POLICY_COMMUNITY -> StaticInfoContent("È severamente vietato pubblicare contenuti offensivi o spam.")
    }
}