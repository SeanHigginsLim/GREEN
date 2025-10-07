package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class IntentLogEntity(
    @PrimaryKey(autoGenerate = true)
    val intentLogId: Long = 0,
    val userQueryId: Long,
    val intentLabel: String,
    val confidenceScore: Double?,
    val entities: String? // Store as JSON or comma-separated
)