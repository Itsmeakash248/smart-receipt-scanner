package com.example.domain.repository

import android.graphics.Bitmap
import com.example.domain.model.Receipt
import kotlinx.coroutines.flow.Flow

interface ReceiptRepository {
    fun getAllReceipts(): Flow<List<Receipt>>
    suspend fun insertReceipt(receipt: Receipt): Long
    suspend fun deleteReceipt(receipt: Receipt)
    suspend fun getReceiptById(id: Long): Receipt?
    suspend fun analyzeReceipt(bitmap: Bitmap): Receipt
}
