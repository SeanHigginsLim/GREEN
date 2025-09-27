package com.thsst2.greenapp
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
import com.thsst2.greenapp.databinding.ActivityAndroidSmallHomeBinding

class AndroidSmallHomeActivity : AppCompatActivity() {
    private lateinit var homeBinding: ActivityAndroidSmallHomeBinding
	private val messages = mutableListOf<String>()
	private lateinit var adapter: MyAdapter
	private lateinit var chatApi: ChatApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeBinding = ActivityAndroidSmallHomeBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)

		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/59e2tcay_expires_30_days.png").into(homeBinding.rn6y677xm2op)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/g96kdk7e_expires_30_days.png").into(homeBinding.r1niin00ofei)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/wklyakqn_expires_30_days.png").into(homeBinding.ra0p1lhf2i3g)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/qz8xcg2m_expires_30_days.png").into(homeBinding.rjvarnxwuapq)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/b60kjme5_expires_30_days.png").into(homeBinding.rlqd4vorx07s)

		// RecyclerView
		adapter = MyAdapter(messages)
		homeBinding.recyclerViewChatReplies.adapter = adapter
		homeBinding.recyclerViewChatReplies.layoutManager = LinearLayoutManager(this)

		// --- Start of Your Retrofit Initialization with OkHttpClient ---
		val okHttpClient = OkHttpClient.Builder()
			.connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
			.readTimeout(60, TimeUnit.SECONDS)    // Read timeout (e.g., 60 seconds or more for Llama)
			.writeTimeout(60, TimeUnit.SECONDS)   // Write timeout
			.build()

		val retrofit = Retrofit.Builder()
			.baseUrl("http://192.168.68.103:8000/") // Ensure this is your PC's current IP and FastAPI port
			.client(okHttpClient) // Add the custom OkHttpClient
			.addConverterFactory(GsonConverterFactory.create())
			.build()
		chatApi = retrofit.create(ChatApi::class.java)
		// --- End of Your Retrofit Initialization with OkHttpClient ---

		val editText = homeBinding.r0zz50xix97ik
		editText.setOnEditorActionListener { v, actionId, event ->
			if (actionId == EditorInfo.IME_ACTION_SEND) {
				val userMessage = editText.text.toString().trim()
				if (userMessage.isNotBlank()) {
					messages.add("You: $userMessage")
					adapter.notifyItemInserted(messages.size - 1)
					homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
					editText.text.clear()

					chatApi.generate(ChatRequest(userMessage)).enqueue(object : Callback<ChatResponse> {
						override fun onResponse(
							call: Call<ChatResponse>,
							response: Response<ChatResponse>
						) {
							runOnUiThread { // Ensure UI updates are on the main thread
								if (response.isSuccessful && response.body() != null) {
									val botReply = response.body()!!.response // Raw response
									Log.d("HomeActivity_Chat", "Bot Raw Reply: $botReply")
									messages.add("Bot: $botReply")
								} else {
									val errorMsg = "Error: ${response.code()} - ${response.message()}"
									Log.e("HomeActivity_Chat", "API Error: $errorMsg, URL: ${call.request().url}, ErrorBody: ${response.errorBody()?.string()}")
									messages.add("Bot: $errorMsg")
								}
								adapter.notifyItemInserted(messages.size - 1)
								homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
							}
						}

						override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
							runOnUiThread { // Ensure UI updates are on the main thread
								Log.e("HomeActivity_Chat", "Network Failure: ${t.message}", t)
								messages.add("Bot: Network connection failed - ${t.localizedMessage}")
								adapter.notifyItemInserted(messages.size - 1)
								homeBinding.recyclerViewChatReplies.scrollToPosition(messages.size - 1)
							}
						}
					})
				}
				true
			} else {
				false // let other actions pass through
			}
		}
	}
}