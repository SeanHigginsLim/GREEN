package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.PoiGraph
import com.thsst2.greenapp.graph.Edge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests to verify that pathfinding algorithms actually compute shortest paths
 * and don't just connect POIs directly. These tests use graph structures where
 * the shortest path requires going through intermediate POIs.
 */
class ShortestPathVerificationTest {

    /**
     * Test ChainedDijkstra with a scenario where going through an intermediate POI
     * is required to reach the destination.
     * 
     * Graph structure:
     *   A --0.5--> C --0.5--> B
     *   (No direct A->B edge)
     * 
     * Shortest path from A to B MUST go through C.
     */
    @Test
    fun testChainedDijkstra_UsesIntermediatePOI() {
        val poiA = PoiEntity(
            poiId = "poi_a",
            generatedPathId = 0L,
            name = "Point A",
            description = "Start point",
            category = listOf("Start"),
            latitude = 40.7128,
            longitude = -74.0060
        )
        
        val poiC = PoiEntity(
            poiId = "poi_c",
            generatedPathId = 0L,
            name = "Point C (Intermediate)",
            description = "Intermediate point",
            category = listOf("Intermediate"),
            latitude = 40.7263,
            longitude = -74.0060
        )
        
        val poiB = PoiEntity(
            poiId = "poi_b",
            generatedPathId = 0L,
            name = "Point B",
            description = "End point",
            category = listOf("End"),
            latitude = 40.7398,
            longitude = -74.0060
        )

        val allPois = listOf(poiA, poiC, poiB)
        
        val knowledgeGraph = PoiGraph(
            nodes = allPois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_a" to listOf(Edge("e1", "poi_c", 0.5)),
                "poi_c" to listOf(
                    Edge("e2", "poi_a", 0.5),
                    Edge("e3", "poi_b", 0.5)
                ),
                "poi_b" to listOf(Edge("e4", "poi_c", 0.5))
            )
        )
        
        // User wants to go from A to B (in that order)
        val preferences = listOf(poiA, poiB)
        
        val chainedDijkstra = ChainedDijkstra()
        val path = chainedDijkstra.findPath(knowledgeGraph, preferences)
        
        println("ChainedDijkstra path from A to B: ${path.map { it.name }}")
        
        // Path should include intermediate POI C
        assertTrue("Path should contain intermediate POI C", path.contains(poiC))
        
