package com.example.domain.usecase

import android.graphics.Bitmap
import com.example.domain.model.Receipt
import com.example.domain.repository.ReceiptRepository

class ScanReceiptUseCase(private val repository: ReceiptRepository) {
    suspend operator fun invoke(bitmap: Bitmap): Receipt {
        return repository.analyzeReceipt(bitmap)
    }
}
