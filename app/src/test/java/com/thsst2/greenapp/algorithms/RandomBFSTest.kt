package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import com.thsst2.greenapp.graph.Edge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class RandomBFSTest {

    @Test
    fun testFindPathExploresRandomly() {
        val dummyPois = listOf(
            PoiEntity(
                poiId = "poi_central_park",
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
                poiId = "poi_garden",
                generatedPathId = 0L,
                name = "Botanical Garden",
                description = "Greenhouse and plant collections.",
                category = listOf("Nature", "Education"),
                latitude = 40.667622,
                longitude = -73.962475
            ),
            PoiEntity(
                poiId = "poi_theater",
                generatedPathId = 0L,
                name = "Historic Theater",
                description = "An old downtown theater hosting plays and concerts.",
                category = listOf("Entertainment", "History"),
                latitude = 40.712776,
                longitude = -74.005974
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
                "poi_central_park" to listOf(
                    Edge("e1", "poi_museum", 0.5),
                    Edge("e2", "poi_zoo", 0.7),
                    Edge("e3", "poi_garden", 1.2)
                ),
                "poi_museum" to listOf(
                    Edge("e4", "poi_central_park", 0.5),
                    Edge("e5", "poi_theater", 0.8),
                    Edge("e6", "poi_science", 1.0)
                ),
                "poi_zoo" to listOf(
                    Edge("e7", "poi_central_park", 0.7),
                    Edge("e8", "poi_garden", 0.6)
                ),
                "poi_garden" to listOf(
                    Edge("e9", "poi_central_park", 1.2),
                    Edge("e10", "poi_zoo", 0.6),
                    Edge("e11", "poi_theater", 0.9)
                ),
                "poi_theater" to listOf(
                    Edge("e12", "poi_museum", 0.8),
                    Edge("e13", "poi_garden", 0.9),
                    Edge("e14", "poi_science", 0.5)
                ),
                "poi_science" to listOf(
                    Edge("e15", "poi_museum", 1.0),
                    Edge("e16", "poi_theater", 0.5)
                )
            )
        )

        val randomBFS = RandomBFS()
        val path = randomBFS.findPath(knowledgeGraph)

        println("RandomBFS produced path = $path")

        // Check that path is valid
        assertTrue(path.isNotEmpty())
        assertTrue(path.all { it in dummyPois })
        
        // RandomBFS should explore the graph, likely visiting multiple POIs
        assertTrue(path.size >= 1)
    }

    @Test
    fun testFindPathEmptyGraph() {
        val knowledgeGraph = PoiGraph(
            nodes = emptyMap(),
            adjacencyList = emptyMap()
        )
        
        val randomBFS = RandomBFS()
        val path = randomBFS.findPath(knowledgeGraph)
        println("RandomBFS with empty graph produced path = $path")
        assertTrue(path.isEmpty())
    }

    @Test
    fun testFindPathWithStartPoint() {
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
                    Edge("e1", "poi_gallery", 0.5),
                    Edge("e2", "poi_zoo", 0.7),
                    Edge("e3", "poi_theater", 1.0)
                ),
                "poi_gallery" to listOf(
                    Edge("e4", "poi_park", 0.5),
                    Edge("e5", "poi_zoo", 0.6),
                    Edge("e6", "poi_theater", 0.8)
                ),
                "poi_zoo" to listOf(
                    Edge("e7", "poi_park", 0.7),
                    Edge("e8", "poi_gallery", 0.6),
                    Edge("e9", "poi_theater", 0.5)
                ),
                "poi_theater" to listOf(
                    Edge("e10", "poi_park", 1.0),
                    Edge("e11", "poi_gallery", 0.8),
                    Edge("e12", "poi_zoo", 0.5)
                )
            )
        )
        
        val startPoint = dummyPois[2] // Start at Zoo
        
        val randomBFS = RandomBFS()
        val path = randomBFS.findPath(knowledgeGraph, startPoint)

        println("RandomBFS with start point produced path = $path")

        // Path should start at the start point
        assertTrue(path.isNotEmpty())
        assertEquals(startPoint, path[0]) // Zoo should be first
        assertTrue(path.all { it in dummyPois })
    }

    @Test
    fun testFindPathVariety() {
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
        
        val knowledgeGraph = PoiGraph(
            nodes = dummyPois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_a" to listOf(
                    Edge("e1", "poi_b", 0.5),
                    Edge("e2", "poi_c", 0.7)
                ),
                "poi_b" to listOf(
                    Edge("e3", "poi_a", 0.5),
                    Edge("e4", "poi_d", 0.6)
                ),
                "poi_c" to listOf(
                    Edge("e5", "poi_a", 0.7),
                    Edge("e6", "poi_d", 0.8)
                ),
                "poi_d" to listOf(
                    Edge("e7", "poi_b", 0.6),
                    Edge("e8", "poi_c", 0.8)
                )
            )
        )

        val randomBFS = RandomBFS()
        val path = randomBFS.findPath(knowledgeGraph)

        println("RandomBFS variety test produced path = $path")

        // RandomBFS should explore variety - check path is valid
        assertTrue(path.isNotEmpty())
        assertTrue(path.all { it in dummyPois })
        
        // Due to randomness, path length may vary
        assertTrue(path.size <= dummyPois.size)
    }
}

