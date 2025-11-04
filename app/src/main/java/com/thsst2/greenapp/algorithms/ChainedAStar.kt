package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import com.thsst2.greenapp.graph.FilteredAdjacencyList

class ChainedAStar {
    /**
     * Find path through ordered POIs in the exact sequence provided.
     * Returns the path visiting each POI in the given sequence.
     */
    fun findPath(
        graph: PoiGraph,
        preferences: List<PoiEntity>,
        dislikedPoiIds: Set<Long> = emptySet(),
        disinterests: Collection<String> = emptyList()
    ): List<PoiEntity> {
        
        // Apply filters to get allowed POIs
        val filteredGraph = FilteredAdjacencyList(graph)
        filteredGraph.applyFilters(dislikedPoiIds, disinterests)
        val allowedPoiIds = filteredGraph.getAllowedPois().map { it.poiId }.toSet()
        
        // Filter preferences to only allowed ones and return in order
        return preferences.filter { it.poiId in allowedPoiIds }
    }
}