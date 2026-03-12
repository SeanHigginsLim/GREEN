package com.thsst2.greenapp

import android.content.Context
import com.thsst2.greenapp.data.PerformanceMetricsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class MetricsCollector(private val context: Context) {
    private val db = MyAppDatabase.getInstance(context)
    private val sessionMetrics = mutableMapOf<Long, SessionMetrics>()
    
    data class SessionMetrics(
        var totalPoisPlanned: Int = 0,
        var totalPoisVisited: Int = 0,
        var preferredPoisPlanned: Int = 0,
        var preferredPoisVisited: Int = 0,
        var totalPreferences: Int = 0,
        var matchedPreferences: Int = 0,
        var queryResponseTimes: MutableList<Long> = mutableListOf(),
        var pathGenerationTime: Long = 0
    )
    
    suspend fun recordPathGeneration(
        sessionId: Long,
        pathLength: Int,
        processingTimeMs: Long,
        preferredPoisCount: Int = 0
    ) = withContext(Dispatchers.IO) {
        val metrics = sessionMetrics.getOrPut(sessionId) { SessionMetrics() }
        metrics.totalPoisPlanned += pathLength
        metrics.preferredPoisPlanned += preferredPoisCount
        metrics.pathGenerationTime = processingTimeMs
        
        Log.d("MetricsCollector", "Path: $pathLength POIs, ${processingTimeMs}ms, $preferredPoisCount preferred")
    }
    
    suspend fun recordQueryResponse(sessionId: Long, responseTimeMs: Long) = withContext(Dispatchers.IO) {
        val metrics = sessionMetrics.getOrPut(sessionId) { SessionMetrics() }
        metrics.queryResponseTimes.add(responseTimeMs)
    }
    
    suspend fun recordPoiVisit(sessionId: Long, poiId: String, wasPreferred: Boolean = false) = withContext(Dispatchers.IO) {
        val metrics = sessionMetrics.getOrPut(sessionId) { SessionMetrics() }
        metrics.totalPoisVisited++
        if (wasPreferred) {
            metrics.preferredPoisVisited++
        }
    }
    
    suspend fun recordPreferenceMatching(
        sessionId: Long,
        totalPreferences: Int,
        matchedPreferences: Int
    ) = withContext(Dispatchers.IO) {
        val metrics = sessionMetrics.getOrPut(sessionId) { SessionMetrics() }
        metrics.totalPreferences = totalPreferences
        metrics.matchedPreferences = matchedPreferences
    }
    
    suspend fun finalizeSessionMetrics(sessionId: Long, experienceRating: Long = 0) = withContext(Dispatchers.IO) {
        val metrics = sessionMetrics[sessionId] ?: return@withContext
        
        // Accuracy: route completion (0-100)
        val routeAccuracy = if (metrics.totalPoisPlanned > 0) {
            ((metrics.totalPoisVisited.toDouble() / metrics.totalPoisPlanned) * 100).toLong()
        } else 0L
        
        val completionRate = routeAccuracy
        
        // Speed: average response time
        val avgResponseTime = if (metrics.queryResponseTimes.isNotEmpty()) {
            metrics.queryResponseTimes.average().toLong()
        } else 0L
        
        // Personalization: preference matching (0-100)
        val preferenceMatch = if (metrics.totalPreferences > 0) {
            ((metrics.matchedPreferences.toDouble() / metrics.totalPreferences) * 100).toLong()
        } else 0L
        
        val visitedPreferredRatio = if (metrics.preferredPoisPlanned > 0) {
            ((metrics.preferredPoisVisited.toDouble() / metrics.preferredPoisPlanned) * 100).toLong()
        } else 0L
        
        val performanceMetrics = PerformanceMetricsEntity(
            sessionId = sessionId,
            routeAccuracyScore = routeAccuracy,
            completionRate = completionRate,
            pathGenerationTimeMs = metrics.pathGenerationTime,
            avgResponseTimeMs = avgResponseTime,
            preferenceMatchScore = preferenceMatch,
            visitedPreferredRatio = visitedPreferredRatio,
            experienceRating = experienceRating
        )
        
        db.performanceMetricsDao().insert(performanceMetrics)
        sessionMetrics.remove(sessionId)
        
        Log.d("MetricsCollector", "Metrics - Accuracy: $routeAccuracy%, Speed: ${avgResponseTime}ms, Personalization: $preferenceMatch%")
    }
}
