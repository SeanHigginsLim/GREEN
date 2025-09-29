package com.thsst2.greenapp
import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import androidx.appcompat.app.AppCompatActivity
import com.thsst2.greenapp.databinding.ActivityAndroidSmallMapBinding

class AndroidSmallMapActivity : AppCompatActivity() {

	private lateinit var mapBinding: ActivityAndroidSmallMapBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Set up view binding
		mapBinding = ActivityAndroidSmallMapBinding.inflate(layoutInflater)
		setContentView(mapBinding.root)

		// Load all images with glide
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/4a89py0h_expires_30_days.png").into(findViewById(R.id.rundefined))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/6nk1t21m_expires_30_days.png").into(findViewById(R.id.rm6mh7dlmac))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/g6j8jjk1_expires_30_days.png").into(findViewById(R.id.rr4suqnw5xu8))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/6ylj9vbk_expires_30_days.png").into(findViewById(R.id.r4ygvniz63xa))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/ufno6m2e_expires_30_days.png").into(findViewById(R.id.resno30rwma6))

		// Run Navigation Bar
		setupNavigationBar()
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