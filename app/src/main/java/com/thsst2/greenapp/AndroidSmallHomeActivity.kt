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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
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
import com.google.android.gms.maps.model.CircleOptions
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
import androidx.activity.OnBackPressedCallback
import androidx.room.withTransaction
import com.thsst2.greenapp.data.PerformanceMetricsEntity
import kotlinx.coroutines.CoroutineScope

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

	// For More Info button
	private var pendingMoreInfoParagraphs: List<String> = emptyList()
	private var nextParagraphIndex: Int = 0
	// For Next Stop button
	private var currentTourPathSequence: List<String> = emptyList()
	private var currentTourStopIndex: Int = -1

	// Geofencing
	private lateinit var fusedLocationClient: FusedLocationProviderClient
	private var locationCallback: LocationCallback? = null
	private lateinit var geofencingClient: GeofencingClient
	private val LOCATION_PERMISSION_CODE = 2001
	private var tourStarted = false
	private var hasPassedEntryCheck = false
	private var numChecks = 0
	private var inGeofenceOrTransition = false

	private var isAsking = false

	private val buildingReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val startTime = System.currentTimeMillis()
			val name = intent?.getStringExtra("buildingName") ?: return
			val poiId = intent?.getStringExtra("poiId") ?: return
			Log.d("BUILDING_ENTERED", "Building entered: $name")
			Log.d("BUILDING_ENTERED", "Building ID: $poiId")

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
				val cleanPoiJson = cleanPoiJson(buildingData)

				Log.d("LLM_BUILDING_DATA", buildingData)

				// =========================
				// STAGE 1: PLANNING
				// =========================
				val buildingPlanPrompt = """
					### Role
					You are an internal planning assistant for a campus tour AI.
			
					### Task
					Read the building data and extract only the most relevant facts needed for a short visitor-friendly building introduction.
			
					### Input
					Building: $name ($poiId)
					Building Data:
					$cleanPoiJson
			
					### Rules
					1. Use only the provided data.
					2. Select only facts directly useful for introducing the building.
					3. Do not invent details.
					4. Output ONLY a valid JSON object.
					5. No explanations.
			
					### Output Format
					{
					  "building_name": "string",
					  "key_facts": ["fact 1", "fact 2"]
					}
				""".trimIndent()

				Log.d("LLM_PLAN_PROMPT", buildingPlanPrompt)

				chatApi.generate(ChatRequest(buildingPlanPrompt)).enqueue(object : Callback<ChatResponse> {

					override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
						if (response.isSuccessful) {

							val rawPlan = response.body()?.response ?: "{}"
							val buildingPlanJson = extractJsonPayload(rawPlan)

							Log.d("LLM_PLAN_RAW", rawPlan)
							Log.d("LLM_PLAN_JSON", buildingPlanJson)

							// =========================
							// STAGE 2: FINAL RESPONSE
							// =========================
							val buildingFinalPrompt = """
								### Persona
								You are G.R.E.E.N., the official AI tour guide for De La Salle University.
			
								### Task
								Provide a short, engaging introduction to this building.
			
								### Structured Plan
								$buildingPlanJson
			
								### Rules
								1. Use only the facts in the structured plan.
								2. Do not explain your reasoning.
								3. Do not include planning text.
								4. Write exactly 2 short paragraphs.
								5. Separate paragraphs with one blank line.
								6. Output ONLY the narration.
			
							""".trimIndent()

							Log.d("LLM_FINAL_PROMPT", buildingFinalPrompt)

							chatApi.generate(ChatRequest(buildingFinalPrompt)).enqueue(object : Callback<ChatResponse> {

								override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
									if (response.isSuccessful) {

										val botReply = response.body()?.response ?: "Area Entered!"
										Log.d("LLM_RESPONSE", botReply)

										addBotMessageWithProgressiveInfo(botReply)

										lifecycleScope.launch {
											dialogueHistoryDao.insert(
												DialogueHistoryEntity(
													userId = userId,
													userText = "geofence trigger",
													systemResponse = botReply,
													contextSnapshot = buildingFinalPrompt,
													turnNumber = messages.size
												)
											)
										}

										val responseTime = System.currentTimeMillis() - startTime
										lifecycleScope.launch(Dispatchers.IO) {
											metricsCollector.recordQueryResponse(sessionId, responseTime)
										}

									} else {
										Log.e("ChatApi", "Final failed: ${response.errorBody()?.string()}")
									}
								}

								override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
									Log.e("ChatApi", "Final error: ${t.message}", t)
								}
							})

						} else {
							Log.e("ChatApi", "Plan failed: ${response.errorBody()?.string()}")
						}
					}

					override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
						Log.e("ChatApi", "Plan error: ${t.message}", t)
					}
				})
			}
		}
	}

	private val floorReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val startTime = System.currentTimeMillis()
			val floor = intent?.getIntExtra("floorNumber", 0) ?: return
			if (floor == 0) return // Invalid floor
			val poiId = intent.getStringExtra("poiId") ?: return
			Log.d("FLOOR_SELECTED", "Floor selected: $floor")
			Log.d("FLOOR_SELECTED", "Building ID: $poiId")

			val currentPoi = GeofenceReceiver.currentPoiInside
			runOnUiThread {
				updateCurrentLocationOverlay(currentPoi, floor)
			}

			Log.d("FLOOR_SELECTED", "currentPoi: $currentPoi")

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
				Log.d("FLOOR_DATA", floorData)
				val cleanPoiJson = cleanPoiJson(floorData)

				// =========================
				// STAGE 1: PLANNING
				// =========================
				val floorPlanPrompt = """
					### Role
					You are an internal planning assistant for a campus tour AI.
			
					### Task
					Analyze the floor data and extract the most relevant visitor-facing information for this floor.
			
					### Input
					Building ID: $poiId
					Floor Number: $floor
					Floor Data:
					$cleanPoiJson
			
					### Rules
					1. Use only the provided floor data.
					2. Extract only amenities, facilities, services, or notable details explicitly present in the data.
					3. Do not invent anything not in the input.
					4. Output ONLY a valid JSON object.
					5. Do not include markdown, explanations, or extra text.
			
					### Output Format
					{
					  "building_id": "string",
					  "floor_number": 0,
					  "amenities": ["amenity 1", "amenity 2"],
					  "notable_details": ["detail 1", "detail 2"]
					}
				""".trimIndent()

				Log.d("LLM_FLOOR_PLAN_PROMPT", floorPlanPrompt)
				Log.d("LLM_FLOOR_DATA", floorData)

				chatApi.generate(ChatRequest(floorPlanPrompt)).enqueue(object : Callback<ChatResponse> {
					override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
						if (response.isSuccessful) {
							val rawPlan = response.body()?.response ?: "{}"
							val floorPlanJson = extractJsonPayload(rawPlan)

							Log.d("LLM_FLOOR_PLAN_RAW", rawPlan)
							Log.d("LLM_FLOOR_PLAN_JSON", floorPlanJson)

							// =========================
							// STAGE 2: FINAL RESPONSE
							// =========================
							val floorFinalPrompt = """
								### Persona
								You are G.R.E.E.N., the official AI tour guide for De La Salle University. You are helpful and love showing off the campus amenities.
			
								### Task
								Generate a short floor description for the user.
			
								### Structured Plan
								$floorPlanJson
			
								### Rules
								1. Use only the facts in the structured plan.
								2. Mention only amenities or details explicitly present in the structured plan.
								3. Do not explain your reasoning.
								4. Do not include planning text.
								5. Write exactly 2 short paragraphs.
								6. Separate the paragraphs with exactly one blank line.
								7. Output ONLY the final narration text.
							""".trimIndent()

							Log.d("LLM_FLOOR_FINAL_PROMPT", floorFinalPrompt)

							chatApi.generate(ChatRequest(floorFinalPrompt)).enqueue(object : Callback<ChatResponse> {
								override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
									if (response.isSuccessful) {
										val botReply = response.body()?.response ?: "Area Entered!"
										Log.d("LLM_RESPONSE", botReply)
										addBotMessageWithProgressiveInfo(botReply)

										lifecycleScope.launch {
											dialogueHistoryDao.insert(
												DialogueHistoryEntity(
													userId = userId,
													userText = "geofence trigger",
													systemResponse = botReply,
													contextSnapshot = floorFinalPrompt,
													turnNumber = messages.size
												)
											)
										}

										val responseTime = System.currentTimeMillis() - startTime
										lifecycleScope.launch(Dispatchers.IO) {
											metricsCollector.recordQueryResponse(sessionId, responseTime)
										}
									} else {
										Log.e("ChatApi", "Failed final floor prompt: ${response.errorBody()?.string()}")
									}
								}

								override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
									Log.e("ChatApi", "Final floor prompt error: ${t.message}", t)

									runOnUiThread {
										messages.add(
											ChatMessage(
												text = "The AI server is currently unavailable. Please try again.",
												isUser = false,
												suggestions = buildSuggestionsForCurrentState(false)
											)
										)
										adapter.notifyItemInserted(messages.size - 1)
										homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
									}
								}
							})
						} else {
							Log.e("ChatApi", "Failed floor plan prompt: ${response.errorBody()?.string()}")
						}
					}

					override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
						Log.e("ChatApi", "Floor plan prompt error: ${t.message}", t)

						runOnUiThread {
							messages.add(
								ChatMessage(
									text = "The AI server is currently unavailable. Please try again.",
									isUser = false,
									suggestions = buildSuggestionsForCurrentState(false)
								)
							)
							adapter.notifyItemInserted(messages.size - 1)
							homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
						}
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
		metricsCollector = MetricsCollector(this)
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
		Log.d("SESSION_ID", "Generated sessionId = $sessionId")

		//tourCoordinator = TourCoordinator(userId, this)
		tourCoordinator = TourCoordinator(userId, this, metricsCollector, sessionId)
		lifecycleScope.launch {
			try {
				Log.d("HomeActivity", "Saving user")
				val user = UserEntity(
					userId = userId
				)
				db.userDao().insert(user)

				Log.d("HomeActivity", "Saving session")
				val sessionProfile = SessionEntity(
					sessionId = sessionId,
					userId = user.userId,
					componentsUsed = null,
					sessionStartedAt = System.currentTimeMillis().toString()
				)
				db.sessionDao().insert(sessionProfile)
			} catch (e: Exception) {
				Log.e("HomeActivity", "User/session save failed: ${e.localizedMessage}", e)
			}
		}
