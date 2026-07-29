package com.example.spottio

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.spottio.users.UserCache

/**
 * Wrapper Stateful che gestisce la logica di caricamento da Cache
 */
@Composable
fun GroupMemberRow(
    memberId: String,
    adminId: String?,
    isCurrentUserAdmin: Boolean,
    onRemoveRequest: (String) -> Unit
) {
    var userData by remember { mutableStateOf(UserCache.getUser(memberId)) }

    LaunchedEffect(memberId) {
        if (userData == null) {
            UserCache.fetchUserAsync(memberId) {
                userData = UserCache.getUser(memberId)
            }
        }
    }

    val roleText = when {
        memberId == adminId -> "Amministratore"
        isCurrentUserAdmin && memberId != adminId -> "Tieni premuto per rimuovere"
        else -> "Partecipante"
    }
    val roleColor = when {
        memberId == adminId -> Color(0xFF1DB954) // Verde Spottio
        isCurrentUserAdmin && memberId != adminId -> Color(0xFFFF0000) // Rosso
        else -> Color(0xFF8E8E8E) // Grigio
    }

    GroupMemberRowContent(
        username = userData?.username ?: memberId,
        pfpUri = userData?.pfpUri ?: "",
        isVerified = userData?.isVerified == true,
        roleText = roleText,
        roleColor = roleColor,
        onLongClick = {
            if (isCurrentUserAdmin && memberId != adminId) {
                onRemoveRequest(memberId)
            }
        }
    )
}

/**
 * UI Stateless pura (Perfetta per la @Preview e fedele all'XML originale)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupMemberRowContent(
    username: String,
    pfpUri: String,
    isVerified: Boolean,
    roleText: String,
    roleColor: Color,
    onLongClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* Profilo utente opzionale */ },
                onLongClick = onLongClick
            ),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pfpUri.isNotEmpty()) {
                AsyncImage(
                    model = pfpUri,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = username,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF262626) // Colore XML
                    )
                    if (isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verificato",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF1DA1F2)
                        )
                    }
                }
                Text(
                    text = roleText,
                    fontSize = 14.sp,
                    color = roleColor // Colore XML
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewGroupMemberRowAdmin() {
    MaterialTheme {
        GroupMemberRowContent(
            username = "Mario Rossi",
            pfpUri = "",
            isVerified = true,
            roleText = "Amministratore",
            roleColor = Color(0xFF1DB954)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewGroupMemberRowParticipant() {
    MaterialTheme {
        GroupMemberRowContent(
            username = "Giulia Bianchi",
            pfpUri = "",
            isVerified = false,
            roleText = "Partecipante",
            roleColor = Color(0xFF8E8E8E)
        )
    }
}
