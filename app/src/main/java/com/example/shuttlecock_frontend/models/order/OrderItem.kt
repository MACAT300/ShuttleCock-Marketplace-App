package com.example.shuttlecock_frontend.models.order

import com.example.shuttlecock_frontend.models.Product

data class OrderItem(
    val id: Int = 0,
    val orderId: Int,
    val productId: Int,
    val quantity: Int,
    val price: Double,
    val product: Product? = null
)