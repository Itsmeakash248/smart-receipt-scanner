package com.example.data.remote

import com.example.domain.model.ReceiptItem
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val merchant: String?,
    val date: String?,
    val total: Double?,
    val tax: Double?,
    val category: String?,
    val currency: String?,
    val items: List<GeminiItem>?
)

@JsonClass(generateAdapter = true)
data class GeminiItem(
    val name: String?,
    val quantity: Int?,
    val price: Double?
) {
    fun toDomain(): ReceiptItem {
        return ReceiptItem(
            name = name ?: "Unknown Item",
            quantity = quantity ?: 1,
            price = price ?: 0.0
        )
    }
}
