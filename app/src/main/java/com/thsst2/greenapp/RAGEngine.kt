    package com.thsst2.greenapp

    import android.R
    import android.util.Log
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseReference
    import com.thsst2.greenapp.data.PoiEntity

    import com.google.firebase.database.FirebaseDatabase
    import com.google.gson.Gson
    import com.thsst2.greenapp.data.TransitionEntity
    import com.thsst2.greenapp.data.UserPreferencesEntity
    import com.thsst2.greenapp.data.UserRoleEntity
    import kotlinx.coroutines.tasks.await
    import kotlin.Long

    class RAGEngine {
        private val db = FirebaseDatabase.getInstance().reference
        private val gson = Gson()

        // Maps each preference name to their corresponding ID
        suspend fun mapPreferencesToTagNames(preferences: List<String>?): List<String> {
            if (preferences.isNullOrEmpty()) return emptyList()

            val annotationTagsSnapshot = db
                .child("server_side")
                .child("annotation_tags")
                .get()
                .await()

            val matchedTagIds = mutableListOf<String>()

            for (tagTypeSnapshot in annotationTagsSnapshot.children) {
                for(preferencesChoiceSnapshot in tagTypeSnapshot.children){
                    try {
                        Log.d("RAGEngine", preferencesChoiceSnapshot.value.toString())
                        val label = preferencesChoiceSnapshot.child("label").getValue(String::class.java)

                        var tagId: String? = null

                        // Find key ending with "tag_id"
                        for (child in preferencesChoiceSnapshot.children) {
                            if (child.key?.endsWith("tag_id") == true) {
                                tagId = child.getValue(String::class.java)
                                break
                            }
                        }

                        if (!label.isNullOrEmpty() &&
                            !tagId.isNullOrEmpty() &&
                            preferences.contains(label)
                        ) {
                            matchedTagIds.add(tagId)
                            Log.d("RAGEngine", "Matched '$label' -> tag_id: $tagId")
                        }
                    } catch (e: Exception) {
                        Log.d("RAGEngine", "Error parsing preferencesChoices: ${e.message}")
                    }
                }
            }

            return matchedTagIds
        }

        // Return list of preference choices
        suspend fun getPreferencesListForProfilePage(): List<String> {
            val annotationTagsSnapshot = db
                .child("server_side")
                .child("annotation_tags")
                .get()
                .await()

            val buildingsSnapshot = db
                .child("server_side")
                .child("pre_collected_data")
                .child("building")
                .get()
                .await()

            val preferencesChoices = mutableListOf<String>()

            for (tagTypeSnapshot in annotationTagsSnapshot.children) {
                // Skip interaction_tags
                if (tagTypeSnapshot.key == "interaction_tags") {
                    Log.d("RAGEngine", "Skipping interaction_tags")
                    continue
                }

                for(preferencesChoiceSnapshot in tagTypeSnapshot.children){
                    try {
                        Log.d("RAGEngine", preferencesChoiceSnapshot.value.toString())
                        val label = preferencesChoiceSnapshot
                            .child("label")
                            .getValue(String::class.java)

                        if (label != null) {
                            preferencesChoices.add(label)
                        }

                        Log.d("RAGEngine", "Added preference: $label for preferences $preferencesChoices")
                    } catch (e: Exception) {
                        Log.d("RAGEngine", "Error parsing preferencesChoices: ${e.message}")
                    }
                }
            }

            for (buildingSnapshot in buildingsSnapshot.children) {
                try {
                    Log.d("RAGEngine", buildingSnapshot.value.toString())
                    val name = buildingSnapshot
                        .child("name")
                        .getValue(String::class.java)

                    if (name != null) {
                        preferencesChoices.add(name)
                    }

                    Log.d("RAGEngine", "Added preference: $name for preferences $preferencesChoices")
                } catch (e: Exception) {
                    Log.d("RAGEngine", "Error parsing preferencesChoices: ${e.message}")
                }
            }

            return preferencesChoices
        }

        // Return list of roles
        suspend fun getRoleList(): List<String> {
            val rolesSnapshot = db
                .child("server_side")
                .child("annotation_tags")
                .child("user_role_tags")
                .get()
                .await()

            val roles = mutableListOf<String>()

            for (role in rolesSnapshot.children) {
                try {
                    Log.d("RAGEngine", "${role.child("user_role_tag_id").getValue(String::class.java)}")
                    // Extract roles
                    val label = role.child("label").getValue(String::class.java) ?: continue

                    roles.add(label)
                    Log.d("RAGEngine", "Added role: $label for roles $roles")
                } catch (e: Exception) {
                    Log.e("RAGEngine", "Error parsing roles: ${e.message}")
                }
            }

            return roles
        }

        // Return POIs from the Database that match the user’s preferences (Match tags or ids to return POI information)
        suspend fun getRelevantPOINames(preferences: List<String>?): List<PoiEntity> {
            if (preferences.isNullOrEmpty()) return emptyList()

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
                    val accessibility = building.child("accessibility").children.mapNotNull { it.value as? String }
                    val poiId = building.child("building_id").getValue(String::class.java) ?: continue
                    val name = building.child("name").getValue(String::class.java) ?: ""
                    val description = building.child("description").getValue(String::class.java) ?: ""

                    // Extract tags
                    val functionTags = building.child("function_tags").children.mapNotNull { it.value as? String }
                    val locationTags = building.child("location_tags").children.mapNotNull { it.value as? String }
                    val typeTags = building.child("type_tags").children.mapNotNull { it.value as? String }

                    val allTags = accessibility + functionTags + locationTags + typeTags

                    // Check if any tag matches the user's preferences
                    val matches = preferences.any { pref ->
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

        // Call recursiveFetchMatchingNodes (For Generating Tour Overview)
        suspend fun getData(poiIds: List<String>, preferences: List<String>?): String {
            Log.d("RAGEngine", "Fetching data for POIs: $poiIds")
            Log.d("RAGEngine", "Fetching data for Preferences: $preferences")
            val matchedData = recursiveFetchMatchingNodes(
                ref = db.child("server_side").child("pre_collected_data").child("buildings"),
                preferences = preferences,
                poiIds = poiIds
            )

            // Number of matched nodes
            Log.d("RAGEngine", "Matched nodes count: ${matchedData.size}")

            val jsonResult = gson.toJson(matchedData)
            return jsonResult
        }

        // Create one for Hector get lat and lon
        // pass israndom
        // get tags with student
        // Get relevant data based on tags
        suspend fun recursiveFetchMatchingNodes(
            ref: DatabaseReference,
            preferences: List<String>?,
            poiIds: List<String>,
            matched: MutableList<Map<String, Any?>> = mutableListOf(),
            seenIds: MutableSet<String> = mutableSetOf()
        ): List<Map<String, Any?>> {
            val snapshot = ref.get().await()
            if (!snapshot.exists()) return matched

            for (childSnapshot in snapshot.children) {
                // If this snapshot has children, recurse
                if (childSnapshot.hasChildren()) {
                    recursiveFetchMatchingNodes(
                        ref = childSnapshot.ref,
                        preferences = preferences,
                        poiIds = poiIds,
                        matched = matched,
                        seenIds = seenIds
                    )
                }

                // Check current snapshot for matching data
                for (data in childSnapshot.children) {
                    val dataValue = data.getValue(String::class.java) ?: continue

                    // Check if this data matches any user preference
                    val preferenceMatch = preferences?.any { it.equals(dataValue, ignoreCase = true) } == true

                    // Check if this data matches any POI ID
                    val poiMatch = poiIds.any { it.equals(dataValue, ignoreCase = true) }

                    if (preferenceMatch || poiMatch) {
                        val buildingId = childSnapshot.child("building_id").getValue(String::class.java)
                        if (buildingId != null && !seenIds.contains(buildingId)) {
                            // This building/data matches — add to your matched list
                            val parentData: Map<String, Any?> = childSnapshot.children.associate {
                                val key = it.key ?: ""
                                val value = it.value
                                key to value
                            }
                            matched.add(parentData)
                            seenIds.add(buildingId)
                        }
                    }
                }
            }

            return matched
        }

        // Call recursiveFilterPoiData (For answering User Queries)
        suspend fun filterPoiData(relevantTags: List<String>?): String {
            Log.d("RAGEngine", "Fetching data for Relevant Tags: $relevantTags")
            val matchedData = recursiveFilterPoiData(
                ref = db.child("server_side").child("pre_collected_data"),
                relevantTags = relevantTags
            )

            // Number of matched nodes
            Log.d("RAGEngine", "Matched nodes count: ${matchedData.size}")

            val jsonResult = gson.toJson(matchedData)
            return jsonResult
        }

        // Get specific query data using relevant tags
        suspend fun recursiveFilterPoiData(
            ref: DatabaseReference,
            relevantTags: List<String>?,
            matched: MutableList<Map<String, Any?>> = mutableListOf(),
            seenIds: MutableSet<String> = mutableSetOf()
        ): List<Map<String, Any?>> {
            val snapshot = ref.get().await()
            if (!snapshot.exists()) return matched

            for (childSnapshot in snapshot.children) {
                // If this snapshot has children, recurse
                if (childSnapshot.hasChildren()) {
                    recursiveFilterPoiData(
                        ref = childSnapshot.ref,
                        relevantTags = relevantTags,
                        matched = matched,
                        seenIds = seenIds
                    )
                }

                // Check current snapshot for matching data
                for (data in childSnapshot.children) {
                    val dataValue = data.getValue(String::class.java) ?: continue

                    // Check if this data matches any user preference
                    val relevantTagMatch = relevantTags?.any { it.equals(dataValue, ignoreCase = true) } == true

                    if (relevantTagMatch) {
                        val parentData: Map<String, Any?> = childSnapshot.children.associate {
                            val key = it.key ?: ""
                            val value = it.value
                            key to value
                        }

                        // Create a hash using JSON string
                        val parentHash = Gson().toJson(parentData)

                        if (!seenIds.contains(parentHash)) {
                            matched.add(parentData)
                            seenIds.add(parentHash)
                        }
                    }
                }
            }

            return matched
        }

        // Get associated floor data upon selecting floor
        suspend fun getFloorData(floor: Number, poiId: String): String {
            Log.d("RAGEngine", "Fetching data for Floor: $floor")
            Log.d("RAGEngine", "Fetching data for Point of Interest: $poiId")
            val matchedData = recursiveGetFloorData(
                ref = db.child("server_side").child("pre_collected_data").child("floor_functions"),
                floor = floor,
                poiId = poiId
            )

            // Number of matched nodes
            Log.d("RAGEngine", "Matched nodes count: ${matchedData?.size}")

            val jsonResult = gson.toJson(matchedData)
            return jsonResult
        }

        suspend fun recursiveGetFloorData(
            ref: DatabaseReference,
            floor: Number,
            poiId: String,
        ): Map<String, Any?>? {
            val snapshot = ref.get().await()
            if (!snapshot.exists()) return null

            // Iterate over children
            for (childSnapshot in snapshot.children) {
                val poiIdMatch = childSnapshot.children.any { data ->
                    val dataValue = data.getValue(String::class.java)
                    data.key == "poiId" && dataValue == poiId
                }

                // Check for poiId match
                if (poiIdMatch) {
                    // Now loop through the children to match the floor number
                    for (data in childSnapshot.children) {
                        val dataValue = data.getValue(String::class.java) ?: continue

                        val floorNumberMatch = try {
                            dataValue.toInt() == floor.toInt() - 1
                        } catch (e: Exception) {
                            false
                        }

                        // Check for floor number match if poiId matches
                        if (floorNumberMatch) {
                            // Return the snapshot's data as a map
                            return childSnapshot.children.associate {
                                val key = it.key ?: ""
                                val value = it.value
                                key to value
                            }
                        }
                    }
                    return null
                }

                val result = recursiveGetFloorData(
                    ref = childSnapshot.ref,
                    floor = floor,
                    poiId = poiId
                )
                if (result != null) return result
            }

            return null
        }

        data class Edge(
            val edgeId: String,
            val to: String,
            val weight: Double
        )

        // Return knowledge graph as adjacency list
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

        // Return list of all buildings (For displaying geofences and distance measurement)
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

                    // Extract radius for geofence
                    val rad = building.child("radius").getValue(Double::class.java) ?: 0.0

                    // Extract floors
                    val floors = building.child("floors").getValue(Int::class.java) ?: 0

                    // Create POI entity and add to list
                    val poi = PoiEntity(
                        poiId = poiId,
                        generatedPathId = null,
                        name = name,
                        description = description,
                        category = category,
                        latitude = lat,
                        longitude = lng,
                        radius = rad,
                        floors = floors
                    )
                    pois.add(poi)

                } catch (e: Exception) {
                    Log.e("RAGEngine", "Error parsing building: ${e.message}")
                }
            }

            return pois
        }

        // Return list of transition areas
        suspend fun getTransitions(): List<TransitionEntity> {
            val transitionsSnapshot = db
                .child("server_side")
                .child("pre_collected_data")
                .child("transition_areas")
                .get()
                .await()
            val transitions = mutableListOf<TransitionEntity>()

            // Loop through all buildings
            for (transition in transitionsSnapshot.children) {
                try {
                    Log.d("RAGEngine", "${transition.child("building_id").getValue(String::class.java)}")

                    // Extract coordinates
                    val lat = transition.child("latitude").getValue(Double::class.java) ?: 0.0
                    val lng = transition.child("longitude").getValue(Double::class.java) ?: 0.0

                    // Extract radius for geofence
                    val rad = transition.child("radius").getValue(Double::class.java) ?: 0.0


                    // Create Transition entity and add to list
                    val transition = TransitionEntity(
                        latitude = lat,
                        longitude = lng,
                        radius = rad,
                    )
                    transitions.add(transition)

                } catch (e: Exception) {
                    Log.e("RAGEngine", "Error parsing building: ${e.message}")
                }
            }

            return transitions
        }

        // Return floor functions data
        suspend fun getFloorFunctions(floor: String): List<String> {
            val floorFunctionsSnapshot = db
                .child("server_side")
                .child("pre_collected_data")
                .child("floor_functions")
                .get()
                .await()
            val floorFunctions = mutableListOf<String>()

            // Loop through all buildings
            for (buildingSnapshot in floorFunctionsSnapshot.children) {
                for(buildingFunctionSnapshot in buildingSnapshot.children) {
                    try {
                        Log.d("RAGEngine", buildingFunctionSnapshot.value.toString())
                        val name = buildingFunctionSnapshot
                            .child("name")
                            .getValue(String::class.java)

                        if (name != null) {
                            if(floor.equals(name, ignoreCase = true)) {
                                floorFunctions.add(name)
                            }
                        }

                        Log.d("RAGEngine", "Added floor: $name for floor fumctions $buildingFunctionSnapshot")
                    } catch (e: Exception) {
                        Log.d("RAGEngine", "Error parsing floorFunctions: ${e.message}")
                    }
                }
            }

            return floorFunctions
        }
    }
