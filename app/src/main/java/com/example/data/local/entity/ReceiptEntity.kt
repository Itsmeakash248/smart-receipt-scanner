package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Receipt
import com.example.domain.model.ReceiptItem

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val merchant: String,
    val date: String,
    val total: Double,
    val tax: Double,
    val category: String,
    val currency: String,
    val items: List<ReceiptItem>,
    val imageUri: String? = null,
    val timestamp: Long
) {
    fun toDomain(): Receipt {
        return Receipt(
            id = id,
            merchant = merchant,
            date = date,
            total = total,
            tax = tax,
            category = category,
            currency = currency,
            items = items,
            imageUri = imageUri,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(receipt: Receipt): ReceiptEntity {
            return ReceiptEntity(
                id = receipt.id,
                merchant = receipt.merchant,
                date = receipt.date,
                total = receipt.total,
                tax = receipt.tax,
                category = receipt.category,
                currency = receipt.currency,
                items = receipt.items,
                imageUri = receipt.imageUri,
                timestamp = receipt.timestamp
            )
        }
    }
}
