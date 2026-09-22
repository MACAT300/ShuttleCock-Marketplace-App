package com.example.shuttlecock_frontend.models

import com.example.shuttlecock_frontend.models.image.Image

data class Brand(
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val imageId: Int? = null,
    val image: Image? = null   // 对应后端 @Transient 的 image 字段,一般是 null,除非后端有做attach
)