package com.thsst2.greenapp.data.sync

import android.content.Context
import com.thsst2.greenapp.MyAppDatabase
import com.thsst2.greenapp.data.repositories.*

class SyncManager(private val context: Context) {
    private val db = MyAppDatabase.getInstance(context)

    suspend fun syncAll() {
        val sessionRepo = SessionRepository(db)
        val logRepo = UserLogRepository(db)
        val perfRepo = PerformanceMetricsRepository(db)
        val feedbackRepo = UserFeedbackRepository(db)
        val geoRepo = GeofenceTriggerRepository(db)
        val pathRepo = PathDeviationAlertRepository(db)
        val locRepo = UserLocationRepository(db)
        val dialogueRepo = DialogueHistoryRepository(db)

        // Example: push unsynced logs (you can filter them in DAO)
        db.sessionDao().getAll().forEach { sessionRepo.insertSession(it, true) }
        db.userLogDao().getAll().forEach { logRepo.insertLog(it, true) }
        db.performanceMetricsDao().getAll().forEach { perfRepo.insertMetrics(it, true) }
        db.userFeedbackDao().getAll().forEach { feedbackRepo.insertFeedback(it, true) }
        db.geofenceTriggerDao().getAll().forEach { geoRepo.insertTrigger(it, true) }
        db.pathDeviationAlertDao().getAll().forEach { pathRepo.insertAlert(it, true) }
        db.userLocationDao().getAll().forEach { locRepo.insertLocation(it, true) }
        db.dialogueHistoryDao().getAll().forEach { dialogueRepo.insertDialogue(it, true) }
    }
}