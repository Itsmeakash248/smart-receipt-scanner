package com.example.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Receipt
import com.example.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val recentScans: List<Receipt> = emptyList(),
    val totalMonthlySpend: Double = 0.0,
    val totalScansCount: Int = 0,
    val monthlyScansCount: Int = 0,
    val currency: String = "$"
)

class HomeViewModel(
    private val repository: ReceiptRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.getAllReceipts()
        .map { receipts ->
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            var monthlyTotal = 0.0
            var monthlyCount = 0

            receipts.forEach { receipt ->
                val receiptCal = Calendar.getInstance().apply {
                    timeInMillis = receipt.timestamp
                }
                if (receiptCal.get(Calendar.MONTH) == currentMonth &&
                    receiptCal.get(Calendar.YEAR) == currentYear) {
                    monthlyTotal += receipt.total
                    monthlyCount++
                }
            }

            HomeUiState(
                recentScans = receipts.take(5),
                totalMonthlySpend = monthlyTotal,
                totalScansCount = receipts.size,
                monthlyScansCount = monthlyCount,
                currency = receipts.firstOrNull()?.currency ?: "$"
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun deleteReceipt(receipt: Receipt) {
        viewModelScope.launch {
            repository.deleteReceipt(receipt)
        }
    }
}
