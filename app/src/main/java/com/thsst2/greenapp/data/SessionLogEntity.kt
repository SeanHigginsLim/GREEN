package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_log",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class SessionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionLogId: Long = 0,
    val userId: Long,
    val performanceMetrics: List<PerformanceMetricsEntity>,
    val sessions: List<SessionEntity>,
    val userLocations: List<UserLocationEntity>,
    val userLogs: List<UserLogEntity>,
    val userSkippedOrDislikedLocations: List<UserSkippedOrDislikedLocationEntity>,
    val userTourPathHistories: List<UserTourPathHistoryEntity>,
    val userVisitedLocations: List<UserVisitedLocationEntity>,
)