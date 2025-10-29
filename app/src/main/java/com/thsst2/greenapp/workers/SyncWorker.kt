package com.thsst2.greenapp.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.thsst2.greenapp.data.sync.SyncManager

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val syncManager = SyncManager(applicationContext)
        syncManager.syncAll()
        return Result.success()
    }
}