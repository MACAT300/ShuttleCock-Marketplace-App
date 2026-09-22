package com.example.shuttlecock_frontend.models

data class ProductImage(
    val id: Int = 0,
    val productId: Int,
    val url: String,
    val sortOrder: Int = 0
)