package com.example.spottio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSettingsScreen(
    groupName: String,
    groupDesc: String,
    groupIconUri: String,
    memberIds: List<String>,
    adminId: String?,
    currentUser: String?,
    onBackClick: () -> Unit,
    onEditNameClick: () -> Unit,
    onEditDescClick: () -> Unit,
    onIconClick: () -> Unit,
    onAddMemberClick: () -> Unit,
    onLeaveGroupClick: () -> Unit,
    onRemoveMemberRequest: (String) -> Unit
) {
    val isAdmin = adminId == null || currentUser == adminId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Impostazioni Gruppo", color = Color(0xFF262626), fontWeight = FontWeight.Bold, fontSize = 18.sp) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = Color(0xFF262626))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header Profilo Gruppo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                        .clickable(enabled = isAdmin) { onIconClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (groupIconUri.isNotEmpty()) {
                        AsyncImage(
                            model = groupIconUri,
                            contentDescription = "Icona Gruppo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = groupName, 
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFF262626),
                            modifier = Modifier.weight(1f)
                        )
                        if (isAdmin) {
                            IconButton(onClick = onEditNameClick) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifica Nome", modifier = Modifier.size(20.dp), tint = Color(0xFF8E8E8E))
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = groupDesc, 
                            fontSize = 14.sp, 
                            color = Color(0xFF8E8E8E), 
                            modifier = Modifier.weight(1f)
                        )
                        if (isAdmin) {
                            IconButton(onClick = onEditDescClick) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifica Descrizione", modifier = Modifier.size(20.dp), tint = Color(0xFF8E8E8E))
                            }
                        }
                    }
                }
            }

            Text("Membri del gruppo (${memberIds.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF262626), modifier = Modifier.padding(bottom = 8.dp))

            // Lista Membri
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(memberIds) { memberId ->
                    GroupMemberRow(
                        memberId = memberId,
                        adminId = adminId,
                        isCurrentUserAdmin = isAdmin,
                        onRemoveRequest = onRemoveMemberRequest
                    )
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                }
            }

            // Pulsanti Azione
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isAdmin) {
                    Button(
                        onClick = onAddMemberClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                    ) {
                        Text("Aggiungi Membro", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Button(
                    onClick = onLeaveGroupClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Abbandona", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewGroupSettingsScreen() {
    MaterialTheme {
        GroupSettingsScreen(
            groupName = "Spottio Team",
            groupDesc = "Sviluppatori dell'app",
            groupIconUri = "",
            memberIds = listOf("user1", "user2", "user3"),
            adminId = "user1",
            currentUser = "user1",
            onBackClick = {},
            onEditNameClick = {},
            onEditDescClick = {},
            onIconClick = {},
            onAddMemberClick = {},
            onLeaveGroupClick = {},
            onRemoveMemberRequest = {}
        )
    }
}
