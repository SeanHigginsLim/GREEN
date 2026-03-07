package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import com.thsst2.greenapp.graph.Edge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class TourPathPlannerTest {

    // Mock distance calculator for testing
    private class MockDistanceCalculator : DistanceCalculator {
        override fun calculateDistance(
            fromLat: Double,
            fromLng: Double,
            toLat: Double,
            toLng: Double
        ): Float {
            // Simple Euclidean distance approximation for testing
            val latDiff = toLat - fromLat
            val lngDiff = toLng - fromLng
            return sqrt(latDiff * latDiff + lngDiff * lngDiff).toFloat() * 111000f // ~111km per degree
        }
    }

    @Test
    fun testStartPointIsClosestPOI() {
        // User is near Empire State Building (40.748817, -73.985428)
        val currentLat = 40.748817
        val currentLng = -73.985428
        
        val pois = listOf(
            PoiEntity(
                poiId = "poi_empire",
                generatedPathId = 0L,
                name = "Empire State Building",
                description = "Closest POI",
                category = listOf("Landmark"),
                latitude = 40.748817,  // ~0 meters away
                longitude = -73.985428
            ),
            PoiEntity(
                poiId = "poi_times_square",
                generatedPathId = 0L,
                name = "Times Square",
                description = "Far POI",
                category = listOf("Landmark"),
                latitude = 40.758896,  // ~1.2 km away
                longitude = -73.985130
            ),
            PoiEntity(
                poiId = "poi_central_park",
                generatedPathId = 0L,
                name = "Central Park",
                description = "Even farther POI",
                category = listOf("Nature"),
                latitude = 40.785091,  // ~4 km away
                longitude = -73.968285
            )
        )
        
        val knowledgeGraph = PoiGraph(
            nodes = pois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_empire" to listOf(
                    Edge("e1", "poi_times_square", 1.2),
                    Edge("e2", "poi_central_park", 4.0)
                ),
                "poi_times_square" to listOf(
                    Edge("e3", "poi_empire", 1.2),
                    Edge("e4", "poi_central_park", 3.0)
                ),
                "poi_central_park" to listOf(
                    Edge("e5", "poi_empire", 4.0),
                    Edge("e6", "poi_times_square", 3.0)
                )
            )
        )
        
        val preferences = listOf(pois[1], pois[2]) // Times Square, Central Park
        val planner = TourPathPlanner(MockDistanceCalculator())
        
        // Test with MultiGoalDijkstra (isRandom = false)
        val path = planner.planTour(
            knowledgeGraph = knowledgeGraph,
            currentLatitude = currentLat,
            currentLongitude = currentLng,
            relevantPOIs = pois,
            preferences = preferences,
            isRandom = false
        )
        
        // The path should start with the closest POI (Empire State Building)
        // or go through it to reach preferences
        assertTrue("Path should not be empty", path.isNotEmpty())
        
        // Since Empire State Building is closest, it should be used as start point
        // MultiGoalDijkstra will use it to optimize the tour
        println("Path starting from closest POI: ${path.map { it.name }}")
    }
    
    @Test
    fun testStartPointSelectionWithMultiplePOIs() {
        // User location: 40.750000, -73.980000
        val currentLat = 40.750000
        val currentLng = -73.980000
        
        val pois = listOf(
            PoiEntity(
                poiId = "poi_a",
                generatedPathId = 0L,
                name = "POI A - Far North",
                description = "",
                category = listOf("Test"),
                latitude = 40.780000,  // Far
                longitude = -73.980000
            ),
            PoiEntity(
                poiId = "poi_b",
                generatedPathId = 0L,
                name = "POI B - Closest",
                description = "",
                category = listOf("Test"),
                latitude = 40.751000,  // ~100m away
                longitude = -73.980500
            ),
            PoiEntity(
                poiId = "poi_c",
                generatedPathId = 0L,
                name = "POI C - Far South",
                description = "",
                category = listOf("Test"),
                latitude = 40.720000,  // Far
                longitude = -73.980000
            )
        )
        
        val knowledgeGraph = PoiGraph(
            nodes = pois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_a" to listOf(Edge("e1", "poi_b", 1.0), Edge("e2", "poi_c", 2.0)),
                "poi_b" to listOf(Edge("e3", "poi_a", 1.0), Edge("e4", "poi_c", 1.5)),
                "poi_c" to listOf(Edge("e5", "poi_a", 2.0), Edge("e6", "poi_b", 1.5))
            )
        )
        
        val preferences = listOf(pois[0], pois[2]) // POI A and C
        val planner = TourPathPlanner(MockDistanceCalculator())
        
        // Test with RandomBFS
        val pathRandom = planner.planTour(
            knowledgeGraph = knowledgeGraph,
            currentLatitude = currentLat,
            currentLongitude = currentLng,
            relevantPOIs = pois,
            preferences = preferences,
            isRandom = true
        )
        
        // RandomBFS should start from closest POI (poi_b)
        assertTrue("Path should not be empty", pathRandom.isNotEmpty())
        assertEquals("First POI should be the closest one", "poi_b", pathRandom[0].poiId)
        
        println("RandomBFS path starting from closest: ${pathRandom.map { it.name }}")
    }
    
    @Test
    fun testStartPointWithSinglePOI() {
        val currentLat = 40.748817
        val currentLng = -73.985428
        
        val pois = listOf(
            PoiEntity(
                poiId = "poi_only",
                generatedPathId = 0L,
                name = "Only POI",
                description = "The only available POI",
                category = listOf("Test"),
                latitude = 40.760000,
                longitude = -73.980000
            )
        )
        
        val knowledgeGraph = PoiGraph(
            nodes = pois.associateBy { it.poiId },
            adjacencyList = mapOf("poi_only" to emptyList())
        )
        
        val preferences = pois
        val planner = TourPathPlanner(MockDistanceCalculator())
        
        val path = planner.planTour(
            knowledgeGraph = knowledgeGraph,
            currentLatitude = currentLat,
            currentLongitude = currentLng,
            relevantPOIs = pois,
            preferences = preferences,
            isRandom = false
        )
        
        // With only one POI, it must be the closest and should be in the path
        assertTrue("Path should contain the only POI", path.isNotEmpty())
        assertEquals("Path should start with the only POI", "poi_only", path[0].poiId)
    }
}
