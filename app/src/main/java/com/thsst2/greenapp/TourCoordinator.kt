package com.thsst2.greenapp.logic

import android.content.Context
import android.util.Log
import com.thsst2.greenapp.MyAppDatabase
import com.thsst2.greenapp.RAGEngine
import com.thsst2.greenapp.algorithms.TourPathPlanner
import com.thsst2.greenapp.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TourCoordinator(private val context: Context) {

    private val db = MyAppDatabase.getInstance(context)
    private val ragEngine = RAGEngine()
    private val tourPathPlanner = TourPathPlanner()

    suspend fun startTourForUser(userId: Long): GeneratedPathEntity? = withContext(Dispatchers.IO) {
        try {
            val user = db.userDao().getUserById(userId)
            val prefs = db.userPreferencesDao().getPreferencesByUser(userId)

            if (user == null || prefs == null) {
                Log.e("TourCoordinator", "User or preferences not found for userId=$userId")
                return@withContext null
            }

            Log.d("TourCoordinator", "Starting tour with prefs: ${prefs.interests}")

            // Retrieve relevant POIs using RAGEngine
            val relevantPOIs = ragEngine.getRelevantPOIs(prefs)
            if (relevantPOIs.isEmpty()) {
                Log.w("TourCoordinator", "No POIs found for preferences: ${prefs.interests}")
                return@withContext null
            }

            // Identify user's disliked POIs/disinterests
            val dislikedIds = db.userSkippedOrDislikedLocationDao()
                .getAll()
                .map { it.poiId }
                .toSet()

            val disinterests = prefs.disinterests ?: emptyList()

            //Determine ordered flag
            val ordered = prefs.tourPace?.contains("ordered", ignoreCase = true) ?: false

            // Generate path using the algos
            val path = tourPathPlanner.planTour(
                allPois = relevantPOIs,
                preferences = relevantPOIs.take(3), // top few as “preferred”
                ordered = ordered,
                dislikedPoiIds = dislikedIds,
                disinterests = disinterests
            )

            if (path.isEmpty()) {
                Log.w("TourCoordinator", "Path generation returned empty.")
                return@withContext null
            }

            //Save generated path metadata
            val generatedPathEntity = GeneratedPathEntity(
                userId = userId,
                poiId = path.firstOrNull()?.poiId ?: 0L,//use first POI id or 0 (not sure)
                pathType = "Generated",
                estimatedDuration = "${path.size * 10} min",                   
                routeAlgorithm = when {
                    ordered -> "ChainedAStar"
                    prefs.interests.isNotEmpty() -> "MultiLabelAStar"
                    else -> "RandomBFS"
                }
            )

            val pathId = db.generatedPathDao().insert(generatedPathEntity)

            //Save each POI in path order
            path.forEachIndexed { index, poi ->
                val poiEntity = poi.copy(generatedPathId = pathId)
                db.poiDao().insert(poiEntity)

                val pathSequence = path.map { it.name }  //simple sequence of POI names

                db.userTourPathHistoryDao().insert(
                    UserTourPathHistoryEntity(
                        sessionId = System.currentTimeMillis(),
                        pathSequence = pathSequence,
                        algorithmUsed = generatedPathEntity.routeAlgorithm,
                        status = "Generated"
                    )
                )
            }

            Log.d("TourCoordinator", "Tour successfully generated with ${path.size} stops.")
            return@withContext generatedPathEntity

        } catch (e: Exception) {
            Log.e("TourCoordinator", "Error generating tour: ${e.localizedMessage}")
            return@withContext null
        }
    }
}

