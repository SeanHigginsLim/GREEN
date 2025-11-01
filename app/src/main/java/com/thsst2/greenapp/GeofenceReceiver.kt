package com.thsst2.greenapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GeofenceReceiver", "onReceive triggered with intent: ${intent.action}")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e("GeofenceReceiver", "Null geofencingEvent")
            return
        }

        if (geofencingEvent.hasError()) {
            val errorMessage = geofencingEvent.errorCode
            Log.e("GeofenceReceiver", "Geofencing error: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences
        val placeNames = triggeringGeofences?.joinToString { it.requestId } ?: "Unknown"

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Toast.makeText(context, "🚶 Entered: $placeNames", Toast.LENGTH_LONG).show()
                Log.d("GeofenceReceiver", "Entered: $placeNames")
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Toast.makeText(context, "🏃 Exited: $placeNames", Toast.LENGTH_LONG).show()
                Log.d("GeofenceReceiver", "Exited: $placeNames")
            }
            else -> {
                Log.w("GeofenceReceiver", "Unknown transition: $geofenceTransition")
            }
        }
    }
}
