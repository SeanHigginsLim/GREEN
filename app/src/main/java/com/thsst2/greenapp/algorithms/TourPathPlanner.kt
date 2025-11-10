package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.GraphBuilder

class TourPathPlanner {

    /**
     * Plan a tour based on user preferences and constraints using graph infrastructure
     * 
     * @param startPoint Optional starting location (e.g., user's current GPS location or preferred start POI)
     */
    fun planTour(
        allPois: List<PoiEntity>,
        startPoint: PoiEntity? = null,
        preferences: List<PoiEntity>? = null,
        ordered: Boolean = false,
        dislikedPoiIds: Set<String> = emptySet(),
        disinterests: Collection<String> = emptyList(),
        maxDistance: Double = Double.MAX_VALUE
    ): List<PoiEntity> {
        
        if (allPois.isEmpty()) return emptyList()
        
        // Build the graph from all POIs
        val graph = GraphBuilder().buildGraph(allPois)
        
        return when {
            // No preferences -> random BFS over filtered graph
            preferences == null || preferences.isEmpty() -> {
                RandomBFS().findPath(graph, dislikedPoiIds, disinterests, startPoint)
            }

            // Preferences provided, no strict order -> multi-goal Dijkstra
            !ordered -> {
                MultiGoalDijkstra().findPath(graph, preferences, dislikedPoiIds, disinterests, startPoint)
            }

            // Ordered preferences -> chained Dijkstra
            else -> {
                ChainedDijkstra().findPath(graph, preferences, dislikedPoiIds, disinterests, startPoint)
            }
        }
    }
}