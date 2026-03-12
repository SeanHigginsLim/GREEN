package com.thsst2.greenapp
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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

	// Get list from db.
	private var allPreferences: List<String> = emptyList()

	// Get list from db.
	private var allRoles: List<String> = emptyList()

	private lateinit var ragEngine: RAGEngine
	private var openedFromChatbotFlow: Boolean = false

//	private val allRoles = listOf("Student", "Faculty", "Guest")

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Set up view binding
		profileBinding = ActivityAndroidSmallProfileBinding.inflate(layoutInflater)
		setContentView(profileBinding.root)

		db = MyAppDatabase.getInstance(this)
		userId = FirebaseAuth.getInstance().currentUser?.uid.hashCode().toLong()

		// Rag Engine
		ragEngine = RAGEngine()

		// Initial load
		loadPreferencesTextView()

		// Load all images with glide
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/0228mxps_expires_30_days.png").into(findViewById(R.id.rorhz9ugpp3))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/ucnrpwvy_expires_30_days.png").into(findViewById(R.id.r92m9lwykfag))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/5uqcjups_expires_30_days.png").into(findViewById(R.id.r1sboj981btw))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/imp958ws_expires_30_days.png").into(findViewById(R.id.r82fycu2d3ik))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/erdezrmi_expires_30_days.png").into(findViewById(R.id.r6k6upscihau))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/gp023gk0_expires_30_days.png").into(findViewById(R.id.r5i26bda9wnh))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/5KZSjaV7Nf/3tidlk1m_expires_30_days.png").into(findViewById(R.id.rxvihqambbch))

		lifecycleScope.launch {
			allRoles = ragEngine.getRoleList()
			loadUserRoleText()
		}

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


		openedFromChatbotFlow = intent.getBooleanExtra("open_prefs_dialog", false)
		if (openedFromChatbotFlow) {
			openPreferencesDialog()
		}
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
				val displayPrefs = selectedPrefs?.map { formatPreferenceForDisplay(it) }
				profileBinding.rdq3i5lpyvv4.text = displayPrefs?.joinToString(", ")
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
			allPreferences = ragEngine.getPreferencesListForProfilePage()
			val prefsEntity = db.userPreferencesDao().getPreferencesByUser(userId)
			val selectedPrefs = prefsEntity?.interests?.toMutableSet()
			val displayPrefs = allPreferences.map { formatPreferenceForDisplay(it) }
			val allPrefsArray = displayPrefs.toTypedArray()
			val checkedItems = allPrefsArray.map {
				val original = formatPreferenceForStorage(it)
				selectedPrefs?.contains(original) ?: false
			}.toBooleanArray()
			Log.d("In User Preferences", "Im here")

			withContext(Dispatchers.Main) {
				AlertDialog.Builder(this@AndroidSmallProfileActivity)
					.setTitle("Select Preferences")
					.setMultiChoiceItems(allPrefsArray, checkedItems) { _, which, isChecked ->
						val original = formatPreferenceForStorage(allPrefsArray[which])

						if (isChecked) selectedPrefs?.add(original)
						else selectedPrefs?.remove(original)
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

								if (openedFromChatbotFlow) {
									val returnIntent = Intent(this@AndroidSmallProfileActivity, AndroidSmallHomeActivity::class.java)
									returnIntent.putExtra("from_pref_edit", true)
									returnIntent.putExtra("pref_edit_saved", true)
									returnIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
									startActivity(returnIntent)
									finish()
								}
							}
						}
					}
					.setNegativeButton("Cancel") { _, _ ->
						if (openedFromChatbotFlow) {
							val returnIntent = Intent(this@AndroidSmallProfileActivity, AndroidSmallHomeActivity::class.java)
							returnIntent.putExtra("from_pref_edit", true)
							returnIntent.putExtra("pref_edit_saved", false)
							returnIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
							startActivity(returnIntent)
							finish()
						}
					}
					.show()
			}
		}
	}

	fun formatPreferenceForDisplay(pref: String): String {
		return pref.replace(Regex("([a-z])([A-Z])"), "$1 $2")
	}

	fun formatPreferenceForStorage(pref: String): String {
		return pref.replace(" ", "")
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
				db.dialogueHistoryDao().deleteAll()
				db.generatedPathDao().deleteAll()
				db.geofenceTriggerDao().deleteAll()
				db.intentLogDao().deleteAll()
				db.localDataDao().deleteAll()
				db.pathDeviationAlertDao().deleteAll()
				db.performanceMetricsDao().deleteAll()
				db.poiDao().deleteAll()
				db.responseJustificationDao().deleteAll()
				db.sessionLogDao().deleteAll()
				db.sessionLogDao().deleteAll()
				db.transitionDao().deleteAll()
//				db.userDao().deleteAll()
				db.userFeedbackDao().deleteAll()
				db.userInteractionTimeDao().deleteAll()
//				db.userLocationDao().deleteAll()
				db.userLogDao().deleteAll()
//				db.userPreferencesDao().deleteAll()
				db.userQueryDao().deleteAll()
//				db.userRoleDao().deleteAll()
				db.userSkippedOrDislikedLocationDao().deleteAll()
				db.userTourPathHistoryDao().deleteAll()
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