package com.thsst2.greenapp.graph

import com.thsst2.greenapp.data.PoiEntity

/**
 * Represents a graph of Points of Interest with weighted edges from Firebase knowledge graph.
 * Uses String IDs to match Firebase schema.
 */
data class PoiGraph(
    val nodes: Map<String, PoiEntity>,
    val adjacencyList: Map<String, List<Edge>>
) {
    fun getNode(poiId: String): PoiEntity? = nodes[poiId]
    
    fun getEdges(poiId: String): List<Edge> = adjacencyList[poiId] ?: emptyList()
    
    fun getAllNodes(): Collection<PoiEntity> = nodes.values
}

/**
 * Represents a weighted edge between two POIs from Firebase knowledge graph
 */
data class Edge(
    val edgeId: String,
    val to: String,
    val weight: Double
)