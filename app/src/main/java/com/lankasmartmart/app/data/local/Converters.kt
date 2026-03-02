package com.lankasmartmart.app.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lankasmartmart.app.data.model.OrderItem
import com.lankasmartmart.app.data.model.Address
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromOrderItemList(value: List<OrderItem>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toOrderItemList(value: String): List<OrderItem>? {
        val listType = object : TypeToken<List<OrderItem>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromAddress(value: Address?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toAddress(value: String): Address? {
         return gson.fromJson(value, Address::class.java)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
