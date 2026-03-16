package com.thsst2.greenapp.data.repositories

import com.thsst2.greenapp.MyAppDatabase
import com.thsst2.greenapp.data.PerformanceMetricsEntity

class PerformanceMetricsRepository(private val db: MyAppDatabase) : BaseRepository() {

    suspend fun insertMetrics(
        performanceMetrics: PerformanceMetricsEntity,
        syncToFirebase: Boolean = true
    ) {
        db.performanceMetricsDao().insert(performanceMetrics)
        if (syncToFirebase) uploadToFirebase(
            "performance_metrics",
            performanceMetrics.performanceMetricsId.toString(),
            performanceMetrics
        )
    }

    suspend fun getMetricsBySession(sessionId: Long): List<PerformanceMetricsEntity> =
        db.performanceMetricsDao().getMetricsBySession(sessionId)

    suspend fun getMetricsBySessionId(sessionId: Long): PerformanceMetricsEntity? =
        db.performanceMetricsDao().getMetricsBySessionId(sessionId)

    suspend fun getAll(): List<PerformanceMetricsEntity> = db.performanceMetricsDao().getAll()

    suspend fun deleteAll() = db.performanceMetricsDao().deleteAll()

    // Get average metrics across all sessions for analysis
    suspend fun getAverageMetrics(): Map<String, Double> {
        val allMetrics = getAll()
        if (allMetrics.isEmpty()) return emptyMap()

        return mapOf(
            "avgAccuracy" to allMetrics.map { it.routeAccuracyScore?.toDouble() ?: 0.0 }.average(),
            "avgSpeed" to allMetrics.map { it.pathGenerationTimeMs?.toDouble() ?: 0.0 }.average(),
            "avgResponseTime" to allMetrics.map { it.avgResponseTimeMs?.toDouble() ?: 0.0 }.average(),
            "avgPersonalization" to allMetrics.map { it.preferenceMatchScore?.toDouble() ?: 0.0 }.average(),
            "avgVisitedPreferred" to allMetrics.map { it.visitedPreferredRatio?.toDouble() ?: 0.0 }.average()
        )
    }
}
