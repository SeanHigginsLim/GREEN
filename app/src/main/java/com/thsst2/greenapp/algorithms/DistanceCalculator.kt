package com.thsst2.greenapp.algorithms

import android.location.Location
import com.thsst2.greenapp.data.PoiEntity

interface DistanceCalculator {
    fun calculateDistance(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double
    ): Float
}

class AndroidDistanceCalculator : DistanceCalculator {
    override fun calculateDistance(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(fromLat, fromLng, toLat, toLng, results)
        return results[0]
    }
}
