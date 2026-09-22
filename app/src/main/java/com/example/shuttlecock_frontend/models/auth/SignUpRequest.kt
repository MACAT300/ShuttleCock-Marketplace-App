package com.example.shuttlecock_frontend.models.auth

data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String
)