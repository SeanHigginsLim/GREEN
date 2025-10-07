package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserSkippedOrDislikedLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val pointsOfInterestId: Long,
    val reason: String?,
    val timestamp: Long
)