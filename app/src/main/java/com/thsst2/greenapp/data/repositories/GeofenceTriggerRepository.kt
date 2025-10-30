package com.thsst2.greenapp.data.repositories

import com.thsst2.greenapp.data.MyAppDatabase
import com.thsst2.greenapp.data.GeofenceTriggerEntity

class GeofenceTriggerRepository(private val db: MyAppDatabase) : BaseRepository() {

    suspend fun insertTrigger(geofenceTrigger: GeofenceTriggerEntity, syncToFirebase: Boolean = true) {
        db.geofenceTriggerDao().insert(geofenceTrigger)
        if (syncToFirebase) uploadToFirebase("geofence_triggers", geofenceTrigger.geofenceTriggerId.toString(), geofenceTrigger)
    }

    suspend fun getAll(): List<GeofenceTriggerEntity> = db.geofenceTriggerDao().getAll()

    suspend fun deleteAll() = db.geofenceTriggerDao().deleteAll()
}
