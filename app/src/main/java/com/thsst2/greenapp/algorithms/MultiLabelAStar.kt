package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity

class MultiLabelAStar {

    /**
     * Minimal multi-label planner that:
     *  - keeps all preference POIs (in the order provided) that are not disliked
     *  - then appends remaining allowed POIs from allPois (if provided)
     *
     * Parameters for dislikes are optional so existing callers continue to work.
     */
    fun findPath(
        preferences: List<PoiEntity>,
        allPois: List<PoiEntity>? = null,
        dislikedPoiIds: Set<Int> = emptySet(),
        disinterests: Collection<String> = emptyList()
    ): List<PoiEntity> {
        val disSet: Set<String> = disinterests
            .map { it.lowercase().trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        fun allowed(p: PoiEntity): Boolean {
            if (p.poiId in dislikedPoiIds) return false
            if (disSet.isNotEmpty()) {
                val cats = p.category.map { it.lowercase().trim() }
                if (cats.any { it in disSet }) return false
            }
            return true
        }

        // keep preferences order but drop disliked ones
        val keptPrefs = preferences.filter { allowed(it) }

        // append remaining allowed POIs (not in keptPrefs) from allPois if provided
        val rest = allPois
            ?.filter { it !in keptPrefs && allowed(it) }
            ?: emptyList()

        return keptPrefs + rest
    }
}