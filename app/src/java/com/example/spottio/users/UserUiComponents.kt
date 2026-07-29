package com.example.spottio.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun UserRow(
    uidOrUsername: String,
    onUserClick: (String) -> Unit
) {
    var userData by remember { mutableStateOf(UserCache.getUser(uidOrUsername)) }

    LaunchedEffect(uidOrUsername) {
        if (userData == null) {
            UserCache.fetchUserAsync(uidOrUsername) {
                userData = UserCache.getUser(uidOrUsername)
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick(uidOrUsername) },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val pfpUri = userData?.pfpUri ?: ""
            if (pfpUri.isNotEmpty()) {
                AsyncImage(
                    model = pfpUri,
                    contentDescription = "Avatar Utente",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Avatar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userData?.username ?: "Caricamento...",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (userData?.isVerified == true) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verificato",
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF1DA1F2)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    title: String,
    searchQuery: String = "",
    onQueryChange: ((String) -> Unit)? = null,
    showSearch: Boolean = false,
    users: List<String>,
    onUserClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF1DB954), // Spottio Green come da XML originario
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (showSearch && onQueryChange != null) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Cerca un utente...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cerca"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(users) { uidOrUsername ->
                UserRow(
                    uidOrUsername = uidOrUsername,
                    onUserClick = onUserClick
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        }
    }
}
