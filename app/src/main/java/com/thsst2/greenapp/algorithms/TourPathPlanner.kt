package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity

class TourPathPlanner() {

    // note: accept allPois so RandomBFS can operate
    fun planTour(
        allPois: List<PoiEntity>,
        startPoint: PoiEntity? = null,
        preferences: List<PoiEntity>? = null,
        ordered: Boolean = false
    ): List<PoiEntity> {
        return when {
            // No start and no preferences -> random BFS over all POIs
            startPoint == null && (preferences == null || preferences.isEmpty()) -> {
                RandomBFS().findPath(allPois)
            }

            // Preferences provided, no strict order -> multi-label A*
            preferences != null && preferences.isNotEmpty() && !ordered -> {
                MultiLabelAStar().findPath(preferences, allPois)
            }

            // Ordered preferences -> chained A*
            preferences != null && preferences.isNotEmpty() && ordered -> {
                ChainedAStar().findPath(preferences)
            }

            else -> {
                emptyList()
            }
        }
    }
}