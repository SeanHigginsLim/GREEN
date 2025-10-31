package com.thsst2.greenapp

import android.util.Log

class FirebaseSync {
    fun syncEntity(collection: String, entity: Any) {
        Log.d("FirebaseSync", "Simulated upload to Firebase [$collection]: $entity")
    }
}