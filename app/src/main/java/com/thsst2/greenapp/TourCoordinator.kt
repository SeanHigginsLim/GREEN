package com.thsst2.greenapp

import android.content.Context
import android.util.Log
import com.thsst2.greenapp.algorithms.TourPathPlanner
import com.thsst2.greenapp.data.*
import com.thsst2.greenapp.graph.PoiGraph
import com.thsst2.greenapp.graph.Edge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.String

class TourCoordinator(private val context: Context) {

    private val db = MyAppDatabase.getInstance(context)
//    private val DialogueManager = DialogueManager(context)
    private val RAGEngine = RAGEngine()
    private val tourPathPlanner = TourPathPlanner()
    private val FirebaseSync = FirebaseSync()
    private val tempPreferences = mutableListOf<String>()

    suspend fun startTourForUser(userId: Long, preferences: List<String>?): UserTourPathHistoryEntity? = withContext(Dispatchers.IO) {
        Log.d("TourCoordinator", "In Tour Coordinator")
        try {
            // Fetch User + Preferences
            val user = db.userDao().getUserById(userId)
            val prefs = db.userPreferencesDao().getPreferencesByUser(userId)
            val databaseMappedPreferences = RAGEngine.mapPreferencesToTagNames(preferences)
            if (prefs == null) {
                Log.e("TourCoordinator", "User or preferences not found for userId=$userId")
                return@withContext null
            }

            Log.d("TourCoordinator", "Starting tour with prefs: ${prefs.interests}")
            Log.d("TourCoordinator", "Mapped Preferences ${databaseMappedPreferences}")

            // Retrieve relevant POIs using RAGEngine
            val relevantPOIs = RAGEngine.getRelevantPOINames(databaseMappedPreferences)
            if (relevantPOIs.isEmpty()) {
                Log.w("TourCoordinator", "No POIs found for preferences: ${prefs.interests}")
                return@withContext null
            }

            val knowledgeGraph = RAGEngine.getKnowledgeGraph()

            // Identify user's disliked POIs/disinterests
            val dislikedIds = db.userSkippedOrDislikedLocationDao()
                .getAll()
                .map { it.poiId }
                .toSet()

            val disinterests = prefs.disinterests ?: emptyList()

            //Determine ordered flag
            val ordered = prefs.tourPace?.contains("ordered", ignoreCase = true) ?: false

            // Convert Firebase knowledge graph format to PoiGraph
            val knowledgeGraphPoi = PoiGraph(
                nodes = relevantPOIs.associateBy { it.poiId },
                adjacencyList = knowledgeGraph.mapValues { (_, edges) ->
                    edges.map { Edge(edgeId = it.edgeId, to = it.to, weight = it.weight) }
                }
            )

            // Generate path using the algos
            val path = tourPathPlanner.planTour(
                knowledgeGraph = knowledgeGraphPoi,
                startPoint = null,
                preferences = relevantPOIs.take(3), // top few as "preferred"
                ordered = ordered
            )
            Log.d("TourCoordinator", "Path: $path")

            if (path.isEmpty()) {
                Log.w("TourCoordinator", "Path generation returned empty.")
                return@withContext null
            }

            var generatedPathEntity: GeneratedPathEntity? = null
            var poiEntity: PoiEntity? = null
            var pathId: Long = 0

            Log.d("TourCoordinator", "Saving Generated Path")

            generatedPathEntity = GeneratedPathEntity(
                userId = userId,
                pathType = "Generated",
                estimatedDuration = "${path.size * 10} min",
                routeAlgorithm = when {
                    ordered -> "ChainedAStar"
                    prefs.interests.isNotEmpty() -> "MultiLabelAStar"
                    else -> "RandomBFS"
                }
            )
            pathId = db.generatedPathDao().insert(generatedPathEntity)

            // Save POIs to Room
            for (poi in relevantPOIs) {
                Log.d("TourCoordinator", "Saving POI")

                poiEntity = PoiEntity(
                    poiId = poi.poiId,
                    generatedPathId = pathId,
                    name = poi.name,
                    description = poi.description,
                    category = poi.category,
                    latitude = poi.latitude,
                    longitude = poi.longitude
                )
                db.poiDao().insert(poiEntity)
            }

            val poiData = RAGEngine.getData(db.poiDao().getAll().map { it.poiId }, databaseMappedPreferences)
            Log.d("TourCoordinator", "POI Data: $poiData")
            // Simulate sync to Firebase
//            FirebaseSync.syncEntity("generated_paths", generatedPathEntity)
//            for (poi in relevantPOIs) {
//                FirebaseSync.syncEntity("poi_entity", poi)
//            }

            var userTourPathHistory: UserTourPathHistoryEntity? = null
            var pathSequence = emptyList<String>()

            //Save each POI in path order
            path.forEachIndexed { index, poi ->
                pathSequence = path.map { it.name }  //simple sequence of POI names

                Log.d("TourCoordinator", "Saving Local Data Entity")
                val localDataEntity = LocalDataEntity(
                    userId = userId,
                    tourName = null,
                    orderedPoisJson = pathSequence,
                    poiInfoJson = poiData
                )
                db.localDataDao().insert(localDataEntity)
            }

            Log.d("TourCoordinator", "Saving User Tour Path History")
            userTourPathHistory = UserTourPathHistoryEntity(
                sessionId = db.sessionDao().getSessionsByUser(userId).firstOrNull()?.sessionId ?: 0,
                pathSequence = pathSequence,
                algorithmUsed = db.generatedPathDao().getGeneratedPathsByUser(userId).firstOrNull()?.routeAlgorithm ?: "Unknown",
                status = "Generated"
            )
            db.userTourPathHistoryDao().insert(userTourPathHistory)

            Log.d("TourCoordinator", "Tour successfully generated with ${path.size} stops.")
            return@withContext userTourPathHistory
        } catch (e: Exception) {
            Log.e("TourCoordinator", "Error generating tour", e)
            return@withContext null
        }
    }
}