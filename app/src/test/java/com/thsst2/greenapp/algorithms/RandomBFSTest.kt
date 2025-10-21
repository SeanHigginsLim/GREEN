package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class RandomBFSTest {

    @Test
    fun testFindPathReturnsAllPois_NoDislikes() {
        val dummyPois = listOf(
            PoiEntity(
                poiId = 1,
                name = "Central Park",
                description = "A large public park.",
                category = listOf("Nature"),
                latitude = 40.785091,
                longitude = -73.968285
            ),
            PoiEntity(
                poiId = 2,
                name = "Museum of Art",
                description = "A famous art museum.",
                category = listOf("Art"),
                latitude = 40.779437,
                longitude = -73.963244
            ),
            PoiEntity(
                poiId = 3,
                name = "City Zoo",
                description = "A popular zoo.",
                category = listOf("Animals"),
                latitude = 40.767778,
                longitude = -73.971834
            ),
            PoiEntity(
                poiId = 4,
                name = "Botanical Garden",
                description = "Greenhouse and plant collections.",
                category = listOf("Nature", "Education"),
                latitude = 40.667622,
                longitude = -73.962475
            ),
            PoiEntity(
                poiId = 5,
                name = "Historic Theater",
                description = "An old downtown theater hosting plays and concerts.",
                category = listOf("Entertainment", "History"),
                latitude = 40.712776,
                longitude = -74.005974
            ),
            PoiEntity(
                poiId = 6,
                name = "Science Center",
                description = "Interactive science exhibits for all ages.",
                category = listOf("Education"),
                latitude = 40.730610,
                longitude = -73.935242
            ),
            PoiEntity(
                poiId = 7,
                name = "Riverside Walk",
                description = "Scenic walking path along the river.",
                category = listOf("Nature", "Recreation"),
                latitude = 40.800537,
                longitude = -73.958241
            ),
            PoiEntity(
                poiId = 8,
                name = "City Aquarium",
                description = "Marine life exhibits and shows.",
                category = listOf("Animals", "Education"),
                latitude = 40.574926,
                longitude = -73.989308
            )
        )

        val randomBFS = RandomBFS()
        val path = randomBFS.findPath(dummyPois, disinterests = emptyList())

        println("RandomBFS produced path = $path")

        // Check that all POIs are visited
        assertEquals(dummyPois.size, path.size)
        assertTrue(path.containsAll(dummyPois))
    }

    @Test
    fun testFindPathEmptyInput_NoDislikes() {
        val randomBFS = RandomBFS()
        val path = randomBFS.findPath(emptyList(), disinterests = emptyList())
        println("RandomBFS produced path = $path")
        assertTrue(path.isEmpty())
    }

    @Test
    fun testFindPathRespectsCategoryDislikes() {
        val dummyPois = listOf(
            PoiEntity(poiId = 1, name = "Park", description = "", category = listOf("Nature"), latitude = 0.0, longitude = 0.0),
            PoiEntity(poiId = 2, name = "Gallery", description = "", category = listOf("Art"), latitude = 0.0, longitude = 0.0),
            PoiEntity(poiId = 3, name = "Zoo", description = "", category = listOf("Animals"), latitude = 0.0, longitude = 0.0),
            PoiEntity(poiId = 4, name = "Theater", description = "", category = listOf("Entertainment"), latitude = 0.0, longitude = 0.0),
            PoiEntity(poiId = 5, name = "Aquarium", description = "", category = listOf("Animals", "Education"), latitude = 0.0, longitude = 0.0)
        )

        val disinterests = listOf("Animals")
        val expectedAllowed = dummyPois.filter { poi -> poi.category.none { it.equals("Animals", ignoreCase = true) } }

        val randomBFS = RandomBFS()
        val path = randomBFS.findPath(dummyPois, disinterests = disinterests)

        println("RandomBFS (disinterests=$disinterests) produced path = $path")

        // Ensure disliked categories are not present
        assertFalse(path.any { poi -> poi.category.any { cat -> cat.equals("Animals", ignoreCase = true) } })

        // Path should contain exactly the allowed POIs
        assertEquals(expectedAllowed.size, path.size)
        assertTrue(path.containsAll(expectedAllowed))
    }
}