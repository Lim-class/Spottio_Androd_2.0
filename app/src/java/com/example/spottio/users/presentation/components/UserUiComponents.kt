package com.example.spottio.users.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.spottio.R
import com.example.spottio.users.data.local.UserCache

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
                .padding(12.dp), // Esattamente come android:padding="12dp"
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar 50dp
            val pfpUri = userData?.pfpUri ?: ""
            if (pfpUri.isNotEmpty()) {
                AsyncImage(
                    model = pfpUri,
                    contentDescription = "Avatar Utente",
                    modifier = Modifier
                        .size(50.dp) // Come nell'XML
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(50.dp)
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

            Spacer(modifier = Modifier.width(16.dp)) // layout_marginStart="16dp"

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = userData?.username ?: "Caricamento...",
                        fontSize = 16.sp, // textSize="16sp"
                        fontWeight = FontWeight.Bold, // textStyle="bold"
                        color = MaterialTheme.colorScheme.onSurface // Al posto di #000000 per supportare Dark Mode
                    )

                    if (userData?.isVerified == true) {
                        Spacer(modifier = Modifier.width(4.dp)) // layout_marginStart="4dp"
                        Icon(
                            painter = painterResource(id = R.drawable.ic_verified_blue),
                            contentDescription = "Utente Verificato",
                            modifier = Modifier.size(16.dp), // layout_width="16dp" layout_height="16dp"
                            tint = Color.Unspecified // Fondamentale: impedisce a Compose di ricolorare la tua spunta blu
                        )
                    }
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
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF00FF), // Spottio Green 1DB954
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