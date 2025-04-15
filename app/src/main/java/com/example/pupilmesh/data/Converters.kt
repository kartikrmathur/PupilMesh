package com.example.pupilmesh.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return if (value == null) "" else gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String>? {
        if (value.isEmpty()) return null
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
    
    @TypeConverter
    fun fromBoolean(value: Boolean?): Int {
        return value?.let { if (it) 1 else 0 } ?: 0
    }
    
    @TypeConverter
    fun toBoolean(value: Int): Boolean? {
        return when (value) {
            0 -> false
            1 -> true
            else -> null
        }
    }
} 