package com.thsst2.greenapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Date
import java.util.Locale
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.FirebaseAuth
import com.thsst2.greenapp.data.GeofenceTriggerEntity
import com.thsst2.greenapp.data.PathDeviationAlertEntity
import com.thsst2.greenapp.data.PoiEntity
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

    companion object {
        @Volatile
        var currentPoiInside: PoiEntity? = null
    }

    override fun onReceive(context: Context, intent: Intent) {

        Log.d("GeofenceReceiver", "onReceive triggered with action = ${intent.action}")
        Log.d("GeofenceReceiver", "Intent extras keys = ${intent.extras?.keySet()?.joinToString()}")

        intent.extras?.keySet()?.forEach { key ->
            Log.d("GeofenceReceiver", "extra[$key] = ${intent.extras?.get(key)}")
        }

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            Log.e("GeofenceReceiver", "GeofencingEvent.fromIntent returned null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "GeofencingEvent has error code = ${geofencingEvent.errorCode}")
            return
        }

        Log.d("GeofenceReceiver", "Valid GeofencingEvent received")

        val transition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        Log.d("GeofenceReceiver", "Transition code = $transition")
        Log.d("GeofenceReceiver", "Triggered geofence count = ${triggeringGeofences?.size ?: 0}")

        triggeringGeofences?.forEach {
            Log.d("GeofenceReceiver", "Triggered requestId = ${it.requestId}")
        }

        val triggeredName = triggeringGeofences?.firstOrNull()?.requestId ?: "Unknown"
        Log.d("GeofenceReceiver", "First triggered requestId = $triggeredName")

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid?.hashCode()?.toLong() ?: -1L

        GlobalScope.launch(Dispatchers.IO) {

            val db = MyAppDatabase.getInstance(context)

            val geoDao = db.geofenceTriggerDao()
            val visitDao = db.userVisitedLocationDao()
            val interactionDao = db.userInteractionTimeDao()
            val deviationDao = db.pathDeviationAlertDao()
            val userLocationDao = db.userLocationDao()

            val now = System.currentTimeMillis()
            val readableNow = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date(now))

            val sessionId = getActiveSessionId(db)
            Log.d("GeofenceReceiver", "Resolved sessionId = $sessionId")

            val hasValidSession = sessionId != 0L
            if (!hasValidSession) {
                Log.e(
                    "GeofenceReceiver",
                    "No valid session found. Will skip DB inserts but continue UI updates."
                )
            }

            val allPois = try {
                RAGEngine().getBuildings()
            } catch (e: Exception) {
                Log.e("GeofenceReceiver", "Error fetching POIs from RAGEngine: ${e.localizedMessage}", e)
                emptyList()
            }

            Log.d("GeofenceReceiver", "RAGEngine POI count = ${allPois.size}")
            allPois.forEach {
                Log.d("GeofenceReceiver", "RAG POI -> id=${it.poiId}, name=${it.name}")
            }

            val matchedPoi = allPois.find { it.name == triggeredName }
            Log.d("GeofenceReceiver", "matchedPoi from RAGEngine = $matchedPoi")

            if (matchedPoi == null) {
                Log.w("GeofenceReceiver", "No RAGEngine POI found for requestId/name = $triggeredName")
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
                    Log.d("GeofenceReceiver", "ENTER detected for ${matchedPoi.name}")
                    if (hasValidSession) {
                        geoDao.insert(
                            GeofenceTriggerEntity(
                                userId = userId,
                                poiId = matchedPoi.poiId,
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
                                accuracyRadius = matchedPoi.radius.toFloat()
                            )
                        )
                    }

                    launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Entered ${matchedPoi.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    currentPoiInside = matchedPoi
                    MapState.currentPoiInside = matchedPoi

                    Log.d("GeofenceReceiver", "Sending BUILDING_ENTERED broadcast")

                    val infoIntent = Intent("BUILDING_ENTERED")
                    infoIntent.putExtra("buildingName", matchedPoi.name)
                    infoIntent.putExtra("poiId", matchedPoi.poiId)

                    LocalBroadcastManager
                        .getInstance(context)
                        .sendBroadcast(infoIntent)
                }

                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    Log.d("GeofenceReceiver", "EXIT detected for ${matchedPoi.name}")
                    if (hasValidSession) {
                        geoDao.insert(
                            GeofenceTriggerEntity(
                                userId = userId,
                                poiId = matchedPoi.poiId,
                                entryTime = "",
                                exitTime = readableNow,
                                triggerType = "EXIT"
                            )
                        )

                        val lastVisit = visitDao.getAll()
                            .filter { it.poiId == matchedPoi.poiId && it.sessionId == sessionId}
                            .maxByOrNull { it.timestamp }

                        if (lastVisit != null) {
                            val duration = now - lastVisit.timestamp
                            Log.d(
                                "GeofenceReceiver",
                                "Visit duration for ${matchedPoi.name} = $duration ms"
                            )
                            visitDao.update(
                                lastVisit.copy(duration = duration)
                            )

                            interactionDao.insert(
                                UserInteractionTimeEntity(
                                    poiId = matchedPoi.poiId,
                                    userId = userId,
                                    duration = duration,
                                    timestamp = now
                                )
                            )

                            deviationDao.insert(
                                PathDeviationAlertEntity(
                                    pathDeviationAlertId = 0,
                                    userId = userId,
                                    deviationLocation = matchedPoi.name,
                                    timeStamp = readableNow,
                                    noticeSent = false
                                )
                            )
                        }
                    }

                    if (currentPoiInside?.poiId == matchedPoi.poiId) {
                        currentPoiInside = null
                    }

                    if (MapState.currentPoiInside?.poiId == matchedPoi.poiId) {
                        MapState.currentPoiInside = null
                    }

                    launch(Dispatchers.Main) {
                        val readableDuration =
                            if (hasValidSession) "unknown duration"
                            else "session unavailable"

                        Toast.makeText(
                            context,
                            "Exited ${matchedPoi.name} ($readableDuration)",
                            Toast.LENGTH_SHORT
                        ).show()
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