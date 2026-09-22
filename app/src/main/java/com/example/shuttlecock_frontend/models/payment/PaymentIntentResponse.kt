package com.example.shuttlecock_frontend.models.payment

// Matches backend com.example.demo.dto.PaymentIntentResponse
// url = Stripe Checkout hosted page URL, paymentId = local Payment row id
data class PaymentIntentResponse(
    val url: String,
    val paymentId: Int
)
