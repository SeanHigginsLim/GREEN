package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserLogEntity(
    @PrimaryKey(autoGenerate = true)
    val logId: Long = 0,
    val userId: Long,
    val generatedPathId: Long?,
    val pathDeviationAlertId: Long?,
    val geofenceTriggerId: Long?,
    val userFeedbackId: Long?,
    val userInteractionTimeId: Long?
)