package com.example.domain.model

data class Receipt(
    val id: Long = 0L,
    val merchant: String,
    val date: String,
    val total: Double,
    val tax: Double,
    val category: String,
    val currency: String,
    val items: List<ReceiptItem>,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
