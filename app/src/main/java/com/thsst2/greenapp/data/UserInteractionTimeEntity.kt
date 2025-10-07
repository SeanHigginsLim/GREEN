package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserInteractionTimeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val pointsOfInterestId: Long,
    val duration: Long, // Duration in seconds or milliseconds
    val timestamp: Long
)