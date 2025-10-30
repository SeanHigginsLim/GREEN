package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_tour_path_history",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserTourPathHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val userTourPathHistoryId: Long = 0,
    val sessionId: Long,
    val pathSequence: List<String>?, // Store as JSON or comma-separated IDs
    val algorithmUsed: String,
    val status: String?
)