package com.thsst2.greenapp

import com.thsst2.greenapp.data.PoiEntity

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

class RAGEngine {
    private val db = FirebaseFirestore.getInstance()
    private val gson = Gson()
    suspend fun getRelevantPOINames(preferences: List<String>): List<PoiEntity> {
        // TODO: replace with actual collection name
        val poiCollection = db.collection("pois").get().await()

        // Filter and map documents to PoiEntity objects based on preference tags
        val poiList = poiCollection.documents.mapNotNull { doc ->
            try {
                val categories = (doc.get("category") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                // Only include POIs that match any of the preferences
                if (preferences.any { pref -> categories.contains(pref) }) {
                    PoiEntity(
                        poiId = doc.getLong("poiId") ?: 0, // map
                        generatedPathId = null,
                        name = doc.getString("name") ?: "", // map
                        description = doc.getString("description"), // map
                        category = categories, // map
                        latitude = doc.getDouble("latitude") ?: 0.0, // map
                        longitude = doc.getDouble("longitude") ?: 0.0 // map
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }.distinctBy { it.poiId } // remove duplicates by POI ID

        return poiList
    }

    // Returns JSON string of all relevant data about the POIs in the given list
    suspend fun getData(poiIds: List<Long>): String {
        val dataMap = mutableMapOf<Long, Map<String, Any?>>()

        poiIds.forEach { id ->
            val doc = db.collection("poi_data").document(id.toString()).get().await()
            if (doc.exists()) {
                dataMap[id] = doc.data ?: emptyMap()
            }
        }

        return gson.toJson(dataMap) // JSON map: poiId -> all relevant info
    }
}
