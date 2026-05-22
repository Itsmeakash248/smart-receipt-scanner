package com.example.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.SmartReceiptScannerApp
import com.example.domain.usecase.GetHistoryUseCase
import com.example.domain.usecase.ScanReceiptUseCase
import com.example.domain.repository.ReceiptRepository
import com.example.presentation.dashboard.DashboardViewModel
import com.example.presentation.history.HistoryViewModel
import com.example.presentation.home.HomeViewModel
import com.example.presentation.result.ResultViewModel
import com.example.presentation.scan.ScanViewModel

class ViewModelFactory(
    private val app: SmartReceiptScannerApp
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(app.repository) as T
            }
            modelClass.isAssignableFrom(ScanViewModel::class.java) -> {
                ScanViewModel(app.scanReceiptUseCase) as T
            }
            modelClass.isAssignableFrom(ResultViewModel::class.java) -> {
                ResultViewModel(app.repository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(app.repository) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(app.repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
