package com.thsst2.greenapp
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.thsst2.greenapp.data.UserPreferencesEntity
import com.thsst2.greenapp.data.UserRoleEntity
import com.thsst2.greenapp.databinding.ActivityAndroidSmallProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AndroidSmallProfileActivity : AppCompatActivity() {
	private lateinit var profileBinding: ActivityAndroidSmallProfileBinding
	private lateinit var prefs: SharedPreferences
	private val allPreferences = listOf(
		"Functional Info", "Operational Info",
		"Henry Sy Sr. Hall", "Brother Bloemen Hall", "Saint La Salle Hall",
		"Velasco Hall", "Enrique Razon Sports Center", "Brother Andrew Gonzalez Hall",
		"Gokongwei Hall", "Saint Mutien Marie Hall", "Science and Technology Research Center",
		"Marian Quadrangle", "Brother John Hall", "Saint Joseph Hall",
		"Don Enrique Yuchengco Hall", "Brother Connon Hall", "Faculty Center",
		"Brother William Hall", "Saint Miguel Hall", "Amphitheater", "Open spaces",
		"Relaxing", "Science", "Mathematics", "Engineering", "Labs", "Library",
		"Recreational", "Accessibility", "Parking", "Drinking Fountain", "Historical",
		"Food", "Warp zones", "Museum", "SDFO", "Merchandise", "Supplies", "Clinic", "Chapel",
		"Auditorium Entrances", "CCS Building", "CE Building", "COB Building",
		"COS Building", "CLA Building", "COE Building", "School of Economics Building"
	)
	private val allRoles = listOf("Student", "Faculty", "Guest")

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Set up view binding
		profileBinding = ActivityAndroidSmallProfileBinding.inflate(layoutInflater)
		setContentView(profileBinding.root)

		prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

		// Initial load
		loadPreferencesTextView()
		loadUserRoleText()

		// Load all images with glide
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/0228mxps_expires_30_days.png").into(findViewById(R.id.rorhz9ugpp3))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/ucnrpwvy_expires_30_days.png").into(findViewById(R.id.r92m9lwykfag))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/5uqcjups_expires_30_days.png").into(findViewById(R.id.r1sboj981btw))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/imp958ws_expires_30_days.png").into(findViewById(R.id.r82fycu2d3ik))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/erdezrmi_expires_30_days.png").into(findViewById(R.id.r6k6upscihau))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/gp023gk0_expires_30_days.png").into(findViewById(R.id.r5i26bda9wnh))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/3tidlk1m_expires_30_days.png").into(findViewById(R.id.rxvihqambbch))

		profileBinding.rxhutp6h6x5j.setOnClickListener {
			openUserRolesDialog()
		}

		profileBinding.rnu7vqd6v5vd.setOnClickListener {
			openPreferencesDialog()
		}

		profileBinding.rqts8trhvc1o.setOnClickListener {
			logout()
		}

		// Run Navigation Bar
		setupNavigationBar()
	}

	private fun loadUserRoleText() {
		val currentRole = prefs.getString("user_role", "Student") ?: "Student"
		val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, allRoles)
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
		profileBinding.actionBarSpinner.adapter = adapter

		val index = allRoles.indexOf(currentRole)
		profileBinding.actionBarSpinner.setSelection(if (index >= 0) index else 0)
	}

	private fun loadPreferencesTextView() {
		val selectedPrefs = prefs.getStringSet("selected_preferences", emptySet()) ?: emptySet()
		profileBinding.rdq3i5lpyvv4.text = selectedPrefs.joinToString(", ")
	}

	private fun openUserRolesDialog() {
		val userId = FirebaseAuth.getInstance().currentUser?.uid.hashCode().toLong()
		val currentRole = prefs.getString("user_role", "Student")
		val selectedIndex = allRoles.indexOf(currentRole)

		AlertDialog.Builder(this)
			.setTitle("Select Role")
			.setSingleChoiceItems(allRoles.toTypedArray(), selectedIndex) { dialog, which ->
				val selectedRole = allRoles[which]

				// Save to SharedPreferences
				prefs.edit { putString("user_role", selectedRole) }

				// Optional: save to Room
				val db = MyAppDatabase.getInstance(this)
				CoroutineScope(Dispatchers.IO).launch {
					// Delete old preferences
					db.userRoleDao().deleteAll()

					// Insert new role
					val userRoleEntity = UserRoleEntity(
						userId = userId,
						role = selectedRole
					)
					db.userRoleDao().insert(userRoleEntity)

					// Update Spinner selection
					val index = allRoles.indexOf(selectedRole)
					profileBinding.actionBarSpinner.setSelection(index)

					dialog.dismiss()
				}
			}
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun openPreferencesDialog() {
		val userId = FirebaseAuth.getInstance().currentUser?.uid.hashCode().toLong()
		val selectedPrefs = prefs.getStringSet("selected_preferences", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
		val allPrefsArray = allPreferences.toTypedArray()
		val checkedItems = allPrefsArray.map { selectedPrefs.contains(it) }.toBooleanArray()

		AlertDialog.Builder(this)
			.setTitle("Select Preferences")
			.setMultiChoiceItems(allPrefsArray, checkedItems) { _, which, isChecked ->
				if (isChecked) selectedPrefs.add(allPrefsArray[which])
				else selectedPrefs.remove(allPrefsArray[which])
			}
			.setPositiveButton("Save") { _, _ ->
				prefs.edit { putStringSet("selected_preferences", selectedPrefs) }

				// Save to Room database
				val db = MyAppDatabase.getInstance(this)
				CoroutineScope(Dispatchers.IO).launch {
					// Delete old preferences
					db.userPreferencesDao().deleteAll()

					// Insert new preferences
					val userPreferencesEntity = UserPreferencesEntity(
						userId = userId,
						interests = selectedPrefs.toList(),
						disinterests = null,
						tourPace = null
					)
					db.userPreferencesDao().insert(userPreferencesEntity)

					// 3. Update UI
					withContext(Dispatchers.Main) {
						loadPreferencesTextView()
					}
				}
			}
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun logout() {
		// 1. Sign out from Firebase
		FirebaseAuth.getInstance().signOut()

		// 2. (Optional) Clear local data, if needed
		// Example: clear SharedPreferences
		val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
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