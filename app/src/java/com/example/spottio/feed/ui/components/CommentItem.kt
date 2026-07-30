package com.example.spottio.feed.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.spottio.feed.data.Comment
import com.example.spottio.users.data.local.UserCache
import java.util.Date

@Composable
fun CommentItem(comment: Comment) {
    val authorIdOrUsername = comment.author

    // Rendi il componente Stateful per caricare l'utente
    var userData by remember { mutableStateOf(UserCache.getUser(authorIdOrUsername)) }

    LaunchedEffect(authorIdOrUsername) {
        if (userData == null && authorIdOrUsername.isNotEmpty()) {
            UserCache.fetchUserAsync(authorIdOrUsername) {
                userData = UserCache.getUser(authorIdOrUsername)
            }
        }
    }

    // Estraiamo i dati dell'utente, con i dovuti fallback
    val displayName = userData?.username ?: if (authorIdOrUsername.length > 20) "Caricamento..." else authorIdOrUsername
    val pfpUrl = userData?.pfpUri ?: comment.userPfpUrl ?: ""
    val isVerified = userData?.isVerified == true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Avatar
        if (pfpUrl.isNotEmpty()) {
            AsyncImage(
                model = pfpUrl,
                contentDescription = "Avatar Utente",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Bubble del Commento
        Column(
            modifier = Modifier
                .background(Color(0xFFF0F2F5), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayName,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF050505),
                    fontSize = 14.sp
                )

                // Badge di Verifica
                if (isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verificato",
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF1DA1F2)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = comment.displayFormattedDate,
                    color = Color(0xFF65676B),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = comment.text,
                color = Color(0xFF050505),
                fontSize = 14.sp
            )
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewCommentItem() {
    MaterialTheme {
        val dummyComment = Comment(
            author = "MarcoVerdi",
            text = "Bellissimo post! Davvero complimenti \uD83D\uDE0D",
            timestamp = Date(),
            formattedDate = "10 min fa"
        )
        CommentItem(comment = dummyComment)
    }
}