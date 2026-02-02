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
        )
    ]
)
data class UserLogEntity(
    @PrimaryKey(autoGenerate = true)
    val userLogId: Long = 0,
    val userId: Long,
    val generatedPaths: List<GeneratedPathEntity>,
    val geofenceTriggers: List<GeofenceTriggerEntity>,
    val pathDeviationAlerts: List<PathDeviationAlertEntity>,
    val dialogueHistories: List<DialogueHistoryEntity>,
    val intentLogs: List<IntentLogEntity>,
    val sessionId: Long,
    val userQueries: List<UserQueryEntity>,
    val userFeedback: List<UserFeedbackEntity>,
    val userInteractionTimes: List<UserInteractionTimeEntity>
)