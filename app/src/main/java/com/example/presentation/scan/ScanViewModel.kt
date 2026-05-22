package com.example.presentation.scan

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Receipt
import com.example.domain.usecase.ScanReceiptUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Analyzing : ScanUiState
    data class Success(val receipt: Receipt) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class ScanViewModel(
    private val scanReceiptUseCase: ScanReceiptUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    // Holds the active bitmap image being analyzed for display on the result screen
    var lastScannedBitmap: Bitmap? = null
        private set

    fun analyzeReceipt(bitmap: Bitmap, onSuccess: (Receipt) -> Unit) {
        viewModelScope.launch {
            _uiState.value = ScanUiState.Analyzing
            lastScannedBitmap = bitmap
            try {
                val receipt = scanReceiptUseCase(bitmap)
                _uiState.value = ScanUiState.Success(receipt)
                onSuccess(receipt)
            } catch (e: Exception) {
                _uiState.value = ScanUiState.Error(e.localizedMessage ?: "Unknown analysis error")
            }
        }
    }

    fun resetState() {
        _uiState.value = ScanUiState.Idle
    }
}
