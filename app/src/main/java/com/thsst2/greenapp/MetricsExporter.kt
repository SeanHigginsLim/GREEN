package com.thsst2.greenapp

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MetricsExporter(private val context: Context) {
    
    private val db = MyAppDatabase.getInstance(context)
    private val gson = Gson()
    
    data class MetricsExport(
        val performanceMetrics: List<PerformanceMetricsData>,
        val userFeedback: List<UserFeedbackData>,
        val summary: MetricsSummary
    )
    
    data class PerformanceMetricsData(
        val sessionId: Long,
        val accuracy: Long,
        val completionRate: Long,
        val speedMs: Long,
        val avgResponseMs: Long,
        val personalizationScore: Long,
        val experienceRating: Long,
        val timestamp: Long
    )
    
    data class UserFeedbackData(
        val userId: Long,
        val poiId: String,
        val overallRating: Int?,
        val experienceRating: Int?,
        val personalizationRating: Int?,
        val navigationEaseRating: Int?,
        val timestamp: Long
    )
    
    data class MetricsSummary(
        val totalSessions: Int,
        val avgAccuracy: Double,
        val avgSpeed: Double,
        val avgPersonalization: Double,
        val avgExperience: Double
    )
    
    suspend fun exportMetrics(): String = withContext(Dispatchers.IO) {
        try {
            val performanceMetrics = db.performanceMetricsDao().getAll().map {
                PerformanceMetricsData(
                    sessionId = it.sessionId,
                    accuracy = it.routeAccuracyScore,
                    completionRate = it.completionRate,
                    speedMs = it.pathGenerationTimeMs,
                    avgResponseMs = it.avgResponseTimeMs,
                    personalizationScore = it.preferenceMatchScore,
                    experienceRating = it.experienceRating,
                    timestamp = it.recordedAt
                )
            }
            
            val userFeedback = db.userFeedbackDao().getAll().map {
                UserFeedbackData(
                    userId = it.userId,
                    poiId = it.poiId,
                    overallRating = it.rating,
                    experienceRating = it.experienceRating,
                    personalizationRating = it.personalizationRating,
                    navigationEaseRating = it.navigationEaseRating,
                    timestamp = it.timestamp
                )
            }
            
            val summary = MetricsSummary(
                totalSessions = performanceMetrics.size,
                avgAccuracy = performanceMetrics.map { it.accuracy }.average(),
                avgSpeed = performanceMetrics.map { it.speedMs }.average(),
                avgPersonalization = performanceMetrics.map { it.personalizationScore }.average(),
                avgExperience = performanceMetrics.map { it.experienceRating }.average()
            )
            
            val export = MetricsExport(performanceMetrics, userFeedback, summary)
            val json = gson.toJson(export)
            
            // Save to file
            val file = File(context.getExternalFilesDir(null), "metrics_export_${System.currentTimeMillis()}.json")
            file.writeText(json)
            
            Log.d("MetricsExporter", "Exported metrics to ${file.absolutePath}")
            json
        } catch (e: Exception) {
            Log.e("MetricsExporter", "Failed to export metrics", e)
            "{\"error\": \"${e.message}\"}"
        }
    }
}
