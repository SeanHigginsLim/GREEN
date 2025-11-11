package com.thsst2.greenapp
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.room.withTransaction
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
	private lateinit var db: MyAppDatabase
	private var userId: Long = 0
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
		"Auditorium", "Entrances", "CCS Building", "CE Building", "COB Building",
		"COS Building", "CLA Building", "COE Building", "School of Economics Building"
	)
	private val allRoles = listOf("Student", "Faculty", "Guest")

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Set up view binding
		profileBinding = ActivityAndroidSmallProfileBinding.inflate(layoutInflater)
		setContentView(profileBinding.root)

		db = MyAppDatabase.getInstance(this)
		userId = FirebaseAuth.getInstance().currentUser?.uid.hashCode().toLong()

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

		profileBinding.actionBarSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(
				parent: AdapterView<*>?,
				view: View?,
				position: Int,
				id: Long
			) {
				val selectedRole = allRoles[position]
				Log.d("UserRoleSelection", "Selected role: $selectedRole")
				openUserRolesDialog(selectedRole)
			}

			override fun onNothingSelected(parent: AdapterView<*>?) {
				// Do nothing
			}
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
		CoroutineScope(Dispatchers.IO).launch {
			val roleEntity = db.userRoleDao().getUserRoleById(userId)
			val currentRole = roleEntity?.role ?: "Student"

			withContext(Dispatchers.Main) {
				val adapter = ArrayAdapter(this@AndroidSmallProfileActivity, android.R.layout.simple_spinner_item, allRoles)
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
				profileBinding.actionBarSpinner.adapter = adapter

				val index = allRoles.indexOf(currentRole)
				profileBinding.actionBarSpinner.setSelection(if (index >= 0) index else 0)
			}
		}
	}

	private fun loadPreferencesTextView() {
		CoroutineScope(Dispatchers.IO).launch {
			val prefsEntity = db.userPreferencesDao().getPreferencesByUser(userId)
			val selectedPrefs = prefsEntity?.interests

			withContext(Dispatchers.Main) {
				profileBinding.rdq3i5lpyvv4.text = selectedPrefs?.joinToString(", ")
			}
		}
	}

	private fun openUserRolesDialog(selectedRole: String) {
		CoroutineScope(Dispatchers.IO).launch {
			db.userRoleDao().deleteAll()
			val userRoleEntity = UserRoleEntity(
				userId = userId,
				role = selectedRole
			)
			db.userRoleDao().insert(userRoleEntity)
		}
//		CoroutineScope(Dispatchers.IO).launch {
//			val roleEntity = db.userRoleDao().getUserRoleById(userId)
//			val currentRole = roleEntity?.role ?: "Student"
//			val selectedIndex = allRoles.indexOf(currentRole)
//			Log.d("In User Roles", "Im here")
//
//			withContext(Dispatchers.Main) {
//				AlertDialog.Builder(this@AndroidSmallProfileActivity)
//					.setTitle("Select Role")
//					.setSingleChoiceItems(allRoles.toTypedArray(), selectedIndex) { dialog, which ->
//						val selectedRole = allRoles[which]
//						Log.d("UserRoleSelection", "Selected role: $selectedRole")
//
//
//						CoroutineScope(Dispatchers.IO).launch {
//							db.userRoleDao().deleteAll()
//							val userRoleEntity = UserRoleEntity(
//								userId = userId,
//								role = selectedRole
//							)
//							db.userRoleDao().insert(userRoleEntity)
//
//							withContext(Dispatchers.Main) {
//								val index = allRoles.indexOf(selectedRole)
//								profileBinding.actionBarSpinner.setSelection(index)
//								dialog.dismiss()
//							}
//						}
//					}
//					.setNegativeButton("Cancel", null)
//					.show()
//			}
//		}
	}

	private fun openPreferencesDialog() {
		CoroutineScope(Dispatchers.IO).launch {
			val prefsEntity = db.userPreferencesDao().getPreferencesByUser(userId)
			val selectedPrefs = prefsEntity?.interests?.toMutableSet()
			val allPrefsArray = allPreferences.toTypedArray()
			val checkedItems = allPrefsArray.map { selectedPrefs?.contains(it) ?: false }.toBooleanArray()
			Log.d("In User Preferences", "Im here")

			withContext(Dispatchers.Main) {
				AlertDialog.Builder(this@AndroidSmallProfileActivity)
					.setTitle("Select Preferences")
					.setMultiChoiceItems(allPrefsArray, checkedItems) { _, which, isChecked ->
						if (isChecked) selectedPrefs?.add(allPrefsArray[which]) ?: false
						else selectedPrefs?.remove(allPrefsArray[which]) ?: false
					}
					.setPositiveButton("Save") { _, _ ->
						CoroutineScope(Dispatchers.IO).launch {
							db.userPreferencesDao().deleteAll()
							val userPreferencesEntity = UserPreferencesEntity(
								userId = userId,
								interests = selectedPrefs?.toList() ?: emptyList(),
								disinterests = null,
								tourPace = null
							)
							db.userPreferencesDao().insert(userPreferencesEntity)

							withContext(Dispatchers.Main) {
								loadPreferencesTextView()
							}
						}
					}
					.setNegativeButton("Cancel", null)
					.show()
			}
		}
	}

	private fun logout() {
		// 1. Sign out from Firebase
		FirebaseAuth.getInstance().signOut()

		// 2. (Optional) Clear local data, if needed
//		CoroutineScope(Dispatchers.IO).launch {
//			db.runInTransaction {
//				val dao = db.openHelper.writableDatabase
//				// Get all tables
//				val cursor = dao.query("SELECT name FROM sqlite_master WHERE type='table'")
//				while (cursor.moveToNext()) {
//					val tableName = cursor.getString(0)
//					if (
//						tableName != "android_metadata" &&
//						tableName != "room_master_table" &&
//						tableName != "user_role" &&
//						tableName != "user_preferences"
//					) {
//						dao.execSQL("DELETE FROM $tableName")
//					}
//				}
//				cursor.close()
//			}
//		}

		CoroutineScope(Dispatchers.IO).launch {
			db.withTransaction {
				db.generatedPathDao().deleteAll()
				db.userTourPathHistoryDao().deleteAll()
				db.dialogueHistoryDao().deleteAll()
				db.userQueryDao().deleteAll()
				db.intentLogDao().deleteAll()
				db.geofenceTriggerDao().deleteAll()
				db.pathDeviationAlertDao().deleteAll()
				db.userSkippedOrDislikedLocationDao().deleteAll()
				db.performanceMetricsDao().deleteAll()
//				db.userLocationDao().deleteAll()
				db.userLogDao().deleteAll()
				db.userInteractionTimeDao().deleteAll()
				db.sessionDao().deleteAll()
//				db.userDao().deleteAll()
				db.poiDao().deleteAll()
				db.responseJustificationDao().deleteAll()
				db.userFeedbackDao().deleteAll()
//				db.userPreferencesDao().deleteAll()
//				db.userRoleDao().deleteAll()
				db.userVisitedLocationDao().deleteAll()
			}
		}

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