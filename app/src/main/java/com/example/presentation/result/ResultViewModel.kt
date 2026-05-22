package com.example.presentation.result

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Receipt
import com.example.domain.model.ReceiptItem
import com.example.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ResultUiState {
    object Idle : ResultUiState
    object Saving : ResultUiState
    object Saved : ResultUiState
    data class Error(val message: String) : ResultUiState
}

class ResultViewModel(
    private val repository: ReceiptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Idle)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    // Mutable state variables representing fields that the user can edit before saving
    var merchant by mutableStateOf("")
    var total by mutableStateOf(0.0)
    var tax by mutableStateOf(0.0)
    var category by mutableStateOf("Other")
    var date by mutableStateOf("")
    var currency by mutableStateOf("$")
    var items by mutableStateOf<List<ReceiptItem>>(emptyList())
    
    // Original receipt reference
    private var originalReceipt: Receipt? = null

    fun initialize(receipt: Receipt) {
        if (originalReceipt == null || originalReceipt?.timestamp != receipt.timestamp) {
            originalReceipt = receipt
            merchant = receipt.merchant
            total = receipt.total
            tax = receipt.tax
            category = receipt.category
            date = receipt.date
            currency = receipt.currency
            items = receipt.items
        }
    }

    fun updateReceiptField(
        merchantName: String = merchant,
        totalAmount: Double = total,
        taxAmount: Double = tax,
        categoryName: String = category,
        dateStr: String = date,
        currencySymbol: String = currency,
        itemList: List<ReceiptItem> = items
    ) {
        merchant = merchantName
        total = totalAmount
        tax = taxAmount
        category = categoryName
        date = dateStr
        currency = currencySymbol
        items = itemList
    }

    fun saveReceipt(imageUri: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = ResultUiState.Saving
            try {
                val receiptToSave = Receipt(
                    merchant = merchant,
                    date = date,
                    total = total,
                    tax = tax,
                    category = category,
                    currency = currency,
                    items = items,
                    imageUri = imageUri,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertReceipt(receiptToSave)
                _uiState.value = ResultUiState.Saved
                onComplete()
            } catch (e: Exception) {
                _uiState.value = ResultUiState.Error(e.localizedMessage ?: "Failed to save receipt")
            }
        }
    }
}