        // Path should be: [A, C, B]
        assertEquals(3, path.size)
        assertEquals(poiA, path[0])
        assertEquals(poiC, path[1]) // Intermediate POI
        assertEquals(poiB, path[2])
    }

    /**
     * Test ChainedDijkstra with multiple preferences where intermediate POIs
     * are needed to create the shortest path.
     * 
     * Graph structure:
     *   A --0.3--> I1 --0.3--> B --0.3--> I2 --0.3--> C
     *   (No direct edges between preferences)
     * 
     * Path A -> B -> C should use intermediates I1 and I2.
     */
    @Test
    fun testChainedDijkstra_MultipleIntermediates() {
        val poiA = PoiEntity(
            poiId = "poi_a",
            generatedPathId = 0L,
            name = "A",
            description = "",
            category = listOf("Pref"),
            latitude = 40.7128,
            longitude = -74.0060
        )
        
        val intermediate1 = PoiEntity(
            poiId = "poi_i1",
            generatedPathId = 0L,
            name = "Intermediate 1",
            description = "",
            category = listOf("Helper"),
            latitude = 40.7218,
            longitude = -74.0060
        )
        
        val poiB = PoiEntity(
            poiId = "poi_b",
            generatedPathId = 0L,
            name = "B",
            description = "",
            category = listOf("Pref"),
            latitude = 40.7308,
            longitude = -74.0060
        )
        
        val intermediate2 = PoiEntity(
            poiId = "poi_i2",
            generatedPathId = 0L,
            name = "Intermediate 2",
            description = "",
            category = listOf("Helper"),
            latitude = 40.7398,
            longitude = -74.0060
        )
        
        val poiC = PoiEntity(
            poiId = "poi_c",
            generatedPathId = 0L,
            name = "C",
            description = "",
            category = listOf("Pref"),
            latitude = 40.7488,
            longitude = -74.0060
        )

        val allPois = listOf(poiA, intermediate1, poiB, intermediate2, poiC)
        
        val knowledgeGraph = PoiGraph(
            nodes = allPois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_a" to listOf(Edge("e1", "poi_i1", 0.3)),
                "poi_i1" to listOf(
                    Edge("e2", "poi_a", 0.3),
                    Edge("e3", "poi_b", 0.3)
                ),
                "poi_b" to listOf(
                    Edge("e4", "poi_i1", 0.3),
                    Edge("e5", "poi_i2", 0.3)
                ),
                "poi_i2" to listOf(
                    Edge("e6", "poi_b", 0.3),
                    Edge("e7", "poi_c", 0.3)
                ),
                "poi_c" to listOf(Edge("e8", "poi_i2", 0.3))
            )
        )
        
        // User wants: A -> B -> C
        val preferences = listOf(poiA, poiB, poiC)
        
        val chainedDijkstra = ChainedDijkstra()
        val path = chainedDijkstra.findPath(knowledgeGraph, preferences)
        
        println("ChainedDijkstra path A->B->C: ${path.map { it.name }}")
        
        // Path should include both intermediates
        assertTrue("Path should contain Intermediate 1", path.contains(intermediate1))
        assertTrue("Path should contain Intermediate 2", path.contains(intermediate2))
        
        // Path should be: [A, I1, B, I2, C]
        assertEquals(5, path.size)
        assertEquals(poiA, path[0])
        assertEquals(intermediate1, path[1])
        assertEquals(poiB, path[2])
        assertEquals(intermediate2, path[3])
        assertEquals(poiC, path[4])
    }

    /**
     * Test MultiGoalDijkstra to ensure it finds the optimal visiting order.
     * 
     * Graph structure:
     *   Start --0.5--> Intermediate --0.5--> End
     *   (No direct Start->End edge)
     * 
     * Preferences are Start and End (unordered).
     * Algorithm should determine an optimal visiting order based on graph weights.
     */
    @Test
    fun testMultiGoalDijkstra_ChoosesOptimalRoute() {
        val start = PoiEntity(
            poiId = "poi_start",
            generatedPathId = 0L,
            name = "Start",
            description = "",
            category = listOf("Pref"),
            latitude = 40.7128,
            longitude = -74.0060
        )
        
        val intermediate = PoiEntity(
            poiId = "poi_intermediate",
            generatedPathId = 0L,
            name = "Intermediate (Bridge)",
            description = "",
            category = listOf("Helper"),
            latitude = 40.7263,
            longitude = -74.0060
        )
        
        val end = PoiEntity(
            poiId = "poi_end",
            generatedPathId = 0L,
            name = "End",
            description = "",
            category = listOf("Pref"),
            latitude = 40.7398,
            longitude = -74.0060
        )

        val allPois = listOf(start, intermediate, end)
        
        val knowledgeGraph = PoiGraph(
            nodes = allPois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_start" to listOf(Edge("e1", "poi_intermediate", 0.5)),
                "poi_intermediate" to listOf(
                    Edge("e2", "poi_start", 0.5),
                    Edge("e3", "poi_end", 0.5)
                ),
                "poi_end" to listOf(Edge("e4", "poi_intermediate", 0.5))
            )
        )
        
        // User wants to visit Start and End (unordered)
        val preferences = listOf(start, end)
        
        val multiGoalDijkstra = MultiGoalDijkstra()
        val path = multiGoalDijkstra.findPath(knowledgeGraph, preferences)
        
        println("MultiGoalDijkstra optimal path: ${path.map { it.name }}")
        
        // MultiGoalDijkstra uses Dijkstra internally to calculate distances
        // and determine optimal visiting order for preferences
        assertTrue("Path should contain all POIs", path.size == 3)
        assertTrue("Path should contain all preferences", path.contains(start) && path.contains(end))
        
        // The algorithm finds optimal order based on graph weights
        assertTrue("Path should start with one of the preferences", 
            path[0] == start || path[0] == end)
    }

    /**
     * Test that ChainedDijkstra doesn't skip intermediate POIs even when
     * preferences are nearby but not directly connected.
     */
    @Test
    fun testChainedDijkstra_DoesNotSkipIntermediates() {
        // Create a linear chain: A - I - B
        // where A and B are preferences but need I to connect
        val poiA = PoiEntity(
            poiId = "poi_a",
            generatedPathId = 0L,
            name = "Preference A",
            description = "",
            category = listOf("Pref"),
            latitude = 40.7128,
            longitude = -74.0060
        )
        
        val intermediate = PoiEntity(
            poiId = "poi_intermediate",
            generatedPathId = 0L,
            name = "Bridge POI",
            description = "",
            category = listOf("Required"),
            latitude = 40.7218,
            longitude = -74.0060
        )
        
        val poiB = PoiEntity(
            poiId = "poi_b",
            generatedPathId = 0L,
            name = "Preference B",
            description = "",
            category = listOf("Pref"),
            latitude = 40.7308,
            longitude = -74.0060
        )

        val allPois = listOf(poiA, intermediate, poiB)
        
        val knowledgeGraph = PoiGraph(
            nodes = allPois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_a" to listOf(Edge("e1", "poi_intermediate", 0.3)),
                "poi_intermediate" to listOf(
                    Edge("e2", "poi_a", 0.3),
                    Edge("e3", "poi_b", 0.3)
                ),
                "poi_b" to listOf(Edge("e4", "poi_intermediate", 0.3))
            )
        )
        
        val preferences = listOf(poiA, poiB)
        
        val chainedDijkstra = ChainedDijkstra()
        val path = chainedDijkstra.findPath(knowledgeGraph, preferences)
        
        println("ChainedDijkstra with bridge POI: ${path.map { it.name }}")
        
        // Verify intermediate is included
        assertEquals("Path should have 3 POIs", 3, path.size)
        assertEquals(poiA, path[0])
        assertEquals(intermediate, path[1])
        assertEquals(poiB, path[2])
    }

    /**
     * Test with a graph where intermediate POIs are required.
     * 
     * Graph:  A--0.4--I1--0.4--C
     * 
     * No direct A->C edge, so must use I1
     */
    @Test
    fun testChainedDijkstra_ComplexGraph() {
        val poiA = PoiEntity(
            poiId = "poi_a",
            generatedPathId = 0L,
            name = "A",
            description = "",
            category = listOf("Pref"),
            latitude = 40.7128,
            longitude = -74.0060
        )
        
        val intermediate1 = PoiEntity(
            poiId = "poi_i1",
            generatedPathId = 0L,
            name = "I1 (Required bridge)",
            description = "",
            category = listOf("Helper"),
            latitude = 40.7263,
            longitude = -74.0060
        )
        
        val poiC = PoiEntity(
            poiId = "poi_c",
            generatedPathId = 0L,
            name = "C",
            description = "",
            category = listOf("Pref"),
            latitude = 40.7398,
            longitude = -74.0060
        )

        val allPois = listOf(poiA, intermediate1, poiC)
        
        val knowledgeGraph = PoiGraph(
            nodes = allPois.associateBy { it.poiId },
            adjacencyList = mapOf(
                "poi_a" to listOf(Edge("e1", "poi_i1", 0.4)),
                "poi_i1" to listOf(
                    Edge("e2", "poi_a", 0.4),
                    Edge("e3", "poi_c", 0.4)
                ),
                "poi_c" to listOf(Edge("e4", "poi_i1", 0.4))
            )
        )
        
        val preferences = listOf(poiA, poiC)
        
        val chainedDijkstra = ChainedDijkstra()
        val path = chainedDijkstra.findPath(knowledgeGraph, preferences)
        
        println("ChainedDijkstra complex graph: ${path.map { it.name }}")
        
        // Path should use intermediate since direct connection is blocked
        assertTrue("Path should have more than 2 POIs", path.size > 2)
        assertTrue("Path should start with A", path[0] == poiA)
        assertTrue("Path should end with C", path[path.size - 1] == poiC)
        
        // Should use I1 as bridge
        assertTrue("Path should use I1 (required bridge)", path.contains(intermediate1))
        assertEquals("Path should be A -> I1 -> C", 3, path.size)
    }
}
