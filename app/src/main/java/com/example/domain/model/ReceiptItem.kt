package com.example.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReceiptItem(
    val name: String,
    val quantity: Int,
    val price: Double
)
