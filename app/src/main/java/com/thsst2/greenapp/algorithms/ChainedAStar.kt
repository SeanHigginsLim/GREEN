package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import com.thsst2.greenapp.graph.FilteredAdjacencyList

class ChainedAStar {
    /**
     * Follow the given ordered preferences using graph structure, skipping any POIs that the user dislikes.
     * This method finds paths between consecutive POIs in the preference order.
     */
    fun findPath(
        graph: PoiGraph,
        preferences: List<PoiEntity>,
        dislikedPoiIds: Set<Int> = emptySet(),
        disinterests: Collection<String> = emptyList()
    ): List<PoiEntity> {
        
        // Apply filters to the graph
        val filteredGraph = FilteredAdjacencyList(graph)
        filteredGraph.applyFilters(dislikedPoiIds, disinterests)
        
        val allowedPrefs = preferences.filter { pref ->
            filteredGraph.getAllowedPois().any { it.poiId == pref.poiId }
        }
        
        if (allowedPrefs.isEmpty()) return emptyList()
        
        // For now, return the filtered preferences in order
        // TODO: Implement actual pathfinding between consecutive preferences
        return allowedPrefs
    }
}