package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import java.util.*

class ChainedDijkstra {
    /**
     * Find path through ordered POIs in the exact sequence provided.
     * Returns the complete path including all intermediate POIs along the shortest routes.
     * Uses knowledge graph edges from Firebase with pre-calculated weights.
     * 
     * @param graph Knowledge graph with weighted edges from Firebase
     * @param preferences Ordered list of POIs to visit in sequence
     * @param startPoint Optional starting location (e.g., user's current location or preferred start)
     * @param strictOrder If true, visits ONLY the specified POIs in order (A -> B -> C).
     *                    If false, allows intermediate POIs along shortest paths (A -> X -> B -> Y -> C).
     *                    Default is false (allows intermediate POIs for shortest route).
     */
    fun findPath(
        graph: PoiGraph,
        preferences: List<PoiEntity>,
        startPoint: PoiEntity? = null,
        strictOrder: Boolean = false
    ): List<PoiEntity> {
        
        if (preferences.isEmpty()) return emptyList()
        
        // Strict order mode: only return the specified POIs in exact order
        if (strictOrder) {
            val strictPath = mutableListOf<PoiEntity>()
            if (startPoint != null && startPoint.poiId != preferences[0].poiId) {
                strictPath.add(startPoint)
            }
            strictPath.addAll(preferences)
            return strictPath
        }
        
        // Non-strict mode: find shortest paths including intermediate POIs
        val completePath = mutableListOf<PoiEntity>()
        
        // If start point provided and different from first preference, route to first preference
        if (startPoint != null && (preferences.isEmpty() || startPoint.poiId != preferences[0].poiId)) {
            val segmentToFirst = findShortestPath(startPoint.poiId, preferences[0].poiId, graph)
            if (segmentToFirst.isNotEmpty()) {
                completePath.addAll(segmentToFirst)
            } else {
                // Can't reach first preference from start point
                completePath.add(startPoint)
                completePath.add(preferences[0])
            }
        } else {
            // Start directly from first preference
            completePath.add(preferences[0])
        }
        
        if (preferences.size == 1) return completePath
        
        // Chain shortest paths between consecutive preferences
        for (i in 0 until preferences.size - 1) {
            val current = preferences[i]
            val next = preferences[i + 1]
            
            // Find shortest path between consecutive POIs
            val segment = findShortestPath(current.poiId, next.poiId, graph)
            
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
        fromId: String, 
        toId: String, 
        graph: PoiGraph
    ): List<PoiEntity> {
        if (fromId == toId) return listOf(graph.getNode(fromId) ?: return emptyList())
        
        // Dijkstra's algorithm using knowledge graph edges
        val distances = mutableMapOf(fromId to 0.0)
        val previous = mutableMapOf<String, String>()
        val queue = PriorityQueue<Pair<Double, String>>(compareBy { it.first })
        queue.add(0.0 to fromId)
        val visited = mutableSetOf<String>()
        
        while (queue.isNotEmpty()) {
            val (dist, nodeId) = queue.poll() ?: continue
            
            if (nodeId in visited) continue
            visited.add(nodeId)
            
            // Found destination
            if (nodeId == toId) {
                return reconstructPath(fromId, toId, previous, graph)
            }
            
            // Explore neighbors from knowledge graph
            graph.getEdges(nodeId).forEach { edge ->
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
        fromId: String,
        toId: String,
        previous: Map<String, String>,
        graph: PoiGraph
    ): List<PoiEntity> {
        val path = mutableListOf<String>()
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