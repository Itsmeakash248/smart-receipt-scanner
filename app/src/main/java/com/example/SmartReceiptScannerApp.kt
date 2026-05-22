package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.ReceiptDatabase
import com.example.data.remote.GeminiService
import com.example.data.repository.ReceiptRepositoryImpl
import com.example.domain.repository.ReceiptRepository
import com.example.domain.usecase.GetHistoryUseCase
import com.example.domain.usecase.ScanReceiptUseCase

class SmartReceiptScannerApp : Application() {

    lateinit var database: ReceiptDatabase
        private set

    lateinit var repository: ReceiptRepository
        private set

    lateinit var scanReceiptUseCase: ScanReceiptUseCase
        private set

    lateinit var getHistoryUseCase: GetHistoryUseCase
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            ReceiptDatabase::class.java,
            "smart_receipt_scanner_db"
        ).fallbackToDestructiveMigration().build()

        val geminiService = GeminiService()

        repository = ReceiptRepositoryImpl(
            receiptDao = database.receiptDao(),
            geminiService = geminiService
        )

        scanReceiptUseCase = ScanReceiptUseCase(repository)
        getHistoryUseCase = GetHistoryUseCase(repository)
    }
}
