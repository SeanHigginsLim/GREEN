package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import java.util.*

class MultiGoalDijkstra {

    /**
     * Find the fastest tour path through unordered POIs using weighted edges from knowledge graph.
     * Returns the path with optimal visiting sequence that minimizes total travel cost.
     * Uses bitmask dynamic programming with Dijkstra for pairwise distances.
     * 
     * @param graph Knowledge graph with weighted edges from Firebase
     * @param preferences Unordered list of POIs to visit optimally
     * @param startPoint Optional starting location (e.g., user's current location or preferred start)
     * @param dislikedPoiIds Set of POI IDs to exclude from the path (skipped/disliked locations)
     */
    fun findPath(
        graph: PoiGraph,
        preferences: List<PoiEntity>,
        startPoint: PoiEntity? = null,
        dislikedPoiIds: Set<String> = emptySet()
    ): List<PoiEntity> {
        
        val allPois = graph.getAllNodes().toList()
        
        // Filter out disliked POIs from all available POIs
        val availablePois = allPois.filter { it.poiId !in dislikedPoiIds }
        
        // Filter out disliked POIs from preferences as well
        val filteredPreferences = preferences.filter { it.poiId !in dislikedPoiIds }
        
        // No preferences: return available POIs as path
        if (filteredPreferences.isEmpty()) return availablePois
        
        // Single preference: return path with this POI plus remaining
        if (filteredPreferences.size == 1) {
            // If start point provided, build path: startPoint -> preference -> remaining
            return if (startPoint != null && startPoint.poiId != filteredPreferences[0].poiId && startPoint.poiId !in dislikedPoiIds) {
                val remaining = availablePois.filterNot { 
                    it.poiId == filteredPreferences[0].poiId || it.poiId == startPoint.poiId 
                }
                listOf(startPoint) + filteredPreferences + remaining
            } else {
                val remaining = availablePois.filterNot { it.poiId == filteredPreferences[0].poiId }
                filteredPreferences + remaining
            }
        }
        
        // Multi-Goal optimization: find fastest path order through preferences using weighted edges
        data class State(val poi: PoiEntity, val unvisited: Int, val path: List<PoiEntity>, val cost: Double)
        
        val goalIndices = filteredPreferences.withIndex().associate { it.value.poiId to it.index }
        val allVisited = 0  // All bits 0 = all goals visited
        
        val openSet = PriorityQueue<State>(compareBy { it.cost })
        val visited = mutableSetOf<Pair<String, Int>>()
        
        // If start point provided, begin from there; otherwise try each preference as start
        if (startPoint != null && startPoint.poiId !in goalIndices && startPoint.poiId !in dislikedPoiIds) {
            // Start from user location, visit all preferences
            val startMask = (1 shl filteredPreferences.size) - 1
            openSet.add(State(startPoint, startMask, listOf(startPoint), 0.0))
        } else {
            // Try starting from each preference to find globally optimal path
            filteredPreferences.forEach { start ->
                val startIndex = goalIndices[start.poiId]!!
                val startMask = ((1 shl filteredPreferences.size) - 1) xor (1 shl startIndex)
                
                // If startPoint is one of the preferences, prioritize starting there
                val initialCost = if (startPoint != null && start.poiId == startPoint.poiId) 0.0 else 0.0
                openSet.add(State(start, startMask, listOf(start), initialCost))
            }
        }
        
        while (openSet.isNotEmpty()) {
            val current = openSet.poll() ?: continue
            
            // Found complete fastest path through all preferences
            if (current.unvisited == allVisited) {
                // Append remaining POIs from knowledge graph to complete the path
                val remaining = availablePois.filterNot { poi -> 
                    filteredPreferences.any { pref -> pref.poiId == poi.poiId } ||
                    (startPoint != null && poi.poiId == startPoint.poiId)
                }
                return current.path + remaining
            }
            
            val stateKey = current.poi.poiId to current.unvisited
            if (stateKey in visited) continue
            visited.add(stateKey)
            
            // Expand to unvisited goals using weighted edges from knowledge graph
            for (i in preferences.indices) {
                if ((current.unvisited and (1 shl i)) != 0) {
                    val nextGoal = preferences[i]
                    val edgeWeight = getShortestWeight(current.poi.poiId, nextGoal.poiId, graph)
                    
                    if (edgeWeight < Double.MAX_VALUE) {
                        val newMask = current.unvisited xor (1 shl i)
                        val newPath = current.path + nextGoal
                        val newCost = current.cost + edgeWeight
                        
                        openSet.add(State(nextGoal, newMask, newPath, newCost))
                    }
                }
            }
        }
        
        // Fallback: return preferences + remaining as path if no optimal path found
        val remaining = allPois.filterNot { poi -> 
            preferences.any { pref -> pref.poiId == poi.poiId }
        }
        
        // Prepend start point if provided
        return if (startPoint != null && startPoint.poiId !in preferences.map { it.poiId }) {
            listOf(startPoint) + preferences + remaining
        } else {
            preferences + remaining
        }
    }
    
    /**
     * Get shortest weighted path between two POIs using knowledge graph's weighted edges
     */
    private fun getShortestWeight(fromId: String, toId: String, graph: PoiGraph): Double {
        if (fromId == toId) return 0.0
        
        // Check direct weighted edge first
        graph.getEdges(fromId).find { it.to == toId }?.let { return it.weight }
        
        // Dijkstra for shortest weighted path using knowledge graph edges
        val distances = mutableMapOf(fromId to 0.0)
        val queue = PriorityQueue<Pair<Double, String>>(compareBy { it.first })
        queue.add(0.0 to fromId)
        val visited = mutableSetOf<String>()
        
        while (queue.isNotEmpty()) {
            val (dist, nodeId) = queue.poll() ?: continue
            if (nodeId in visited) continue
            visited.add(nodeId)
            
            if (nodeId == toId) return dist
            
            graph.getEdges(nodeId).forEach { edge ->
                if (edge.to !in visited) {
                    val newDist = dist + edge.weight
                    if (newDist < (distances[edge.to] ?: Double.MAX_VALUE)) {
                        distances[edge.to] = newDist
                        queue.add(newDist to edge.to)
                    }
                }
            }
        }
        
        return Double.MAX_VALUE
    }
}