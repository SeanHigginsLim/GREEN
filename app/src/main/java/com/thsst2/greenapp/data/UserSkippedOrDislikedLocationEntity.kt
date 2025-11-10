package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_skipped_or_disliked_location",
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
data class UserSkippedOrDislikedLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val userSkippedOrDislikedLocationId: Long = 0,
    val poiId: String,
    val sessionId: Long,
    val reason: String?,
    val timestamp: Long
)