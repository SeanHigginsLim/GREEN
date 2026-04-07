package com.thsst2.greenapp.algorithms

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.graph.Edge
import com.thsst2.greenapp.graph.PoiGraph
import org.junit.Test
import kotlin.system.measureNanoTime

class PerformanceNestingTest {

    private val PREFERENCE_TO_TAGS: Map<String, List<String>> = mapOf(
        "wheelchair ramp"     to listOf("acc_ramp"),
        "elevator"            to listOf("acc_elevator"),
        "braille signs"       to listOf("acc_braille"),
        "accessible restroom" to listOf("acc_restroom"),
        "academic"            to listOf("fn_academic"),
        "administration"      to listOf("fn_admin"),
        "sports"              to listOf("fn_sports"),
        "cultural"            to listOf("fn_cultural"),
        "dining"              to listOf("fn_dining"),
        "worship"             to listOf("fn_worship"),
        "building"            to listOf("loc_building"),
        "facility"            to listOf("loc_facility"),
        "landmark"            to listOf("loc_landmark"),
        "open space"          to listOf("loc_open_space"),
        "food"                to listOf("loc_food"),
        "modern"              to listOf("type_modern"),
        "historic"            to listOf("type_historic"),
        "green building"      to listOf("type_green"),
        "study area"          to listOf("type_study")
    )

    private val USER_TYPE_TAG: Map<String, String?> = mapOf(
        "student" to "type_student",
        "faculty" to "type_faculty",
        "guest"   to "type_guest"
    )

    private data class CampusData(
        val allPois: List<PoiEntity>,
        val graph: PoiGraph
    )

    private val campusData: CampusData by lazy {
        val resource = javaClass.classLoader!!
            .getResourceAsStream("buildings/green-45704-default-rtdb-buildings-export.json")
            ?: error("buildings JSON not found in test resources")

        val jsonArray: JsonArray = JsonParser.parseReader(resource.reader()).asJsonArray

        val pois = mutableListOf<PoiEntity>()
        val adjacency = mutableMapOf<String, MutableList<Edge>>()
        val seenEdgeIds = mutableSetOf<String>()

        for (element in jsonArray) {
            val obj = element.asJsonObject

            val buildingId  = obj.get("building_id")?.asString ?: continue
            val name        = obj.get("name")?.asString ?: buildingId
            val description = obj.get("description")?.asString

            val coords = obj.getAsJsonObject("coordinates")
            val lat    = coords?.get("lat")?.asDouble ?: 0.0
            val lng    = coords?.get("lng")?.asDouble ?: 0.0
            val radius = obj.get("radius")?.asDouble ?: 20.0
            val floors = obj.get("floors")?.asInt

            fun tags(key: String) = obj.getAsJsonArray(key)
                ?.mapNotNull { it.asString } ?: emptyList()

            val category = tags("accessibility") +
                           tags("function_tags")  +
                           tags("location_tags")  +
                           tags("type_tags")

            pois += PoiEntity(
                poiId          = buildingId,
                generatedPathId = null,
                name           = name,
                description    = description,
                category       = category,
                latitude       = lat,
                longitude      = lng,
                radius         = radius,
                floors         = floors
            )

            adjacency.getOrPut(buildingId) { mutableListOf() }

            val edgesArray = obj.getAsJsonArray("edges") ?: continue
            for (edgeEl in edgesArray) {
                val e          = edgeEl.asJsonObject
                val edgeId     = e.get("edgeId")?.asString ?: continue
                val fromId     = e.get("from")?.asString ?: continue
                val toId       = e.get("to")?.asString ?: continue
                val weight     = e.get("w")?.asDouble ?: 1.0
                val bidir      = e.get("bidirectional")?.asBoolean ?: false

                if (edgeId in seenEdgeIds) continue
                seenEdgeIds += edgeId

                adjacency.getOrPut(fromId) { mutableListOf() }
                    .add(Edge(edgeId, toId, weight))

                if (bidir) {
                    adjacency.getOrPut(toId) { mutableListOf() }
                        .add(Edge("${edgeId}_rev", fromId, weight))
                }
            }
        }

        val graph = PoiGraph(
            nodes         = pois.associateBy { it.poiId },
            adjacencyList = adjacency
        )

        CampusData(pois, graph)
    }

