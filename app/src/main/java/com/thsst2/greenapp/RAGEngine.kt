    package com.thsst2.greenapp

    import android.util.Log
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseReference
    import com.thsst2.greenapp.data.PoiEntity

    import com.google.firebase.database.FirebaseDatabase
    import com.google.gson.Gson
    import kotlinx.coroutines.tasks.await

    class RAGEngine {
        private val db = FirebaseDatabase.getInstance().reference
        private val gson = Gson()
        fun mapPreferencesToTagNames(preferences: List<String>?): List<String> {
            val rawMapping = mapOf(
                // CATEGORY preferences
                "Functional Info" to listOf("campus_tags", "function_tags"),
                "Operational Info" to listOf("campus_tags", "function_tags"),

                // BUILDINGS
                "Henry Sy Sr. Hall" to listOf("B_HSSH"),
                "Brother Bloemen Hall" to listOf("B_BLOEMEN"),
                "Saint La Salle Hall" to listOf("B_LASALLE"),
                "Velasco Hall" to listOf("B_VELASCO"),
                "Enrique Razon Sports Center" to listOf("B_RAZON"),
                "Brother Andrew Gonzalez Hall" to listOf("B_ANDREW"),
                "Gokongwei Hall" to listOf("B_GOKO"),
    //            "Saint Mutien Marie Hall" to listOf(""),
                "Science and Technology Research Center" to listOf("B_STRC"),
    //            "Marian Quadrangle" to listOf(""),
    //            "Brother John Hall" to listOf(""),
                "Saint Joseph Hall" to listOf("B_SJH"),
                "Don Enrique Yuchengco Hall" to listOf("B_YUCHENGCO"),
                "Brother Connon Hall" to listOf("B_CONNON"),
                "Faculty Center" to listOf("B_FC"),
                "Brother William Hall" to listOf("B_WH"),
                "Saint Miguel Hall" to listOf("B_SMIG"),

                // FUNCTIONS AND SERVICES
    //            "Amphitheater" to listOf(""),
    //            "Open spaces" to listOf(""),
    //            "Relaxing" to listOf(""),
                "science" to listOf("campus_science"),
    //            "Mathematics" to listOf(""),
                "Engineering" to listOf("campus_engineering"),
                "Labs" to listOf("campus_labs"),
    //            "Library" to listOf(""),
    //            "Recreational" to listOf(""),
                "Accessibility" to listOf("acc_elevator", "acc_ramp"),
    //            "Parking" to listOf(""),
    //            "Drinking Fountain" to listOf(""),
                "Historical" to listOf("type_historic"),
                "Food" to listOf("Food and Dining"),
    //            "Warp Zones" to listOf(""),
    //            "Museum" to listOf(""),
    //            "Merchandise" to listOf(""),
    //            "Supplies" to listOf(""),
    //            "SDFO" to listOf(""),
                "Clinic" to listOf("health_services", "ERSC Clinic"),
                "Chapel" to listOf("B_LASALLE", "2F"),
                "Auditorium" to listOf("campus_auditorium"),
    //            "Entrances" to listOf(""),
                "CCS Building" to listOf("B_GOKO"),
                "CE Building" to listOf("B_VELASCO"),
                "COB Building" to listOf("B_YUCHENGCO"),
                "COS Building" to listOf("B_WH"),
                "CLA Building" to listOf("B_FC"),
    //            "COE Building" to listOf(""),
    //            "School of Economics Building" to listOf(""),
            )

            val mapping = rawMapping.mapKeys { it.key.lowercase().trim() }

            val result = mutableListOf<String>()

            preferences?.forEach { pref ->
                val key = pref.lowercase().trim()
                mapping[key]?.let { result.addAll(it) }
            }

            return result
        }

        /**
         * Fetches POIs from the Realtime Database that match the user’s preferences.
         */
        suspend fun getRelevantPOINames(preferences: List<String>?): List<PoiEntity> {
//            if (preferences.isNullOrEmpty()) return emptyList()

            val buildingsSnapshot = db
                .child("server_side")
                .child("pre_collected_data")
                .child("buildings")
                .get()
                .await()

            Log.d("RAGEngine", "Retrieved ${buildingsSnapshot.childrenCount} buildings from Firebase.")

            val pois = mutableListOf<PoiEntity>()


            Log.d("DEBUG", "All preferences: $preferences")
            // Loop through all buildings
            for (building in buildingsSnapshot.children) {
                try {
                    Log.d("RAGEngine", "${building.child("building_id").getValue(String::class.java)}")
                    val poiId = building.child("building_id").getValue(String::class.java) ?: continue
                    val name = building.child("name").getValue(String::class.java) ?: ""
                    val description = building.child("description").getValue(String::class.java) ?: ""

                    // Extract tags
                    val functionTags = building.child("function_tags").children.mapNotNull { it.value as? String }
                    val locationTags = building.child("location_tags").children.mapNotNull { it.value as? String }
                    val typeTags = building.child("type_tags").children.mapNotNull { it.value as? String }

                    val allTags = functionTags + locationTags + typeTags

                    // Check if any tag matches the user's preferences
                    val matches = preferences?.any { pref ->
                        poiId.contains(pref, ignoreCase = true) ||
                        allTags.any { tag -> tag.equals(pref, ignoreCase = true) }
                    }

                    matches?.let { if (!it) continue }  // Skip if no match found

                    // Extract coordinates
                    val geo = building.child("coordinates")
                    val lat = geo.child("lat").getValue(Double::class.java) ?: 0.0
                    val lng = geo.child("lng").getValue(Double::class.java) ?: 0.0

                    // Create POI entity and add to list
                    val poi = PoiEntity(
                        poiId = poiId,
                        generatedPathId = null,
                        name = name,
                        description = description,
                        category = allTags,
                        latitude = lat,
                        longitude = lng
                    )

                    pois.add(poi)
                    Log.d("RAGEngine", "Added matching POI: $name for preferences $preferences")
                } catch (e: Exception) {
                    Log.e("RAGEngine", "Error parsing building: ${e.message}")
                }
            }

            return pois
        }

        /**
         * Returns JSON data for all POIs with the given IDs.
         */
        suspend fun getData(poiIds: List<String>, preferences: List<String>?): String {
            Log.d("RAGEngine", "Fetching data for POIs: $poiIds")
            Log.d("RAGEngine", "Fetching data for POIs: $preferences")
            val matchedData = fetchMatchingNodesWithBacktrack(
                ref = db.child("server_side"),
                preferences = preferences,
                poiIds = poiIds
            )

            val jsonResult = gson.toJson(matchedData)
            return jsonResult
        }

        suspend fun fetchMatchingNodesWithBacktrack(
            ref: DatabaseReference,
            preferences: List<String>?,
            poiIds: List<String>,
            parent: DataSnapshot? = null,
            grandParent: DataSnapshot? = null,
            matched: MutableList<Map<String, Any?>> = mutableListOf()
        ): List<Map<String, Any?>> {
            val snapshot = ref.get().await()
            if (!snapshot.exists()) return matched

            for (child in snapshot.children) {
                // Extract tags
                val tags = child.child("function_tags").children.mapNotNull { it.value as? String } +
                        child.child("location_tags").children.mapNotNull { it.value as? String } +
                        child.child("type_tags").children.mapNotNull { it.value as? String }

                // Extract building_id
                val buildingId = child.child("building_id").getValue(String::class.java)

                // Tag match -> store grandparent
                if (tags.any { preferences?.contains(it) ?: false } && grandParent != null) {
                    val grandParentData: Map<String, Any?> = grandParent.children.associate {
                        val key = child.key ?: ""
                        val value: Any? = child.value  // cast explicitly
                        key to value
                    }
                    matched.add(grandParentData)
                }

                // Building ID match -> store parent
                if (buildingId != null && poiIds.contains(buildingId) && parent != null) {
                    val parentData: Map<String, Any?> = parent.children.associate {
                        val key = child.key ?: ""
                        val value: Any? = child.value  // cast explicitly
                        key to value
                    }
                    matched.add(parentData)
                }

                // Recursive, updating parent references
                fetchMatchingNodesWithBacktrack(
                    ref = child.ref,
                    preferences = preferences,
                    poiIds = poiIds,
                    parent = child,
                    grandParent = parent,
                    matched = matched
                )
            }

            return matched
        }

        data class Edge(
            val edgeId: String,
            val to: String,
            val weight: Double
        )

        suspend fun getKnowledgeGraph(): Map<String, List<Edge>> {
            val poiSnapshot = db.child("poi_nodes").get().await()

            // Adjacency list: POI ID -> list of edges
            val adjacencyList = mutableMapOf<String, MutableList<Edge>>()

            for (poi in poiSnapshot.children) {
                val poiId = poi.key ?: continue

                // Initialize adjacency list for this POI
                adjacencyList[poiId] = mutableListOf()

                // Traverse "adj" children
                val adjSnapshot = poi.child("adj")
                for (adjChild in adjSnapshot.children) {
                    val edgeId = adjChild.child("edge_id").getValue(String::class.java) ?: continue
                    val to = adjChild.child("to").getValue(String::class.java) ?: continue
                    val weight = adjChild.child("w").getValue(Double::class.java) ?: 0.0

                    val edge = Edge(edgeId = edgeId, to = to, weight = weight)
                    adjacencyList[poiId]?.add(edge)
                }
            }

            return adjacencyList
        }

        suspend fun getBuildings(): List<PoiEntity> {
            val buildingsSnapshot = db
                .child("server_side")
                .child("pre_collected_data")
                .child("buildings")
                .get()
                .await()
            val pois = mutableListOf<PoiEntity>()

            // Loop through all buildings
            for (building in buildingsSnapshot.children) {
                try {
                    Log.d("RAGEngine", "${building.child("building_id").getValue(String::class.java)}")
                    val poiId = building.child("building_id").getValue(String::class.java) ?: continue
                    val name = building.child("name").getValue(String::class.java) ?: ""
                    val description = building.child("description").getValue(String::class.java) ?: ""

                    // Extract tags
                    val functionTags =
                        building.child("function_tags").children.mapNotNull { it.value as? String }
                    val locationTags =
                        building.child("location_tags").children.mapNotNull { it.value as? String }
                    val typeTags =
                        building.child("type_tags").children.mapNotNull { it.value as? String }

                    // Combine all tag types into a single list
                    val category = functionTags + locationTags + typeTags

                    // Extract coordinates
                    val geo = building.child("coordinates")
                    val lat = geo.child("lat").getValue(Double::class.java) ?: 0.0
                    val lng = geo.child("lng").getValue(Double::class.java) ?: 0.0

                    val poi = PoiEntity(
                        poiId = poiId,
                        generatedPathId = null,
                        name = name,
                        description = description,
                        category = category,
                        latitude = lat,
                        longitude = lng
                    )
                } catch (e: Exception) {
                    Log.e("RAGEngine", "Error parsing building: ${e.message}")
                }
            }

            return pois
        }
    }
