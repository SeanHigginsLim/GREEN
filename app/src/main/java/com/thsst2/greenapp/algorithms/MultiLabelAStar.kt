package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import com.thsst2.greenapp.graph.FilteredAdjacencyList

class MultiLabelAStar {

    /**
     * Multi-label planner using graph structure that:
     *  - keeps all preference POIs (in the order provided) that are not disliked
     *  - then appends remaining allowed POIs from the graph
     */
    fun findPath(
        graph: PoiGraph,
        preferences: List<PoiEntity>,
        dislikedPoiIds: Set<Long> = emptySet(),
        disinterests: Collection<String> = emptyList()
    ): List<PoiEntity> {
        
        // Apply filters to the graph
        val filteredGraph = FilteredAdjacencyList(graph)
        filteredGraph.applyFilters(dislikedPoiIds, disinterests)
        
        val allowedPois = filteredGraph.getAllowedPois()
        
        fun allowed(p: PoiEntity): Boolean {
            return allowedPois.any { it.poiId == p.poiId }
        }

        // keep preferences order but drop disliked ones
        val keptPrefs = preferences.filter { allowed(it) }

        // append remaining allowed POIs (not in keptPrefs) from graph
        val rest = allowedPois.filterNot { poi -> keptPrefs.any { it.poiId == poi.poiId } }

        return keptPrefs + rest
    }
}