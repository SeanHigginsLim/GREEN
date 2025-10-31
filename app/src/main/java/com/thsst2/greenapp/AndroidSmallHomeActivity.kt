package com.thsst2.greenapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
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

class AndroidSmallHomeActivity : AppCompatActivity() {

	private lateinit var homeBinding: ActivityAndroidSmallHomeBinding
	private val messages = mutableListOf<String>()
	private lateinit var adapter: MyAdapter
	private lateinit var chatApi: ChatApi
	private lateinit var auth: FirebaseAuth
	private lateinit var sessionManager: SessionManager

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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		homeBinding = ActivityAndroidSmallHomeBinding.inflate(layoutInflater)
		setContentView(homeBinding.root)

		FirebaseApp.initializeApp(this)
		auth = FirebaseAuth.getInstance()
		sessionManager = SessionManager(this)

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
			userId = userId,
			userRoleId = null,
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
				val existingProfile = db.userDao().getUserById(userId)

				if (existingProfile == null || existingProfile.userRoleId == null) {
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

		// Setup message input
		setupMessageInput()

		// TODO: Setup geofence listener
		setupGeofenceListener()
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
		val userRoleId = db.userDao().getUserById(userId)?.userRoleId ?: 1
		val userRoleName = userRoleDao.getUserRoleById(userRoleId)?.role ?: "student"
		val activePreferences = userPreferencesDao.getPreferencesByUser(userId)
		val userVisitedLocation = userVisitedLocationDao.getById(userId)
		val visitedPOIs = userVisitedLocation?.let { visited ->
			db.poiDao().getPoiById(visited.poiId)?.let { listOf(it) }
		} ?: emptyList()

		val allPreferences = activePreferences.interests + tempAdditionalPreferences

		val contextString = buildString {
			append("I am a $userRoleName. ")
			append("I like ${allPreferences.joinToString(", ")}. ")
			append("I have visited ${visitedPOIs.joinToString(", ") { it.name }}. ")
		}

		val aiPrompt = "$contextString $userMessage"

		messages.add("You: $userMessage")
		adapter.notifyItemInserted(messages.size - 1)
		homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)

		chatApi.generate(ChatRequest(aiPrompt)).enqueue(object : Callback<ChatResponse> {
			override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
				runOnUiThread {
					if (response.isSuccessful && response.body() != null) {
						val botReply = response.body()!!.response
						messages.add("Bot: $botReply")
						adapter.notifyItemInserted(messages.size - 1)
						homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)

						lifecycleScope.launch {
							dialogueHistoryDao.insert(
								DialogueHistoryEntity(
									userId = userId,
									userText = userMessage,
									systemResponse = botReply,
									contextSnapshot = contextString,
									turnNumber = messages.size
								)
							)
						}

						// TODO: Ask for additional preferences & optionally update UserPreferencesEntity
						// TODO: Trigger RAG + path generation
					}
				}
			}

			override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
				runOnUiThread {
					messages.add("Bot: Network connection failed - ${t.localizedMessage}")
					adapter.notifyItemInserted(messages.size - 1)
					homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
				}
			}
		})
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

	// GEOFENCE HANDLER
	private fun setupGeofenceListener() {
		// TODO: Replace with actual geofence listener integration
		// (LOGIC IDEA ONLY)
		// onEnterPoi(poiId: Long)
		//     insert UserVisitedLocationEntity
		//     update UserLocationEntity
		//     display POI info based on floor/preferences
		//
		// onExitPoi(poiId: Long)
		//     insert UserInteractionTimeEntity
		//     track skipped/disliked POIs
		//     check path deviation -> PathDeviationAlertEntity
	}
}
