package com.example.shuttlecock_frontend.models

import com.example.shuttlecock_frontend.models.image.Image

data class Product(
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val price: Double,
    val quantity: Int,
    val brandId: Int,
    val imageId: Int? = null,
    val brand: Brand? = null,
    val image: Image? = null
)