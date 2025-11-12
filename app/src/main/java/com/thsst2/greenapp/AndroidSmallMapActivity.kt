package com.thsst2.greenapp
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.databinding.ActivityAndroidSmallMapBinding
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap


class AndroidSmallMapActivity : AppCompatActivity(), OnMapReadyCallback {

	private lateinit var mapBinding: ActivityAndroidSmallMapBinding
	private lateinit var mMap: GoogleMap

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mapBinding = ActivityAndroidSmallMapBinding.inflate(layoutInflater)
		setContentView(mapBinding.root)

		val mapFragment = supportFragmentManager
			.findFragmentById(R.id.map_fragment) as SupportMapFragment
		mapFragment.getMapAsync(this)

		setupNavigationBar()
	}

	override fun onMapReady(googleMap: GoogleMap) {
		mMap = googleMap

		mMap.uiSettings.isZoomControlsEnabled = true
		mMap.uiSettings.isScrollGesturesEnabled = true
		mMap.uiSettings.isZoomGesturesEnabled = true

		syncWithHomeMap()
	}

	private fun syncWithHomeMap() {
		mMap.clear()
		MapState.pois.forEach { poi ->
			mMap.addMarker(
				MarkerOptions()
					.position(LatLng(poi.latitude, poi.longitude))
					.title(poi.name)
			)
		}
		if (MapState.pathLatLngs.isNotEmpty()) {
			mMap.addPolyline(
				PolylineOptions()
					.addAll(MapState.pathLatLngs)
					.width(6f)
					.color(Color.BLUE)
					.pattern(listOf(Dot(), Gap(10f), Dash(20f)))
			)
			val bounds = LatLngBounds.Builder()
			MapState.pathLatLngs.forEach { bounds.include(it) }
			mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80))
		}
		MapState.currentUserLatLng?.let {
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 18f))
		}
		mMap.setOnMarkerClickListener { marker ->
			val inside = MapState.currentPoiInside
			if (inside != null && inside.name == marker.title) {
				showFloorDialog(inside)
			}
			true
		}
	}

	private fun showFloorDialog(poi: PoiEntity) {
		val maxFloor = poi.floors ?: 1
		val floors = (1..maxFloor).map { "Floor $it" }.toTypedArray()

		AlertDialog.Builder(this)
			.setTitle("Select floor at ${poi.name}")
			.setItems(floors) { _, which ->
				MapState.selectedFloor = which + 1
				Toast.makeText(this, "Floor set to ${which + 1}", Toast.LENGTH_SHORT).show()
			}
			.setNegativeButton("Cancel", null)
			.show()
	}
	private fun setupNavigationBar() {
		// Home
		mapBinding.homeButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallHomeActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}

		// Trivia
		mapBinding.triviaButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallTriviaActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}

		// Map (Current Activity for AndroidSmallMapActivity)
		mapBinding.mapButton.setOnClickListener {
			recreate()
		}

		// Profile
		mapBinding.profileButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallProfileActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}
	}
}