//
//		lifecycleScope.launch {
//			try {
//				withContext(Dispatchers.IO) {
//					Log.d("HomeActivity", "Saving user")
//					saveUserLocally(user)
//
//					Log.d("HomeActivity", "Saving session")
//					saveSessionLocally(sessionProfile)
//				}
//			} catch (e: Exception) {
//				Log.e("HomeActivity", "User/session save failed: ${e.localizedMessage}", e)
//			}
//		}

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

		// Setup Recycler View
		adapter = MyAdapter(messages) { suggestion ->
			when (suggestion) {
				"Change Floor" -> handleChangeFloorAction()
				"Edit Tour" -> showEditTourOverlay()
				"More Info" -> showNextMoreInfoParagraph()
				"Ask" -> { isAsking = true }
				"Next Stop" -> goToNextStop()
			}
		}
		homeBinding.recyclerViewChatReplies.adapter = adapter
		homeBinding.recyclerViewChatReplies.layoutManager = LinearLayoutManager(this)


		// show input box while keyboard is open
		homeBinding.recyclerViewChatReplies.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
			if (bottom < oldBottom) {
				val lastPos = adapter.itemCount - 1
				if (lastPos >= 0) {
					// delay slightly to allow the layout to finish resizing
					homeBinding.recyclerViewChatReplies.postDelayed({
						homeBinding.recyclerViewChatReplies.scrollToPosition(lastPos)
					}, 100)
				}
			}
		}


		updateCurrentLocationOverlay(null)

		homeBinding.currentLocationOverlay.setOnClickListener {
			val currentPoi = GeofenceReceiver.currentPoiInside
			if (currentPoi != null && (currentPoi.floors ?: 1) > 1) {
				showFloorSelectionDialog(currentPoi)
			}
		}

		// Setup Retrofit
		setupRetrofit()

//		// Setup location and geofencing
		lifecycleScope.launch {
			try {
				withContext(Dispatchers.IO) {
					syncPoisToRoom()
				}
			} catch (e: Exception) {
				Log.e("HomeActivity", "POI sync failed: ${e.localizedMessage}", e)
			}

			setupLocationAndGeofence()
		}

