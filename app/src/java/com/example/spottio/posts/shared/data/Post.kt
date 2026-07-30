package com.example.spottio.posts.shared.data

import com.example.spottio.feed.data.Comment
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import java.util.Date

@IgnoreExtraProperties
data class Post @JvmOverloads constructor(
    @JvmField var user: String = "",
    @JvmField var author: String? = null,
    @JvmField var text: String? = null,

    // Campi legacy singoli
    @JvmField var mediaUri: String? = null,
    @JvmField
    @PropertyName("video")
    var isVideo: Boolean = false,
    @JvmField var category: String? = null,

    // Mappatura esatta della struttura Firestore (mediaList come lista di oggetti)
    @JvmField var mediaList: MutableList<MediaItem> = mutableListOf(),
    @JvmField var categories: MutableList<String> = mutableListOf(),

    @JvmField var timestamp: Date = Date(),
    var comments: MutableList<Comment> = mutableListOf(),

    @get:PropertyName("likes")
    @set:PropertyName("likes")
    var likes: MutableList<String> = mutableListOf()
) {
    @JvmField
    @Exclude
    var postId: String? = null

    fun addComment(comment: Comment) {
        comments.add(comment)
    }

    fun toggleLike(uid: String) {
        if (likes.contains(uid)) {
            likes.remove(uid)
        } else {
            likes.add(uid)
        }
    }
}

// Per inserire il verificato e riutilizzarlo
// <include layout="@layout/layout_verified_badge" />

