package com.thsst2.greenapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.thsst2.greenapp.databinding.ActivityAndroidSmallLoginBinding

class AndroidSmallLoginActivity : AppCompatActivity() {
	private lateinit var loginBinding: ActivityAndroidSmallLoginBinding
	private lateinit var auth: FirebaseAuth

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		loginBinding = ActivityAndroidSmallLoginBinding.inflate(layoutInflater)
		setContentView(loginBinding.root)

		auth = FirebaseAuth.getInstance()

		// Login button click
		loginBinding.r7k1xvbfvys.setOnClickListener {
			val email = loginBinding.rrv4j3rl69ve.text.toString().trim()
			val password = loginBinding.r3h40j0ejym3.text.toString().trim()
			loginUser(email, password)
		}

		// Signup text click
		loginBinding.rakhddqjl1ls.setOnClickListener {
//			startActivity(Intent(this, SignupActivity::class.java))
		}
//		setContentView(R.layout.activity_android_small_login)
//		val button1: View = findViewById(R.id.rumm9kmpqgsm)
//		button1.setOnClickListener {
//			println("Pressed")
//		}
//		val button2: View = findViewById(R.id.rrc8z2w6cq5l)
//		button2.setOnClickListener {
//			println("Pressed")
//		}
//		val button3: View = findViewById(R.id.r7k1xvbfvys)
//		button3.setOnClickListener {
//			println("Pressed")
//		}
	}

	private fun loginUser(email: String, password: String) {
		if (email.isEmpty() || password.isEmpty()) {
			Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
			return
		}

		auth.signInWithEmailAndPassword(email, password)
			.addOnCompleteListener(this) { task ->
				if (task.isSuccessful) {
					Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
					startActivity(Intent(this, AndroidSmallHomeActivity::class.java))
					finish()
				} else {
					Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
				}
			}
	}
}