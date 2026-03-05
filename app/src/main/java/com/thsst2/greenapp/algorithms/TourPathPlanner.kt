package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph

class TourPathPlanner {
    // TODO: Update parameter information
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
     * @param strictOrder If true (and ordered=true), visits ONLY specified POIs with no intermediates.
     *                    If false, allows intermediate POIs along shortest paths. Default is false.
     */
    fun planTour(
        knowledgeGraph: PoiGraph,
        currentLatitude: Double,
        currentLongitude: Double,
        relevantPOIs: List<PoiEntity>,
        preferences: List<PoiEntity>? = null,
        isRandom: Boolean
    ): List<PoiEntity> {
        // TODO: SAMPLE LOGIC ONLY. Iterate relevant pois, get latitude and longitude. Use AndroidSmallHomeActivity Location.distanceBetween
        //      as basis to compare distances between current to the closest relevant poi. Use this as starting point, then iterate over each
        //      adjacent node from the starting node, compute distance between to choose where to go to(This will be the weight). Use isRandom
        //      to determing whether or not random bfs or dijkstra. Also, come back to me on why we are using only top 3 preferences?

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