package com.example.shuttlecock_frontend.models.cart

import com.example.shuttlecock_frontend.models.Product

data class CartItem(
    val id: Int = 0,
    val cartId: Int,
    val productId: Int,
    val quantity: Int,
    val product: Product? = null
)