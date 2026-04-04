package com.thsst2.greenapp
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.databinding.ActivityAndroidSmallMapBinding


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

		mapBinding.rm6mh7dlmac.setImageResource(R.drawable.white_home_page_icon)
		mapBinding.rr4suqnw5xu8.setImageResource(R.drawable.white_trivia_page)
		mapBinding.r4ygvniz63xa.setImageResource(R.drawable.black_map_page)
		mapBinding.resno30rwma6.setImageResource(R.drawable.white_profile_page)

		setupNavigationBar()

		val isTutorial = intent.getBooleanExtra("tutorial_mode", false)
		val step = intent.getIntExtra("tutorial_step", 0)

		if (isTutorial && step == 2) {
			TapTargetView.showFor(
				this,
				TapTarget.forView(
					mapBinding.rundefinedRlRl,
					"Google Map",
					"This is the Map Page, where you can view a larger version of the Home Page map."
				)
					.outerCircleColor(R.color.black)
					.targetCircleColor(android.R.color.white)
					.titleTextSize(20)
					.descriptionTextSize(16)
					.textColor(android.R.color.white)
					.dimColor(android.R.color.black)
					.cancelable(false)
					.transparentTarget(true)
			)

			Handler(Looper.getMainLooper()).postDelayed({
				val nextIntent = Intent(this, AndroidSmallProfileActivity::class.java)
				nextIntent.putExtra("tutorial_mode", true)
				nextIntent.putExtra("tutorial_step", 3)
				startActivity(nextIntent)
				finish()
			}, 3000)
		}
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