package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import kotlin.random.Random

class RandomBFS {

    fun findPath(allPois: List<PoiEntity>): List<PoiEntity> {
        if (allPois.isEmpty()) return emptyList()

        val visited = mutableSetOf<PoiEntity>()
        val path = mutableListOf<PoiEntity>()
        val queue = ArrayDeque<PoiEntity>()

        val start = allPois.random()
        queue.add(start)
        visited.add(start)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            path.add(current)

            // Treat all unvisited POIs as neighbors
            val neighbors = allPois.filter { it !in visited }.shuffled(Random.Default)

            for (neighbor in neighbors) {
                queue.add(neighbor)
                visited.add(neighbor)
            }
        }

        return path
    }
}