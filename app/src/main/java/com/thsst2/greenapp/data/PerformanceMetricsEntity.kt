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
    val completionRate: Long,             // Percentage of planned POIs actually visited(Cross check final tour plan with visited locations)
    
    // Speed metrics (milliseconds)
    val pathGenerationTimeMs: Long,       // Time to generate the tour path(Add timer for path generation)
    val avgResponseTimeMs: Long,          // Average response time for user queries(Add timer for responses)
    
    // Personalization effectiveness (0-100 scale)
    val preferenceMatchScore: Long,       // How well POIs matched user preferences(Cross check user preferences with planned path)
    val visitedPreferredRatio: Long,      // Ratio of preferred POIs visited vs planned(Cross check user preferences with planned path)
    
    // User experience (0-5 scale, collected from survey)
    val experienceRating: Long = 0,       // Overall user experience rating(From user feedback entity)
    
    val recordedAt: Long = System.currentTimeMillis()
)
