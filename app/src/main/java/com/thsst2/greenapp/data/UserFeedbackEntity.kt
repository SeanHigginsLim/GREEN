package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserFeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val pointsOfInterestId: Long,
    val rating: Int?, // e.g., 1-5 stars
    val comments: String?,
    val timestamp: Long
)