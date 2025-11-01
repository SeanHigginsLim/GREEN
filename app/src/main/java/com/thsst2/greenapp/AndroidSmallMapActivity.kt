package com.thsst2.greenapp
import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
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
import com.google.android.gms.maps.model.MarkerOptions
import com.thsst2.greenapp.databinding.ActivityAndroidSmallMapBinding


class AndroidSmallMapActivity : AppCompatActivity(), OnMapReadyCallback {

	private lateinit var mapBinding: ActivityAndroidSmallMapBinding
	private lateinit var mMap: GoogleMap
	private lateinit var fusedLocationClient: FusedLocationProviderClient
	private lateinit var locationCallback: LocationCallback
	private lateinit var geofencingClient: GeofencingClient

	// Temporary data for now (modify this for testing)
	private val poiList = listOf(
		LatLng(14.565072, 120.993073) to "Henry Sy Sr. Hall",
		LatLng(14.565503, 120.993258) to "Velasco Hall",
		LatLng(14.564171, 120.993834) to "St. La Salle Hall"
	)

	companion object {
		private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Set up view binding
		mapBinding = ActivityAndroidSmallMapBinding.inflate(layoutInflater)
		setContentView(mapBinding.root)

		// Load all images with glide
		//Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/4a89py0h_expires_30_days.png").into(findViewById(R.id.rundefined))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/6nk1t21m_expires_30_days.png").into(findViewById(R.id.rm6mh7dlmac))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/g6j8jjk1_expires_30_days.png").into(findViewById(R.id.rr4suqnw5xu8))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/6ylj9vbk_expires_30_days.png").into(findViewById(R.id.r4ygvniz63xa))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/ufno6m2e_expires_30_days.png").into(findViewById(R.id.resno30rwma6))

		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
		geofencingClient = LocationServices.getGeofencingClient(this)

		// Check location permission firs
		if (hasLocationPermission()) {
			initMap()
		} else {
			requestLocationPermission()
		}

		setupNavigationBar()
	}

	private fun hasLocationPermission(): Boolean {
		return ContextCompat.checkSelfPermission(
			this, Manifest.permission.ACCESS_FINE_LOCATION
		) == PackageManager.PERMISSION_GRANTED
	}

	private fun requestLocationPermission() {
		ActivityCompat.requestPermissions(
			this,
			arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
			LOCATION_PERMISSION_REQUEST_CODE
		)
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				initMap()
			} else {
				Toast.makeText(this, "Location permission required!", Toast.LENGTH_SHORT).show()
			}
		}
	}

	// Load map
	private fun initMap() {
		val mapFragment = supportFragmentManager
			.findFragmentById(R.id.map_fragment) as? SupportMapFragment
		mapFragment?.getMapAsync(this)
	}

	@SuppressLint("MissingPermission")
	override fun onMapReady(googleMap: GoogleMap) {
		mMap = googleMap

		if (ActivityCompat.checkSelfPermission(
				this,
				Manifest.permission.ACCESS_FINE_LOCATION
			) != PackageManager.PERMISSION_GRANTED
		) {
			ActivityCompat.requestPermissions(
				this,
				arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
				1
			)
			return
		}

		// Enable moving of map
		mMap.isMyLocationEnabled = true
		mMap.uiSettings.isZoomControlsEnabled = true
		mMap.uiSettings.isScrollGesturesEnabled = true
		mMap.uiSettings.isZoomGesturesEnabled = true
		mMap.uiSettings.isRotateGesturesEnabled = true

		// Add POI markers
		poiList.forEach { (location, name) ->
			mMap.addMarker(MarkerOptions().position(location).title(name))
		}

		// Start map w/ view of dlsu campus
		val dlsu = LatLng(14.5649, 120.9930)
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dlsu, 17f))

		// Start location tracking
		startLocationUpdates()
		// Add geofences for each POI
		addGeofences()
	}

	@SuppressLint("MissingPermission")
	private fun startLocationUpdates() {
		val locationRequest = LocationRequest.Builder(
			Priority.PRIORITY_HIGH_ACCURACY,
			5000L
		).build()

		locationCallback = object : LocationCallback() {
			override fun onLocationResult(locationResult: LocationResult) {
				super.onLocationResult(locationResult)
				for (location in locationResult.locations) {
					updateUserLocation(location)
				}
			}
		}

		fusedLocationClient.requestLocationUpdates(
			locationRequest,
			locationCallback,
			mainLooper
		)
	}

	private fun updateUserLocation(location: Location) {
		val currentLatLng = LatLng(location.latitude, location.longitude)
		mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng))
	}

	@SuppressLint("MissingPermission")
	private fun addGeofences() {
		val geofenceRadius = 50f // change value later on

		val geofences = poiList.map { (latLng, name) ->
			Geofence.Builder()
				.setRequestId(name)
				.setCircularRegion(latLng.latitude, latLng.longitude, geofenceRadius)
				.setExpirationDuration(Geofence.NEVER_EXPIRE)
				.setTransitionTypes(
					Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
				)
				.build()
		}

		val geofencingRequest = GeofencingRequest.Builder()
			.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
			.addGeofences(geofences)
			.build()

		// custom action for broadcast
		val intent = Intent("com.thsst2.greenapp.GEOFENCE_TRANSITION_ACTION")
		intent.setClass(this, GeofenceReceiver::class.java)

		val pendingIntent = PendingIntent.getBroadcast(
			this,
			0,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
		)

		geofencingClient.addGeofences(geofencingRequest, pendingIntent)
			.addOnSuccessListener {
				Toast.makeText(this, "Geofences added!", Toast.LENGTH_SHORT).show()
			}
			.addOnFailureListener {
				Toast.makeText(this, "Failed to add geofences: ${it.message}", Toast.LENGTH_SHORT).show()
				Log.e("GEOFENCE", "Add failed", it)
			}
	}

	override fun onDestroy() {
		super.onDestroy()
		fusedLocationClient.removeLocationUpdates(locationCallback)
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