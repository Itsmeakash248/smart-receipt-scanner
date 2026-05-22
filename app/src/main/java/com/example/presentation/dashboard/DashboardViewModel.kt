package com.example.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Receipt
import com.example.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class CategorySpend(
    val category: String,
    val totalAmount: Double,
    val percentage: Float
)

data class DashboardUiState(
    val categorySpends: List<CategorySpend> = emptyList(),
    val totalMonthlySpend: Double = 0.0,
    val selectedMonthName: String = ""
)

class DashboardViewModel(
    private val repository: ReceiptRepository
) : ViewModel() {

    private val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val uiState: StateFlow<DashboardUiState> = repository.getAllReceipts()
        .map { receipts ->
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            val monthName = monthNames.getOrElse(currentMonth) { "Current Month" } + " $currentYear"

            // Filter for current month scans
            val currentMonthReceipts = receipts.filter { receipt ->
                val rCal = Calendar.getInstance().apply { timeInMillis = receipt.timestamp }
                rCal.get(Calendar.MONTH) == currentMonth && rCal.get(Calendar.YEAR) == currentYear
            }

            val totalSpend = currentMonthReceipts.sumOf { it.total }

            // Group scans by category
            val spendsByCategory = currentMonthReceipts.groupBy { it.category }
                .map { (category, itemReceipts) ->
                    val categoryTotal = itemReceipts.sumOf { it.total }
                    val percentage = if (totalSpend > 0) (categoryTotal / totalSpend).toFloat() else 0f
                    CategorySpend(category, categoryTotal, percentage)
                }
                .sortedByDescending { it.totalAmount }

            DashboardUiState(
                categorySpends = spendsByCategory,
                totalMonthlySpend = totalSpend,
                selectedMonthName = monthName
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )
}
