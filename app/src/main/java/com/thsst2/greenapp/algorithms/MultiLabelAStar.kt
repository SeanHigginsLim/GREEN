package com.thsst2.greenapp.algorithms

import com.thsst2.greenapp.data.PoiEntity

class MultiLabelAStar {
    // temporary code used only to compile tests
    fun findPath(preferences: List<PoiEntity>, allPois: List<PoiEntity>? = null): List<PoiEntity> {
        val rest = allPois?.filter { it !in preferences } ?: emptyList()
        return preferences + rest
    }
}