package com.thsst2.greenapp.graph

import com.thsst2.greenapp.data.PoiEntity

/**
 * Provides a filtered view of the POI graph based on user preferences and constraints
 */
class FilteredAdjacencyList(private val baseGraph: PoiGraph) {
    
    private var filteredAdjacency: Map<Int, List<Edge>> = baseGraph.adjacencyList
    private var allowedNodes: Set<Int> = baseGraph.nodes.keys
    
    /**
     * Apply filters to the graph based on user preferences
     */
    fun applyFilters(
        dislikedPoiIds: Set<Int> = emptySet(),
        disinterests: Collection<String> = emptyList(),
        maxDistance: Double = Double.MAX_VALUE
    ) {
        val disSet = disinterests
            .map { it.lowercase().trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        // First, determine which nodes are allowed
        allowedNodes = baseGraph.nodes.filter { (poiId, poi) ->
            isPoiAllowed(poi, dislikedPoiIds, disSet)
        }.keys.toSet()

        // Then filter adjacency list to only include edges to allowed nodes
        filteredAdjacency = baseGraph.adjacencyList.mapValues { (fromId, edges) ->
            if (fromId in allowedNodes) {
                edges.filter { edge ->
                    edge.to in allowedNodes &&
                    edge.distance <= maxDistance
                }
            } else {
                emptyList()
            }
        }.filterKeys { it in allowedNodes }
    }
    
    /**
     * Get filtered neighbors for a specific POI
     */
    fun getNeighbors(poiId: Int): List<Edge> {
        return filteredAdjacency[poiId] ?: emptyList()
    }
    
    /**
     * Get all allowed POIs after filtering
     */
    fun getAllowedPois(): List<PoiEntity> {
        return baseGraph.nodes.values.filter { poi ->
            poi.poiId in allowedNodes
        }
    }
    
    /**
     * Get the filtered graph
     */
    fun getFilteredGraph(): PoiGraph {
        val filteredNodes = baseGraph.nodes.filterKeys { it in allowedNodes }
        return PoiGraph(filteredNodes, filteredAdjacency)
    }
    
    /**
     * Check if a POI is allowed based on user preferences
     */
    private fun isPoiAllowed(
        poi: PoiEntity, 
        dislikedPoiIds: Set<Int>, 
        disinterests: Set<String>
    ): Boolean {
        // Check if POI is explicitly disliked
        if (poi.poiId in dislikedPoiIds) return false
        
        // Check if POI category matches user disinterests
        if (disinterests.isNotEmpty()) {
            val poiCats = poi.category.map { it.lowercase().trim() }
            if (poiCats.any { it in disinterests }) return false
        }
        
        return true
    }
    
    /**
     * Find the nearest allowed POI to a given POI
     */
    fun findNearestPoi(fromPoiId: Int): PoiEntity? {
        val neighbors = getNeighbors(fromPoiId)
        return if (neighbors.isNotEmpty()) {
            val nearestEdge = neighbors.minByOrNull { it.distance }
            baseGraph.getNode(nearestEdge?.to ?: return null)
        } else null
    }
    
    /**
     * Check if there's a connection between two POIs in the filtered graph
     */
    fun hasConnection(fromPoiId: Int, toPoiId: Int): Boolean {
        return getNeighbors(fromPoiId).any { it.to == toPoiId }
    }
}