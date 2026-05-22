package com.example.domain.usecase

import com.example.domain.model.Receipt
import com.example.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow

class GetHistoryUseCase(private val repository: ReceiptRepository) {
    operator fun invoke(): Flow<List<Receipt>> {
        return repository.getAllReceipts()
    }
}
