package com.thsst2.greenapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Date
import java.util.Locale
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.FirebaseAuth
import com.thsst2.greenapp.data.GeofenceTriggerEntity
import com.thsst2.greenapp.data.PathDeviationAlertEntity
import com.thsst2.greenapp.data.UserInteractionTimeEntity
import com.thsst2.greenapp.data.UserLocationEntity
import com.thsst2.greenapp.data.UserVisitedLocationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GeofenceReceiver", "onReceive triggered with intent: ${intent.action}")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "Invalid geofencing event: ${geofencingEvent?.errorCode}")
            return
        }

        val transition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences
        val triggeredNames = triggeringGeofences?.joinToString { it.requestId } ?: "Unknown"

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid?.hashCode()?.toLong() ?: -1L

        GlobalScope.launch(Dispatchers.IO) {
            val db = MyAppDatabase.getInstance(context)
            val poiDao = db.poiDao()
            val geoDao = db.geofenceTriggerDao()
            val visitDao = db.userVisitedLocationDao()
            val interactionDao = db.userInteractionTimeDao()
            val deviationDao = db.pathDeviationAlertDao()
            val userLocationDao = db.userLocationDao()

            val now = System.currentTimeMillis()
            val readableNow = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(now))
            val sessionId = getActiveSessionId(db)

            val matchedPoi = try {
                poiDao.getAll().find { it.name == triggeredNames }
            } catch (e: Exception) {
                Log.e("GeofenceReceiver", "Error fetching POI: ${e.localizedMessage}")
                null
            }

            if (matchedPoi == null) {
                Log.w("GeofenceReceiver", "No POI found for $triggeredNames")
                return@launch
            }

            when (transition) {
                // (LOGIC IDEA ONLY)
                // onEnterPoi(poiId: Long)
                //     insert UserVisitedLocationEntity
                //     update UserLocationEntity
                //     display POI info based on floor/preferences
                //
                // onExitPoi(poiId: Long)
                //     insert UserInteractionTimeEntity
                //     track skipped/disliked POIs
                //     check path deviation -> PathDeviationAlertEntity

                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    geoDao.insert(
                        GeofenceTriggerEntity(
                            userId = userId,
                            poiId = matchedPoi.poiId,
                            userLogId = null,
                            entryTime = readableNow,
                            exitTime = "",
                            triggerType = "ENTER"
                        )
                    )

                    visitDao.insert(
                        UserVisitedLocationEntity(
                            poiId = matchedPoi.poiId,
                            sessionId = sessionId,
                            timestamp = now,
                            duration = 0L
                        )
                    )

                    userLocationDao.insert(
                        UserLocationEntity(
                            userLocationId = 0,
                            userId = userId,
                            sessionId = sessionId,
                            latitude = matchedPoi.latitude,
                            longitude = matchedPoi.longitude,
                            timestamp = now,
                            accuracyRadius = 5f
                        )
                    )

                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Entered ${matchedPoi.name}", Toast.LENGTH_SHORT).show()
                    }

                    // TODO: display POI info based on floor/preferences


                }

                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    geoDao.insert(
                        GeofenceTriggerEntity(
                            userId = userId,
                            poiId = matchedPoi.poiId,
                            userLogId = null,
                            entryTime = "",
                            exitTime = readableNow,
                            triggerType = "EXIT"
                        )
                    )

                    val lastVisit = visitDao.getAll()
                        .filter { it.poiId == matchedPoi.poiId && it.sessionId == sessionId }
                        .maxByOrNull { it.timestamp }

                    if (lastVisit != null) {
                        val duration = now - lastVisit.timestamp
                        visitDao.update(lastVisit.copy(duration = duration))

                        interactionDao.insert(
                            UserInteractionTimeEntity(
                                poiId = matchedPoi.poiId,
                                userLogId = null,
                                duration = duration,
                                timestamp = now
                            )
                        )

                        deviationDao.insert(
                            PathDeviationAlertEntity(
                                pathDeviationAlertId = 0,
                                userId = userId,
                                userLogId = null,
                                deviationLocation = matchedPoi.name,
                                timeStamp = readableNow,
                                noticeSent = false
                            )
                        )

                        launch(Dispatchers.Main) {
                            val readableDuration = String.format(Locale.getDefault(), "%.1f min", duration / 60000.0)
                            Toast.makeText(
                                context,
                                "Exited ${matchedPoi.name} (Stayed $readableDuration)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                }
            }
        }
    }

    private suspend fun getActiveSessionId(db: MyAppDatabase): Long {
        return withContext(Dispatchers.IO) {
            db.sessionDao().getAll().lastOrNull()?.sessionId ?: 0L
        }
    }
}