package com.example.shuttlecock_frontend.models.auth

data class LoginResponse(
    val token: String,
    val user: UserSummary
)

data class UserSummary(
    val id: Int,
    val name: String,
    val email: String,
    val isGoogleAccount: Boolean = false
)