package com.example.spottio.users

data class User(
    @JvmField var uid: String = "",
    @JvmField var username: String = "",
    @JvmField var email: String = "",
    @JvmField var bio: String = "",
    @JvmField var following: MutableList<String> = mutableListOf(),
    @JvmField var followers: MutableList<String> = mutableListOf(),
    @JvmField var interests: MutableList<String> = mutableListOf(),

    @get:JvmName("isAdmin")
    var isAdmin: Boolean = false
)
