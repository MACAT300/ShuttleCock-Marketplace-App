package com.example.shuttlecock_frontend.models.order

data class Order(
    val id: Int = 0,
    val userId: Int,
    val totalAmount: Double,
    val status: String = "PENDING"
)