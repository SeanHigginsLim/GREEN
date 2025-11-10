package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_data")
data class LocalDataEntity(
    @PrimaryKey(autoGenerate = true)
    val localDataId: Long = 0,
    val userId: Long,
    val tourName: String?,
    val orderedPoisJson: List<String>, // JSON map: ["LS", "SJ", "Agno"]
    val poiInfoJson: String,     // JSON map: {"LS": {...}, "SJ": {...}}
)