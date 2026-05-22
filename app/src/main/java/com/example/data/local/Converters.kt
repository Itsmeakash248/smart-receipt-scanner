package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.model.ReceiptItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class Converters {
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, ReceiptItem::class.java)
    private val adapter = moshi.adapter<List<ReceiptItem>>(listType)

    @TypeConverter
    fun fromItemsList(items: List<ReceiptItem>?): String? {
        return items?.let { adapter.toJson(it) }
    }

    @TypeConverter
    fun toItemsList(json: String?): List<ReceiptItem>? {
        return json?.let { adapter.fromJson(it) } ?: emptyList()
    }
}
