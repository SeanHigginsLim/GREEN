package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import com.thsst2.greenapp.graph.FilteredAdjacencyList
import kotlin.random.Random

class RandomBFS {

    /**
     * Find a random BFS-like ordering over POIs using graph structure while excluding POIs whose
     * categories match any of the user's disinterests (case-insensitive).
     * 
     * @param startPoint Optional starting location (e.g., user's current location or preferred start)
     */
    fun findPath(
        graph: PoiGraph,
        dislikedPoiIds: Set<Long> = emptySet(),
        disinterests: Collection<String> = emptyList(),
        startPoint: PoiEntity? = null
    ): List<PoiEntity> {
        
        // Apply filters to the graph
        val filteredGraph = FilteredAdjacencyList(graph)
        filteredGraph.applyFilters(dislikedPoiIds, disinterests)
        
        val allowedPois = filteredGraph.getAllowedPois()
        if (allowedPois.isEmpty()) return emptyList()

        val visited = mutableSetOf<PoiEntity>()
        val path = mutableListOf<PoiEntity>()
        val queue = ArrayDeque<PoiEntity>()

        // Pick start: user-provided startPoint or random from allowed POIs
        val start: PoiEntity = if (startPoint != null && allowedPois.contains(startPoint)) {
            startPoint
        } else if (startPoint != null) {
            // Start point provided but not in allowed list - use it anyway as starting position
            startPoint
        } else {
            // No start point - pick random
            allowedPois.shuffled(Random.Default).first()
        }
        
        queue.add(start)
        visited.add(start)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            path.add(current)

            // Get actual graph neighbors, then randomize the order
            val graphNeighbors = filteredGraph.getNeighbors(current.poiId)
                .mapNotNull { edge -> graph.getNode(edge.to) }
                .filter { it !in visited }
                .shuffled(Random.Default)

            for (neighbor in graphNeighbors) {
                if (neighbor !in visited) {
                    queue.add(neighbor)
                    visited.add(neighbor)
                }
            }
        }

        return path
    }
}