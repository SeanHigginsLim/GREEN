package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import com.thsst2.greenapp.graph.Edge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChainedDijkstraTest {

    @Test
    fun testFindPathReturnsAllPreferences_OrderedSequence() {
        // Create POIs with String IDs
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

        // Create knowledge graph with String IDs and weighted edges
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
                    Edge("e9", "poi_science", 0.9)
                ),
                "poi_science" to listOf(
                    Edge("e10", "poi_park", 1.2),
                    Edge("e11", "poi_zoo", 1.0),
                    Edge("e12", "poi_museum", 0.9)
                )
            )
        )

        val preferences = listOf(dummyPois[0], dummyPois[2], dummyPois[1]) // Park, Zoo, Museum
        
        val chainedDijkstra = ChainedDijkstra()
        val path = chainedDijkstra.findPath(knowledgeGraph, preferences)

        println("chainedDijkstra produced path = $path")

        // Check that all preferences are visited in order
        assertTrue("Path should contain Park", path.any { it.poiId == "poi_park" })
        assertTrue("Path should contain Zoo", path.any { it.poiId == "poi_zoo" })
        assertTrue("Path should contain Museum", path.any { it.poiId == "poi_museum" })
        
        // Verify order: Park should come before Zoo, Zoo before Museum
        val parkIdx = path.indexOfFirst { it.poiId == "poi_park" }
        val zooIdx = path.indexOfFirst { it.poiId == "poi_zoo" }
        val museumIdx = path.indexOfFirst { it.poiId == "poi_museum" }
        
        assertTrue("Park should come before Zoo", parkIdx < zooIdx)
        assertTrue("Zoo should come before Museum", zooIdx < museumIdx)
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
            )
        )
        
        val knowledgeGraph = PoiGraph(
            nodes = dummyPois.associateBy { it.poiId },
            adjacencyList = emptyMap()
        )
        
        val chainedDijkstra = ChainedDijkstra()
        val path = chainedDijkstra.findPath(knowledgeGraph, emptyList())
        
        println("chainedDijkstra with empty preferences produced path = $path")
        assertTrue(path.isEmpty())
    }

    @Test
    fun testFindPathWithStartPoint() {
        val dummyPois = listOf(
            PoiEntity(
                poiId = "poi_start",
                generatedPathId = 0L,
                name = "Start Location", 
                description = "User's current location", 
                category = listOf("Start"), 
                latitude = 40.748817, 
                longitude = -73.985428
            ),
            PoiEntity(
                poiId = "poi_museum",
                generatedPathId = 0L,
                name = "Museum", 
                description = "Art museum", 
                category = listOf("Art"), 
                latitude = 40.761431, 
                longitude = -73.977621
            ),
            PoiEntity(
                poiId = "poi_park",
                generatedPathId = 0L,
                name = "Park", 
                description = "Central park", 
                category = listOf("Nature"), 
                latitude = 40.767778, 
                longitude = -73.971834
            )
        )

        val knowledgeGraph = PoiGraph(
            nodes = dummyPois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_start" to listOf(
                    Edge("e1", "poi_museum", 0.5),
                    Edge("e2", "poi_park", 1.0)
                ),
                "poi_museum" to listOf(
                    Edge("e3", "poi_start", 0.5),
                    Edge("e4", "poi_park", 0.7)
                ),
                "poi_park" to listOf(
                    Edge("e5", "poi_start", 1.0),
                    Edge("e6", "poi_museum", 0.7)
                )
            )
        )

        val preferences = listOf(dummyPois[1], dummyPois[2]) // Museum, Park
        val startPoint = dummyPois[0] // Start location
        
        val chainedDijkstra = ChainedDijkstra()
        val path = chainedDijkstra.findPath(knowledgeGraph, preferences, startPoint)

        println("chainedDijkstra with start point produced path = $path")

        // Path should include start point and all preferences
        assertTrue("Path should contain start", path.any { it.poiId == "poi_start" })
        assertTrue("Path should contain museum", path.any { it.poiId == "poi_museum" })
        assertTrue("Path should contain park", path.any { it.poiId == "poi_park" })
        
        // Start should be first
        assertEquals("Start should be first", "poi_start", path.first().poiId)
    }

    @Test
    fun testFindPathFindsIntermediatePOIs() {
        // Test that algorithm finds intermediate POIs when needed
        val dummyPois = listOf(
            PoiEntity(
                poiId = "poi_a",
                generatedPathId = 0L,
                name = "Point A", 
                description = "", 
                category = listOf("Start"), 
                latitude = 40.748817, 
                longitude = -73.985428
            ),
            PoiEntity(
                poiId = "poi_b",
                generatedPathId = 0L,
                name = "Point B", 
                description = "Intermediate", 
                category = listOf("Mid"), 
                latitude = 40.761431, 
                longitude = -73.977621
            ),
            PoiEntity(
                poiId = "poi_c",
                generatedPathId = 0L,
                name = "Point C", 
                description = "Destination", 
                category = listOf("End"), 
                latitude = 40.767778, 
                longitude = -73.971834
            )
        )

        // A can only reach C through B
        val knowledgeGraph = PoiGraph(
            nodes = dummyPois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_a" to listOf(Edge("e1", "poi_b", 0.5)),
                "poi_b" to listOf(
                    Edge("e2", "poi_a", 0.5),
                    Edge("e3", "poi_c", 0.5)
                ),
                "poi_c" to listOf(Edge("e4", "poi_b", 0.5))
            )
        )

        val preferences = listOf(dummyPois[0], dummyPois[2]) // A to C
        
        val chainedDijkstra = ChainedDijkstra()
        val path = chainedDijkstra.findPath(knowledgeGraph, preferences)

        println("chainedDijkstra with intermediate POI = $path")

        // Path should include intermediate B
        assertEquals("Path should have 3 POIs", 3, path.size)
        assertEquals("First should be A", "poi_a", path[0].poiId)
        assertEquals("Second should be B (intermediate)", "poi_b", path[1].poiId)
        assertEquals("Third should be C", "poi_c", path[2].poiId)
    }
}

