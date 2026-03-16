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
        val accuracy: Long?,
        val speedMs: Long?,
        val avgResponseMs: Long?,
        val personalizationScore: Long?,
        val visitedPreferredRatio: Long?,
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
        val avgVisitedPreferred: Double
    )
    
    suspend fun exportMetrics(): String = withContext(Dispatchers.IO) {
        try {
            val performanceMetrics = db.performanceMetricsDao().getAll().map {
                PerformanceMetricsData(
                    sessionId = it.sessionId,
                    accuracy = it.routeAccuracyScore,
                    speedMs = it.pathGenerationTimeMs,
                    avgResponseMs = it.avgResponseTimeMs,
                    personalizationScore = it.preferenceMatchScore,
                    visitedPreferredRatio = it.visitedPreferredRatio,
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
                avgAccuracy = performanceMetrics.map { it.accuracy?.toDouble() ?: 0.0 }.average(),
                avgSpeed = performanceMetrics.map { it.speedMs?.toDouble() ?: 0.0 }.average(),
                avgPersonalization = performanceMetrics.map { it.personalizationScore?.toDouble() ?: 0.0 }.average(),
                avgVisitedPreferred = performanceMetrics.map { it.visitedPreferredRatio?.toDouble() ?: 0.0 }.average()
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
