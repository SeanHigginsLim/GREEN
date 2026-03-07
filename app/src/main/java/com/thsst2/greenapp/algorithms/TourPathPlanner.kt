package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph

class TourPathPlanner(
    private val distanceCalculator: DistanceCalculator = AndroidDistanceCalculator()
) {
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
    fun planTour(
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
    }
}