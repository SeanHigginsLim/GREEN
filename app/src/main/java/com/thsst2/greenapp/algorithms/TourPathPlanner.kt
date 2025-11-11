package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph

class TourPathPlanner {

    /**
     * Plan a tour using pre-computed knowledge graph from Firebase.
     * 
     * The knowledge graph contains weighted edges calculated server-side.
     * Algorithms use these weights directly for pathfinding - no filtering applied here.
     * All filtering (preferences, dislikes, interests) should be handled in the knowledge graph itself.
     * 
     * @param knowledgeGraph Pre-computed weighted graph from Firebase with filtered POIs and edges
     * @param startPoint Optional starting location (e.g., user's current GPS location or preferred start POI)
     * @param preferences Optional list of POIs user wants to visit (already filtered by knowledge graph)
     * @param ordered Whether preferences must be visited in the exact order provided
     */
    fun planTour(
        knowledgeGraph: PoiGraph,
        startPoint: PoiEntity? = null,
        preferences: List<PoiEntity>? = null,
        ordered: Boolean = false
    ): List<PoiEntity> {
        
        return when {
            // No preferences -> random BFS exploration over knowledge graph
            preferences == null || preferences.isEmpty() -> {
                RandomBFS().findPath(knowledgeGraph, startPoint)
            }

            // Preferences provided, no strict order -> multi-goal optimization
            !ordered -> {
                MultiGoalDijkstra().findPath(knowledgeGraph, preferences, startPoint)
            }

            // Ordered preferences -> chained Dijkstra
            else -> {
                ChainedDijkstra().findPath(knowledgeGraph, preferences, startPoint)
            }
        }
    }
}