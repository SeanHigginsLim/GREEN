package com.thsst2.greenapp.graph

import com.thsst2.greenapp.data.PoiEntity

/**
 * Represents a graph of Points of Interest with weighted edges
 */
data class PoiGraph(
    val nodes: Map<Int, PoiEntity>,
    val adjacencyList: Map<Int, List<Edge>>
) {
    fun getNode(poiId: Int): PoiEntity? = nodes[poiId]
    
    fun getEdges(poiId: Int): List<Edge> = adjacencyList[poiId] ?: emptyList()
    
    fun getAllNodes(): Collection<PoiEntity> = nodes.values
}

/**
 * Represents a weighted edge between two POIs
 */
data class Edge(
    val from: Int,
    val to: Int,
    val weight: Double,
    val distance: Double
)