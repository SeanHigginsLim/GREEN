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
    
    // Accuracy metrics (0-100 scale)
    val routeAccuracyScore: Long,        // How accurately the route followed planned path
    
    // Speed metrics (milliseconds)
    val pathGenerationTimeMs: Long,       // Time to generate the tour path
    val avgResponseTimeMs: Long,          // Average response time for user queries
    
    // Personalization effectiveness (0-100 scale)
    val preferenceMatchScore: Long,       // How well POIs matched user preferences
    val visitedPreferredRatio: Long,      // Ratio of preferred POIs visited vs planned
    
    val recordedAt: Long = System.currentTimeMillis()
)
