package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserTourPathHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val pathSequence: String, // Store as JSON or comma-separated IDs
    val algorithmUsed: String?,
    val status: String?
)