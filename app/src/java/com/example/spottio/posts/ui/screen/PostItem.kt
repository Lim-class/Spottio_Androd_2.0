package com.example.spottio.posts.ui.screen

import androidx.compose.runtime.*
import com.example.spottio.posts.data.MediaItem
import com.example.spottio.posts.data.Post
import com.example.spottio.posts.ui.adapter.PostAdapter
import com.example.spottio.users.data.local.UserCache
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PostItem(
    post: Post,
    currentUser: String,
    isAdmin: Boolean,
    position: Int,
    interactionListener: PostAdapter.PostInteractionListener
) {
    val postUidOrUsername = post.user.ifEmpty { post.author ?: "" }

    var userData by remember { mutableStateOf(UserCache.getUser(postUidOrUsername)) }

    LaunchedEffect(postUidOrUsername) {
        if (userData == null && postUidOrUsername.isNotEmpty()) {
            UserCache.fetchUserAsync(postUidOrUsername) {
                userData = UserCache.getUser(postUidOrUsername)
            }
        }
    }

    val effectiveMediaList = remember(post) {
        when {
            post.mediaList.isNotEmpty() -> post.mediaList
            !post.mediaUri.isNullOrEmpty() -> listOf(MediaItem(url = post.mediaUri!!, isVideo = post.isVideo))
            else -> emptyList()
        }
    }

    val isMyPost = postUidOrUsername == currentUser
    val isLiked = post.likes.contains(currentUser)
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    val fallbackUsername = if (postUidOrUsername.length > 20) "Caricamento..." else postUidOrUsername
    val username = userData?.username ?: fallbackUsername
    val pfpUri = userData?.pfpUri ?: ""
    val isVerified = userData?.isVerified == true

    // NOTA: Usa l'import corretto in base alla tua struttura dei package!
    // Se hai creato la cartella components, assicurati che PostItemContent e PostMediaCarousel
    // siano importati correttamente se l'IDE lo richiede.

    PostItemContent(
        username = username,
        pfpUri = pfpUri,
        isVerified = isVerified,
        dateString = sdf.format(post.timestamp),
        text = post.text,
        mediaList = effectiveMediaList,
        likeCount = post.likes.size,
        commentCount = post.comments.size,
        isLiked = isLiked,
        canEdit = isMyPost && effectiveMediaList.isEmpty(),
        canDelete = isMyPost || isAdmin,
        onUserClick = { interactionListener.onUserClick(postUidOrUsername) },
        onEditClick = { interactionListener.onEditClick(post, position) },
        onDeleteClick = { interactionListener.onDeleteClick(post, position) },
        onLikeClick = { interactionListener.onLikeClick(post, position) },
        onCommentClick = { interactionListener.onCommentClick(post, position) },
        onReportClick = { interactionListener.onReportClick(post) },
        onShareClick = {
            var shareContent = ""
            if (!post.text.isNullOrEmpty()) shareContent += post.text
            if (effectiveMediaList.isNotEmpty()) {
                if (shareContent.isNotEmpty()) shareContent += "\n\n"
                shareContent += "Guarda l'allegato: ${effectiveMediaList[0].url}"
            }
            if (shareContent.isEmpty()) shareContent = "Guarda questo post su Spottio!"
            interactionListener.onShareClick(post, shareContent)
        },
        onImageClick = { imageView, uri ->
            // FIX: Ora passiamo direttamente l'imageView corretta restituita dal Carosello!
            interactionListener.onImageZoomClick(imageView, uri)
        }
    )
}