//		val testIntent = Intent(this, GeofenceReceiver::class.java)
//		testIntent.action = "com.thsst2.greenapp.GEOFENCE_TRANSITION_ACTION"
//		sendBroadcast(testIntent)

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
						//setupGeofences()
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
		onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				if (tourStarted && !sessionEnded) {
					showEndTourDialog(true)
				} else {
					finish()
				}
			}
		})
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

	// LOADED MODEL USING GOOGLE COLAB
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

	// LOADED MODEL USING GCP VIRTUAL MACHINE
	// Uncomment to use VM and comment out the setupRetrofit() above
	// NOTE: vmApiUrl may change
//	private fun setupRetrofit() {
//		val okHttpClient = OkHttpClient.Builder()
//			.connectTimeout(30, TimeUnit.SECONDS)
//			.readTimeout(120, TimeUnit.SECONDS)
//			.writeTimeout(120, TimeUnit.SECONDS)
//			.build()
//
//		val vmApiUrl = "http://35.221.229.69:8000" // this may change
//		val retrofit = Retrofit.Builder()
//			.baseUrl("$vmApiUrl/")
//			.client(okHttpClient)
//			.addConverterFactory(GsonConverterFactory.create())
//			.build()
//
//		chatApi = retrofit.create(ChatApi::class.java)
//
//		setupMessageInput()
//	}

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
	private fun splitIntoParagraphs(text: String): List<String> {
		return text
			.split(Regex("\\n\\s*\\n"))
			.map { it.trim() }
			.filter { it.isNotEmpty() }
	}

	private fun buildSuggestionsForCurrentState(hasMoreInfo: Boolean): List<String> {
		val suggestions = mutableListOf<String>()
//		suggestions.add("Ask") // comment out after testing

		if (hasMoreInfo) {
			suggestions.add("More Info")
		}

		if (tourStarted && currentTourPathSequence.isNotEmpty()) {
			suggestions.add("Edit Tour")
			suggestions.add("Ask")
			suggestions.add("Next Stop")
		}

		val currentPoi = GeofenceReceiver.currentPoiInside
		if (currentPoi != null && (currentPoi.floors ?: 1) > 1) {
			suggestions.add("Change Floor")
		}

		return suggestions
	}

	private fun addBotMessageWithProgressiveInfo(fullReply: String) {
		val paragraphs = splitIntoParagraphs(fullReply)
		Log.d("LLM_PARAGRAPHS", paragraphs.joinToString(" | "))

		if (paragraphs.isEmpty()) return

		pendingMoreInfoParagraphs = paragraphs
		nextParagraphIndex = 1

		val firstParagraph = paragraphs.first()
		val suggestions = buildSuggestionsForCurrentState(
			hasMoreInfo = nextParagraphIndex < pendingMoreInfoParagraphs.size
		)

		messages.add(
			ChatMessage(
				text = firstParagraph,
				isUser = false,
				suggestions = suggestions
			)
		)
		adapter.notifyItemInserted(messages.size - 1)
		homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
	}

	private fun showNextMoreInfoParagraph() {
		if (nextParagraphIndex >= pendingMoreInfoParagraphs.size) {
			Toast.makeText(this, "No more info available.", Toast.LENGTH_SHORT).show()
			return
		}

		val nextParagraph = pendingMoreInfoParagraphs[nextParagraphIndex]
		nextParagraphIndex++

		val suggestions = buildSuggestionsForCurrentState(
			hasMoreInfo = nextParagraphIndex < pendingMoreInfoParagraphs.size
		)

		messages.add(
			ChatMessage(
				text = nextParagraph,
				isUser = false,
				suggestions = suggestions
			)
		)
		adapter.notifyItemInserted(messages.size - 1)
		homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
	}
	private fun handleChangeFloorAction() {
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
	private fun goToNextStop() {
		if (!tourStarted || currentTourPathSequence.isEmpty()) {
			Toast.makeText(this, "No active tour path yet.", Toast.LENGTH_SHORT).show()
			return
		}

		if (currentTourStopIndex >= currentTourPathSequence.lastIndex) {
			messages.add(
				ChatMessage(
					text = "You’ve already reached the last stop of the tour.",
					isUser = false,
					suggestions = buildSuggestionsForCurrentState(hasMoreInfo = false)
				)
			)
			adapter.notifyItemInserted(messages.size - 1)
			homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)

			resetTour()

			return
		}

		currentTourStopIndex++

		val nextStopName = currentTourPathSequence[currentTourStopIndex]

		messages.add(
			ChatMessage(
				text = "Next stop: $nextStopName",
				isUser = false,
				suggestions = buildSuggestionsForCurrentState(hasMoreInfo = false)
			)
		)
		adapter.notifyItemInserted(messages.size - 1)
		homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)

		// Track POI visit for metrics
		lifecycleScope.launch(Dispatchers.IO) {
			val userPrefs = db.userPreferencesDao().getPreferencesByUser(userId)
			val preferences = userPrefs?.interests ?: emptyList()
			// Check if this POI matches user preferences (simplified check)
			val wasPreferred = preferences.any { pref -> 
				nextStopName.contains(pref, ignoreCase = true)
			}
			metricsCollector.recordPoiVisit(sessionId, wasPreferred)
		}

		val mapFragment =
			supportFragmentManager.findFragmentById(R.id.home_map_fragment) as? SupportMapFragment

		mapFragment?.getMapAsync { googleMap ->
			lifecycleScope.launch(Dispatchers.IO) {
				val pois = db.poiDao().getAll()
				val nextPoi = pois.find { it.name == nextStopName }

				withContext(Dispatchers.Main) {
					if (nextPoi != null) {
						val latLng = LatLng(nextPoi.latitude, nextPoi.longitude)
						googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
					} else {
						Toast.makeText(
							this@AndroidSmallHomeActivity,
							"Could not find the next stop on the map.",
							Toast.LENGTH_SHORT
						).show()
					}
				}
			}
		}
	}

	private fun showEditTourOverlay() {
		if (currentTourPathSequence.isEmpty()) {
			Toast.makeText(this, "No active tour path.", Toast.LENGTH_SHORT).show()
			return
		}

		val editablePath = currentTourPathSequence.toMutableList()

		val dialogContainer = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(32, 24, 32, 16)
		}

		val titleView = TextView(this).apply {
			text = "Edit Tour Order"
			textSize = 18f
			setTypeface(null, android.graphics.Typeface.BOLD)
		}

		val pathListContainer = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
		}

		fun renderPathRows() {
			pathListContainer.removeAllViews()

			if (editablePath.isEmpty()) {
				pathListContainer.addView(TextView(this).apply {
					text = "No stops in tour. Add at least one location."
					textSize = 14f
					setPadding(0, 16, 0, 16)
				})
				return
			}

			editablePath.forEachIndexed { index, stopName ->
				val row = LinearLayout(this).apply {
					orientation = LinearLayout.HORIZONTAL
					setPadding(0, 8, 0, 8)
				}

				val label = TextView(this).apply {
					text = "${index + 1}. $stopName"
					textSize = 14f
					layoutParams = LinearLayout.LayoutParams(
						0,
						LinearLayout.LayoutParams.WRAP_CONTENT,
						1f
					)
				}

				val upBtn = Button(this).apply {
					text = "⬆"
					isEnabled = index > 0
					setOnClickListener {
						val item = editablePath.removeAt(index)
						editablePath.add(index - 1, item)
						renderPathRows()
					}
				}

				val downBtn = Button(this).apply {
					text = "⬇"
					isEnabled = index < editablePath.lastIndex
					setOnClickListener {
						val item = editablePath.removeAt(index)
						editablePath.add(index + 1, item)
						renderPathRows()
					}
				}

				val delBtn = Button(this).apply {
					text = "✕"
					setOnClickListener {
						editablePath.removeAt(index)
						renderPathRows()
					}
				}

				row.addView(label)
				row.addView(upBtn)
				row.addView(downBtn)
				row.addView(delBtn)
				pathListContainer.addView(row)
			}
		}

		val addButton = Button(this).apply {
			text = "+ Add Location"
			setOnClickListener {
				lifecycleScope.launch {
					val allBuildings = withContext(Dispatchers.IO) {
						// Fetch ALL buildings from Firebase, not just preference-matched ones in local DB
						RAGEngine().getBuildings()
					}
					
					val availableStops = allBuildings
						.filterNot { it.name in editablePath }
						.sortedBy { it.name }

					if (availableStops.isEmpty()) {
						Toast.makeText(this@AndroidSmallHomeActivity, "No more locations available.", Toast.LENGTH_SHORT).show()
						return@launch
					}

					AlertDialog.Builder(this@AndroidSmallHomeActivity)
						.setTitle("Add Location")
						.setItems(availableStops.map { it.name }.toTypedArray()) { _, which ->
							val selectedPoi = availableStops[which]
							editablePath.add(selectedPoi.name)
							
							// Save POI to local database so navigation/geofencing works
							lifecycleScope.launch(Dispatchers.IO) {
								try {
									// Check if POI already exists in database
									val existingPoi = db.poiDao().getPoiById(selectedPoi.poiId)
									if (existingPoi == null) {
										// Save new POI to local database
										db.poiDao().insert(selectedPoi)
										Log.d("EditTour", "Saved manually added POI: ${selectedPoi.name}")
									}
								} catch (e: Exception) {
									Log.e("EditTour", "Failed to save POI: ${e.message}")
								}
							}
							
							renderPathRows()
						}
						.setNegativeButton("Cancel", null)
						.show()
				}
			}
		}

		val scrollView = ScrollView(this).apply {
			addView(pathListContainer)
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				0,
				1f
			)
		}

		renderPathRows()

		dialogContainer.addView(titleView)
		dialogContainer.addView(addButton)
		dialogContainer.addView(scrollView)

		AlertDialog.Builder(this)
			.setView(dialogContainer)
			.setNegativeButton("Cancel", null)
			.setPositiveButton("Save") { _, _ ->
				if (editablePath.isEmpty()) {
					Toast.makeText(this, "Add at least one stop before saving.", Toast.LENGTH_SHORT).show()
					return@setPositiveButton
				}

				currentTourPathSequence = editablePath.toList()
				messages.add(ChatMessage(
					text = "Tour updated! New order: ${currentTourPathSequence.joinToString(" → ")}",
					isUser = false,
					suggestions = buildSuggestionsForCurrentState(hasMoreInfo = false)
				))
				adapter.notifyItemInserted(messages.size - 1)
				homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
			}
			.show()

		resetTour()
		Log.d("HomeActivity", "Tour path updated.")
		Log.d("HomeActivity", "Tour path reset.")
	}

	private fun cleanPoiJson(rawJson: String): String {
		val gson = Gson()
		return try {
			val listType = object : com.google.gson.reflect.TypeToken<List<Any>>() {}.type
			val rawList: List<Any> = gson.fromJson(rawJson, listType)

			// Recursive helper to walk through the JSON tree
			fun clean(node: Any?): Any? {
				return when (node) {
					is Map<*, *> -> {
						// BLACKLIST: Technical data that bloats the prompt but the AI doesn't need
						val noise = setOf(
							"edges", "coordinates", "graphVersion", "radius", "edgeId",
							"from", "to", "metric", "w", "attrs", "accuracyRadius",
							"history_id", "entity_id", "cultural_landmark_id",
							"near_building_id", "sources", "lat", "lng",
							"bidirectional", "max_floors", "campus_tags"
						)

						val cleaned = mutableMapOf<String, Any?>()
						for ((key, value) in node) {
							val k = key.toString()
							// If it's a "noisy" key, skip it. If it's an index (0, 1, 2) or a "good" key, keep it.
							if (k !in noise) {
								cleaned[k] = clean(value)
							}
						}
						if (cleaned.isEmpty()) null else cleaned
					}
					is List<*> -> {
						val cleanedList = node.map { clean(it) }.filterNotNull()
						if (cleanedList.isEmpty()) null else cleanedList
					}
					else -> node
				}
			}

			// Clean the entire list
			val finalOutput = rawList.mapNotNull { clean(it) }
			gson.toJson(finalOutput)
		} catch (e: Exception) {
			Log.e("HomeActivity", "Error cleaning JSON: ${e.message}")
			rawJson // Fallback to raw if it breaks
		}
	}

	private fun extractJsonPayload(rawText: String): String {
		val startObj = rawText.indexOf('{')
		val endObj = rawText.lastIndexOf('}')

		val startArr = rawText.indexOf('[')
		val endArr = rawText.lastIndexOf(']')

		return when {
			startObj != -1 && endObj != -1 && endObj > startObj -> {
				rawText.substring(startObj, endObj + 1)
			}
			startArr != -1 && endArr != -1 && endArr > startArr -> {
				rawText.substring(startArr, endArr + 1)
			}
			else -> rawText.trim()
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
		Log.d("HomeActivity", "userId: $userId")
		val userRole = db.userRoleDao().getUserRoleById(userId)
		Log.d("HomeActivity", "userRole: $userRole")
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

		// Let DialogueManager process the input and track response time
		val startTime = System.currentTimeMillis()
		val dmResult = if (tourStarted) null else dialogueManager.processMessage(userId, userMessage)

//		val metrics = PerformanceMetricsEntity(
//			sessionId = sessionId,
//			routeAccuracyScore = null,
//			pathGenerationTimeMs = null,
//			avgResponseTimeMs = null,
//			preferenceMatchScore = null,
//			visitedPreferredRatio = null
//		)
//
//		performanceMetricsDao.insert(metrics)

		when (dmResult?.intent) {
			IntentType.MORE_INFO -> {
				showNextMoreInfoParagraph()
				return
			}

			IntentType.CHANGE_FLOOR -> {
				handleChangeFloorAction()
				return
			}

			IntentType.NEXT_STOP -> {
				goToNextStop()
				return
			}

			else -> {
				// continue normal flow
			}
		}

		if (dmResult?.message?.isNotBlank() == true) {
			val suggestions = if (tourStarted) {
				listOf("More Info", "Next Stop", "Change Floor")
			} else {
				emptyList()
			}
			addBotMessageWithProgressiveInfo(dmResult.message)
		}

		// Open profile preferences edit box
		if (dmResult?.openProfileForPrefs == true){
			val intent = Intent(this@AndroidSmallHomeActivity, AndroidSmallProfileActivity::class.java)
			intent.putExtra("open_prefs_dialog", true)
			startActivity(intent)
			return
		}

		// If user wants a tour, call TourCoordinator
		if (dmResult?.context == ConversationContext.TOUR_READY && !tourStarted){
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
				currentTourPathSequence = userTourPathHistory?.pathSequence ?: emptyList()
				currentTourStopIndex = -1

				val mapFragment = supportFragmentManager.findFragmentById(R.id.home_map_fragment) as? SupportMapFragment
				mapFragment?.getMapAsync { googleMap ->
					lifecycleScope.launch {
						drawTourPath(googleMap, userTourPathHistory.pathSequence)
						homeBinding.mapLoadingIndicator.visibility = View.GONE
					}
				}

				val poiJson = Gson().toJson(userTourPathHistory.pathSequence)
//				val localData = db.localDataDao().getLocalData(userId)
//				val poiInfoOnly = poiData?.poiInfoJson ?: "[]"
//				val cleanPoiJson = cleanPoiJson(poiInfoOnly)
				val poiData = ragEngine.getTourPlanBuildings(userTourPathHistory.pathSequence)
				val poiInfoOnly = Gson().toJson(poiData)
				val cleanPoiJson = cleanPoiJson(poiInfoOnly)
				val startingPoint = null // building starting point

				// =========================
				// STAGE 1: PLANNING
				// =========================
				val tourPlanPrompt = """
					### SYSTEM
					You are a JSON generator.
					
					### TASK
					Extract structured tour data from the input.
					
					### INPUT
					User Role: $userRoleName
					User Interests: $allPreferences
					
					Tour Route:
					$poiJson
					
					Building Details:
					$cleanPoiJson
					
					### STRICT RULES
					1. Output ONLY valid JSON.
					2. Do NOT write explanations.
					3. Do NOT write sentences.
					4. Do NOT include any text outside JSON.
					5. If you fail to follow this, the output is invalid.
					
					### INSTRUCTIONS
					- Follow the route order exactly.
					- Include each building exactly once.
					- Extract 1 to 2 short facts per building.
					- Use ONLY the provided data.
					- Do NOT invent information.
					
					### EXAMPLE OUTPUT
					[
					  {
						"building": "St. La Salle Hall",
						"facts": [
						  "Historic campus landmark",
						  "Central academic and administrative building"
						]
					  }
					]
					
					### OUTPUT
					""".trimIndent()

				Log.d("HomeActivity", "User Role Name: $userRoleName")
				Log.d("HomeActivity", "All Preferences: $allPreferences")
				Log.d("HomeActivity", "Starting Point: $startingPoint")
				Log.d("HomeActivity", "POI Sequence: $poiJson")
				logLargeString("HomeActivity", "POI Info Only: $poiInfoOnly")
				logLargeString("HomeActivity", "Cleaned POI Info: $cleanPoiJson")
				Log.d("HomeActivity", "POI Data: $poiData")
				Log.d("HomeActivity", "tourPlanPrompt: $tourPlanPrompt")

				chatApi.generate(ChatRequest(tourPlanPrompt)).enqueue(object : Callback<ChatResponse> {
					override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
						if (response.isSuccessful) {
							val rawPlan = response.body()?.response ?: "[]"
							val tourPlanJson = extractJsonPayload(rawPlan)

							Log.d("LLM_TOUR_PLAN_RAW", rawPlan)
							logLargeString("LLM_TOUR_PLAN_JSON", tourPlanJson)

							// =========================
							// STAGE 2: FINAL RESPONSE
							// =========================
							val tourFinalPrompt = """
								### Persona
								You are G.R.E.E.N., the official AI tour guide for De La Salle University.
								
								### Task
								Generate a guided tour narration that walks the user through the campus step-by-step.
								
								### Structured Plan
								$tourPlanJson
								
								### CRITICAL INSTRUCTIONS
								- This is a TOUR, not a list of descriptions.
								- You must describe movement between locations.
								- Use a narrative flow like a real tour guide.
								
								### REQUIRED STYLE
								- Start with: "We begin our tour at..."
								- For every next building, use transitions like:
								  - "Next, we head to..."
								  - "From there, we continue to..."
								  - "Our next stop is..."
								- Maintain a sense of progression through the campus.
								
								### RULES
								1. Follow the building order exactly.
								2. Mention each building exactly once.
								3. Write exactly ${userTourPathHistory.pathSequence.size} paragraphs.
								4. Each paragraph must describe only one building.
								5. Each paragraph must contain exactly 1 to 2 sentences.
								6. Separate paragraphs with exactly one blank line.
								7. Use only the facts in the structured plan.
								8. Do NOT add headings, titles, or labels.
								9. Do NOT explain your reasoning.
								10. Output ONLY the narration.
								
							""".trimIndent()

							Log.d("HomeActivity", "tourFinalPrompt: $tourFinalPrompt")

							chatApi.generate(ChatRequest(tourFinalPrompt)).enqueue(object : Callback<ChatResponse> {
								override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
									if (response.isSuccessful) {
										val botReply = response.body()?.response ?: "Tour overview ready."
										Log.d("LLM_LENGTH", "Length = ${botReply.length}")
										Log.d("LLM_ENDING", "Ending = ${botReply.takeLast(80)}")
										logLongText("LLM_RESPONSE", botReply)

										addBotMessageWithProgressiveInfo(botReply)

										lifecycleScope.launch {
											dialogueHistoryDao.insert(
												DialogueHistoryEntity(
													userId = userId,
													userText = userMessage,
													systemResponse = botReply,
													contextSnapshot = tourFinalPrompt,
													turnNumber = messages.size
												)
											)
										}

										val responseTime = System.currentTimeMillis() - startTime
										lifecycleScope.launch(Dispatchers.IO) {
											metricsCollector.recordQueryResponse(sessionId, responseTime)
										}
									} else {
										Log.e("ChatApi", "Failed final tour prompt: ${response.errorBody()?.string()}")
									}
								}

								override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
									Log.e("ChatApi", "Final tour prompt error: ${t.message}", t)

									runOnUiThread {
										messages.add(
											ChatMessage(
												text = "The AI server is currently unavailable. Please try again.",
												isUser = false,
												suggestions = buildSuggestionsForCurrentState(false)
											)
										)
										adapter.notifyItemInserted(messages.size - 1)
										homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
									}
								}
							})
						} else {
							Log.e("ChatApi", "Failed tour plan prompt: ${response.errorBody()?.string()}")
						}
					}

					override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
						Log.e("ChatApi", "Tour plan prompt error: ${t.message}", t)

						runOnUiThread {
							messages.add(
								ChatMessage(
									text = "The AI server is currently unavailable. Please try again.",
									isUser = false,
									suggestions = buildSuggestionsForCurrentState(false)
								)
							)
							adapter.notifyItemInserted(messages.size - 1)
							homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
						}
					}
				})
			}
		} else if (isAsking) {
			isAsking = false

//			val poiJson = Gson().toJson(db.userTourPathHistoryDao().getById(userId)?.pathSequence)
////			val poiData = db.localDataDao().getLocalData(userId)
			val startingPoint = null
			val allTags = ragEngine.getTags()
			val aiTagPrompt = """
				### Task
				You are a metadata classifier. Identify relevant tags from the user's query that match the available tags list.

				### Available Tags
				$allTags

				### User Query
				"$userMessage"

				### Rules
				1. Output ONLY a valid JSON array of strings.
				2. If no tags match, output [].
				3. Do not include any explanations or extra text.

				### Examples
				Query: "Are there elevators in Andrew Hall?" -> ["acc_elevator"]
				Query: "I want to see old buildings." -> ["type_historic"]
				
				### Output Format
				Return ONLY the JSON array. Do not include markdown code blocks (like ```json).
				
				Correct Output: ["acc_elevator", "loc_building"]
				Incorrect Output: The tags are ["acc_elevator"]
				
				JSON Array:
			""".trimIndent()

			Log.d("HomeActivity", "aiTagPrompt: $aiTagPrompt")
			Log.d("HomeActivity", "User Message: $userMessage")
			Log.d("HomeActivity", "All Tags: $allTags")

			chatApi.generate(ChatRequest(aiTagPrompt)).enqueue(object : Callback<ChatResponse> {
				override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
					if (response.isSuccessful) {
						val tagResponse = response.body()?.response ?: "[]"
						Log.d("HomeActivity", "Tag Response: $tagResponse")
						val relevantTags: List<String> = try {
							// Find the first occurrence of [...]
							val regex = Regex("\\[(.*?)\\]")
							val match = regex.find(tagResponse)

							if (match != null) {
								var jsonPart = match.value

								// Add quotes if the LLM returned [tag1, tag2] instead of ["tag1", "tag2"]
								if (!jsonPart.contains("\"")) {
									jsonPart = jsonPart.replace(Regex("([a-zA-Z0-9_]+)"), "\"$1\"")
								}

								Gson().fromJson(jsonPart, Array<String>::class.java).toList()
							} else {
								emptyList()
							}
						} catch (e: Exception) {
							Log.e("ChatApi", "Tag extraction failed: ${e.message}")
							emptyList()
						}

						Log.d("HomeActivity", "Tags Chosen: $relevantTags")

						lifecycleScope.launch {
							try {
								// Filter POI data based on relevant tags
								val filteredPoiData = ragEngine.filterPoiData(relevantTags)
								val cleanPoiJson = cleanPoiJson(filteredPoiData)
								// =========================
								// STAGE 1: PLANNING
								// =========================
								val qaPlanPrompt = """
									### Role
									You are an internal planning assistant for a campus tour AI.
								
									### Task
									Determine whether the user's question can be answered using the provided context, and extract only the supporting facts.
								
									### Input
									User Question: "$userMessage"
									User Role: $userRoleName
								
									Context JSON:
									$cleanPoiJson
								
									### Rules
									1. Use only the provided context.
									2. Identify the most relevant entity, if any.
									3. Extract only facts that directly support the answer.
									4. If the context is insufficient, mark the question as unanswerable.
									5. Do not write the final answer.
									6. Output ONLY a valid JSON object.
									7. Do not include markdown, explanations, or extra text.
								
									### Output Format
									{
									  "answerable": true,
									  "matched_entity": "string or null",
									  "supporting_facts": [
										"fact 1",
										"fact 2"
									  ]
									}
								""".trimIndent()

								Log.d("HomeActivityQuestion", "qaPlanPrompt: $qaPlanPrompt")
								Log.d("HomeActivityQuestion", "User Message: $userMessage")
								Log.d("HomeActivityQuestion", "Filter POI Data: $filteredPoiData")
								Log.d("HomeActivityQuestion", "Cleaned POI Info: $cleanPoiJson")

								chatApi.generate(ChatRequest(qaPlanPrompt)).enqueue(object : Callback<ChatResponse> {
									override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
										if (response.isSuccessful) {
											val rawPlan = response.body()?.response ?: "{}"
											val qaPlanJson = extractJsonPayload(rawPlan)

											Log.d("LLM_QA_PLAN_RAW", rawPlan)
											Log.d("LLM_QA_PLAN_JSON", qaPlanJson)

											// =========================
											// STAGE 2: FINAL RESPONSE
											// =========================
											val qaFinalPrompt = """
												### Persona
												You are G.R.E.E.N., the official AI tour guide for De La Salle University.
								
												### Task
												Answer the user's question using the structured plan.
								
												### User Question
												"$userMessage"
								
												### Structured Plan
												$qaPlanJson
								
												### Rules
												1. Use only the supporting facts in the structured plan.
												2. If "answerable" is false or no supporting facts are present, reply exactly:
												"I'm not sure, but I can help you with other campus information."
												3. Do not explain your reasoning.
												4. Do not include planning text.
												5. Keep the answer short, direct, and friendly.
												6. Output ONLY the final answer text.
											""".trimIndent()

											Log.d("HomeActivityQuestion", "qaFinalPrompt: $qaFinalPrompt")

											chatApi.generate(ChatRequest(qaFinalPrompt)).enqueue(object : Callback<ChatResponse> {
												override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
													if (response.isSuccessful) {
														val botReply = response.body()?.response ?: "Answer:"
														Log.d("LLM_RESPONSE_TO_QUESTION", botReply)

														messages.add(
															ChatMessage(
																text = botReply,
																isUser = false,
																suggestions = buildSuggestionsForCurrentState(hasMoreInfo = false)
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
																	contextSnapshot = qaFinalPrompt,
																	turnNumber = messages.size
																)
															)
														}

														val responseTime = System.currentTimeMillis() - startTime
														lifecycleScope.launch(Dispatchers.IO) {
															metricsCollector.recordQueryResponse(sessionId, responseTime)
														}
													} else {
														Log.e("ChatApi", "Failed final QA prompt: ${response.errorBody()?.string()}")
													}
												}

												override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
													Log.e("ChatApi", "Final QA prompt error: ${t.message}", t)

													runOnUiThread {
														messages.add(
															ChatMessage(
																text = "The AI server is currently unavailable. Please try again.",
																isUser = false,
																suggestions = buildSuggestionsForCurrentState(false)
															)
														)
														adapter.notifyItemInserted(messages.size - 1)
														homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
													}
												}
											})
										} else {
											Log.e("ChatApi", "Failed QA plan prompt: ${response.errorBody()?.string()}")
										}
									}

									override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
										Log.e("ChatApi", "QA plan prompt error: ${t.message}", t)

										runOnUiThread {
											messages.add(
												ChatMessage(
													text = "The AI server is currently unavailable. Please try again.",
													isUser = false,
													suggestions = buildSuggestionsForCurrentState(false)
												)
											)
											adapter.notifyItemInserted(messages.size - 1)
											homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
										}
									}
								})
							} catch (e: Exception) {
								Log.e("HomeActivity", "Error processing tags: ${e.message}")
							}
						}
					} else {
						Log.e("ChatApi", "Failed: ${response.errorBody()?.string()}")
					}
				}

				override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
					Log.e("ChatApi", "Error: ${t.message}", t)

					runOnUiThread {
						messages.add(
							ChatMessage(
								text = "The AI server is currently unavailable. Please try again.",
								isUser = false,
								suggestions = buildSuggestionsForCurrentState(false)
							)
						)
						adapter.notifyItemInserted(messages.size - 1)
						homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
					}
				}
			})
		}
	}

	private fun logLongText(tag: String, text: String) {
		val chunkSize = 1000
		var start = 0
		while (start < text.length) {
			val end = minOf(start + chunkSize, text.length)
			Log.d(tag, text.substring(start, end))
			start = end
		}
	}

	// NAVIGATION BAR
	private fun setupNavigationBar() {
		homeBinding.homeButton.setOnClickListener {
			if (tourStarted && !sessionEnded) {
				showEndTourDialog(false)
			} else {
				recreate()
			}
		}
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
	private suspend fun saveSessionLocally(session: SessionEntity) {
		val repo = SessionRepository(db)
		repo.insertSession(session, true)
	}

	private suspend fun saveUserLocally(user: UserEntity) {
		val repo = UserRepository(db)
		repo.insertUser(user, true)
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
			Log.d("END_TOUR", "Starting endTour for sessionId=$sessionId")

			// Finalize and save performance metrics for this session
			metricsCollector.finalizeSessionMetrics(sessionId)

			// Mark session as ended
			db.sessionDao().setSessionEndedAt(
				sessionId = sessionId,
				endedAt = System.currentTimeMillis().toString()
			)
			Log.d("END_TOUR", "Marked session as ended for sessionId=$sessionId")

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
				performanceMetrics = db.performanceMetricsDao().getAll(),
				sessions = db.sessionDao().getSessionsByUser(userId),
				userLocations = db.userLocationDao().getAll(),
				userLogs = db.userLogDao().getUserLogsByUser(userId),
				userSkippedOrDislikedLocations = db.userSkippedOrDislikedLocationDao().getAll(),
				userTourPathHistories = db.userTourPathHistoryDao().getAll(),
				userVisitedLocations = db.userVisitedLocationDao().getAll(),
			)

			sessionLogDao.insert(sessionLog)

			Log.d("HomeActivity", "Session log saved to Firebase. ${sessionLog.sessionLogId}")
			lifecycleScope.launch(Dispatchers.IO) {
				saveSessionLogToFirebase(sessionLog)
			}

			CoroutineScope(Dispatchers.IO).launch {
				db.withTransaction {
					db.dialogueHistoryDao().deleteAll()
					db.generatedPathDao().deleteAll()
					db.geofenceTriggerDao().deleteAll()
					db.intentLogDao().deleteAll()
					db.localDataDao().deleteAll()
					db.pathDeviationAlertDao().deleteAll()
					db.performanceMetricsDao().deleteAll()
					db.poiDao().deleteAll()
					db.responseJustificationDao().deleteAll()
					db.sessionLogDao().deleteAll()
					db.sessionLogDao().deleteAll()
					db.transitionDao().deleteAll()
//				db.userDao().deleteAll()
					db.userFeedbackDao().deleteAll()
					db.userInteractionTimeDao().deleteAll()
//				db.userLocationDao().deleteAll()
					db.userLogDao().deleteAll()
//				db.userPreferencesDao().deleteAll()
					db.userQueryDao().deleteAll()
//				db.userRoleDao().deleteAll()
					db.userSkippedOrDislikedLocationDao().deleteAll()
					db.userTourPathHistoryDao().deleteAll()
					db.userVisitedLocationDao().deleteAll()
				}
			}
		}
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
					Log.d(
						"HomeActivity",
						"Location update -> lat=${location.latitude}, lng=${location.longitude}"
					)
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
								Log.d(
									"HomeActivity",
									"Entry check -> POI=${poi.name}, distance=$distance, radius=${poi.radius}"
								)

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
		Log.d("HomeActivity", "setupGeofences() called")
		lifecycleScope.launch {
			try {
//				val pois = db.poiDao().getAll()
				val pois = RAGEngine().getBuildings()
				Log.d("HomeActivity", "Found $pois POIs.")

				Log.d("HomeActivity", "POIs fetched for geofencing = ${pois.size}")

//				pois.forEach { poi ->
//					Log.d(
//						"HomeActivity",
//						"Geofence candidate -> name=${poi.name}, lat=${poi.latitude}, lng=${poi.longitude}, radius=${poi.radius}"
//					)
//				}

				if (pois.isEmpty()) {
					Toast.makeText(this@AndroidSmallHomeActivity, "No POIs found.", Toast.LENGTH_SHORT).show()
					return@launch
				}

				val validPois = pois.filter { it.radius > 0.0 }

				Log.d("HomeActivity", "Valid POIs for geofencing = ${validPois.size}")
//				validPois.forEach { poi ->
//					Log.d(
//						"HomeActivity",
//						"Valid geofence -> name=${poi.name}, lat=${poi.latitude}, lng=${poi.longitude}, radius=${poi.radius}"
//					)
//				}

				val invalidPois = pois.filter { it.radius <= 0.0 }
				invalidPois.forEach { poi ->
					Log.w(
						"HomeActivity",
						"Skipping POI with invalid radius -> name=${poi.name}, radius=${poi.radius}"
					)
				}

				val geofences = validPois.map { poi ->
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

				Log.d("HomeActivity", "Submitting geofences to GeofencingClient")
				geofencingClient.addGeofences(geofencingRequest, pendingIntent)
					.addOnSuccessListener {
						Log.d("HomeActivity", "addGeofences SUCCESS for ${pois.size} POIs")
					}
					.addOnFailureListener { e ->
						Log.e("HomeActivity", "addGeofences FAILED: ${e.localizedMessage}", e)
					}
			} catch (e: Exception) {
				Log.e("HomeActivity", "Error setting up geofences: ${e.localizedMessage}")
			}
		}
	}
	private suspend fun syncPoisToRoom() {
		val remotePois = ragEngine.getBuildings()

		if (remotePois.isEmpty()) {
			Log.w("HomeActivity", "syncPoisToRoom: No POIs returned from RAGEngine")
			return
		}

		db.poiDao().deleteAll()
		remotePois.forEach { poi ->
			db.poiDao().insert(poi)
		}

		Log.d("HomeActivity", "syncPoisToRoom: Inserted ${remotePois.size} POIs into Room")
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
			googleMap.addCircle(
				CircleOptions()
					.center(position)
					.radius(poi.radius)  // meters
					.strokeColor(Color.argb(100, 0, 150, 136))
					.fillColor(Color.argb(40, 0, 150, 136))
					.strokeWidth(2f)
			)
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

	private fun showEndTourDialog(closeActivity: Boolean = false) {
		if (!tourStarted || sessionEnded) {
			if (closeActivity) finish()
			else resetTour()
			return
		}

		AlertDialog.Builder(this)
			.setTitle("End Tour")
			.setMessage("Are you sure you want to end your current tour?")
			.setPositiveButton("End Tour") { _, _ ->
				lifecycleScope.launch {
					try {
						sessionEnded = true
						Log.d("SESSION", "User confirmed end tour for sessionId=$sessionId")

						endTour(userId, sessionId)

						Log.d("SESSION", "endTour completed for sessionId=$sessionId")

						if (closeActivity) {
							finish()
						} else {
							resetTour()
						}

					} catch (e: Exception) {
						sessionEnded = false
						Log.e("SESSION", "Failed to end tour: ${e.localizedMessage}", e)
					}
				}
			}
			.setNegativeButton("Cancel", null)
			.show()
	}

	fun logLargeString(tag: String, content: String) {
		if (content.length > 4000) {
			Log.d(tag, content.substring(0, 4000))
			logLargeString(tag, content.substring(4000))
		} else {
			Log.d(tag, content)
		}
	}

	private fun resetTour() {
		// Reset logic flags
		tourStarted = false
		currentTourPathSequence = emptyList()
		currentTourStopIndex = -1
		pendingMoreInfoParagraphs = emptyList()
		nextParagraphIndex = 0

		// Clear Chat
		messages.clear()
		adapter.notifyDataSetChanged()

		// Reset Dialogue Manager
		dialogueManager.reset()

		// Clear Map Path
		val mapFragment = supportFragmentManager.findFragmentById(R.id.home_map_fragment) as? SupportMapFragment
		mapFragment?.getMapAsync { googleMap ->
			// Clear and redraw markers
			googleMap.clear()
			lifecycleScope.launch {
				val fetchedPois = ragEngine.getBuildings()
				drawMarkers(googleMap, fetchedPois)
			}
		}

		// Reset Overlay
		updateCurrentLocationOverlay(null)

		// Restart the initial greeting
		lifecycleScope.launch {
			val result = dialogueManager.processMessage(userId, "hi")
			messages.add(ChatMessage(text = result.message, isUser = false))
			adapter.notifyItemInserted(messages.size - 1)
		}

		Toast.makeText(this, "Tour has been reset.", Toast.LENGTH_SHORT).show()
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
				addBotMessageWithProgressiveInfo(result.message)
			}
		}
	}

	override fun onStop() {
		super.onStop()

//		if (isFinishing && !sessionEnded) {
			sessionEnded = true

			Log.d("SESSION", "App backgrounded → ending session $sessionId")

			endTour(userId, sessionId)
//		}
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

		if (!sessionEnded && userId != -1L && sessionId != -1L) {
			sessionEnded = true
			Log.d("SESSION", "Activity destroyed → final session save $sessionId")
			endTour(userId, sessionId)
		}
	}
}
