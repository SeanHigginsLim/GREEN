package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.GraphBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ChainedDijkstraTest {

    @Test
    fun testFindPathReturnsAllPreferences_NoDislikes() {
        val dummyPois = listOf(
            PoiEntity(
                poiId = 1L,
                name = "Central Park",
                description = "A large public park.",
                category = listOf("Nature"),
                latitude = 40.785091,
                longitude = -73.968285
            ),
            PoiEntity(
                poiId = 2L,
                name = "Museum of Art",
                description = "A famous art museum.",
                category = listOf("Art"),
                latitude = 40.779437,
                longitude = -73.963244
            ),
            PoiEntity(
                poiId = 3L,
                name = "City Zoo",
                description = "A popular zoo.",
                category = listOf("Animals"),
                latitude = 40.767778,
                longitude = -73.971834
            ),
            PoiEntity(
                poiId = 4L,
                name = "Science Center",
                description = "Interactive science exhibits for all ages.",
                category = listOf("Education"),
                latitude = 40.730610,
                longitude = -73.935242
            )
        )

        val preferences = listOf(dummyPois[0], dummyPois[2], dummyPois[1]) // Park, Zoo, Museum
        val graph = GraphBuilder().buildGraph(dummyPois)
        
        val chainedDijkstra = ChainedDijkstra()
        val path = chainedDijkstra.findPath(graph, preferences)

        println("chainedDijkstra produced path = $path")

        // Check that all preferences are visited in order
        assertEquals(preferences.size, path.size)
        assertEquals(preferences, path) // Should maintain exact order
    }

    @Test
    fun testFindPathEmptyPreferences() {
        val dummyPois = listOf(
            PoiEntity(
                poiId = 1L, 
                name = "Park", 
                description = "", 
                category = listOf("Nature"), 
                latitude = 40.748817, 
                longitude = -73.985428
            )
        )
        val graph = GraphBuilder().buildGraph(dummyPois)
        
        val chainedDijkstra = ChainedDijkstra()
        val path = chainedDijkstra.findPath(graph, emptyList())
        
        println("chainedDijkstra with empty preferences produced path = $path")
        assertTrue(path.isEmpty())
    }

    @Test
    fun testFindPathRespectsCategoryDislikes() {
        val dummyPois = listOf(
            PoiEntity(
                poiId = 1L, 
                name = "Park", 
                description = "", 
                category = listOf("Nature"), 
                latitude = 40.748817, 
                longitude = -73.985428
            ),
            PoiEntity(
                poiId = 2L, 
                name = "Gallery", 
                description = "", 
                category = listOf("Art"), 
                latitude = 40.761431, 
                longitude = -73.977621
            ),
            PoiEntity(
                poiId = 3L, 
                name = "Zoo", 
                description = "", 
                category = listOf("Animals"), 
                latitude = 40.767778, 
                longitude = -73.971834
            ),
            PoiEntity(
                poiId = 4L, 
                name = "Theater", 
                description = "", 
                category = listOf("Entertainment"), 
                latitude = 40.759776, 
                longitude = -73.984018
            ),
            PoiEntity(
                poiId = 5L, 
                name = "Aquarium", 
                description = "", 
                category = listOf("Animals", "Education"), 
                latitude = 40.574926, 
                longitude = -73.989308
            )
        )

        val preferences = listOf(dummyPois[0], dummyPois[2], dummyPois[1], dummyPois[4]) // Park, Zoo, Gallery, Aquarium
        val disinterests = listOf("Animals")
        val expectedAllowed = preferences.filter { poi -> poi.category.none { it.equals("Animals", ignoreCase = true) } }
        val graph = GraphBuilder().buildGraph(dummyPois)

        val chainedDijkstra = ChainedDijkstra()
        val path = chainedDijkstra.findPath(graph, preferences, disinterests = disinterests)

        println("chainedDijkstra (disinterests=$disinterests) produced path = $path")

        // Ensure disliked categories are not present
        assertFalse(path.any { poi -> poi.category.any { cat -> cat.equals("Animals", ignoreCase = true) } })

        // Path should contain exactly the allowed preferences in order
        assertEquals(expectedAllowed.size, path.size)
        assertEquals(expectedAllowed, path)
    }

    @Test
    fun testFindPathRespectsDislikedPoiIds() {
        val dummyPois = listOf(
            PoiEntity(
                poiId = 1L, 
                name = "Park", 
                description = "", 
                category = listOf("Nature"), 
                latitude = 40.748817, 
                longitude = -73.985428
            ),
            PoiEntity(
                poiId = 2L, 
                name = "Gallery", 
                description = "", 
                category = listOf("Art"), 
                latitude = 40.761431, 
                longitude = -73.977621
            ),
            PoiEntity(
                poiId = 3L, 
                name = "Theater", 
                description = "", 
                category = listOf("Entertainment"), 
                latitude = 40.759776, 
                longitude = -73.984018
            )
        )

        val preferences = dummyPois
        val dislikedPoiIds = setOf(2L) // Dislike Gallery
        val graph = GraphBuilder().buildGraph(dummyPois)

        val chainedDijkstra = ChainedDijkstra()
        val path = chainedDijkstra.findPath(graph, preferences, dislikedPoiIds = dislikedPoiIds)

        println("chainedDijkstra (dislikedPoiIds=$dislikedPoiIds) produced path = $path")

        // Ensure disliked POI is not present
        assertFalse(path.any { it.poiId == 2L })

        // Path should contain only allowed preferences
        assertEquals(2, path.size)
        assertTrue(path.contains(dummyPois[0])) // Park
        assertTrue(path.contains(dummyPois[2])) // Theater
    }
}
