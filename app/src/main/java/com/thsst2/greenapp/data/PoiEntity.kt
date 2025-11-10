package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "poi",
    foreignKeys = [
        ForeignKey(
            entity = GeneratedPathEntity::class,
            parentColumns = ["generatedPathId"],
            childColumns = ["generatedPathId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PoiEntity(
    @PrimaryKey()
    val poiId: Long,
    val generatedPathId: Long?,
    val name: String,
    val description: String?,
    val category: List<String>,
//    Not sure if will use
//    val referenceId: Long = 0,
    val latitude: Double,
    val longitude: Double
)
