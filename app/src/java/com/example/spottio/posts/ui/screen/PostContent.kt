package com.example.spottio.posts.ui.screen

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.StarOutline
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
import com.example.spottio.posts.data.MediaItem
import com.example.spottio.posts.ui.components.PostMediaCarousel

@Composable
fun PostItemContent(
    username: String,
    pfpUri: String,
    isVerified: Boolean,
    dateString: String,
    text: String?,
    mediaList: List<MediaItem>,
    likeCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    onUserClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onReportClick: () -> Unit,
    onShareClick: () -> Unit,
    onImageClick: (View, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(bottom = 12.dp)
    ) {
        // --- HEADER DEL POST ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pfpUri.isNotEmpty()) {
                AsyncImage(
                    model = pfpUri,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onUserClick),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                        .clickable(onClick = onUserClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = username,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF262626),
                        modifier = Modifier.clickable(onClick = onUserClick)
                    )
                    if (isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verificato",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF1DA1F2)
                        )
                    }
                }
                Text(text = dateString, fontSize = 12.sp, color = Color(0xFF8E8E8E))
            }

            if (canEdit) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifica", tint = Color(0xFF757575))
                }
            }
            if (canDelete) {
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = Color(0xFFFF5252))
                }
            }
        }

        // --- CAROSELLO MEDIA ---
        if (mediaList.isNotEmpty()) {
            PostMediaCarousel(
                mediaList = mediaList,
                onImageClick = onImageClick
            )
        }

        // --- TESTO DEL POST ---
        if (!text.isNullOrEmpty()) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color(0xFF262626),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        // --- BARRA DELLE INTERAZIONI ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onLikeClick)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = likeCount.toString(),
                    color = if (isLiked) Color.Red else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            Row(
                modifier = Modifier
                    .clickable(onClick = onCommentClick)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = Color(0xFF262626),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = commentCount.toString(),
                    color = Color(0xFF8E8E8E),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            IconButton(onClick = onShareClick, modifier = Modifier.padding(4.dp)) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF262626))
            }

            IconButton(onClick = onReportClick, modifier = Modifier.padding(4.dp)) {
                Icon(Icons.Default.Warning, contentDescription = "Report", tint = Color(0xFF757575))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewPostItem() {
    MaterialTheme {
        PostItemContent(
            username = "MarioRossi",
            pfpUri = "",
            isVerified = true,
            dateString = "28/07/2026 10:15",
            text = "Questo è un post di test per verificare che la UI di Jetpack Compose funzioni perfettamente! 🚀",
            mediaList = emptyList(),
            likeCount = 128,
            commentCount = 14,
            isLiked = true,
            canEdit = true,
            canDelete = true,
            onUserClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onLikeClick = {},
            onCommentClick = {},
            onReportClick = {},
            onShareClick = {},
            onImageClick = { _, _ -> }
        )
    }
}