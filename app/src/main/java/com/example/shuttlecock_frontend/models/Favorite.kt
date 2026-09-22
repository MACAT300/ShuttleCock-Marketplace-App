package com.example.shuttlecock_frontend.models.favorite

import com.example.shuttlecock_frontend.models.Product

// Matches backend com.example.demo.model.Favorite
data class Favorite(
    val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val product: Product? = null
)