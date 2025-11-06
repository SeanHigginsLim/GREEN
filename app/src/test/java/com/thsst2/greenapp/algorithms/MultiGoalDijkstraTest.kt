package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.GraphBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class MultiGoalDijkstraTest {

    @Test
    fun testFindPathReturnsPreferencesFirst_NoDislikes() {
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

        val preferences = listOf(dummyPois[1], dummyPois[3]) // Museum, Science Center
        val graph = GraphBuilder().buildGraph(dummyPois)
        
        val multiGoalDijkstra = MultiGoalDijkstra()
        val path = multiGoalDijkstra.findPath(graph, preferences)

        println("multiGoalDijkstra produced path = $path")

        // Check that all POIs are visited and preferences come first
        assertEquals(dummyPois.size, path.size)
        assertEquals(dummyPois[1], path[0]) // Museum first
        assertEquals(dummyPois[3], path[1]) // Science Center second
        assertTrue(path.containsAll(dummyPois))
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
            ),
            PoiEntity(
                poiId = 2L, 
                name = "Gallery", 
                description = "", 
                category = listOf("Art"), 
                latitude = 40.761431, 
                longitude = -73.977621
            )
        )
        val graph = GraphBuilder().buildGraph(dummyPois)
        
        val multiGoalDijkstra = MultiGoalDijkstra()
        val path = multiGoalDijkstra.findPath(graph, emptyList())
        
        println("multiGoalDijkstra with empty preferences produced path = $path")
        
        // Should return all POIs when no preferences
        assertEquals(dummyPois.size, path.size)
        assertTrue(path.containsAll(dummyPois))
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

        val preferences = listOf(dummyPois[0], dummyPois[2]) // Park, Zoo
        val disinterests = listOf("Animals")
        val graph = GraphBuilder().buildGraph(dummyPois)

        val multiGoalDijkstra = MultiGoalDijkstra()
        val path = multiGoalDijkstra.findPath(graph, preferences, disinterests = disinterests)

        println("multiGoalDijkstra (disinterests=$disinterests) produced path = $path")

        // Ensure disliked categories are not present
        assertFalse(path.any { poi -> poi.category.any { cat -> cat.equals("Animals", ignoreCase = true) } })

        // Path should start with allowed preferences (only Park in this case)
        assertTrue(path.isNotEmpty())
        assertEquals(dummyPois[0], path[0]) // Park should be first
        
        // Should contain Park, Gallery, Theater but not Zoo or Aquarium
        assertEquals(3, path.size)
        assertTrue(path.contains(dummyPois[0])) // Park
        assertTrue(path.contains(dummyPois[1])) // Gallery
        assertTrue(path.contains(dummyPois[3])) // Theater
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

        val preferences = listOf(dummyPois[0], dummyPois[1]) // Park, Gallery
        val dislikedPoiIds = setOf(2L) // Dislike Gallery
        val graph = GraphBuilder().buildGraph(dummyPois)

        val multiGoalDijkstra = MultiGoalDijkstra()
        val path = multiGoalDijkstra.findPath(graph, preferences, dislikedPoiIds = dislikedPoiIds)

        println("multiGoalDijkstra (dislikedPoiIds=$dislikedPoiIds) produced path = $path")

        // Ensure disliked POI is not present
        assertFalse(path.any { it.poiId == 2L })

        // Path should contain Park first (from preferences), then Theater
        assertEquals(2, path.size)
        assertEquals(dummyPois[0], path[0]) // Park first
        assertTrue(path.contains(dummyPois[2])) // Theater
    }

    @Test
    fun testFindPathPreferencesOrder() {
        val dummyPois = listOf(
            PoiEntity(
                poiId = 1L, 
                name = "A", 
                description = "", 
                category = listOf("Nature"), 
                latitude = 40.748817, 
                longitude = -73.985428
            ),
            PoiEntity(
                poiId = 2L, 
                name = "B", 
                description = "", 
                category = listOf("Art"), 
                latitude = 40.761431, 
                longitude = -73.977621
            ),
            PoiEntity(
                poiId = 3L, 
                name = "C", 
                description = "", 
                category = listOf("Entertainment"), 
                latitude = 40.759776, 
                longitude = -73.984018
            ),
            PoiEntity(
                poiId = 4L, 
                name = "D", 
                description = "", 
                category = listOf("Education"), 
                latitude = 40.730610, 
                longitude = -73.935242
            )
        )

        val preferences = listOf(dummyPois[2], dummyPois[0], dummyPois[3]) // C, A, D
        val graph = GraphBuilder().buildGraph(dummyPois)

        val multiGoalDijkstra = MultiGoalDijkstra()
        val path = multiGoalDijkstra.findPath(graph, preferences)

        println("multiGoalDijkstra preferences order test produced path = $path")

        // Should maintain preference order at the beginning
        assertEquals(4, path.size)
        assertEquals(dummyPois[2], path[0]) // C first
        assertEquals(dummyPois[0], path[1]) // A second  
        assertEquals(dummyPois[3], path[2]) // D third
        assertEquals(dummyPois[1], path[3]) // B last (not in preferences)
    }
}
