package com.example.shuttlecock_frontend.models.history

import com.example.shuttlecock_frontend.models.Product

data class BrowsingHistoryItem(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val viewedAt: String,
    val product: Product? = null
)