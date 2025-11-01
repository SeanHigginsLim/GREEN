package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.GraphBuilder
import com.thsst2.greenapp.graph.FilteredAdjacencyList

class TourPathPlanner {

    /**
     * Plan a tour based on user preferences and constraints using graph infrastructure
     */
    fun planTour(
        allPois: List<PoiEntity>,
        startPoint: PoiEntity? = null,
        preferences: List<PoiEntity>? = null,
        ordered: Boolean = false,
        dislikedPoiIds: Set<Long> = emptySet(),
        disinterests: Collection<String> = emptyList(),
        maxDistance: Double = Double.MAX_VALUE
    ): List<PoiEntity> {
        
        if (allPois.isEmpty()) return emptyList()
        
        // Build the graph from all POIs
        val graph = GraphBuilder().buildGraph(allPois)
        
        return when {
            // No preferences -> random BFS over filtered graph
            preferences == null || preferences.isEmpty() -> {
                RandomBFS().findPath(graph, dislikedPoiIds, disinterests)
            }

            // Preferences provided, no strict order -> multi-label approach
            !ordered -> {
                MultiLabelAStar().findPath(graph, preferences, dislikedPoiIds, disinterests)
            }

            // Ordered preferences -> chained approach
            else -> {
                ChainedAStar().findPath(graph, preferences, dislikedPoiIds, disinterests)
            }
        }
    }
}