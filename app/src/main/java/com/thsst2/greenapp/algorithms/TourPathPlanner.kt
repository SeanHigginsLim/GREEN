package com.thsst2.greenapp.algorithms

import android.content.Context
import android.util.Log
import com.thsst2.greenapp.MyAppDatabase
import com.thsst2.greenapp.RAGEngine
import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import kotlinx.coroutines.runBlocking

class TourPathPlanner(
    private val distanceCalculator: DistanceCalculator = AndroidDistanceCalculator(),
    private val userId: Long? = null,
    private val db: MyAppDatabase? = null,
    private val ragEngine: RAGEngine? = null
) {
    constructor(userId: Long, context: Context) : this(
        distanceCalculator = AndroidDistanceCalculator(),
        userId = userId,
        db = MyAppDatabase.getInstance(context),
        ragEngine = RAGEngine()
    )

    /**
     * Plan a tour using pre-computed knowledge graph from Firebase.
     *
     * @param knowledgeGraph Weighted graph built from available POIs and edges.
     * @param currentLatitude User's current GPS latitude.
     * @param currentLongitude User's current GPS longitude.
     * @param relevantPOIs POIs available to this planning run.
     * @param preferences POIs to prioritize for optimized routing.
     * @param dislikedPoiIds POI IDs excluded from the route.
     * @param isRandom If true, uses RandomBFS; otherwise uses MultiGoalDijkstra.
     */
    fun planTour(
        knowledgeGraph: PoiGraph,
        currentLatitude: Double,
        currentLongitude: Double,
        relevantPOIs: List<PoiEntity>,
        preferences: List<PoiEntity>,
        dislikedPoiIds: Set<String> = emptySet(),
        isRandom: Boolean
    ): List<PoiEntity> {
        Log.d("TourPathPlanner", "isRandom: $isRandom")
        val effectiveRelevantPOIs = if (isRandom) {
            Log.d("TourPathPlanner", "Entered isRandom")
            resolveRoleFilteredPOIs(relevantPOIs)
        } else {
            relevantPOIs
        }

        if (effectiveRelevantPOIs.isEmpty()) return emptyList()

        val allowedIds = effectiveRelevantPOIs.map { it.poiId }.toSet()
        Log.d("TourPathPlanner", "allowedIds: $allowedIds")
        val effectiveGraph = if (isRandom) {
            PoiGraph(
                nodes = knowledgeGraph.nodes.filterKeys { it in allowedIds },
                adjacencyList = knowledgeGraph.adjacencyList
                    .filterKeys { it in allowedIds }
                    .mapValues { (_, edges) -> edges.filter { it.to in allowedIds } }
            )
        } else {
            knowledgeGraph
        }
        Log.d("TourPathPlanner", "effectiveGraph: $effectiveGraph")

        val effectivePreferences = preferences.filter { it.poiId in allowedIds }
        Log.d("TourPathPlanner", "effectivePreferences: $effectivePreferences")

        // Find closest available POI as start point.
        var closestPOI: PoiEntity? = null
        var minDistance = Float.MAX_VALUE

        for (poi in effectiveRelevantPOIs) {
            val distance = distanceCalculator.calculateDistance(
                currentLatitude,
                currentLongitude,
                poi.latitude,
                poi.longitude
            )
            if (distance < minDistance) {
                minDistance = distance
                closestPOI = poi
            }
        }

        val startPoint = closestPOI
        Log.d("TourPathPlanner", "startPoint: $startPoint")

        return if (isRandom) {
            RandomBFS().findPath(effectiveGraph, startPoint, dislikedPoiIds)
        } else {
            MultiGoalDijkstra().findPath(effectiveGraph, effectivePreferences, startPoint, dislikedPoiIds)
        }
    }

    private fun resolveRoleFilteredPOIs(relevantPOIs: List<PoiEntity>): List<PoiEntity> {
        if (userId == null || db == null || ragEngine == null) return relevantPOIs

        val roleFiltered = runBlocking {
            val userRoleEntity = db.userRoleDao().getUserRoleById(userId)
            val userRole = userRoleEntity?.role
            val userRoleData = if (!userRole.isNullOrBlank()) {
                ragEngine.getUserRoleData(userRole)
            } else {
                emptyList()
            }
            Log.d("TourPathPlanner", "userRole: $userRole")
            Log.d("TourPathPlanner", "userRoleData: $userRoleData")

            when {
                userRoleData.isNotEmpty() -> {
                    val rolePoiIds = userRoleData.map { it.poiId }.toSet()
                    relevantPOIs.filter { it.poiId in rolePoiIds }
                }
                !userRole.isNullOrBlank() -> {
                    relevantPOIs.filter { poi ->
                        poi.category.any { tag -> tag.equals(userRole, ignoreCase = true) }
                    }
                }
                else -> relevantPOIs
            }
        }

        Log.d("TourPathPlanner", "roleFiltered: $roleFiltered")
        return if (roleFiltered.isNotEmpty()) roleFiltered else relevantPOIs
    }
}
