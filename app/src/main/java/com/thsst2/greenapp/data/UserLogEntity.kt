package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_log",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GeneratedPathEntity::class,
            parentColumns = ["generatedPathId"],
            childColumns = ["generatedPathId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GeofenceTriggerEntity::class,
            parentColumns = ["geofenceTriggerId"],
            childColumns = ["geofenceTriggerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PathDeviationAlertEntity::class,
            parentColumns = ["pathDeviationAlertId"],
            childColumns = ["pathDeviationAlertId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserFeedbackEntity::class,
            parentColumns = ["userFeedbackId"],
            childColumns = ["userFeedbackId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserInteractionTimeEntity::class,
            parentColumns = ["userInteractionTimeId"],
            childColumns = ["userInteractionTimeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserLogEntity(
    @PrimaryKey(autoGenerate = true)
    val userLogId: Long = 0,
    val userId: Long,
    val generatedPathId: Long,
    val geofenceTriggerId: Long,
    val pathDeviationAlertId: Long,
    val userFeedbackId: Long,
    val userInteractionTimeId: Long
)