package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import com.thsst2.greenapp.graph.FilteredAdjacencyList
import java.util.*

class ChainedAStar {
    /**
     * Find path through ordered POIs in the exact sequence provided.
     * Returns the complete path including all intermediate POIs along the shortest routes.
     * Computes shortest paths between consecutive POIs to ensure optimal routing.
     * 
     * @param startPoint Optional starting location (e.g., user's current location or preferred start)
     */
    fun findPath(
        graph: PoiGraph,
        preferences: List<PoiEntity>,
        dislikedPoiIds: Set<Long> = emptySet(),
        disinterests: Collection<String> = emptyList(),
        startPoint: PoiEntity? = null
    ): List<PoiEntity> {
        
        // Apply filters to get allowed POIs
        val filteredGraph = FilteredAdjacencyList(graph)
        filteredGraph.applyFilters(dislikedPoiIds, disinterests)
        val allowedPoiIds = filteredGraph.getAllowedPois().map { it.poiId }.toSet()
        
        // Filter preferences to only allowed ones
        val orderedPrefs = preferences.filter { it.poiId in allowedPoiIds }
        
        if (orderedPrefs.isEmpty()) return emptyList()
        
        // Build complete path starting from startPoint (if provided) or first preference
        val completePath = mutableListOf<PoiEntity>()
        
        // If start point provided and different from first preference, route to first preference
        if (startPoint != null && (orderedPrefs.isEmpty() || startPoint.poiId != orderedPrefs[0].poiId)) {
            val segmentToFirst = findShortestPath(startPoint.poiId, orderedPrefs[0].poiId, filteredGraph, graph)
            if (segmentToFirst.isNotEmpty()) {
                completePath.addAll(segmentToFirst)
            } else {
                // Can't reach first preference from start point
                completePath.add(startPoint)
                completePath.add(orderedPrefs[0])
            }
        } else {
            // Start directly from first preference
            completePath.add(orderedPrefs[0])
        }
        
        if (orderedPrefs.size == 1) return completePath
        
        // Chain shortest paths between consecutive preferences
        for (i in 0 until orderedPrefs.size - 1) {
            val current = orderedPrefs[i]
            val next = orderedPrefs[i + 1]
            
            // Find shortest path between consecutive POIs
            val segment = findShortestPath(current.poiId, next.poiId, filteredGraph, graph)
            
            if (segment.isNotEmpty()) {
                // Add segment excluding first POI (already in path)
                completePath.addAll(segment.drop(1))
            } else {
                // No path exists - skip this destination or handle as needed
                // For now, we skip unreachable POIs
            }
        }
        
        return completePath
    }
    
    /**
     * Find shortest path between two POIs using Dijkstra's algorithm
     * Returns the complete path including intermediate POIs
     */
    private fun findShortestPath(
        fromId: Long, 
        toId: Long, 
        filteredGraph: FilteredAdjacencyList,
        graph: PoiGraph
    ): List<PoiEntity> {
        if (fromId == toId) return listOf(graph.getNode(fromId) ?: return emptyList())
        
        // Dijkstra's algorithm
        val distances = mutableMapOf(fromId to 0.0)
        val previous = mutableMapOf<Long, Long>()
        val queue = PriorityQueue<Pair<Double, Long>>(compareBy { it.first })
        queue.add(0.0 to fromId)
        val visited = mutableSetOf<Long>()
        
        while (queue.isNotEmpty()) {
            val (dist, nodeId) = queue.poll() ?: continue
            
            if (nodeId in visited) continue
            visited.add(nodeId)
            
            // Found destination
            if (nodeId == toId) {
                return reconstructPath(fromId, toId, previous, graph)
            }
            
            // Explore neighbors
            filteredGraph.getNeighbors(nodeId).forEach { edge ->
                if (edge.to !in visited) {
                    val newDist = dist + edge.weight
                    if (newDist < (distances[edge.to] ?: Double.MAX_VALUE)) {
                        distances[edge.to] = newDist
                        previous[edge.to] = nodeId
                        queue.add(newDist to edge.to)
                    }
                }
            }
        }
        
        // No path found
        return emptyList()
    }
    
    /**
     * Reconstruct path from Dijkstra's previous map
     */
    private fun reconstructPath(
        fromId: Long,
        toId: Long,
        previous: Map<Long, Long>,
        graph: PoiGraph
    ): List<PoiEntity> {
        val path = mutableListOf<Long>()
        var current = toId
        
        while (current != fromId) {
            path.add(current)
            current = previous[current] ?: return emptyList()
        }
        path.add(fromId)
        path.reverse()
        
        // Convert to PoiEntity list
        return path.mapNotNull { graph.getNode(it) }
    }
}