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
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.RequiresPermission
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
import com.google.firebase.database.FirebaseDatabase
import com.thsst2.greenapp.MyAppDatabase
import com.thsst2.greenapp.data.SessionLogEntity
import com.thsst2.greenapp.data.UserLocationEntity
import com.thsst2.greenapp.data.UserLogEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlin.Long
import kotlin.math.sqrt

class AndroidSmallHomeActivity : AppCompatActivity() {
	private lateinit var homeBinding: ActivityAndroidSmallHomeBinding
	//private val messages = mutableListOf<String>()
	private val messages = mutableListOf<ChatMessage>()
	private lateinit var adapter: MyAdapter
	private lateinit var chatApi: ChatApi
	private lateinit var auth: FirebaseAuth
	private lateinit var sessionManager: SessionManager
	private lateinit var tourCoordinator: TourCoordinator
	private lateinit var dialogueManager: DialogueManager
	private lateinit var metricsCollector: MetricsCollector

	private lateinit var ragEngine: RAGEngine
	private var sessionEnded = false

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
	private lateinit var userLogDao: UserLogDao
	private lateinit var sessionLogDao: SessionLogDao

	// Temporary additional preferences during session
	private val tempAdditionalPreferences = mutableListOf<String>()

	// Session info
	private var sessionId: Long = 0
	private var userId: Long = 0

    // Store current location
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

	// Geofencing
	private lateinit var fusedLocationClient: FusedLocationProviderClient
	private var locationCallback: LocationCallback? = null
	private lateinit var geofencingClient: GeofencingClient
	private val LOCATION_PERMISSION_CODE = 2001
	private var tourStarted = false
	private var hasPassedEntryCheck = false
	private var numChecks = 0
	private var inGeofenceOrTransition = false

	private val buildingReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val name = intent?.getStringExtra("buildingName") ?: return
			val poiId = intent.getStringExtra("poiId") ?: return

			val currentPoi = GeofenceReceiver.currentPoiInside
			MapState.selectedFloor = 1
			runOnUiThread {
				updateCurrentLocationOverlay(currentPoi, 1)
			}

			messages.add(
				ChatMessage(
					text = "You entered $name",
					isUser = false
				)
			)
			adapter.notifyItemInserted(messages.size - 1)
			homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
			val building = List<String>(1) { poiId }

