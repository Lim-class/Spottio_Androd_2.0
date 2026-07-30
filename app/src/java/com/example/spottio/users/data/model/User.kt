package com.example.spottio.users.data.model

data class User(
    var uid: String = "",
    var username: String = "",
    var email: String = "",
    var bio: String = "",
    var following: MutableList<String> = mutableListOf(),
    var followers: MutableList<String> = mutableListOf(),
    var interests: MutableList<String> = mutableListOf(),
    var isAdmin: Boolean = false
)