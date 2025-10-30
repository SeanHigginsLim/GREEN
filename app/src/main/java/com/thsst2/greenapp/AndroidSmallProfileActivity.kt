package com.thsst2.greenapp
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.thsst2.greenapp.databinding.ActivityAndroidSmallProfileBinding
import androidx.core.content.edit

class AndroidSmallProfileActivity : AppCompatActivity() {
	private lateinit var profileBinding: ActivityAndroidSmallProfileBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Set up view binding
		profileBinding = ActivityAndroidSmallProfileBinding.inflate(layoutInflater)
		setContentView(profileBinding.root)

		// Load all images with glide
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/0228mxps_expires_30_days.png").into(findViewById(R.id.rorhz9ugpp3))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/ucnrpwvy_expires_30_days.png").into(findViewById(R.id.r92m9lwykfag))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/5uqcjups_expires_30_days.png").into(findViewById(R.id.r1sboj981btw))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/imp958ws_expires_30_days.png").into(findViewById(R.id.r82fycu2d3ik))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/erdezrmi_expires_30_days.png").into(findViewById(R.id.r6k6upscihau))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/gp023gk0_expires_30_days.png").into(findViewById(R.id.r5i26bda9wnh))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/f081lhs7_expires_30_days.png").into(findViewById(R.id.rr6cbpqtve3))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/3tidlk1m_expires_30_days.png").into(findViewById(R.id.rxvihqambbch))
		val button1: View = profileBinding.rxhutp6h6x5j
		button1.setOnClickListener {
			println("Pressed")
		}
		val logoutButton: View = profileBinding.rqts8trhvc1o
		logoutButton.setOnClickListener {
			logout()
		}

		// Run Navigation Bar
		setupNavigationBar()
	}

	private fun logout() {
		// 1. Sign out from Firebase
		FirebaseAuth.getInstance().signOut()

		// 2. (Optional) Clear local data, if needed
		// Example: clear SharedPreferences
		val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
		prefs.edit { clear() }

		// 3. Redirect to LoginActivity
		val intent = Intent(this, AndroidSmallLoginActivity::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		startActivity(intent)
		finish()
	}

	private fun setupNavigationBar() {
		// Home
		profileBinding.homeButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallHomeActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}

		// Trivia
		profileBinding.triviaButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallTriviaActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}

		// Map
		profileBinding.mapButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallMapActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}

		// Profile (Current Activity for AndroidSmallProfileActivity)
		profileBinding.profileButton.setOnClickListener {
			recreate()
		}
	}
}