package com.thsst2.greenapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
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
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.thsst2.greenapp.data.DialogueHistoryEntity
import com.thsst2.greenapp.data.IntentLogEntity
import com.thsst2.greenapp.data.SessionEntity
import com.thsst2.greenapp.data.UserEntity
import com.thsst2.greenapp.data.UserQueryEntity
import com.thsst2.greenapp.data.repositories.SessionRepository
import com.thsst2.greenapp.data.repositories.UserRepository
import com.thsst2.greenapp.databinding.ActivityAndroidSmallHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.thsst2.greenapp.data.PoiEntity
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.thsst2.greenapp.data.UserLocationEntity
import kotlinx.coroutines.delay

class AndroidSmallHomeActivity : AppCompatActivity() {
	private lateinit var homeBinding: ActivityAndroidSmallHomeBinding
	private val messages = mutableListOf<String>()
	private lateinit var adapter: MyAdapter
	private lateinit var chatApi: ChatApi
	private lateinit var auth: FirebaseAuth
	private lateinit var sessionManager: SessionManager
	private lateinit var tourCoordinator: TourCoordinator
	private lateinit var dialogueManager: DialogueManager

	private lateinit var ragEngine: RAGEngine

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
	private val LOCATION_PERMISSION_CODE = 2001

	private val buildingReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val name = intent?.getStringExtra("buildingName") ?: return
			lifecycleScope.launch {
				onBuildingEntered(name)
			}
		}
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		homeBinding = ActivityAndroidSmallHomeBinding.inflate(layoutInflater)
		setContentView(homeBinding.root)

		FirebaseApp.initializeApp(this)
		auth = FirebaseAuth.getInstance()
		sessionManager = SessionManager(this)
		tourCoordinator = TourCoordinator(this)
		dialogueManager = DialogueManager(this)
		ragEngine = RAGEngine()

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

		// 🔹 NEW: Check if user has an existing profile
		lifecycleScope.launch {
			try {
				// You can replace `userDao()` and `UserProfileDao` depending on your schema
				val existingProfile = db.userPreferencesDao().getPreferencesByUser(userId)
		// TODO
			// problem: keeps redirectly user to profile creation even though user already did
				// uncomment when solved

				if (existingProfile == null) {
					// If profile is missing or incomplete → redirect to profile creation
					Log.d("HomeActivity", "No profile found. Redirecting to Profile Creation...")

					val intent = Intent(
						this@AndroidSmallHomeActivity,
						AndroidSmallProfileActivity::class.java // ⚠️ Make sure this Activity exists
					)
					intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
					startActivity(intent)
					finish()
					return@launch
				} else {
					Log.d("HomeActivity", "User profile exists. Proceeding to home UI.")
				}
			} catch (e: Exception) {
				Log.e("HomeActivity", "Profile check failed: ${e.localizedMessage}")
			}
		}
		// 🔹 END OF NEW BLOCK

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

		// Setup location and geofencing
		setupLocationAndGeofence()
		LocalBroadcastManager.getInstance(this)
			.registerReceiver(buildingReceiver, IntentFilter("BUILDING_ENTERED"))


		mapFragment.getMapAsync @androidx.annotation.RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]) { googleMap ->
			// Center map around DLSU
			val dlsu = LatLng(14.5649, 120.9930)
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dlsu, 17f))

			// Enable basic map controls
			googleMap.uiSettings.isZoomControlsEnabled = true
			googleMap.uiSettings.isScrollGesturesEnabled = true
			googleMap.uiSettings.isZoomGesturesEnabled = true
			googleMap.uiSettings.isRotateGesturesEnabled = true

			if (ContextCompat.checkSelfPermission(
					this,
					Manifest.permission.ACCESS_FINE_LOCATION
				) == PackageManager.PERMISSION_GRANTED
			) {
				try {
					googleMap.isMyLocationEnabled = true // Enable user Location (blue dot)
				} catch (e: SecurityException) {
					Log.w("HomeActivity", "Cannot enable user location: ${e.localizedMessage}")
				}
			}

			lifecycleScope.launch(Dispatchers.IO) {
				withContext(Dispatchers.Main) {

					// Fetch all POIs from Firebase via RAG
					val fetchedPois = try {
						ragEngine.getBuildings() // null = fetch all POIs
					} catch (e: Exception) {
						Log.e("HomeActivity", "Failed to fetch POIs: ${e.localizedMessage}")
						emptyList()
					}
					Log.d("HomeActivity", "Retrieved ${fetchedPois.size} POIs to Geofence.")

					// Display POIs on map
					drawMarkers(googleMap, fetchedPois)

					// Setup geofences
					try {
						setupGeofences()
						Log.d("HomeActivity", "Registered geofences for ${fetchedPois.size} POIs.")
					} catch (e: Exception) {
						Log.e("HomeActivity", "Failed to register geofences: ${e.localizedMessage}")
					}
				}
			}

			// floor selection
			googleMap.setOnMarkerClickListener { marker ->
				val currentPoi = GeofenceReceiver.currentPoiInside

				// user must be inside THAT POI
				if (currentPoi != null && marker.title == currentPoi.name) {
					showFloorSelectionDialog(currentPoi)
				}
				false
			}

			// start the dialogue manager looping
			lifecycleScope.launch {
				val result = dialogueManager.processMessage(userId, "hi")  // start greeting phase
				messages.add("Bot: ${result.message}")
				adapter.notifyItemInserted(messages.size - 1)
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

		// Setup message input
		setupMessageInput()
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
		// Save UserQuery + IntentLog
		val queryId = userQueryDao.insert(
			UserQueryEntity(
				userId = userId,
				text = userMessage,
				timestamp = System.currentTimeMillis(),
				intentDetected = null,
				responseType = null
			)
		)
		intentLogDao.insert(
			IntentLogEntity(
				userId = userId,
				userQueryId = queryId,
				intentLabel = "user_message",
				confidenceScore = null,
				entities = null
			)
		)

		// Build context string
		val userRole = db.userRoleDao().getUserRoleById(userId)
		val userRoleName = userRole?.role
		val activePreferences = userPreferencesDao.getPreferencesByUser(userId)
		val userVisitedLocation = userVisitedLocationDao.getById(userId)
		val visitedPOIs = userVisitedLocation?.let { visited ->
			db.poiDao().getPoiById(visited.poiId)?.let { listOf(it) }
		} ?: emptyList()

		val allPreferences = activePreferences?.interests?.plus(tempAdditionalPreferences)

//		var contextString = buildString {
//			append("I am a $userRoleName. ")
//			append("I like ${allPreferences?.joinToString(", ")}. ")
//			append("I have visited ${visitedPOIs.joinToString(", ") { it.name }}. ")
//		}

//		val aiPrompt = "$contextString $userMessage"

		messages.add("You: $userMessage")
		adapter.notifyItemInserted(messages.size - 1)
		homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)

		// Let DialogueManager process the input
		val dmResult = dialogueManager.processMessage(userId, userMessage)

		if (dmResult.message.isNotBlank()) {
			messages.add("Bot: ${dmResult.message}")
			adapter.notifyItemInserted(messages.size - 1)
			homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
		}

		// If user wants a tour, call TourCoordinator
		if (dmResult.intent == IntentType.START_TOUR) {
			lifecycleScope.launch {
//				homeBinding.mapLoadingIndicator.visibility = View.VISIBLE // show loading
//				delay(100)

				val userTourPathHistory = withContext(Dispatchers.IO) {
					tourCoordinator.startTourForUser(userId, allPreferences)
				}

				// draw path
				if (userTourPathHistory?.pathSequence.isNullOrEmpty()) {
					homeBinding.mapLoadingIndicator.visibility = View.GONE
					Log.w("HomeActivity", "TourCoordinator returned empty path.")
					return@launch
				}

				val mapFragment = supportFragmentManager.findFragmentById(R.id.home_map_fragment) as? SupportMapFragment
				mapFragment?.getMapAsync { googleMap ->
					lifecycleScope.launch {
						drawTourPath(googleMap, userTourPathHistory!!.pathSequence)
						homeBinding.mapLoadingIndicator.visibility = View.GONE
					}
				}

				val poiJson = Gson().toJson(userTourPathHistory?.pathSequence)
				val poiData = db.localDataDao().getLocalData(userId)
				val startingPoint = null
				val aiPrompt = """
					You are an AI tour guide for De La Salle University.
		
					TASK: Generate a personalized tour overview for the user based on the following POIs and data.
		
					User Role: $userRoleName
					Preferences: $allPreferences
					Starting Location: $startingPoint
		
					POI Sequence: $poiJson
					POI Data: $poiData
		
					INSTRUCTIONS:
					1. Analyze the POIs and their sequence to form a coherent tour route.
					2. Write a short, friendly summary introducing the tour, then describe the route in order.
					3. Include relevant context or facts about each stop when available.
					4. Maintain a warm, toueinformative tone.
					5. Use only the data provided above for generating the overview text.
		
					OUTPUT FORMAT:
					{
					  "tour_title": "string",
					  "overview_text": "string"
					}
					
					EXAMPLE:
					Input:
					User Role: Guest
					Preferences: History, Architecture
					POI Sequence: [ ... ]
					POI Data: [ ... ]
					Output:
					{
					  "tour_title": "DLSU Heritage Trail",
					  "overview_text": "Welcome to your DLSU Heritage Trail! You'll begin at St. La Salle Hall..."
					}
				
					Follow the tour overview instructions.
				""".trimIndent()

				chatApi.generate(ChatRequest(aiPrompt)).enqueue(object : Callback<ChatResponse> {
					override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
						if (response.isSuccessful) {
							val botReply = response.body()?.response ?: "Tour overview ready."
							messages.add("Bot: $botReply")
							adapter.notifyItemInserted(messages.size - 1)
							homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)

							lifecycleScope.launch {
								dialogueHistoryDao.insert(
									DialogueHistoryEntity(
										userId = userId,
										userText = userMessage,
										systemResponse = botReply,
										contextSnapshot = aiPrompt,
										turnNumber = messages.size
									)
								)
							}
						} else {
							Log.e("ChatApi", "Failed: ${response.errorBody()?.string()}")
						}
					}

					override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
						Log.e("ChatApi", "Error: ${t.message}", t)
					}
				})
			}
		} else {
			val poiJson = Gson().toJson(db.userTourPathHistoryDao().getById(userId)?.pathSequence)
			val poiData = db.localDataDao().getLocalData(userId)
			val startingPoint = null
			val aiPrompt = """
				You are an AI tour guide for De La Salle University.
	
				TASK: Answer the user query as accurate as you can
				
				User Role: $userRoleName
				Preferences: $allPreferences
				Starting Location: $startingPoint
	
				POI Sequence: $poiJson
				POI Data: $poiData
	
				INSTRUCTIONS:
				1. Write a short, accurate response to the user query
				2. Use only the needed data provided above for generating the answer.
	
				OUTPUT FORMAT:
				{
				  "answer": "string"
				}
				
				EXAMPLE:
				User Query: "Where is the Henry Sy Sr. Hall located?"
				POI Data: [
				  {"name": "Henry Sy Sr. Hall", "description": "Located along Taft Avenue, this 14-storey building serves as DLSU’s modern academic tower."}
				]
				Output:
				{
				  "answer": "The Henry Sy Sr. Hall is located along Taft Avenue and serves as DLSU’s modern academic tower."
				}
			""".trimIndent()

			chatApi.generate(ChatRequest(aiPrompt)).enqueue(object : Callback<ChatResponse> {
				override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
					if (response.isSuccessful) {
						val botReply = response.body()?.response ?: "Tour overview ready."
						messages.add("Bot: $botReply")
						adapter.notifyItemInserted(messages.size - 1)
						homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)

						lifecycleScope.launch {
							dialogueHistoryDao.insert(
								DialogueHistoryEntity(
									userId = userId,
									userText = userMessage,
									systemResponse = botReply,
									contextSnapshot = aiPrompt,
									turnNumber = messages.size
								)
							)
						}
					} else {
						Log.e("ChatApi", "Failed: ${response.errorBody()?.string()}")
					}
				}

				override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
					Log.e("ChatApi", "Error: ${t.message}", t)
				}
			})
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
						.setCircularRegion(poi.latitude, poi.longitude, poi.radius.toFloat())
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

	private suspend fun onBuildingEntered(name: String) {
		messages.add("Bot: You entered $name")
		adapter.notifyItemInserted(messages.size - 1)
		homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
	}


	private suspend fun drawMarkers(googleMap: GoogleMap, pois: List<PoiEntity>) = withContext(Dispatchers.Main) {
		if (pois.isEmpty()) {
			Toast.makeText(this@AndroidSmallHomeActivity, "No POIs available.", Toast.LENGTH_SHORT).show()
			return@withContext
		}

		googleMap.clear()
		val boundsBuilder = LatLngBounds.Builder()

		pois.forEach { poi ->
			val position = LatLng(poi.latitude, poi.longitude)
			ContextCompat.getDrawable(this@AndroidSmallHomeActivity, R.drawable.ic_marker)?.let { d ->
				val bmp = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
				val c = Canvas(bmp)
				d.setBounds(0, 0, c.width, c.height)
				d.draw(c)
				googleMap.addMarker(
					MarkerOptions()
						.position(position)
						.title(poi.name)
						.icon(BitmapDescriptorFactory.fromBitmap(bmp))
						.anchor(0.5f, 1f)
				)
			}
			// Draw geofence circle
//			googleMap.addCircle(
//				CircleOptions()
//					.center(position)
//					.radius(poi.radius)  // meters
//					.strokeColor(Color.argb(100, 0, 150, 136))
//					.fillColor(Color.argb(40, 0, 150, 136))
//					.strokeWidth(2f)
//			)
			boundsBuilder.include(position)
		}
		MapState.pois = pois

		//all markers fit in view
		val bounds = boundsBuilder.build()
		val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100)
		googleMap.moveCamera(cameraUpdate)

		Log.d("HomeActivity", "Plotted ${pois.size} POIs on map.")
	}
	private suspend fun drawTourPath(googleMap: GoogleMap, pathSequence: List<String>) = withContext(Dispatchers.Main) {
		if (pathSequence.isEmpty()) {
			Log.w("HomeActivity", "No tour path to draw.")
			return@withContext
		}

		val pois = db.poiDao().getAll()
		val orderedPois = pathSequence.mapNotNull { poiName ->
			pois.find { it.name == poiName }
		}

		if (orderedPois.size < 2) {
			Log.w("HomeActivity", "Not enough POIs to draw a path.")
			return@withContext
		}

		val polylineOptions = PolylineOptions()
			.color(Color.BLUE)
			.width(6f)
			.geodesic(true)
			.pattern(listOf(Dot(), Gap(10f), Dash(20f)))

		orderedPois.forEach { poi ->
			polylineOptions.add(LatLng(poi.latitude, poi.longitude))
		}

		googleMap.addPolyline(polylineOptions)
		MapState.pathLatLngs = orderedPois.map { LatLng(it.latitude, it.longitude) }

		Log.d("HomeActivity", "Tour path drawn with ${orderedPois.size} points.")
	}

	private fun showFloorSelectionDialog(poi: PoiEntity) {
		val maxFloor = poi.floors ?: 1
		val floors = (1..maxFloor).map { "Floor $it" }.toTypedArray()

		AlertDialog.Builder(this)
			.setTitle("Select floor in ${poi.name}")
			.setItems(floors) { _, which ->
				val selectedFloor = which + 1
				saveFloorSelection(poi, selectedFloor)
				MapState.selectedFloor = selectedFloor
			}
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun saveFloorSelection(poi: PoiEntity, floor: Int) {
		lifecycleScope.launch(Dispatchers.IO) {
			val sessionId = db.sessionDao().getAll().lastOrNull()?.sessionId ?: return@launch
			val userId = this@AndroidSmallHomeActivity.userId

			val entity = UserLocationEntity(
				userId = userId,
				sessionId = sessionId,
				latitude = poi.latitude,
				longitude = poi.longitude,
				timestamp = System.currentTimeMillis(),
				accuracyRadius = poi.radius.toFloat(),
				floor = floor
			)

			db.userLocationDao().insert(entity)

			withContext(Dispatchers.Main) {
				Toast.makeText(
					this@AndroidSmallHomeActivity,
					"Floor $floor selected for ${poi.name}",
					Toast.LENGTH_SHORT
				).show()
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		if (::fusedLocationClient.isInitialized) {
			fusedLocationClient.removeLocationUpdates(locationCallback)
		}
		LocalBroadcastManager.getInstance(this).unregisterReceiver(buildingReceiver)
	}
}