    @Test
    fun testPerformance_UserType_Preference_Algorithm() {

        val (allPois, fullGraph) = campusData

        println("\nDataset loaded: ${allPois.size} real campus buildings, " +
                "${fullGraph.adjacencyList.values.sumOf { it.size }} directed edges")
        println("Buildings: ${allPois.map { it.name }}\n")

        data class PerfResult(
            val userType: String,
            val preference: String,
            val algorithm: String,
            val relevantPoiCount: Int,
            val preferencePoiCount: Int,
            val durationMs: Double,
            val pathLength: Int,
            val status: String
        )

        val results = mutableListOf<PerfResult>()

        for (userType in listOf("student", "faculty", "guest")) {

            val typeTag      = USER_TYPE_TAG[userType]
            val relevantPOIs = if (typeTag == null) allPois
                               else allPois.filter { typeTag in it.category }
            val allowedIds   = relevantPOIs.map { it.poiId }.toSet()

            val userGraph = PoiGraph(
                nodes         = fullGraph.nodes.filterKeys { it in allowedIds },
                adjacencyList = fullGraph.adjacencyList
                    .filterKeys { it in allowedIds }
                    .mapValues { (_, edges) -> edges.filter { it.to in allowedIds } }
            )

            for ((preference, fbTags) in PREFERENCE_TO_TAGS) {

                val prefPOIs = relevantPOIs.filter { poi ->
                    poi.category.any { cat -> cat in fbTags }
                }

                if (prefPOIs.isEmpty()) {
                    val status = if (allPois.none { poi -> poi.category.any { it in fbTags } })
                        "no_data" else "no_pois_for_user"
                    results += PerfResult(userType, preference, "RandomBFS",         0, 0, 0.0, 0, status)
                    results += PerfResult(userType, preference, "MultiGoalDijkstra", 0, 0, 0.0, 0, status)
                    continue
                }

                val startPOI = prefPOIs.first()

                var rbfsPath: List<PoiEntity> = emptyList()
                val rbfsNs = measureNanoTime {
                    rbfsPath = RandomBFS().findPath(userGraph, startPOI, emptySet())
                }
                results += PerfResult(
                    userType, preference, "RandomBFS",
                    relevantPOIs.size, prefPOIs.size, rbfsNs / 1_000_000.0, rbfsPath.size, "ok"
                )

                var dijkstraPath: List<PoiEntity> = emptyList()
                val dijkstraNs = measureNanoTime {
                    dijkstraPath = MultiGoalDijkstra().findPath(userGraph, prefPOIs, startPOI, emptySet())
                }
                results += PerfResult(
                    userType, preference, "MultiGoalDijkstra",
                    relevantPOIs.size, prefPOIs.size, dijkstraNs / 1_000_000.0, dijkstraPath.size, "ok"
                )
            }
        }

        val ok     = results.filter { it.status == "ok" }
        val sorted = ok.sortedByDescending { it.durationMs }

        val c = intArrayOf(11, 22, 20, 9, 10, 8, 13)
        val header = "%-${c[0]}s | %-${c[1]}s | %-${c[2]}s | %${c[3]}s | %${c[4]}s | %${c[5]}s | %${c[6]}s"
            .format("User Type", "Preference", "Algorithm", "Rel.POIs", "Pref.POIs", "PathLen", "Duration(ms)")
        val bar = "=".repeat(header.length)
        val sep = "-".repeat(header.length)

        println("\n$bar")
        println("  PERFORMANCE RESULTS — real campus data  (slowest ► fastest)")
        println(bar)
        println(header)
        println(sep)
        for (r in sorted) {
            println(
                "%-${c[0]}s | %-${c[1]}s | %-${c[2]}s | %${c[3]}d | %${c[4]}d | %${c[5]}d | %${c[6]}.4f"
                    .format(r.userType, r.preference, r.algorithm,
                            r.relevantPoiCount, r.preferencePoiCount, r.pathLength, r.durationMs)
            )
        }

        println("\n--- TOP 10 SLOWEST COMBINATIONS ---")
        sorted.take(10).forEachIndexed { i, r ->
            println(
                "%2d. [%-8s] %-22s via %-20s → %8.4f ms  (rel=%d, pref=%d, path=%d)"
                    .format(i + 1, r.userType, r.preference, r.algorithm,
                            r.durationMs, r.relevantPoiCount, r.preferencePoiCount, r.pathLength)
            )
        }

        println("\n--- AVERAGE DURATION BY ALGORITHM ---")
        ok.groupBy { it.algorithm }
            .map { (algo, list) -> algo to list.map { it.durationMs }.average() }
            .sortedByDescending { it.second }
            .forEach { (algo, avg) ->
                println("  %-22s avg = %8.4f ms".format(algo, avg))
            }

        println("\n--- AVERAGE DURATION BY USER TYPE ---")
        ok.groupBy { it.userType }
            .map { (ut, list) -> ut to list.map { it.durationMs }.average() }
            .sortedByDescending { it.second }
            .forEach { (ut, avg) ->
                println("  %-12s avg = %8.4f ms".format(ut, avg))
            }

        println("\n--- AVERAGE DURATION BY PREFERENCE (slowest first) ---")
        ok.groupBy { it.preference }
            .map { (pref, list) -> pref to list.map { it.durationMs }.average() }
            .sortedByDescending { it.second }
            .forEach { (pref, avg) ->
                println("  %-22s avg = %8.4f ms".format(pref, avg))
            }

        val noData = results.filter { it.status == "no_data" }
            .map { it.preference }.distinct()
        if (noData.isNotEmpty()) {
            println("\n--- PREFERENCES WITH NO MATCHING BUILDINGS IN EXPORT ---")
            noData.forEach { println("  $it") }
        }

        val noAccess = results.filter { it.status == "no_pois_for_user" }
            .distinctBy { it.userType to it.preference }
        if (noAccess.isNotEmpty()) {
            println("\n--- PREFERENCES BLOCKED BY USER TYPE (buildings exist but user can't see them) ---")
            noAccess.forEach { r -> println("  [${r.userType}]  ${r.preference}") }
        }

        println()
    }
}
