package com.thsst2.greenapp

import com.google.android.gms.maps.model.LatLng
import com.thsst2.greenapp.data.PoiEntity

object MapState {
    var pois: List<PoiEntity> = emptyList()
    var pathLatLngs: List<LatLng> = emptyList()
    var currentUserLatLng: LatLng? = null
    var currentPoiInside: PoiEntity? = null
    var selectedFloor: Int? = null
}