			lifecycleScope.launch {
					val buildingData = ragEngine.getData(building, building)
//					val poiData = db.localDataDao().getLocalData(userId)
					val aiPrompt = """
					You are an AI tour guide for De La Salle University.

					TASK:	
					When a user enters a geofenced building, display a short and informative introduction based only on the building’s data.
					
					INPUT:
					Building Data: $buildingData
					
					INSTRUCTIONS:
					1. Read and understand the building data.
					2. Use building data to get relevant building information. Only use the data that is related to the current building ${name} or ${poiId}.
					3. Generate a short, friendly description of this building — including its name, purpose, and any notable details from the data.
					4. Keep the tone warm, concise, and welcoming (like a campus tour guide speaking to a visitor).
					5. Do not invent information that isn’t provided.
					6. Don't tell me at the start of the sentence if this geofence prompt template is used, just respond in natural language. 
					7. Start with the description immediately, don't add any other reply and be engaging.
					
					EXAMPLE OUTPUT:
						  Henry Sy Sr. Hall,
						  Welcome to Henry Sy Sr. Hall — a 14-story academic complex that houses modern classrooms, research facilities, and student spaces overlooking the DLSU campus.
				""".trimIndent()

					chatApi.generate(ChatRequest(aiPrompt)).enqueue(object : Callback<ChatResponse> {
						override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
							if (response.isSuccessful) {
								val botReply = response.body()?.response ?: "Area Entered!"
								messages.add(
									ChatMessage(
										text = botReply,
										isUser = false,
										suggestions = listOf("More Info", "Next Stop", "Change Floor")
									)
								)
								adapter.notifyItemInserted(messages.size - 1)
								homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)

								lifecycleScope.launch {
									dialogueHistoryDao.insert(
										DialogueHistoryEntity(
											userId = userId,
											userText = "geofence trigger",
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
	}

	private val floorReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val floor = intent?.getIntExtra("floorNumber", 0) ?: return
			if (floor == 0) return // Invalid floor
			val poiId = intent.getStringExtra("poiId") ?: return

			val currentPoi = GeofenceReceiver.currentPoiInside
			runOnUiThread {
				updateCurrentLocationOverlay(currentPoi, floor)
			}

			messages.add(
				ChatMessage(
					text = "You selected floor $floor",
					isUser = false
				)
			)
			adapter.notifyItemInserted(messages.size - 1)
			homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
			val building = List<String>(1) { poiId }

			lifecycleScope.launch {
					val floorData = ragEngine.getFloorData(floor, poiId)
					val aiPrompt = """
					You are an AI tour guide for De La Salle University.

					TASK:	
					When a user enters a geofenced building, display a short and informative introduction based only on the floor’s data.
					
					INPUT:
					Floor Data: $floorData
					
					INSTRUCTIONS:
					1. Read and understand the floor data.
					2. Use floor data to get relevant floor information. Only use the data that is related to the current building ${floor}.
					3. Generate a short, friendly description of this floor — its amenities, labels, notes, and any notable details from the data.
					4. Keep the tone warm, concise, and welcoming (like a campus tour guide speaking to a visitor).
					5. Do not invent information that isn’t provided.
					6. Don't tell me at the start of the sentence if this geofence prompt template is used, just respond in natural language. 
					7. Start with the description immediately, don't add any other reply and be engaging.
					
					EXAMPLE OUTPUT:
						  Henry Sy Sr. Hall Floor 12,
						  Welcome to the 12th floor. The 12th floor includes the library, escalators, and bathrooms. 
						  Inside the library, there are multiple books to read, and a cozy place to stay.
				""".trimIndent()

					chatApi.generate(ChatRequest(aiPrompt)).enqueue(object : Callback<ChatResponse> {
						override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
							if (response.isSuccessful) {
								val botReply = response.body()?.response ?: "Area Entered!"
								messages.add(
									ChatMessage(
										text = botReply,
										isUser = false,
										suggestions = listOf("More Info", "Next Stop", "Change Floor")
									)
								)
								adapter.notifyItemInserted(messages.size - 1)
								homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)

								lifecycleScope.launch {
									dialogueHistoryDao.insert(
										DialogueHistoryEntity(
											userId = userId,
											userText = "geofence trigger",
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
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		homeBinding = ActivityAndroidSmallHomeBinding.inflate(layoutInflater)
		setContentView(homeBinding.root)

		FirebaseApp.initializeApp(this)
		auth = FirebaseAuth.getInstance()
		sessionManager = SessionManager(this)
		//tourCoordinator = TourCoordinator(userId, this)
		dialogueManager = DialogueManager(this)
		ragEngine = RAGEngine()

		// Initialize DB & DAOs
		db = MyAppDatabase.getInstance(applicationContext)
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
		userLogDao = db.userLogDao()
		sessionLogDao = db.sessionLogDao()
		metricsCollector = MetricsCollector(db)
		numChecks

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
		tourCoordinator = TourCoordinator(userId, this)

		val user = UserEntity(
			userId = userId
		)
		saveUserLocally(user)

		val sessionProfile = SessionEntity(
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
		adapter = MyAdapter(messages) { suggestion ->
			when (suggestion) {
				"Change Floor" -> {
					val currentPoi = GeofenceReceiver.currentPoiInside
					if (currentPoi != null && (currentPoi.floors ?: 1) > 1) {
						showFloorSelectionDialog(currentPoi)
					} else {
						Toast.makeText(
							this,
							"No multi-floor building is currently selected.",
							Toast.LENGTH_SHORT
						).show()
					}
				}

				"More Info" -> {
					Toast.makeText(this, "More Info not implemented yet.", Toast.LENGTH_SHORT).show()
				}

				"Next Stop" -> {
					Toast.makeText(this, "Next Stop not implemented yet.", Toast.LENGTH_SHORT).show()
				}
			}
		}
		homeBinding.recyclerViewChatReplies.adapter = adapter
		homeBinding.recyclerViewChatReplies.layoutManager = LinearLayoutManager(this)

		updateCurrentLocationOverlay(null)

		homeBinding.currentLocationOverlay.setOnClickListener {
			val currentPoi = GeofenceReceiver.currentPoiInside
			if (currentPoi != null && (currentPoi.floors ?: 1) > 1) {
				showFloorSelectionDialog(currentPoi)
			}
		}

		// Setup Retrofit
		setupRetrofit()

		// Setup location and geofencing
		setupLocationAndGeofence()

		LocalBroadcastManager.getInstance(this)
			.registerReceiver(buildingReceiver, IntentFilter("BUILDING_ENTERED"))

		LocalBroadcastManager.getInstance(this)
			.registerReceiver(floorReceiver, IntentFilter("FLOOR_SELECTED"))


		mapFragment.getMapAsync @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]) { googleMap ->
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
				messages.add(
					ChatMessage(
						text = result.message,
						isUser = false
					)
				)
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

		messages.add(
			ChatMessage(
				text = userMessage,
				isUser = true
			)
		)
		adapter.notifyItemInserted(messages.size - 1)
		homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)

		// Let DialogueManager process the input
		val dmResult = dialogueManager.processMessage(userId, userMessage)

		if (dmResult.message.isNotBlank()) {
//			val suggestions = if (tourStarted) {
//				listOf("More Info", "Next Stop", "Change Floor")
//			} else {
//				emptyList()
//			}
			val suggestions = listOf("More Info", "Next Stop", "Change Floor")

			messages.add(
				ChatMessage(
					text = dmResult.message,
					isUser = false,
					suggestions = suggestions
				)
			)
			adapter.notifyItemInserted(messages.size - 1)
			homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
		}

		// Open profile preferences edit box
		if (dmResult.openProfileForPrefs) {
			val intent = Intent(this@AndroidSmallHomeActivity, AndroidSmallProfileActivity::class.java)
			intent.putExtra("open_prefs_dialog", true)
			startActivity(intent)
			return
		}

		// If user wants a tour, call TourCoordinator
		if (dmResult.context == ConversationContext.TOUR_READY && !tourStarted) {
			tourStarted = true

			Log.d("HomeActivity", "Intent: ${dmResult.intent}")
			Log.d("HomeActivity", "Message: ${dmResult.message}")
			Log.d("HomeActivity", "Tour Started: $tourStarted")
			lifecycleScope.launch {
//				homeBinding.mapLoadingIndicator.visibility = View.VISIBLE // show loading
//				delay(100)
                val lat = currentLatitude
                val long = currentLongitude

                if (lat == null || long == null) {
                    Log.e("HomeActivity", "Cannot start tour: Location not available yet.")
                    homeBinding.mapLoadingIndicator.visibility = View.GONE
                    return@launch
                }

				val userTourPathHistory = withContext(Dispatchers.IO) {
					val isRandom = dmResult.isRandom ?: false
					tourCoordinator.startTourForUser(userId, allPreferences, lat, long, isRandom)
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
						drawTourPath(googleMap, userTourPathHistory.pathSequence)
						homeBinding.mapLoadingIndicator.visibility = View.GONE
					}
				}

				val poiJson = Gson().toJson(userTourPathHistory.pathSequence)
				val poiData = db.localDataDao().getLocalData(userId)
				val startingPoint = null // building starting point
				val aiPrompt = """
					You are an AI tour guide for De La Salle University.
		
					TASK: Generate a personalized tour overview for the user based on the following POIs and data.
		
					User Role: $userRoleName
					Preferences: $allPreferences
					Starting Location: $startingPoint
		
					POI Sequence: $poiJson
					POI Data: $poiData
		
					INSTRUCTIONS:
					1. Write sectioned paragraphs.
					2. Start from the beginning of the tour.
					3. Do NOT include headings, labels, greetings, or conclusions.
					4. Do NOT repeat or restart the text.
					5. Do NOT use ellipses (...).
					6. Use only the data provided.
					7. Output ONLY the tour narration text.
					8. Don't tell me at the start of the sentence if this tour overview prompt template is used, just respond in natural language. 
					9. Start with the tour overview immediately, don't add any other reply and be engaging.
					
					EXAMPLE:
					  	Welcome to your DLSU Heritage Trail! You'll begin at St. La Salle Hall...
				""".trimIndent()

				Log.d("HomeActivity", "aiPrompt: $aiPrompt")

				chatApi.generate(ChatRequest(aiPrompt)).enqueue(object : Callback<ChatResponse> {
					override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
						if (response.isSuccessful) {
							val botReply = response.body()?.response ?: "Tour overview ready."
							messages.add(
								ChatMessage(
									text = botReply,
									isUser = false,
									suggestions = listOf("More Info", "Next Stop", "Change Floor")
								)
							)
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
		} else if (dmResult.intent == IntentType.ASK_INFO) {

			val poiJson = Gson().toJson(db.userTourPathHistoryDao().getById(userId)?.pathSequence)
//			val poiData = db.localDataDao().getLocalData(userId)
			val startingPoint = null
			val allTags = ragEngine.getPreferencesListForProfilePage()
			val aiTagPrompt = """
				You are an AI that identifies relevant tags from a user's query.

				USER QUERY: "$userMessage"
		
				AVAILABLE TAGS: $allTags
		
				TASK:
				1. Read the user's query carefully.
				2. Return only the tags that are relevant to this query.
				3. Output the tags as a JSON array of strings, e.g. ["fn_academic", "acc_braille"]
			""".trimIndent()

			val relevantTags: List<String> = try {
				val tagResponse = chatApi.generate(ChatRequest(aiTagPrompt)).execute().body()?.response
				Gson().fromJson(tagResponse, Array<String>::class.java).toList()
			} catch (e: Exception) {
				Log.e("ChatApi", "Tag extraction failed: ${e.message}")
				emptyList()
			}

			Log.d("HomeActivity", "Tags Chosen: $relevantTags")


			// Filter POI data based on relevant tags
			val filteredPoiData = ragEngine.filterPoiData(relevantTags)

			val aiPrompt = """
				You are an AI tour guide for De La Salle University.
	
				TASK: Answer the user query as accurate as you can
				
				User Query: $userMessage
				User Role: $userRoleName
				Preferences: $allPreferences
				Relevant Tags: $relevantTags
				Starting Location: $startingPoint
	
				POI Sequence: $poiJson
       			POI Data: ${Gson().toJson(filteredPoiData)}
	
				INSTRUCTIONS:
				1. Write a short, accurate response to the user query
				2. Use only the needed data provided above for generating the answer.
				3. Don't tell me at the start of the sentence if this query prompt template is used, just respond in natural language. 
				4. Start with the answer immediately, don't add any other reply and be engaging.
				
				EXAMPLE:
				  	The Henry Sy Sr. Hall is located along Taft Avenue and serves as DLSU’s modern academic tower.
			""".trimIndent()

			chatApi.generate(ChatRequest(aiPrompt)).enqueue(object : Callback<ChatResponse> {
				override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
					if (response.isSuccessful) {
						val botReply = response.body()?.response ?: "Answer:"
						messages.add(
							ChatMessage(
								text = botReply,
								isUser = false,
								suggestions = listOf("More Info", "Next Stop", "Change Floor")
							)
						)
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
	private fun endTour(userId: Long, sessionId: Long) {
		lifecycleScope.launch {
			// Finalize and save performance metrics for this session
			metricsCollector.finalizeSessionMetrics(sessionId)
			
			userLogDao.insert(
				UserLogEntity(
					userId = userId,
                    generatedPaths = db.generatedPathDao().getGeneratedPathsByUser(userId),
                    geofenceTriggers = db.geofenceTriggerDao().getGeofenceTriggersByUser(userId),
                    pathDeviationAlerts = db.pathDeviationAlertDao().getPathDeviationAlertsByUser(userId),
                    dialogueHistories = db.dialogueHistoryDao().getDialogueHistoryByUser(userId),
                    intentLogs = db.intentLogDao().getIntentLogsByUser(userId),
					sessionId = sessionId,
                    userQueries = db.userQueryDao().getUserQueriesByUser(userId),
                    userFeedback = db.userFeedbackDao().getFeedbackByUserLog(userId),
                    userInteractionTimes = db.userInteractionTimeDao().getInteractionTimesByUserLog(userId),
                )
			)

			val sessionLog = SessionLogEntity(
				userId = userId,
				performanceMetrics = db.performanceMetricsDao().getMetricsBySession(sessionId),
				sessions = db.sessionDao().getSessionsByUser(userId),
				userLocations = db.userLocationDao().getLocationsBySession(sessionId),
				userLogs = db.userLogDao().getUserLogsByUser(userId),
				userSkippedOrDislikedLocations = db.userSkippedOrDislikedLocationDao().getBySession(sessionId),
				userTourPathHistories = db.userTourPathHistoryDao().getBySession(sessionId),
				userVisitedLocations = db.userVisitedLocationDao().getBySession(sessionId),
			)

			sessionLogDao.insert(sessionLog)

			lifecycleScope.launch(Dispatchers.IO) {
				saveSessionLogToFirebase(sessionLog)
			}
		}
		clearLocalSession()
	}

	// SAVE FIREBASE SESSION LOGS
	suspend fun saveSessionLogToFirebase(
		sessionLog: SessionLogEntity
	) {
		val ref =  FirebaseDatabase.getInstance()
			.reference
			.child("client_side")
			.child("log")
			.push()

		ref.setValue(sessionLog).await()
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
                    // Save current location
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude

					if (numChecks == 0) {
						lifecycleScope.launch {
							val pois = RAGEngine().getBuildings()

							for (poi in pois) {
								val results = FloatArray(1)

								// For Hector check as references
								Location.distanceBetween(
									location.latitude,
									location.longitude,
									poi.latitude,
									poi.longitude,
									results
								)

								val distance = results[0]

								Log.d("HomeActivity", "Distance: ${distance}, Radius: ${poi.radius}")
								if (distance <= poi.radius) {
									hasPassedEntryCheck = true
									break
								}
							}

							hasPassedEntryCheck = true //Temporary for testing
							if (!hasPassedEntryCheck) {
//								finishAffinity()
							}
						}
						numChecks += 1
					} else {
						lifecycleScope.launch {
							val pois = RAGEngine().getBuildings()
							val transitions = RAGEngine().getTransitions()
							inGeofenceOrTransition = false

							for (poi in pois) {
								val results = FloatArray(1)

								Location.distanceBetween(
									location.latitude,
									location.longitude,
									poi.latitude,
									poi.longitude,
									results
								)

								val distance = results[0]

								// Simulate a circle inside a square. If a circle is inside a square, the distance
								// from the center of the circle to the corner is r * square root of 2
								if (distance <= (poi.radius * sqrt(2.0))) {
									inGeofenceOrTransition = true
									break
								}
							}

							for (transition in transitions) {
								val results = FloatArray(1)

								Location.distanceBetween(
									location.latitude,
									location.longitude,
									transition.latitude,
									transition.longitude,
									results
								)

								val distance = results[0]

								Log.d("HomeActivity", "Distance: ${distance}, Radius: ${transition.radius}")

								if (distance <= transition.radius) {
									inGeofenceOrTransition = true
									break
								}
							}

							inGeofenceOrTransition = true //Temporary for testing
							if (!inGeofenceOrTransition || !hasPassedEntryCheck) {
//								finishAffinity()
							}
						}
					}
				}
			}
		}

		fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, mainLooper)
	}

	// ADDING GEOFENCES
	@SuppressLint("MissingPermission")
	private fun setupGeofences() {
		lifecycleScope.launch {
			try {
//				val pois = db.poiDao().getAll()
				val pois = RAGEngine().getBuildings()
				Log.d("HomeActivity", "Found $pois POIs.")

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

	private fun updateCurrentLocationOverlay(poi: PoiEntity?, floor: Int? = null) {
		if (poi == null) {
			homeBinding.currentLocationOverlay.text = "Not in a building"
			homeBinding.currentLocationOverlay.isClickable = false
			homeBinding.currentLocationOverlay.alpha = 0.5f
			return
		}

		val displayFloor = floor ?: 1
		val canChangeFloor = (poi.floors ?: 1) > 1

		homeBinding.currentLocationOverlay.text = if (canChangeFloor) {
			"${poi.name} • Floor $displayFloor ▼"
		} else {
			"${poi.name} • Floor $displayFloor"
		}

		homeBinding.currentLocationOverlay.isClickable = canChangeFloor
		homeBinding.currentLocationOverlay.alpha = if (canChangeFloor) 1f else 0.85f
	}

	private fun showFloorSelectionDialog(poi: PoiEntity) {
		val maxFloor = poi.floors ?: 1
		val floors = (1..maxFloor).map { "Floor $it" }.toTypedArray()

		AlertDialog.Builder(this)
			.setTitle("Select floor in ${poi.name}")
			.setItems(floors) { _, which ->
				val selectedFloor = which + 1

				runOnUiThread {
					updateCurrentLocationOverlay(poi, selectedFloor)
				}

				saveFloorSelection(poi, selectedFloor)
				MapState.selectedFloor = selectedFloor

				val intent = Intent("FLOOR_SELECTED")
				intent.putExtra("floorNumber", selectedFloor)
				intent.putExtra("poiId", poi.poiId)
				LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
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

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		setIntent(intent)

		val fromPrefEdit = intent.getBooleanExtra("from_pref_edit", false)
		if (!fromPrefEdit) return

		val didSave = intent.getBooleanExtra("pref_edit_saved", false)

		lifecycleScope.launch {
			val result = dialogueManager.handleProfilePreferenceResult(userId, didSave)

			if (result.message.isNotBlank()) {
				val suggestions = listOf("More Info", "Next Stop", "Change Floor")

				messages.add(
					ChatMessage(
						text = result.message,
						isUser = false,
						suggestions = suggestions
					)
				)
				adapter.notifyItemInserted(messages.size - 1)
				homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
			}
		}
	}

	override fun onStop() {
		super.onStop()

		if (sessionEnded) return
		sessionEnded = true

		Log.d("SESSION", "App backgrounded → ending session $sessionId")

		endTour(userId, sessionId)
	}


	override fun onDestroy() {
		if (::fusedLocationClient.isInitialized) {
			locationCallback?.let {
				fusedLocationClient.removeLocationUpdates(it)
			}
		}
		LocalBroadcastManager.getInstance(this).unregisterReceiver(buildingReceiver)
		LocalBroadcastManager.getInstance(this).unregisterReceiver(floorReceiver)

		super.onDestroy()
	}
}
