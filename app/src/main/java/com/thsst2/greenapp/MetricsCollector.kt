package com.thsst2.greenapp

import android.content.Context
import android.util.Log
import com.thsst2.greenapp.data.PerformanceMetricsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MetricsCollector(private val context: Context) {
    private val db = MyAppDatabase.getInstance(context)

    data class SessionAccumulator(
        var totalPathTimeMs: Long = 0,
        var pathCount: Int = 0,

        var totalResponseTimeMs: Long = 0,
        var responseCount: Int = 0,

        var totalPreferences: Int = 0,
        var matchedPreferences: Int = 0,
        var preferenceCount: Int = 0,

        var totalPoisPlanned: Int = 0,
        var totalPoisVisited: Int = 0,
        var totalPreferredPlanned: Int = 0,
        var totalPreferredVisited: Int = 0
    )

    private val sessionAccumulators = mutableMapOf<Long, SessionAccumulator>()

    /** RECORD PATH GENERATION */
    suspend fun recordPathGeneration(sessionId: Long, pathLength: Int, processingTimeMs: Long, preferredPoisCount: Int = 0) =
        withContext(Dispatchers.IO) {
            val acc = sessionAccumulators.getOrPut(sessionId) { SessionAccumulator() }
            acc.totalPathTimeMs += processingTimeMs
            acc.pathCount++
            acc.totalPoisPlanned += pathLength
            acc.totalPreferredPlanned += preferredPoisCount

            updatePerformanceMetricsDb(sessionId, acc)
            Log.d("MetricsCollector", "Path recorded: $pathLength POIs, $processingTimeMs ms, $preferredPoisCount preferred")
        }

    /** RECORD QUERY RESPONSE */
    suspend fun recordQueryResponse(sessionId: Long, responseTimeMs: Long) = withContext(Dispatchers.IO) {
        val acc = sessionAccumulators.getOrPut(sessionId) { SessionAccumulator() }
        acc.totalResponseTimeMs += responseTimeMs
        acc.responseCount++

        Log.d("MetricsCollector", "Response recorded: $responseTimeMs ms")

        updatePerformanceMetricsDb(sessionId, acc)
    }

    /** RECORD POI VISIT */
    suspend fun recordPoiVisit(sessionId: Long, wasPreferred: Boolean = false) = withContext(Dispatchers.IO) {
        val acc = sessionAccumulators.getOrPut(sessionId) { SessionAccumulator() }
        acc.totalPoisVisited++
        if (wasPreferred) acc.totalPreferredVisited++

        updatePerformanceMetricsDb(sessionId, acc)
        Log.d("MetricsCollector", "POI visit recorded. Was preferred: $wasPreferred")
    }

    /** RECORD PREFERENCE MATCHING */
    suspend fun recordPreferenceMatching(sessionId: Long, totalPreferences: Int, matchedPreferences: Int) =
        withContext(Dispatchers.IO) {
            val acc = sessionAccumulators.getOrPut(sessionId) { SessionAccumulator() }
            acc.totalPreferences += totalPreferences
            acc.matchedPreferences += matchedPreferences
            acc.preferenceCount++

            updatePerformanceMetricsDb(sessionId, acc)

            Log.d("MetricsCollector", "Preference matching recorded: $totalPreferences total, $matchedPreferences matched")
        }

    /** FINALIZE SESSION METRICS */
    suspend fun finalizeSessionMetrics(sessionId: Long) = withContext(Dispatchers.IO) {
        val acc = sessionAccumulators[sessionId] ?: return@withContext
        updatePerformanceMetricsDb(sessionId, acc, finalize = true)
        sessionAccumulators.remove(sessionId)
        Log.d("MetricsCollector", "Session $sessionId finalized")
    }

    /** INTERNAL FUNCTION TO INSERT OR UPDATE PERFORMANCE METRICS */
    private suspend fun updatePerformanceMetricsDb(sessionId: Long, acc: SessionAccumulator, finalize: Boolean = false) {
        // Compute averages
        val avgPathTime = if (acc.pathCount > 0) acc.totalPathTimeMs / acc.pathCount else 0L
        val avgResponse = if (acc.responseCount > 0) acc.totalResponseTimeMs / acc.responseCount else 0L
        val avgPreferenceMatch = if (acc.preferenceCount > 0 && acc.totalPreferences > 0) {
            (acc.matchedPreferences.toDouble() / acc.totalPreferences * 100).toLong()
        } else 0L
        val routeAccuracy = if (acc.totalPoisPlanned > 0) {
            (acc.totalPoisVisited.toDouble() / acc.totalPoisPlanned * 100).toLong()
        } else 0L
        val visitedPreferredRatio = if (acc.totalPreferredPlanned > 0) {
            (acc.totalPreferredVisited.toDouble() / acc.totalPreferredPlanned * 100).toLong()
        } else 0L

        // Try to get existing row
        val existing = db.performanceMetricsDao().getMetricsBySessionId(sessionId)

        Log.d("MetricsCollector", "Existing metrics: $existing")

        if (existing == null) {
            val newMetrics = PerformanceMetricsEntity(
                sessionId = sessionId,
                routeAccuracyScore = routeAccuracy,
                pathGenerationTimeMs = avgPathTime,
                avgResponseTimeMs = avgResponse,
                preferenceMatchScore = avgPreferenceMatch,
                visitedPreferredRatio = visitedPreferredRatio
            )
            db.performanceMetricsDao().insert(newMetrics)
        } else {
            val updatedMetrics = existing.copy(
                routeAccuracyScore = routeAccuracy,
                pathGenerationTimeMs = avgPathTime,
                avgResponseTimeMs = avgResponse,
                preferenceMatchScore = avgPreferenceMatch,
                visitedPreferredRatio = visitedPreferredRatio
            )
            val rowsUpdated = db.performanceMetricsDao().update(updatedMetrics)
            Log.d("MetricsCollector", "Updated metrics: $updatedMetrics")
            Log.d("MetricsCollector", "Rows updated: $rowsUpdated")
        }

        if (finalize) Log.d("MetricsCollector", "Final metrics updated for session $sessionId")
    }
}