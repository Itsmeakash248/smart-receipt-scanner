package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.local.dao.ReceiptDao
import com.example.data.local.entity.ReceiptEntity
import com.example.data.remote.GeminiService
import com.example.domain.model.Receipt
import com.example.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReceiptRepositoryImpl(
    private val receiptDao: ReceiptDao,
    private val geminiService: GeminiService
) : ReceiptRepository {

    override fun getAllReceipts(): Flow<List<Receipt>> {
        return receiptDao.getAllReceipts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertReceipt(receipt: Receipt): Long {
        return receiptDao.insertReceipt(ReceiptEntity.fromDomain(receipt))
    }

    override suspend fun deleteReceipt(receipt: Receipt) {
        receiptDao.deleteReceipt(ReceiptEntity.fromDomain(receipt))
    }

    override suspend fun getReceiptById(id: Long): Receipt? {
        return receiptDao.getReceiptById(id)?.toDomain()
    }

    override suspend fun analyzeReceipt(bitmap: Bitmap): Receipt {
        val geminiResult = geminiService.analyzeReceiptBitmap(bitmap)
        
        // Map raw Gemini extracted data to our domain Receipt structure
        val itemsMap = geminiResult.items?.map { it.toDomain() } ?: emptyList()
        
        return Receipt(
            merchant = geminiResult.merchant ?: "Unknown Merchant",
            date = geminiResult.date ?: "2026-05-21",
            total = geminiResult.total ?: 0.0,
            tax = geminiResult.tax ?: 0.0,
            category = geminiResult.category ?: "Other",
            currency = geminiResult.currency ?: "$",
            items = itemsMap,
            timestamp = System.currentTimeMillis()
        )
    }
}
