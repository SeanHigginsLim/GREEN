package com.thsst2.greenapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.thsst2.greenapp.databinding.ActivityAndroidSmallTriviaBinding

class AndroidSmallTriviaActivity : AppCompatActivity() {

	private lateinit var triviaBinding: ActivityAndroidSmallTriviaBinding
	private lateinit var auth: FirebaseAuth
	private lateinit var triviaRef: DatabaseReference

	private val approvedTriviaList = mutableListOf<TriviaItem>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		triviaBinding = ActivityAndroidSmallTriviaBinding.inflate(layoutInflater)
		setContentView(triviaBinding.root)

		auth = FirebaseAuth.getInstance()
		triviaRef = FirebaseDatabase.getInstance()
			.getReference("client_side")
			.child("trivia")

		loadImages()
		setupNavigationBar()
		setupButtons()
		loadRandomApprovedTrivia()
	}

	private fun loadImages() {
		triviaBinding.rtpmb3sfkxec.setImageResource(R.drawable.white_home_page_icon)
		triviaBinding.rv2mkds8qk.setImageResource(R.drawable.black_trivia_page)
		triviaBinding.rm8ia53ujvf.setImageResource(R.drawable.white_map_page)
		triviaBinding.rwt2433i01up.setImageResource(R.drawable.white_profile_page)
//		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/euck9yhs_expires_30_days.png").into(findViewById(R.id.rtpmb3sfkxec))
//		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/2a2i3aud_expires_30_days.png").into(findViewById(R.id.rv2mkds8qk))
//		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/5tyj8imq_expires_30_days.png").into(findViewById(R.id.rm8ia53ujvf))
//		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/eirszlo0_expires_30_days.png").into(findViewById(R.id.rwt2433i01up))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/8g667ulq_expires_30_days.png").into(findViewById(R.id.rjr0kuafq29l))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/dfc89q7y_expires_30_days.png").into(findViewById(R.id.rjim834nbvr))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/w0zoa2pz_expires_30_days.png").into(findViewById(R.id.r8jqrksqg5si))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/mq69qsuu_expires_30_days.png").into(findViewById(R.id.rybdh3kflfnm))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/g70x0upd_expires_30_days.png").into(findViewById(R.id.rjuo5tbsq7m))
	}

	private fun setupButtons() {
		findViewById<LinearLayout>(R.id.shareTriviaButton).setOnClickListener {
			showShareTriviaDialog()
		}
	}

	private fun loadRandomApprovedTrivia() {
		triviaBinding.triviaText.text = "Loading trivia..."

		triviaRef.addListenerForSingleValueEvent(object : ValueEventListener {
			override fun onDataChange(snapshot: DataSnapshot) {
				approvedTriviaList.clear()

				for (child in snapshot.children) {
					val item = child.getValue(TriviaItem::class.java)
					// if (item != null && item.approved) { // uncomment later
					if (item != null) {
						approvedTriviaList.add(item)
					}
				}

				if (approvedTriviaList.isEmpty()) {
					triviaBinding.triviaText.text = "No approved trivia yet. Be the first to share one."
				} else {
					showAnotherRandomTrivia()
				}
			}

			override fun onCancelled(error: DatabaseError) {
				triviaBinding.triviaText.text = "Failed to load trivia."
				Toast.makeText(
					this@AndroidSmallTriviaActivity,
					"Error: ${error.message}",
					Toast.LENGTH_SHORT
				).show()
			}
		})
	}

	private fun showAnotherRandomTrivia() {
		if (approvedTriviaList.isEmpty()) {
			triviaBinding.triviaText.text = "No approved trivia yet. Be the first to share one."
			return
		}

		val randomTrivia = approvedTriviaList.random()
		triviaBinding.triviaText.text = randomTrivia.text
	}

	private fun showShareTriviaDialog() {
		val container = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(48, 32, 48, 0)
		}

		val triviaInput = EditText(this).apply {
			hint = "Enter a campus trivia fact"
			inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
		}

		container.addView(triviaInput)

		AlertDialog.Builder(this)
			.setTitle("Share Trivia")
			.setView(container)
			.setPositiveButton("Submit") { _, _ ->
				val triviaText = triviaInput.text.toString().trim()

				if (triviaText.isBlank()) {
					Toast.makeText(this, "Trivia text cannot be empty.", Toast.LENGTH_SHORT).show()
					return@setPositiveButton
				}

				submitTrivia(triviaText)
			}
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun submitTrivia(text: String) {
		val currentUserId = auth.currentUser?.uid ?: "anonymous"
		val newTriviaRef = triviaRef.push()

		val triviaItem = TriviaItem(
			text = text,
			userId = currentUserId,
			timestamp = System.currentTimeMillis(),
			approved = false
		)

		newTriviaRef.setValue(triviaItem)
			.addOnSuccessListener {
				Toast.makeText(
					this,
					"Thanks! Your trivia will be reviewed before it appears.",
					Toast.LENGTH_LONG
				).show()
				loadRandomApprovedTrivia()
			}
			.addOnFailureListener { e ->
				Toast.makeText(
					this,
					"Failed to submit trivia: ${e.message}",
					Toast.LENGTH_LONG
				).show()
			}
	}

	private fun setupNavigationBar() {
		triviaBinding.homeButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallHomeActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}

		triviaBinding.triviaButton.setOnClickListener {
			recreate()
		}

		triviaBinding.mapButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallMapActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}

		triviaBinding.profileButton.setOnClickListener {
			val intent = Intent(this, AndroidSmallProfileActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)
		}
	}
}

data class TriviaItem(
	val text: String = "",
	val userId: String = "",
	val timestamp: Long = 0L,
	val approved: Boolean = false
)