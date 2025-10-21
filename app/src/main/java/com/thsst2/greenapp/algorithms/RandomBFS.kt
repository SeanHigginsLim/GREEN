package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import kotlin.random.Random

class RandomBFS {

    /**
     * Find a random BFS-like ordering over POIs while excluding POIs whose
     * categories match any of the user's disinterests (case-insensitive).
     *
     * Calling code can pass an empty collection to keep all POIs.
     */
    fun findPath(
        allPois: List<PoiEntity>,
        disinterests: Collection<String> = emptyList()
    ): List<PoiEntity> {
        if (allPois.isEmpty()) return emptyList()

        val disSet: Set<String> = disinterests
            .map { it.lowercase().trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        // Filter out POIs whose any category matches a disinterest
        val allowedPois: List<PoiEntity> = if (disSet.isEmpty()) {
            allPois
        } else {
            allPois.filter { poi ->
                val poiCats = poi.category.map { it.lowercase().trim() }
                poiCats.none { it in disSet }
            }
        }

        if (allowedPois.isEmpty()) return emptyList()

        val visited = mutableSetOf<PoiEntity>()
        val path = mutableListOf<PoiEntity>()
        val queue = ArrayDeque<PoiEntity>()

        // pick a random start from allowed POIs
        val start: PoiEntity = allowedPois.shuffled(Random.Default).first()
        queue.add(start)
        visited.add(start)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            path.add(current)

            // treat remaining allowed POIs as neighbors in random order
            val neighbors: List<PoiEntity> = allowedPois.filter { it !in visited }.shuffled(Random.Default)
            for (neighbor in neighbors) {
                queue.add(neighbor)
                visited.add(neighbor)
            }
        }

        return path
    }
}