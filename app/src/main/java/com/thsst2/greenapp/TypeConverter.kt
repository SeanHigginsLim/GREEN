package com.thsst2.greenapp

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import androidx.room.TypeConverter

class TypeConverter {
    @TypeConverter
    fun fromList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}