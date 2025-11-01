package com.thsst2.greenapp.graph

import com.thsst2.greenapp.data.PoiEntity

/**
 * Represents a graph of Points of Interest with weighted edges
 */
data class PoiGraph(
    val nodes: Map<Long, PoiEntity>,
    val adjacencyList: Map<Long, List<Edge>>
) {
    fun getNode(poiId: Long): PoiEntity? = nodes[poiId]
    
    fun getEdges(poiId: Long): List<Edge> = adjacencyList[poiId] ?: emptyList()
    
    fun getAllNodes(): Collection<PoiEntity> = nodes.values
}

/**
 * Represents a weighted edge between two POIs
 */
data class Edge(
    val from: Long,
    val to: Long,
    val weight: Double,
    val distance: Double
)