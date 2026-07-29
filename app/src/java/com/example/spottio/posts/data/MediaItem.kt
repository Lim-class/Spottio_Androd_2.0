package com.example.spottio.posts.data

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import java.io.Serializable

@IgnoreExtraProperties
data class MediaItem @JvmOverloads constructor(
    @JvmField var url: String = "",

    @JvmField
    @PropertyName("isVideo")
    var isVideo: Boolean = false
) : Serializable