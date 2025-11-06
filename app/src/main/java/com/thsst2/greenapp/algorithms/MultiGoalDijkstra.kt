package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import com.thsst2.greenapp.graph.FilteredAdjacencyList
import java.util.*

class MultiGoalDijkstra {

    /**
     * Find the fastest tour path through unordered POIs using weighted edges.
     * Returns the path with optimal visiting sequence that minimizes total travel cost.
     * Uses bitmask dynamic programming with Dijkstra for pairwise distances.
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
        val allowedPois = filteredGraph.getAllowedPois()
        val allowedPoiIds = allowedPois.map { it.poiId }.toSet()
        
        val validPreferences = preferences.filter { it.poiId in allowedPoiIds }
        
        // No preferences: return all allowed POIs as path
        if (validPreferences.isEmpty()) return allowedPois
        
        // Single preference: return path with this POI plus remaining POIs
        if (validPreferences.size == 1) {
            val remaining = allowedPois.filterNot { it.poiId == validPreferences[0].poiId }
            
            // If start point provided, prepend route to first preference
            return if (startPoint != null && startPoint.poiId != validPreferences[0].poiId) {
                listOf(startPoint) + validPreferences + remaining
            } else {
                validPreferences + remaining
            }
        }
        
        // Multi-Goal A*: find fastest path order through preferences using weighted edges
        data class State(val poi: PoiEntity, val unvisited: Int, val path: List<PoiEntity>, val cost: Double)
        
        val goalIndices = validPreferences.withIndex().associate { it.value.poiId to it.index }
        val allVisited = 0  // All bits 0 = all goals visited
        
        val openSet = PriorityQueue<State>(compareBy { it.cost })
        val visited = mutableSetOf<Pair<Long, Int>>()
        
        // If start point provided, begin from there; otherwise try each preference as start
        if (startPoint != null && startPoint.poiId !in goalIndices) {
            // Start from user location, visit all preferences
            val startMask = (1 shl validPreferences.size) - 1
            openSet.add(State(startPoint, startMask, listOf(startPoint), 0.0))
        } else {
            // Try starting from each preference to find globally optimal path
            validPreferences.forEach { start ->
                val startIndex = goalIndices[start.poiId]!!
                val startMask = ((1 shl validPreferences.size) - 1) xor (1 shl startIndex)
                
                // If startPoint is one of the preferences, prioritize starting there
                val initialCost = if (startPoint != null && start.poiId == startPoint.poiId) 0.0 else 0.0
                openSet.add(State(start, startMask, listOf(start), initialCost))
            }
        }
        
        while (openSet.isNotEmpty()) {
            val current = openSet.poll() ?: continue
            
            // Found complete fastest path through all preferences
            if (current.unvisited == allVisited) {
                // Append remaining allowed POIs to complete the path
                val remaining = allowedPois.filterNot { poi -> 
                    validPreferences.any { pref -> pref.poiId == poi.poiId } ||
                    (startPoint != null && poi.poiId == startPoint.poiId)
                }
                return current.path + remaining
            }
            
            val stateKey = current.poi.poiId to current.unvisited
            if (stateKey in visited) continue
            visited.add(stateKey)
            
            // Expand to unvisited goals using weighted edges from graph
            for (i in validPreferences.indices) {
                if ((current.unvisited and (1 shl i)) != 0) {
                    val nextGoal = validPreferences[i]
                    val edgeWeight = getShortestWeight(current.poi.poiId, nextGoal.poiId, filteredGraph)
                    
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
        val remaining = allowedPois.filterNot { poi -> 
            validPreferences.any { pref -> pref.poiId == poi.poiId }
        }
        
        // Prepend start point if provided
        return if (startPoint != null && startPoint.poiId !in validPreferences.map { it.poiId }) {
            listOf(startPoint) + validPreferences + remaining
        } else {
            validPreferences + remaining
        }
    }
    
    /**
     * Get shortest weighted path between two POIs using graph's weighted edges
     */
    private fun getShortestWeight(fromId: Long, toId: Long, filteredGraph: FilteredAdjacencyList): Double {
        if (fromId == toId) return 0.0
        
        // Check direct weighted edge first
        filteredGraph.getNeighbors(fromId).find { it.to == toId }?.let { return it.weight }
        
        // Dijkstra for shortest weighted path using graph's edges
        val distances = mutableMapOf(fromId to 0.0)
        val queue = PriorityQueue<Pair<Double, Long>>(compareBy { it.first })
        queue.add(0.0 to fromId)
        val visited = mutableSetOf<Long>()
        
        while (queue.isNotEmpty()) {
            val (dist, nodeId) = queue.poll() ?: continue
            if (nodeId in visited) continue
            visited.add(nodeId)
            
            if (nodeId == toId) return dist
            
            filteredGraph.getNeighbors(nodeId).forEach { edge ->
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