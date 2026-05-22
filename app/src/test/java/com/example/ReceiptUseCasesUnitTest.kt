package com.example

import android.graphics.Bitmap
import com.example.domain.model.Receipt
import com.example.domain.repository.ReceiptRepository
import com.example.domain.usecase.GetHistoryUseCase
import com.example.domain.usecase.ScanReceiptUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class MockReceiptRepository : ReceiptRepository {
    val receiptsList = mutableListOf<Receipt>()
    var mockAnalyzedReceipt: Receipt? = null

    override fun getAllReceipts(): Flow<List<Receipt>> {
        return flowOf(receiptsList)
    }

    override suspend fun insertReceipt(receipt: Receipt): Long {
        receiptsList.add(receipt)
        return receiptsList.size.toLong()
    }

    override suspend fun deleteReceipt(receipt: Receipt) {
        receiptsList.remove(receipt)
    }

    override suspend fun getReceiptById(id: Long): Receipt? {
        return receiptsList.find { it.id == id }
    }

    override suspend fun analyzeReceipt(bitmap: Bitmap): Receipt {
        return mockAnalyzedReceipt ?: Receipt(
            merchant = "Mock Target Shop",
            date = "2026-05-21",
            total = 120.50,
            tax = 8.50,
            category = "Shopping",
            currency = "$",
            items = emptyList()
        )
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReceiptUseCasesUnitTest {

    @Test
    fun testScanReceiptUseCase() = runBlocking {
        val repository = MockReceiptRepository()
        val useCase = ScanReceiptUseCase(repository)

        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val result = useCase(mockBitmap)

        assertEquals("Mock Target Shop", result.merchant)
        assertEquals(120.50, result.total, 0.0)
        assertEquals("Shopping", result.category)
    }

    @Test
    fun testGetHistoryUseCase() = runBlocking {
        val repository = MockReceiptRepository()
        val scan = Receipt(
            id = 5L,
            merchant = "Starbucks Coffee",
            date = "2026-05-21",
            total = 4.75,
            tax = 0.35,
            category = "Food",
            currency = "$",
            items = emptyList()
        )
        repository.insertReceipt(scan)

        val useCase = GetHistoryUseCase(repository)
        val list = useCase().first()

        assertEquals(1, list.size)
        assertEquals("Starbucks Coffee", list[0].merchant)
        assertEquals(4.75, list[0].total, 0.0)
    }
}
