package com.thsst2.greenapp.algorithms

import android.content.Context
import com.thsst2.greenapp.MyAppDatabase
import com.thsst2.greenapp.RAGEngine
import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph

class TourPathPlanner(
    private val distanceCalculator: DistanceCalculator = AndroidDistanceCalculator()
) {
class TourPathPlanner(private val userId: Long, private val context: Context) {
    private val db = MyAppDatabase.getInstance(context)
    private val RAGEngine = RAGEngine()

    // TODO: Update parameter information
    /**
     * Plan a tour using pre-computed knowledge graph from Firebase.
     * 
     * The knowledge graph contains weighted edges calculated server-side.
     * Algorithms use these weights directly for pathfinding - no filtering applied here.
     * All filtering (preferences, dislikes, interests) should be handled in the knowledge graph itself.
     * 
     * @param knowledgeGraph Pre-computed weighted graph from Firebase with filtered POIs and edges
     * @param currentLatitude User's current GPS latitude
     * @param currentLongitude User's current GPS longitude
     * @param relevantPOIs List of POIs available in the knowledge graph
     * @param preferences List of POIs user wants to visit (already filtered by knowledge graph)
     * @param dislikedPoiIds Set of POI IDs that user has skipped or disliked
     * @param isRandom If true, uses RandomBFS for exploration; otherwise uses MultiGoalDijkstra for optimization
     */
    suspend fun planTour(
        knowledgeGraph: PoiGraph,
        currentLatitude: Double,
        currentLongitude: Double,
        relevantPOIs: List<PoiEntity>,
        preferences: List<PoiEntity>,
        dislikedPoiIds: Set<String> = emptySet(),
        isRandom: Boolean
    ): List<PoiEntity> {
        // Find the closest POI to current location to use as starting point
        var closestPOI: PoiEntity? = null
        var minDistance = Float.MAX_VALUE
        
        for (poi in relevantPOIs) {
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
        
        return when {
            isRandom -> {
                // Random exploration using BFS
                RandomBFS().findPath(knowledgeGraph, startPoint, dislikedPoiIds)
            }
            else -> {
                // Optimized tour through preferences using multi-goal optimization
                MultiGoalDijkstra().findPath(knowledgeGraph, preferences, startPoint, dislikedPoiIds)
            }
        }
        // TODO: SAMPLE LOGIC ONLY. Iterate relevant pois, get latitude and longitude. Use AndroidSmallHomeActivity Location.distanceBetween
        //      as basis to compare distances between current to the closest relevant poi. Use this as starting point, then iterate over each
        //      adjacent node from the starting node, compute distance between to choose where to go to(This will be the weight). Use isRandom
        //      to determing whether or not random bfs or dijkstra. Also, come back to me on why we are using only top 3 preferences?

        val userRoleEntity = db.userRoleDao().getUserRoleById(userId)
        val userRole = userRoleEntity?.role
        val userRoleData = RAGEngine.getUserRoleData(userRole.toString())

        return TODO("Provide the return value")
//        return when {
////            // No preferences -> random BFS exploration over knowledge graph
//////            preferences == null || preferences.isEmpty() -> {
//////                RandomBFS().findPath(knowledgeGraph, startPoint)
//////            }
//////
//////            // Preferences provided, no strict order -> multi-goal optimization
//////            !ordered -> {
//////                MultiGoalDijkstra().findPath(knowledgeGraph, preferences, startPoint)
//////            }
//////
//////            // Ordered preferences -> chained Dijkstra
//////            else -> {
//////                ChainedDijkstra().findPath(knowledgeGraph, preferences, startPoint, strictOrder)
//////            }
//        }
    }
}