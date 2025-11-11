package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import com.thsst2.greenapp.graph.Edge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MultiGoalDijkstraTest {

    @Test
    fun testFindPathOptimizesPreferenceOrder() {
        val dummyPois = listOf(
            PoiEntity(
                poiId = "poi_park",
                generatedPathId = 0L,
                name = "Central Park",
                description = "A large public park.",
                category = listOf("Nature"),
                latitude = 40.785091,
                longitude = -73.968285
            ),
            PoiEntity(
                poiId = "poi_museum",
                generatedPathId = 0L,
                name = "Museum of Art",
                description = "A famous art museum.",
                category = listOf("Art"),
                latitude = 40.779437,
                longitude = -73.963244
            ),
            PoiEntity(
                poiId = "poi_zoo",
                generatedPathId = 0L,
                name = "City Zoo",
                description = "A popular zoo.",
                category = listOf("Animals"),
                latitude = 40.767778,
                longitude = -73.971834
            ),
            PoiEntity(
                poiId = "poi_science",
                generatedPathId = 0L,
                name = "Science Center",
                description = "Interactive science exhibits for all ages.",
                category = listOf("Education"),
                latitude = 40.730610,
                longitude = -73.935242
            )
        )

        val knowledgeGraph = PoiGraph(
            nodes = dummyPois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_park" to listOf(
                    Edge("e1", "poi_zoo", 0.5),
                    Edge("e2", "poi_museum", 0.8),
                    Edge("e3", "poi_science", 1.2)
                ),
                "poi_zoo" to listOf(
                    Edge("e4", "poi_park", 0.5),
                    Edge("e5", "poi_museum", 0.6),
                    Edge("e6", "poi_science", 1.0)
                ),
                "poi_museum" to listOf(
                    Edge("e7", "poi_park", 0.8),
                    Edge("e8", "poi_zoo", 0.6),
                    Edge("e9", "poi_science", 0.4)
                ),
                "poi_science" to listOf(
                    Edge("e10", "poi_park", 1.2),
                    Edge("e11", "poi_zoo", 1.0),
                    Edge("e12", "poi_museum", 0.4)
                )
            )
        )

        val preferences = listOf(dummyPois[1], dummyPois[3]) // Museum, Science Center
        
        val multiGoalDijkstra = MultiGoalDijkstra()
        val path = multiGoalDijkstra.findPath(knowledgeGraph, preferences)

        println("multiGoalDijkstra produced path = $path")

        // Check that all POIs are visited and preferences are included
        assertEquals(dummyPois.size, path.size)
        assertTrue("Path should contain all preferences", path.containsAll(preferences))
        assertTrue("Path should contain all POIs", path.containsAll(dummyPois))
    }

    @Test
    fun testFindPathEmptyPreferences() {
        val dummyPois = listOf(
            PoiEntity(
                poiId = "poi_park",
                generatedPathId = 0L,
                name = "Park", 
                description = "", 
                category = listOf("Nature"), 
                latitude = 40.748817, 
                longitude = -73.985428
            ),
            PoiEntity(
                poiId = "poi_gallery",
                generatedPathId = 0L,
                name = "Gallery", 
                description = "", 
                category = listOf("Art"), 
                latitude = 40.761431, 
                longitude = -73.977621
            )
        )
        
        val knowledgeGraph = PoiGraph(
            nodes = dummyPois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_park" to listOf(Edge("e1", "poi_gallery", 0.5)),
                "poi_gallery" to listOf(Edge("e2", "poi_park", 0.5))
            )
        )
        
        val multiGoalDijkstra = MultiGoalDijkstra()
        val path = multiGoalDijkstra.findPath(knowledgeGraph, emptyList())
        
        println("multiGoalDijkstra with empty preferences produced path = $path")
        
        // Should return all POIs when no preferences
        assertEquals(dummyPois.size, path.size)
        assertTrue(path.containsAll(dummyPois))
    }

    @Test
    fun testFindPathWithSinglePreference() {
        val dummyPois = listOf(
            PoiEntity(
                poiId = "poi_park",
                generatedPathId = 0L,
                name = "Park", 
                description = "", 
                category = listOf("Nature"), 
                latitude = 40.748817, 
                longitude = -73.985428
            ),
            PoiEntity(
                poiId = "poi_gallery",
                generatedPathId = 0L,
                name = "Gallery", 
                description = "", 
                category = listOf("Art"), 
                latitude = 40.761431, 
                longitude = -73.977621
            ),
            PoiEntity(
                poiId = "poi_zoo",
                generatedPathId = 0L,
                name = "Zoo", 
                description = "", 
                category = listOf("Animals"), 
                latitude = 40.767778, 
                longitude = -73.971834
            )
        )

        
        val knowledgeGraph = PoiGraph(
            nodes = dummyPois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_park" to listOf(
                    Edge("e1", "poi_gallery", 0.5),
                    Edge("e2", "poi_zoo", 1.0)
                ),
                "poi_gallery" to listOf(
                    Edge("e3", "poi_park", 0.5),
                    Edge("e4", "poi_zoo", 0.8)
                ),
                "poi_zoo" to listOf(
                    Edge("e5", "poi_park", 1.0),
                    Edge("e6", "poi_gallery", 0.8)
                )
            )
        )
        
        val preferences = listOf(dummyPois[0]) // Just Park
        
        val multiGoalDijkstra = MultiGoalDijkstra()
        val path = multiGoalDijkstra.findPath(knowledgeGraph, preferences)

        println("multiGoalDijkstra with single preference produced path = $path")

        // Path should contain all POIs and start with the preference
        assertEquals(dummyPois.size, path.size)
        assertTrue(path.containsAll(dummyPois))
        assertEquals(dummyPois[0], path[0]) // Park should be first
    }

    @Test
    fun testFindPathStartPoint() {
        val dummyPois = listOf(
            PoiEntity(
                poiId = "poi_park",
                generatedPathId = 0L,
                name = "Park", 
                description = "", 
                category = listOf("Nature"), 
                latitude = 40.748817, 
                longitude = -73.985428
            ),
            PoiEntity(
                poiId = "poi_gallery",
                generatedPathId = 0L,
                name = "Gallery", 
                description = "", 
                category = listOf("Art"), 
                latitude = 40.761431, 
                longitude = -73.977621
            ),
            PoiEntity(
                poiId = "poi_theater",
                generatedPathId = 0L,
                name = "Theater", 
                description = "", 
                category = listOf("Entertainment"), 
                latitude = 40.759776, 
                longitude = -73.984018
            )
        )
        
        val knowledgeGraph = PoiGraph(
            nodes = dummyPois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_park" to listOf(
                    Edge("e1", "poi_gallery", 0.6),
                    Edge("e2", "poi_theater", 1.0)
                ),
                "poi_gallery" to listOf(
                    Edge("e3", "poi_park", 0.6),
                    Edge("e4", "poi_theater", 0.4)
                ),
                "poi_theater" to listOf(
                    Edge("e5", "poi_park", 1.0),
                    Edge("e6", "poi_gallery", 0.4)
                )
            )
        )

        val preferences = listOf(dummyPois[1]) // Gallery preference
        val startPoint = dummyPois[2] // Start at Theater
        
        val multiGoalDijkstra = MultiGoalDijkstra()
        val path = multiGoalDijkstra.findPath(knowledgeGraph, preferences, startPoint)

        println("multiGoalDijkstra with start point produced path = $path")

        // With single preference and start point, path should include all POIs
        assertEquals(dummyPois.size, path.size)
        
        // Start point should be first
        assertEquals("Theater should be first", startPoint, path[0])
        
        // Preference should be in path (typically second)
        assertTrue("Gallery should be in path", path.contains(preferences[0]))
    }

    @Test
    fun testFindPathHandlesDisconnectedGraph() {
        val dummyPois = listOf(
            PoiEntity(
                poiId = "poi_a",
                generatedPathId = 0L,
                name = "A", 
                description = "", 
                category = listOf("Nature"), 
                latitude = 40.748817, 
                longitude = -73.985428
            ),
            PoiEntity(
                poiId = "poi_b",
                generatedPathId = 0L,
                name = "B", 
                description = "", 
                category = listOf("Art"), 
                latitude = 40.761431, 
                longitude = -73.977621
            ),
            PoiEntity(
                poiId = "poi_c",
                generatedPathId = 0L,
                name = "C", 
                description = "", 
                category = listOf("Entertainment"), 
                latitude = 40.759776, 
                longitude = -73.984018
            ),
            PoiEntity(
                poiId = "poi_d",
                generatedPathId = 0L,
                name = "D", 
                description = "", 
                category = listOf("Education"), 
                latitude = 40.730610, 
                longitude = -73.935242
            )
        )
        
        // Create a disconnected graph: A-B connected, C-D connected, but no path between them
        val knowledgeGraph = PoiGraph(
            nodes = dummyPois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_a" to listOf(Edge("e1", "poi_b", 0.5)),
                "poi_b" to listOf(Edge("e2", "poi_a", 0.5)),
                "poi_c" to listOf(Edge("e3", "poi_d", 0.5)),
                "poi_d" to listOf(Edge("e4", "poi_c", 0.5))
            )
        )

        val preferences = listOf(dummyPois[2], dummyPois[0]) // C, A (in disconnected components)
        
        val multiGoalDijkstra = MultiGoalDijkstra()
        val path = multiGoalDijkstra.findPath(knowledgeGraph, preferences)

        println("multiGoalDijkstra with disconnected graph produced path = $path")

        // With a disconnected graph, algorithm should handle it gracefully
        // May return partial path or all nodes from reachable component
        assertTrue(path.isNotEmpty())
        assertTrue(path.size <= dummyPois.size)
        
        // If preferences are in disconnected components, at least one should be included
        val containsC = path.contains(dummyPois[2])
        val containsA = path.contains(dummyPois[0])
        assertTrue("Path should contain at least one preference", containsC || containsA)
    }
}

