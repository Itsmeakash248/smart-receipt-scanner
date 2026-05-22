package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.ReceiptDao
import com.example.data.local.entity.ReceiptEntity

@Database(entities = [ReceiptEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ReceiptDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
}
