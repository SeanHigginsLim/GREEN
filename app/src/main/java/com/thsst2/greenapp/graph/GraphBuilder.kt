package com.thsst2.greenapp.graph

import com.thsst2.greenapp.data.PoiEntity
import kotlin.math.*

/**
 * Builds a graph from a list of POIs by connecting nearby locations
 */
class GraphBuilder {
    companion object {
        private const val EARTH_RADIUS_KM = 6371.0
        private const val MAX_WALKING_DISTANCE_KM = 2.0 // only connect POIs within 2km
    }

    fun buildGraph(pois: List<PoiEntity>): PoiGraph {
        if (pois.isEmpty()) return PoiGraph(emptyMap(), emptyMap())
        
        val nodes = pois.associateBy { it.poiId }
        val adjacencyList = mutableMapOf<Long, MutableList<Edge>>()

        // Initialize empty adjacency lists
        pois.forEach { poi ->
            adjacencyList[poi.poiId] = mutableListOf()
        }

        // Connect each POI to nearby POIs
        for (i in pois.indices) {
            for (j in pois.indices) {
                if (i != j) {
                    val poi1 = pois[i]
                    val poi2 = pois[j]
                    val distance = calculateDistance(poi1, poi2)
                    
                    // Only connect nearby POIs
                    if (distance <= MAX_WALKING_DISTANCE_KM) {
                        val weight = calculateWeight(distance)
                        
                        adjacencyList[poi1.poiId]?.add(
                            Edge(poi1.poiId, poi2.poiId, weight, distance)
                        )
                    }
                }
            }
        }

        return PoiGraph(nodes, adjacencyList.mapValues { it.value.toList() })
    }

    /**
     * Calculate distance between two POIs using Haversine formula
     */
    private fun calculateDistance(poi1: PoiEntity, poi2: PoiEntity): Double {
        val lat1Rad = Math.toRadians(poi1.latitude)
        val lat2Rad = Math.toRadians(poi2.latitude)
        val deltaLat = Math.toRadians(poi2.latitude - poi1.latitude)
        val deltaLon = Math.toRadians(poi2.longitude - poi1.longitude)

        val a = sin(deltaLat / 2).pow(2) + 
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    /**
     * Calculate static weight based on distance only
     * Formula: w = distance
     */
    private fun calculateWeight(distance: Double): Double {
        return distance
    }
}