package com.example.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Receipt
import com.example.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val receipts: List<Receipt> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = ""
)

class HistoryViewModel(
    private val repository: ReceiptRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getAllReceipts(),
        _selectedCategory,
        _searchQuery
    ) { allReceipts, category, search ->
        val categories = listOf("All") + allReceipts.map { it.category }.distinct()
        
        val filtered = allReceipts.filter { receipt ->
            val matchesCategory = category == "All" || receipt.category.equals(category, ignoreCase = true)
            val matchesSearch = search.isEmpty() ||
                    receipt.merchant.contains(search, ignoreCase = true) ||
                    receipt.category.contains(search, ignoreCase = true)
            matchesCategory && matchesSearch
        }

        HistoryUiState(
            receipts = filtered,
            categories = categories,
            selectedCategory = category,
            searchQuery = search
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteReceipt(receipt: Receipt) {
        viewModelScope.launch {
            repository.deleteReceipt(receipt)
        }
    }
}
