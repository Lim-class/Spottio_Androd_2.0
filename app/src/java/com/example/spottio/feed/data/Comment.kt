package com.example.spottio.feed.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@IgnoreExtraProperties
data class Comment @JvmOverloads constructor(
    @get:PropertyName("user")
    @set:PropertyName("user")
    var author: String = "",

    @JvmField var text: String = "",
    @JvmField var timestamp: Date = Date(),
    @JvmField var userPfpUrl: String? = null,
    @JvmField var formattedDate: String? = null
) : Serializable {

    @get:Exclude
    val displayFormattedDate: String
        get() = formattedDate ?: SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(timestamp)
}