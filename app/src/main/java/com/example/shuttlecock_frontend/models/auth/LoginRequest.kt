package com.example.shuttlecock_frontend.models.auth

data class LoginRequest(
    val email: String,
    val password: String
)