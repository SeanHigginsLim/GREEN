package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import kotlin.random.Random

class RandomBFS {

    /**
     * Find a random BFS-like ordering over POIs using knowledge graph structure.
     * Explores the graph randomly for variety while respecting connectivity.
     * 
     * @param graph Knowledge graph with weighted edges from Firebase
     * @param startPoint Optional starting location (e.g., user's current location or preferred start)
     * @param dislikedPoiIds Set of POI IDs to exclude from the path (skipped/disliked locations)
     */
    fun findPath(
        graph: PoiGraph,
        startPoint: PoiEntity? = null,
        dislikedPoiIds: Set<String> = emptySet()
    ): List<PoiEntity> {
        
        val allPois = graph.getAllNodes().toList()
        if (allPois.isEmpty()) return emptyList()

        // Filter out disliked POIs
        val availablePois = allPois.filter { it.poiId !in dislikedPoiIds }
        if (availablePois.isEmpty()) return emptyList()

        val visited = mutableSetOf<PoiEntity>()
        val path = mutableListOf<PoiEntity>()
        val queue = ArrayDeque<PoiEntity>()

        // Pick start: user-provided startPoint or random from available POIs
        val start: PoiEntity = if (startPoint != null && availablePois.contains(startPoint)) {
            startPoint
        } else if (startPoint != null && startPoint.poiId !in dislikedPoiIds) {
            // Start point provided but not in graph - use it anyway as starting position
            startPoint
        } else {
            // No start point or start is disliked - pick random from available POIs
            availablePois.shuffled(Random.Default).first()
        }
        
        queue.add(start)
        visited.add(start)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            path.add(current)

            // Get neighbors from knowledge graph, then randomize the order for variety
            val graphNeighbors = graph.getEdges(current.poiId)
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