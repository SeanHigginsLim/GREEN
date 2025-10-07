package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SessionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionLogId: Long = 0,
    val userId: Long,
    val startTime: Long,
    val endTime: Long?,
    val componentsUsed: String? // Store as comma-separated or JSON
)