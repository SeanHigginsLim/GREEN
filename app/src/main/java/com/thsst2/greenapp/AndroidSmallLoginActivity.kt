package com.thsst2.greenapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.thsst2.greenapp.databinding.ActivityAndroidSmallLoginBinding
import com.thsst2.greenapp.databinding.ActivityAndroidSmallSignupBinding

class AndroidSmallLoginActivity : AppCompatActivity() {
	private lateinit var loginBinding: ActivityAndroidSmallLoginBinding
	private lateinit var signupBinding: ActivityAndroidSmallSignupBinding
	private lateinit var auth: FirebaseAuth
	private lateinit var sessionManager: SessionManager

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Get Firebase Authentication
		auth = FirebaseAuth.getInstance()
		sessionManager = SessionManager(this)

		showLoginLayout()
	}

	private fun showLoginLayout() {
		loginBinding = ActivityAndroidSmallLoginBinding.inflate(layoutInflater)
		setContentView(loginBinding.root)

		// Auto redirect if already logged in
		if (auth.currentUser != null && sessionManager.isLoggedIn()) {
			startActivity(Intent(this, AndroidSmallHomeActivity::class.java))
			finish()
			return
		}

		loginBinding.r7k1xvbfvys.setOnClickListener {
			val email = loginBinding.rrv4j3rl69ve.text.toString().trim()
			val password = loginBinding.r3h40j0ejym3.text.toString().trim()
			loginUser(email, password)
		}

		loginBinding.rakhddqjl1ls.setOnClickListener {
			showSignupLayout()
		}
	}

	private fun showSignupLayout() {
		val signupBinding = ActivityAndroidSmallSignupBinding.inflate(layoutInflater)
		setContentView(signupBinding.root)

		val emailInput = signupBinding.signupEmail
		val passwordInput = signupBinding.signupPassword
		val confirmInput = signupBinding.signupConfirm
		val signupButton = signupBinding.signupButton
		val backToLogin = signupBinding.backToLogin

		signupButton.setOnClickListener {
			val email = emailInput.text.toString().trim()
			val password = passwordInput.text.toString().trim()
			val confirm = confirmInput.text.toString().trim()

			if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
				Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}
			if (password != confirm) {
				Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			auth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(this) { task ->
					if (task.isSuccessful) {
						Toast.makeText(this, "Account created! You can now log in.", Toast.LENGTH_SHORT).show()
						showLoginLayout()
					} else {
						Toast.makeText(this, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
					}
				}
		}

		backToLogin.setOnClickListener {
			showLoginLayout()
		}
	}

	private fun loginUser(email: String, password: String) {
		if (email.isEmpty() || password.isEmpty()) {
			Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
			return
		}

		auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
			if (task.isSuccessful) {
				val user = auth.currentUser
				if (user != null) {
					sessionManager.saveLoginSession(user.uid, user.email)
				}
				Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
				startActivity(Intent(this, AndroidSmallHomeActivity::class.java))
				finish()
			} else {
				Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
			}
		}
	}
}