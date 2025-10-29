package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "performance_metrics",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PerformanceMetricsEntity(
    @PrimaryKey(autoGenerate = true)
    val performanceMetricsId: Long = 0,
    val sessionId: Long,
    val routeAccuracyScore: Long,
    val querySuccessRate: Long,
    val completionRate: Long
)
