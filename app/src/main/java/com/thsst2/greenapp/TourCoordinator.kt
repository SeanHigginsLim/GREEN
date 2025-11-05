package com.thsst2.greenapp

import android.content.Context
import android.util.Log
import com.thsst2.greenapp.algorithms.TourPathPlanner
import com.thsst2.greenapp.data.GeneratedPathEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TourCoordinator(private val context: Context) {

    private val db = MyAppDatabase.getInstance(context)
    private val DialogueManager = DialogueManager()
    private val RAGEngine = RAGEngine()
    private val tourPathPlanner = TourPathPlanner()
    private val FirebaseSync = FirebaseSync()

    suspend fun startTourForUser(userId: Long): GeneratedPathEntity? = withContext(Dispatchers.IO) {
        try {
            // Fetch User + Preferences
            val user = db.userDao().getUserById(userId)
            val prefs = db.userPreferencesDao().getPreferencesByUser(userId)

            if (user == null || prefs == null) {
                Log.e("TourCoordinator", "User or preferences not found for userId=$userId")
                return@withContext null
            }

            Log.d(
                "TourCoordinator",
                "Starting tour with prefs: ${prefs.interests}"
            )

            // Dialogue Manager interprets intent
            val intent = DialogueManager.processUserIntent("Generate me a tour", prefs)

            // Retrieve POIs based on preferences (RAG)
            val relevantPOIs = RAGEngine.getRelevantPOIs(prefs)

            // Generate tour path
            val generatedPath = tourPathPlanner.planTour(relevantPOIs)

            // Save Generated Path to Room
//            val generatedPathEntity = GeneratedPathEntity(
//                userId = userId,
//                pathType = generatedPath.pathType,
//                estimatedDuration = generatedPath.estimatedDuration,
//                routeAlgorithm = generatedPath.routeAlgorithm
//            )
//            db.generatedPathDao().insert(generatedPathEntity)

            // Save POIs to Room
//            for (poi in relevantPOIs) {
    //            val poiEntity = PoiEntity(
    //                generatedPathId = generatedPathEntity.generatedPathId,
    //                name = poi.name,
    //                description = poi.description,
    //                category = poi.category,
    //                latitude = poi.latitude,
    //                longitude = poi.longitude
    //            )
    //            db.poiDao().insert(poiEntity)
//            }

            // Simulate sync to Firebase
//            FirebaseSync.syncEntity("generated_paths", generatedPathEntity)
//            for (poi in relevantPOIs) {
//                FirebaseSync.syncEntity("poi_entity", poi)
//            }

//            Log.d("TourCoordinator", "Tour successfully generated: ${generatedPath.size} stops")
//            return@withContext generatedPathEntity
            return@withContext null
        } catch (e: Exception) {
            Log.e("TourCoordinator", "Error generating tour", e)
            return@withContext null
        }
    }
}