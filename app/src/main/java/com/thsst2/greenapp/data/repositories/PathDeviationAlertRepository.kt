package com.thsst2.greenapp.data.repositories

import com.thsst2.greenapp.MyAppDatabase
import com.thsst2.greenapp.data.PathDeviationAlertEntity

class PathDeviationAlertRepository(private val db: MyAppDatabase) : BaseRepository() {

    suspend fun insertAlert(pathDeviationAlert: PathDeviationAlertEntity, syncToFirebase: Boolean = true) {
        db.pathDeviationAlertDao().insert(pathDeviationAlert)
        if (syncToFirebase) uploadToFirebase("path_deviation_alerts", pathDeviationAlert.pathDeviationAlertId.toString(), pathDeviationAlert)
    }

    suspend fun getAll(): List<PathDeviationAlertEntity> = db.pathDeviationAlertDao().getAll()

    suspend fun deleteAll() = db.pathDeviationAlertDao().deleteAll()
}
