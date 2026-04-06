package com.habitflow.app.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarInitials: String = name.take(2).uppercase()
)
