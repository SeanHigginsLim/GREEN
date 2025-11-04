package com.thsst2.greenapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationCallback
import com.google.firebase.FirebaseApp
import com.thsst2.greenapp.data.*
import com.thsst2.greenapp.data.repositories.SessionRepository
import com.thsst2.greenapp.databinding.ActivityAndroidSmallHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.thsst2.greenapp.data.repositories.UserRepository
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLngBounds
import com.thsst2.greenapp.logic.TourCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidSmallHomeActivity : AppCompatActivity() {
	private lateinit var homeBinding: ActivityAndroidSmallHomeBinding
	private val messages = mutableListOf<String>()
	private lateinit var adapter: MyAdapter
	private lateinit var chatApi: ChatApi
	private lateinit var auth: FirebaseAuth
	private lateinit var sessionManager: SessionManager
	private lateinit var tourCoordinator: TourCoordinator
	private lateinit var dialogueManager: DialogueManager

	// DAOs
	private lateinit var db: MyAppDatabase
	private lateinit var userPreferencesDao: UserPreferencesDao
	private lateinit var userVisitedLocationDao: UserVisitedLocationDao
	private lateinit var generatedPathDao: GeneratedPathDao
	private lateinit var userTourPathHistoryDao: UserTourPathHistoryDao
	private lateinit var dialogueHistoryDao: DialogueHistoryDao
	private lateinit var userQueryDao: UserQueryDao
	private lateinit var intentLogDao: IntentLogDao
	private lateinit var geofenceTriggerDao: GeofenceTriggerDao
	private lateinit var pathDeviationAlertDao: PathDeviationAlertDao
	private lateinit var userSkippedOrDislikedLocationDao: UserSkippedOrDislikedLocationDao
	private lateinit var performanceMetricsDao: PerformanceMetricsDao
	private lateinit var userRoleDao: UserRoleDao
	private lateinit var userLocationDao: UserLocationDao
	private lateinit var userInteractionTimeDao: UserInteractionTimeDao

	// Temporary additional preferences during session
	private val tempAdditionalPreferences = mutableListOf<String>()

	// Session info
	private var sessionId: Long = 0
	private var userId: Long = 0

	// Geofencing
	private lateinit var fusedLocationClient: FusedLocationProviderClient
	private lateinit var locationCallback: LocationCallback
	private lateinit var geofencingClient: GeofencingClient

	private val GEOFENCE_RADIUS = 50f // modify later
	private val LOCATION_PERMISSION_CODE = 2001


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		homeBinding = ActivityAndroidSmallHomeBinding.inflate(layoutInflater)
		setContentView(homeBinding.root)

		FirebaseApp.initializeApp(this)
		auth = FirebaseAuth.getInstance()
		sessionManager = SessionManager(this)
		tourCoordinator = TourCoordinator(this)
		dialogueManager = DialogueManager(this)

		// Initialize DB & DAOs
		db = MyAppDatabase.getInstance(this)
		userPreferencesDao = db.userPreferencesDao()
		userVisitedLocationDao = db.userVisitedLocationDao()
		generatedPathDao = db.generatedPathDao()
		userTourPathHistoryDao = db.userTourPathHistoryDao()
		dialogueHistoryDao = db.dialogueHistoryDao()
		userQueryDao = db.userQueryDao()
		intentLogDao = db.intentLogDao()
		geofenceTriggerDao = db.geofenceTriggerDao()
		pathDeviationAlertDao = db.pathDeviationAlertDao()
		userSkippedOrDislikedLocationDao = db.userSkippedOrDislikedLocationDao()
		performanceMetricsDao = db.performanceMetricsDao()
		userRoleDao = db.userRoleDao()
		userLocationDao = db.userLocationDao()
		userInteractionTimeDao = db.userInteractionTimeDao()

		// Initialize map top fragment
		val mapFragment = supportFragmentManager.findFragmentById(R.id.home_map_fragment)
				as? SupportMapFragment ?: SupportMapFragment.newInstance()

		supportFragmentManager.beginTransaction()
			.replace(R.id.home_map_fragment, mapFragment)
			.commitAllowingStateLoss()
		

		// Authenticate User
		val currentUser = auth.currentUser
		if (currentUser == null || !sessionManager.isLoggedIn()) {
			startActivity(Intent(this, AndroidSmallLoginActivity::class.java))
			finish()
			return
		} else {
			Log.d("HomeActivity", "Logged in as: ${currentUser.email}")
		}

		userId = currentUser.uid.hashCode().toLong()
		Log.d("USER_ID", "Generated userId = $userId (from uid = ${currentUser.uid})")
		sessionId = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE

		val user = UserEntity(
			userId = userId
		)
		saveUserLocally(user)

		val sessionProfile = SessionEntity(
			sessionId = sessionId,
			userId = user.userId,
			componentsUsed = null,
			sessionStartedAt = System.currentTimeMillis().toString()
		)
		saveSessionLocally(sessionProfile)

		// TODO
			// problem: keeps redirectly user to profile creation even though user already did
				// uncomment when solved

//		// 🔹 NEW: Check if user has an existing profile
//		lifecycleScope.launch {
//			try {
//				// You can replace `userDao()` and `UserProfileDao` depending on your schema
//				val existingProfile = db.userRoleDao().getUserRoleById(userId)
//
//				if (existingProfile == null) {
//					// If profile is missing or incomplete → redirect to profile creation
//					Log.d("HomeActivity", "No profile found. Redirecting to Profile Creation...")
//
//					val intent = Intent(
//						this@AndroidSmallHomeActivity,
//						AndroidSmallProfileActivity::class.java // ⚠️ Make sure this Activity exists
//					)
//					intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//					startActivity(intent)
//					finish()
//					return@launch
//				} else {
//					Log.d("HomeActivity", "User profile exists. Proceeding to home UI.")
//				}
//			} catch (e: Exception) {
//				Log.e("HomeActivity", "Profile check failed: ${e.localizedMessage}")
//			}
//		}
//		// 🔹 END OF NEW BLOCK

		// Load images
		loadImages()

		// Setup navigation bar
		setupNavigationBar()

		// Setup RecyclerView
		adapter = MyAdapter(messages)
		homeBinding.recyclerViewChatReplies.adapter = adapter
		homeBinding.recyclerViewChatReplies.layoutManager = LinearLayoutManager(this)

		// Setup Retrofit
		setupRetrofit()

		// Setup message input
		setupMessageInput()

		// Setup location and geofencing
		setupLocationAndGeofence()

		mapFragment.getMapAsync { googleMap ->
			// Center map around DLSU
			val dlsu = LatLng(14.5649, 120.9930)
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dlsu, 17f))

			// Enable basic map controls
			googleMap.uiSettings.isZoomControlsEnabled = true
			googleMap.uiSettings.isScrollGesturesEnabled = true
			googleMap.uiSettings.isZoomGesturesEnabled = true
			googleMap.uiSettings.isRotateGesturesEnabled = true

			// enable user Location (blue dot)
            googleMap.isMyLocationEnabled = true

			lifecycleScope.launch(Dispatchers.IO) {
				val db = MyAppDatabase.getInstance(this@AndroidSmallHomeActivity)
				val poiList = db.poiDao().getAll()

				if (poiList.isEmpty()) {
					Log.w("HomeActivity", "No POIs found in Room.")
					return@launch
				}

				withContext(Dispatchers.Main) {
					val builder = LatLngBounds.Builder()

					poiList.forEach {poi ->
						val position = LatLng(poi.latitude, poi.longitude)
						googleMap.addMarker( // red
							MarkerOptions()
								.position(position)
								.title(poi.name)
						)
						builder.include(position)
					}

					//all markers fit in view
					val bounds = builder.build()
					val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100)
					googleMap.moveCamera(cameraUpdate)
				}
			}
		}
	}

	private fun loadImages() {
		val urls = listOf(
			homeBinding.rn6y677xm2op to "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/59e2tcay_expires_30_days.png",
			homeBinding.r1niin00ofei to "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/g96kdk7e_expires_30_days.png",
			homeBinding.ra0p1lhf2i3g to "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/wklyakqn_expires_30_days.png",
			homeBinding.rjvarnxwuapq to "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/qz8xcg2m_expires_30_days.png",
			homeBinding.rlqd4vorx07s to "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/b60kjme5_expires_30_days.png"
		)
		urls.forEach { Glide.with(this).load(it.second).into(it.first) }
	}

	private fun setupRetrofit() {
		val okHttpClient = OkHttpClient.Builder()
			.connectTimeout(30, TimeUnit.SECONDS)
			.readTimeout(120, TimeUnit.SECONDS)
			.writeTimeout(120, TimeUnit.SECONDS)
			.build()

		val ngrokApiUrl = "https://preharmonious-saul-spectrologically.ngrok-free.dev"
		val retrofit = Retrofit.Builder()
			.baseUrl("$ngrokApiUrl/")
			.client(okHttpClient)
			.addConverterFactory(GsonConverterFactory.create())
			.build()
		chatApi = retrofit.create(ChatApi::class.java)
	}

	private fun setupMessageInput() {
		val editText = homeBinding.r0zz50xix97ik
		editText.setOnEditorActionListener { _, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_SEND) {
				val userMessage = editText.text.toString().trim()
				if (userMessage.isNotBlank()) {
					lifecycleScope.launch { handleUserMessage(userMessage, userId) }
					editText.text.clear()
				}
				true
			} else false
		}
	}

	// USER MESSAGE HANDLER
	private suspend fun handleUserMessage(userMessage: String, userId: Long) {
		val prefs = db.userPreferencesDao().getPreferencesByUser(userId) ?: return

		// Display typed user message
		messages.add("You: $userMessage")
		adapter.notifyItemInserted(messages.size - 1)
		homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)

		// Let DialogueManager detect intent + phase
		val dialogueResult = dialogueManager.processUserIntent(userId, userMessage, prefs)

		// Display bot reply
		messages.add("Bot: ${dialogueResult.reply}")
		adapter.notifyItemInserted(messages.size - 1)
		homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)

		// Handle info/recommend requests using your LLM backend
		if (dialogueResult.phase == "IDLE" &&
			(userMessage.contains("info", ignoreCase = true) || userMessage.contains("recommend", ignoreCase = true))
		) {
			val contextPrompt = buildString {
				append("User role: ${userRoleDao.getUserRoleById(userId)}. Interests: ${prefs.interests.joinToString()}. ")
				append("User message: $userMessage")
			}

			chatApi.generate(ChatRequest(contextPrompt)).enqueue(object : Callback<ChatResponse> {
				override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
					if (response.isSuccessful && response.body() != null) {
						val botReply = response.body()!!.response
						messages.add("Bot: $botReply")
						adapter.notifyItemInserted(messages.size - 1)
						homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
					}
				}

				override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
					messages.add("Bot: Failed to load info: ${t.localizedMessage}")
					adapter.notifyItemInserted(messages.size - 1)
				}
			})
		}

		// Dialogue flow has reached tour generation
		if (dialogueResult.phase == "GENERATING_TOUR") {
			messages.add("Bot: Alright! Generating your personalized tour now...")
			adapter.notifyItemInserted(messages.size - 1)

			lifecycleScope.launch {
				val tourSummary = dialogueManager.handleAction(userId)

				messages.add("Bot: $tourSummary")
				adapter.notifyItemInserted(messages.size - 1)
				homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
			}
		}
	}


	// NAVIGATION BAR
	private fun setupNavigationBar() {
		homeBinding.homeButton.setOnClickListener { recreate() }
		homeBinding.triviaButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallTriviaActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}
		homeBinding.mapButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallMapActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}
		homeBinding.profileButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallProfileActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}
	}

	// SESSION MANAGEMENT
	private fun saveSessionLocally(session: SessionEntity) {
		lifecycleScope.launch {
			val repo = SessionRepository(db)
			repo.insertSession(session, true)
		}
	}

	private fun saveUserLocally(user: UserEntity) {
		lifecycleScope.launch {
			val repo = UserRepository(db)
			repo.insertUser(user, true)
		}
	}

	private fun uploadSessionToFirestore(session: SessionEntity) {
		lifecycleScope.launch {
			val repo = SessionRepository(db)
			repo.insertSession(session, true)
		}
	}

	private fun clearLocalSession() {
		lifecycleScope.launch {
			db.sessionDao().deleteAll()
		}
	}

	// TOUR END
	private fun endTour(sessionProfile: SessionEntity) {
		val endedSession = sessionProfile.copy(sessionEndedAt = System.currentTimeMillis().toString())
		uploadSessionToFirestore(endedSession)

		// TODO: Save performance metrics
		clearLocalSession()
	}

	// LOCATION AND GEOFENCING
	private fun setupLocationAndGeofence() {
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
		geofencingClient = LocationServices.getGeofencingClient(this)

		// Request permission if needed
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
			!= PackageManager.PERMISSION_GRANTED
		) {
			ActivityCompat.requestPermissions(
				this,
				arrayOf(
					Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION,
					Manifest.permission.ACCESS_BACKGROUND_LOCATION
				),
				LOCATION_PERMISSION_CODE
			)
			return
		}

		startLocationTracking()
		setupGeofences()
	}

	// LOCATION PERMISSIONS
	@SuppressLint("MissingPermission")
	private fun startLocationTracking() {
		val locationRequest = LocationRequest.Builder(
			Priority.PRIORITY_HIGH_ACCURACY,
			7000L
		).build()

		locationCallback = object : LocationCallback() {
			override fun onLocationResult(locationResult: LocationResult) {
				super.onLocationResult(locationResult)
				val location = locationResult.lastLocation
				if (location != null) {
					Log.d("HomeActivity", "Current location: ${location.latitude}, ${location.longitude}")
				}
			}
		}

		fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
	}

	// ADDING GEOFENCES
	@SuppressLint("MissingPermission")
	private fun setupGeofences() {
		lifecycleScope.launch {
			try {
				val pois = db.poiDao().getAll()
				if (pois.isEmpty()) {
					Toast.makeText(this@AndroidSmallHomeActivity, "No POIs found.", Toast.LENGTH_SHORT).show()
					return@launch
				}

				val geofences = pois.map { poi ->
					Geofence.Builder()
						.setRequestId(poi.name)
						.setCircularRegion(poi.latitude, poi.longitude, GEOFENCE_RADIUS)
						.setTransitionTypes(
							Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
						)
						.setExpirationDuration(Geofence.NEVER_EXPIRE)
						.build()
				}

				val geofencingRequest = GeofencingRequest.Builder()
					.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
					.addGeofences(geofences)
					.build()

				val intent = Intent("com.thsst2.greenapp.GEOFENCE_TRANSITION_ACTION")
				intent.setClass(this@AndroidSmallHomeActivity, GeofenceReceiver::class.java)

				val pendingIntent = PendingIntent.getBroadcast(
					this@AndroidSmallHomeActivity,
					0,
					intent,
					PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
				)

				geofencingClient.addGeofences(geofencingRequest, pendingIntent)
					.addOnSuccessListener {
						Log.d("HomeActivity", "Geofences added: ${pois.size}")
					}
					.addOnFailureListener { e ->
						Log.e("HomeActivity", "Failed: ${e.localizedMessage}")
					}
			} catch (e: Exception) {
				Log.e("HomeActivity", "Error setting up geofences: ${e.localizedMessage}")
			}
		}
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == LOCATION_PERMISSION_CODE &&
			grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
		) {
			setupLocationAndGeofence()
		} else {
			Toast.makeText(this, "Location permission is required for geofencing.", Toast.LENGTH_LONG).show()
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		if (::fusedLocationClient.isInitialized) {
			fusedLocationClient.removeLocationUpdates(locationCallback)
		}
	}
}
