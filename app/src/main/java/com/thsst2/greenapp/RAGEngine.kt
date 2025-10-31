package com.thsst2.greenapp

import com.thsst2.greenapp.data.PoiEntity

class RAGEngine {
    fun getRelevantPOIs(prefs: com.thsst2.greenapp.data.UserPreferencesEntity): List<PoiEntity> {
        val allPOIs = listOf(
            PoiEntity(1, 1, "DLSU Museum", "Arts & Culture", listOf("sean", "john"), 14.564, 120.993),
            PoiEntity(2, 1, "Agno Food Court", "Food", listOf("sean", "john"), 14.565, 120.992),
            PoiEntity(3, 1, "LS Building", "Academics", listOf("sean", "john"), 14.566, 120.993),
            PoiEntity(4, 1, "The Lagoon", "Green Spaces", listOf("sean", "john"), 14.564, 120.994)
        )

        return allPOIs.filter { poi ->
            prefs.interests.any { interest ->
                poi.category.contains(interest)
            }
        }
    }
}
