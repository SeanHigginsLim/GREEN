//package com.thsst2.greenapp
//
//import android.content.Context
//import com.thsst2.greenapp.data.UserPreferencesEntity
//import com.thsst2.greenapp.TourCoordinator
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//// multi-turn dialogue logic
//// context manager: tracks current user state (phase)
//// goal planning + intent recognition: detects what user wants to do
//class DialogueManager(private val context: Context) {
//
//    private val ragEngine = RAGEngine()
//    private val tourCoordinator = TourCoordinator(context)
//    private val userContexts = mutableMapOf<Long, DialogueContext>()
//
//    //Processes a new user message and decides the next action
//    suspend fun processUserIntent(userId: Long, message: String, prefs: UserPreferencesEntity
//                                ): DialogueResult = withContext(Dispatchers.IO) {
//
//        val lower = message.lowercase()
//        val contextState = userContexts.getOrPut(userId) { DialogueContext() }
//
//        val reply = when (contextState.phase) {
//            // Phases established based on sample dialogue flow
//            Phase.AWAITING_TOUR_CONFIRM -> handleTourConfirmation(contextState, lower, prefs)
//            Phase.COLLECTING_PREFERENCES -> handlePreferenceCollection(contextState, lower)
//            Phase.ASKING_SPECIFICS -> handleSpecificQuestions(contextState, lower)
//            Phase.CONFIRMING_UPDATE -> handleProfileUpdate(contextState, lower, prefs, userId)
//            Phase.GENERATING_TOUR -> "Hang on! I’m preparing your tour..."
//            else -> detectNewIntent(contextState, lower, prefs)
//        }
//
//        DialogueResult(reply, contextState.phase.name)
//    }
//
//    // Detects if the user wants a tour or information.
//    private fun detectNewIntent(contextState: DialogueContext, message: String, prefs: UserPreferencesEntity
//                                ): String {
//        return when {
//            message.contains("tour") -> {
//                contextState.phase = Phase.AWAITING_TOUR_CONFIRM
//                "Sure! I can generate a tour based on your preferences: ${prefs.interests.joinToString()}. " +
//                        "Would you like to add or remove any more interests before I start?"
//            }
//
//            message.contains("info") || message.contains("tell me about") -> {
//                //val keyword = extractKeyword(message)
//                val poi = runCatching { ragEngine.getRelevantPOIs(prefs) }.getOrNull()?.firstOrNull()
//                contextState.phase = Phase.IDLE
//                if (poi != null) "Here’s what I found about ${poi.name}: ${poi.category}."
//                else "I couldn’t find that location."
//            }
//
//            message.contains("recommend") -> {
//                contextState.phase = Phase.IDLE
//                "Based on your interests, I recommend visiting ${prefs.interests.firstOrNull() ?: "popular spots"} around campus."
//            }
//
//            else -> "Hmm, would you like to start a tour or learn about a building?"
//        }
//    }
//
//    // Confirm whether to start a tour
//    private fun handleTourConfirmation(
//        contextState: DialogueContext,
//        message: String,
//        prefs: UserPreferencesEntity
//    ): String {
//        return when {
//            message.contains("yes") -> {
//                contextState.phase = Phase.COLLECTING_PREFERENCES
//                "Awesome! Is there anything specific you like or dislike?"
//            }
//
//            message.contains("no") -> {
//                contextState.phase = Phase.GENERATING_TOUR
//                "Alright, I’ll use your saved preferences: ${prefs.interests.joinToString()}. Generating your tour now..."
//            }
//
//            else -> "Would you like to adjust your preferences before generating the tour?"
//        }
//    }
//
//    //Collect additional likes/dislikes
//    private fun handlePreferenceCollection(contextState: DialogueContext, message: String): String {
//        if (message.contains("like")) {
//            contextState.additionalLikes.addAll(extractEntities(message))
//            return "Got it, you like ${contextState.additionalLikes.joinToString()}. Anything else?"
//        }
//        if (message.contains("dislike")) {
//            contextState.additionalDislikes.addAll(extractEntities(message))
//            return "Okay, I’ll avoid ${contextState.additionalDislikes.joinToString()}. Anything else?"
//        }
//
//        contextState.phase = Phase.ASKING_SPECIFICS
//        return "Good to know! Do you have specific places you want to visit or a certain order?"
//    }
//
//    //Step 4: Ask for more specific route choices.
//    private fun handleSpecificQuestions(contextState: DialogueContext, message: String): String {
//        return if (message.contains("yes")) {
//            contextState.phase = Phase.CONFIRMING_UPDATE
//            "Got it! Would you like me to save these new preferences for future tours?"
//        } else if (message.contains("no")) {
//            contextState.phase = Phase.CONFIRMING_UPDATE
//            "Alright, I’ll just use your current preferences. Generate tour now?"
//        } else {
//            "Please say 'yes' or 'no' so I can proceed."
//        }
//    }
//
//    //Step 5: Confirm profile update and trigger tour generation
//    private suspend fun handleProfileUpdate(contextState: DialogueContext, message: String, prefs: UserPreferencesEntity, userId: Long
//                                            ): String = withContext(Dispatchers.IO) {
//        when {
//            message.contains("yes") -> {
//                contextState.phase = Phase.GENERATING_TOUR
//                val db = MyAppDatabase.getInstance(context)
//                val updatedPrefs = prefs.copy(
//                    interests = prefs.interests + contextState.additionalLikes,
//                    disinterests = prefs.disinterests.orEmpty() + contextState.additionalDislikes
//                )
//                db.userPreferencesDao().insert(updatedPrefs.copy(userId = userId))
//                "Preferences saved! Generating your tour now..."
//            }
//
//            message.contains("no") -> {
//                contextState.phase = Phase.GENERATING_TOUR
//                "Alright, generating your tour now..."
//            }
//
//            else -> "Would you like to save your new preferences before we start?"
//        }
//    }
//
//    //when DialogueManager transitions into GENERATING_TOUR, this function is called from HomeActivity.
//    // It calls tourcoordinator to now generate tour path
//    suspend fun handleAction(userId: Long): String = withContext(Dispatchers.IO) {
//        val path = tourCoordinator.startTourForUser(userId)
//        if (path != null)
//            "Tour path generated successfully!"
//        else
//            "I couldn’t generate a tour right now."
//    }
//
////    private fun extractKeyword(message: String): String? =
////        message.split(" ").lastOrNull { it.length > 3 }
//
//    private fun extractEntities(message: String): List<String> {
//        val words = message.split(" ")
//        return words.filter { it.length > 3 && it != "like" && it != "dislike" }
//    }
//
//    data class DialogueResult(val reply: String, val phase: String)
//
//    private data class DialogueContext(
//        var phase: Phase = Phase.IDLE,
//        val additionalLikes: MutableList<String> = mutableListOf(),
//        val additionalDislikes: MutableList<String> = mutableListOf()
//    )
//
//    // Phases established based on sample dialogue flow
//    enum class Phase {
//        IDLE,
//        AWAITING_TOUR_CONFIRM,
//        COLLECTING_PREFERENCES,
//        ASKING_SPECIFICS,
//        CONFIRMING_UPDATE,
//        GENERATING_TOUR
//    }
//}
