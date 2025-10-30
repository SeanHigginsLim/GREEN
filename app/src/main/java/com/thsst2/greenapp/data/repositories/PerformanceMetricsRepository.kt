package com.thsst2.greenapp.data.repositories

import com.thsst2.greenapp.MyAppDatabase
import com.thsst2.greenapp.data.PerformanceMetricsEntity

class PerformanceMetricsRepository(private val db: MyAppDatabase) : BaseRepository() {

    suspend fun insertMetrics(performanceMetrics: PerformanceMetricsEntity, syncToFirebase: Boolean = true) {
        db.performanceMetricsDao().insert(performanceMetrics)
        if (syncToFirebase) uploadToFirebase("performance_metrics", performanceMetrics.performanceMetricsId.toString(), performanceMetrics)
    }

    suspend fun getAll(): List<PerformanceMetricsEntity> = db.performanceMetricsDao().getAll()

    suspend fun deleteAll() = db.performanceMetricsDao().deleteAll()
}
