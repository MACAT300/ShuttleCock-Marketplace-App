package com.example.shuttlecock_frontend.models

data class Payment(
    val id: Int = 0,
    val orderId: Int,
    val amount: Double,
    val paymentMethod: String,
    val status: String = "PENDING",
    val transactionRef: String? = null
)