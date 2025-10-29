package com.thsst2.greenapp
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import android.view.inputmethod.EditorInfo
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.thsst2.greenapp.data.MyAppDatabase
import com.thsst2.greenapp.databinding.ActivityAndroidSmallHomeBinding
import java.util.UUID
import com.thsst2.greenapp.data.SessionEntity
import com.thsst2.greenapp.data.UserEntity
import com.thsst2.greenapp.data.repositories.SessionRepository
import kotlinx.coroutines.launch

class AndroidSmallHomeActivity : AppCompatActivity() {
    private lateinit var homeBinding: ActivityAndroidSmallHomeBinding
    private val messages = mutableListOf<String>()
    private lateinit var adapter: MyAdapter
    private lateinit var chatApi: ChatApi
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

		// Set up view binding
        homeBinding = ActivityAndroidSmallHomeBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)

        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(this)

		// Authenticate User
        val currentUser = auth.currentUser
        if (currentUser == null || !sessionManager.isLoggedIn()) {
            startActivity(Intent(this, AndroidSmallLoginActivity::class.java))
            finish()
            return
        } else {
            Log.d("HomeActivity", "Logged in as: ${currentUser.email}")
        }

        // Load all images with glide
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/59e2tcay_expires_30_days.png").into(homeBinding.rn6y677xm2op)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/g96kdk7e_expires_30_days.png").into(homeBinding.r1niin00ofei)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/wklyakqn_expires_30_days.png").into(homeBinding.ra0p1lhf2i3g)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/qz8xcg2m_expires_30_days.png").into(homeBinding.rjvarnxwuapq)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/b60kjme5_expires_30_days.png").into(homeBinding.rlqd4vorx07s)

		// Navigation bar
		setupNavigationBar()

		// RecyclerView
		adapter = MyAdapter(messages)
		homeBinding.recyclerViewChatReplies.adapter = adapter
		homeBinding.recyclerViewChatReplies.layoutManager = LinearLayoutManager(this)

		// Initialize retrofit, okHttpClient
		val okHttpClient = OkHttpClient.Builder()
			.connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
			.readTimeout(120, TimeUnit.SECONDS)    // Read timeout (e.g., 60 seconds or more for Llama)
			.writeTimeout(120, TimeUnit.SECONDS)   // Write timeout
			.build()

        // NOTE: URL might vary each run, please replace with the most recent one
        val ngrokApiUrl = "https://preharmonious-saul-spectrologically.ngrok-free.dev"

		val retrofit = Retrofit.Builder()
			.baseUrl("$ngrokApiUrl/")
			.client(okHttpClient) // Add custom OkHttpClient
			.addConverterFactory(GsonConverterFactory.create())
			.build()
		chatApi = retrofit.create(ChatApi::class.java)

		// Get user message and send to API
		val editText = homeBinding.r0zz50xix97ik
		editText.setOnEditorActionListener { v, actionId, event ->
			if (actionId == EditorInfo.IME_ACTION_SEND) {
				val userMessage = editText.text.toString().trim()
				if (userMessage.isNotBlank()) {
					sendMessageToChat(userMessage)
					editText.text.clear()
				}
				true
			} else {
				false
			}
		}

        //Manage session
        val sessionId = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
		val userId = currentUser.uid.hashCode().toLong() // convert Firebase UID to Long for DB
		val userProfile = UserEntity(userId = userId, userRoleId = 1) // role 1 default
		val sessionProfile = SessionEntity(
			sessionId = sessionId,
			userId = userId,
			componentsUsed = null,
			sessionStartedAt = System.currentTimeMillis().toString()
		)
		saveSessionLocally(sessionProfile)
	}

	private fun sendMessageToChat(userMessage: String) {
		// Mock contextual data (replace with actual DAO calls later)
		val userRole = "student" // from userRoleDao.getById(userProfile.userRoleId)?.roleName
		val activePreferences = listOf("historical landmarks", "cafeterias") // userPreferencesDao.getByUser(userId)
		val visitedPOIs = listOf("St. La Salle Hall", "Henry Sy Sr. Hall") // userVisitedLocationDao.getByUser(userId)

		val contextString = buildString {
			append("I am a $userRole. ")
			append("I like ${activePreferences.joinToString(", ")}. ")
			append("I have visited ${visitedPOIs.joinToString(", ")}. ")
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
						Log.d("HomeActivity_Chat", "Bot Raw Reply: $botReply")
						messages.add("Bot: $botReply")
					} else {
						val errorMsg = "Error: ${response.code()} - ${response.message()}"
						Log.e("HomeActivity_Chat", "API Error: $errorMsg")
						messages.add("Bot: $errorMsg")
					}
					adapter.notifyItemInserted(messages.size - 1)
					homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
				}
			}

			override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
				runOnUiThread {
					Log.e("HomeActivity_Chat", "Network Failure: ${t.message}", t)
					messages.add("Bot: Network connection failed - ${t.localizedMessage}")
					adapter.notifyItemInserted(messages.size - 1)
					homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
				}
			}
		})
	}

	private fun endTour(sessionProfile: SessionEntity) {
		val endedSession = sessionProfile.copy(sessionEndedAt = System.currentTimeMillis().toString())
		uploadSessionToFirestore(endedSession)
		clearLocalSession()
	}

	private fun setupNavigationBar() {
		// Home (Current Activity for AndroidSmallHomeActivity)
		homeBinding.homeButton.setOnClickListener {
			recreate()
		}

		// Trivia
		homeBinding.triviaButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallTriviaActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}

		// Map
		homeBinding.mapButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallMapActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}

		// Profile
		homeBinding.profileButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallProfileActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}
	}

	private fun saveSessionLocally(session: SessionEntity) {
		lifecycleScope.launch {
			val db = MyAppDatabase.getInstance(this@AndroidSmallHomeActivity)
			val repo = SessionRepository(db)
			repo.insertSession(session, true) // true means also upload to Firebase
		}
	}

	private fun uploadSessionToFirestore(session: SessionEntity) {
		lifecycleScope.launch {
			val db = MyAppDatabase.getInstance(this@AndroidSmallHomeActivity)
			val repo = SessionRepository(db)
			repo.insertSession(session, true)
		}
	}

	private fun clearLocalSession() {
		lifecycleScope.launch {
			val db = MyAppDatabase.getInstance(this@AndroidSmallHomeActivity)
			db.sessionDao().deleteAll()
		}
	}

}