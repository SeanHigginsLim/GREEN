package com.thsst2.greenapp.graph

import com.thsst2.greenapp.data.PoiEntity

/**
 * Provides a filtered view of the POI graph based on user preferences and constraints
 */
class FilteredAdjacencyList(private val baseGraph: PoiGraph) {
    
    private var filteredAdjacency: Map<Long, List<Edge>> = baseGraph.adjacencyList
    private var allowedNodes: Set<Long> = baseGraph.nodes.keys
    
    /**
     * Apply filters to the graph based on user preferences
     */
    fun applyFilters(
        dislikedPoiIds: Set<Long> = emptySet(),
        disinterests: Collection<String> = emptyList(),
        maxDistance: Double = Double.MAX_VALUE
    ) {
        val disSet = disinterests
            .map { it.lowercase().trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        // First, determine which nodes are allowed
        allowedNodes = baseGraph.nodes.filterKeys { poiId ->
            val poi = baseGraph.nodes[poiId]!!
            isPoiAllowed(poi, dislikedPoiIds, disSet)
        }.keys

        // Then filter adjacency list to only include edges to allowed nodes
        filteredAdjacency = baseGraph.adjacencyList.filterKeys { fromId ->
            fromId in allowedNodes
        }.mapValues { (fromId, edges) ->
            edges.filter { edge ->
                edge.to in allowedNodes &&
                edge.distance <= maxDistance
            }
        }
    }
    
    /**
     * Get filtered neighbors for a specific POI
     */
    fun getNeighbors(poiId: Long): List<Edge> {
        return filteredAdjacency[poiId] ?: emptyList()
    }
    
    /**
     * Get all allowed POIs after filtering
     */
    fun getAllowedPois(): List<PoiEntity> {
        return baseGraph.nodes.filterKeys { it in allowedNodes }.values.toList()
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
        dislikedPoiIds: Set<Long>, 
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
    fun findNearestPoi(fromPoiId: Long): PoiEntity? {
        val neighbors = getNeighbors(fromPoiId)
        return if (neighbors.isNotEmpty()) {
            val nearestEdge = neighbors.minByOrNull { it.distance }
            baseGraph.getNode(nearestEdge?.to ?: return null)
        } else null
    }
    
    /**
     * Check if there's a connection between two POIs in the filtered graph
     */
    fun hasConnection(fromPoiId: Long, toPoiId: Long): Boolean {
        return getNeighbors(fromPoiId).any { it.to == toPoiId }
    }
}