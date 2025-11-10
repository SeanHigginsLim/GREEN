package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_visited_location",
    foreignKeys = [
        ForeignKey(
            entity = PoiEntity::class,
            parentColumns = ["poiId"],
            childColumns = ["poiId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserVisitedLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val userVisitedLocationId: Long = 0,
    val poiId: String,
    val sessionId: Long,
    val timestamp: Long,
    val duration: Long
)