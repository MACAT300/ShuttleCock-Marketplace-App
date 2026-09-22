package com.example.shuttlecock_frontend.models

data class User(
    val id: Int = 0,
    val name: String,
    val email: String,
    val password: String? = null,   // 后端返回时永远是null,只有注册/更新时才会传
    val isGoogleAccount: Boolean = false,
    val avatarUrl: String? = null
)