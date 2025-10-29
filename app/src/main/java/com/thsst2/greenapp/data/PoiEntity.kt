package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poi",)
data class PoiEntity(
    @PrimaryKey(autoGenerate = true)
    val poiId: Long = 0,
    val name: String,
    val description: String?,
    val category: List<String>,
//    Not sure if will use
//    val referenceId: Long = 0,
    val latitude: Double,
    val longitude: Double
)
