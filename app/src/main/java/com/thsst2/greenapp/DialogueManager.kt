package com.thsst2.greenapp

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class IntentType {
    GREETING,
    EDIT_PREFS,
    FINALIZE_PREFS,
    ASK_INFO,
    FEEDBACK,
    CHANGE_FLOOR,
    MORE_INFO,
    NEXT_STOP,
    UNKNOWN
}

enum class ConversationContext {
    GREETING,       // user just started conversation
    PRETOUR,        // user reviewing or editing preferences
    TOUR_READY,     // preferences finalized, ready for tour / RAG + planner
    GENERAL         // normal conversation after tour setup
}

data class DialogueResult(
    val intent: IntentType,
    val context: ConversationContext,
    val message: String = "",
    val isRandom: Boolean? = null,
    val openProfileForPrefs: Boolean = false
)

class DialogueManager(private val context: Context) {

    private val db = MyAppDatabase.getInstance(context)
    private var currentContext = ConversationContext.GREETING

    // internal step tracker
    private var pendingStep: String? = null
    private var selectedTourTypeIsRandom: Boolean? = null

    companion object {
        private const val STEP_TOUR_TYPE = "tour_type"
        private const val STEP_EDIT_PREFS = "edit_prefs"
        private const val STEP_START_CONFIRMATION = "start_confirmation"
    }

    fun reset() {
        currentContext = ConversationContext.GREETING
        pendingStep = null
        selectedTourTypeIsRandom = null
    }

    suspend fun processMessage(userId: Long, input: String): DialogueResult = withContext(Dispatchers.IO) {
        val message = input.lowercase().trim()
        val intent = detectIntent(message)

        Log.d(
            "DialogueManager",
            "Intent=$intent, Context=$currentContext, pendingStep=$pendingStep, isRandom=$selectedTourTypeIsRandom"
        )

        when (currentContext) {
            ConversationContext.GREETING -> {
                return@withContext when {
                    intent == IntentType.GREETING -> DialogueResult(
                        intent = intent,
                        context = currentContext,
                        message = "Hi there! Ready to explore? (please type yes or no)"
                    )

                    isYes(message) -> {
                        currentContext = ConversationContext.PRETOUR
                        pendingStep = STEP_TOUR_TYPE

                        DialogueResult(
                            intent = intent,
                            context = currentContext,
                            message = "Great! Would you like a random tour or an ordered tour?"
                        )
                    }

                    isNo(message) -> {
                        DialogueResult(
                            intent = intent,
                            context = currentContext,
                            message = "Alright! Let me know when you're ready to set up your tour."
                        )
                    }

                    else -> {
                        DialogueResult(
                            intent = intent,
                            context = currentContext,
                            message = "Please say 'hi' to start the tour setup."
                        )
                    }
                }
            }

            ConversationContext.PRETOUR -> {
                return@withContext handlePretourStep(userId, message, intent)
            }

            ConversationContext.TOUR_READY -> {
                DialogueResult(
                    intent = IntentType.FINALIZE_PREFS,
                    context = currentContext,
                    message = "Generating your personalized tour now!",
                    isRandom = selectedTourTypeIsRandom
                )
            }

            else -> {
                DialogueResult(
                    intent = intent,
                    context = ConversationContext.GENERAL,
                    message = generateSmallTalk(intent, message)
                )
            }
        }
    }

    private suspend fun handlePretourStep(userId: Long, message: String, intent: IntentType): DialogueResult {
        return when (pendingStep) {
            STEP_TOUR_TYPE -> handleTourTypeStep(message, intent)
            STEP_EDIT_PREFS -> handleEditPrefsStep(userId, message, intent)
            STEP_START_CONFIRMATION -> handleStartConfirmationStep(message, intent)
            else -> {
                pendingStep = STEP_TOUR_TYPE
                DialogueResult(
                    intent = intent,
                    context = currentContext,
                    message = "Would you like a random tour or an ordered tour?"
                )
            }
        }
    }

    private suspend fun handleTourTypeStep(message: String, intent: IntentType): DialogueResult {
        val parsedTourType = parseTourType(message)

        return when (parsedTourType) {
            true -> {
                selectedTourTypeIsRandom = true
                pendingStep = STEP_START_CONFIRMATION

                DialogueResult(
                    intent = intent,
                    context = currentContext,
                    message = "Got it — random tour selected. Would you like to start your tour now?",
                    isRandom = true
                )
            }

            false -> {
                selectedTourTypeIsRandom = false
                pendingStep = STEP_EDIT_PREFS

                DialogueResult(
                    intent = intent,
                    context = currentContext,
                    message = "Got it — ordered tour selected. Would you like to review or edit your preferences first?",
                    isRandom = false
                )
            }

            null -> {
                DialogueResult(
                    intent = intent,
                    context = currentContext,
                    message = "Please choose either 'random' or 'ordered' tour."
                )
            }
        }
    }

    private suspend fun handleEditPrefsStep(userId: Long, message: String, intent: IntentType): DialogueResult {
        val prefsText = getPreferenceSummary(userId)

        return when {
            isYes(message) || intent == IntentType.EDIT_PREFS -> {
                DialogueResult(
                    intent = IntentType.EDIT_PREFS,
                    context = currentContext,
                    message = "Sure! I'll open your profile so you can edit your preferences.",
                    isRandom = false,
                    openProfileForPrefs = true
                )
            }

            isNo(message) -> {
                pendingStep = STEP_START_CONFIRMATION

                DialogueResult(
                    intent = IntentType.EDIT_PREFS,
                    context = currentContext,
                    message = "No problem. Your current preferences are: $prefsText. Would you like to start your ordered tour now?",
                    isRandom = false
                )
            }

            else -> {
                DialogueResult(
                    intent = intent,
                    context = currentContext,
                    message = "Would you like to edit your preferences first? Please answer yes or no."
                )
            }
        }
    }

    private fun handleStartConfirmationStep(message: String, intent: IntentType): DialogueResult {
        return when {
            isYes(message) -> {
                currentContext = ConversationContext.TOUR_READY
                pendingStep = null

                DialogueResult(
                    intent = IntentType.FINALIZE_PREFS,
                    context = currentContext,
                    message = "Perfect! Starting your tour now.",
                    isRandom = selectedTourTypeIsRandom
                )
            }

            isNo(message) -> {
                DialogueResult(
                    intent = intent,
                    context = currentContext,
                    message = "No worries. Let me know when you'd like to start your tour.",
                    isRandom = selectedTourTypeIsRandom
                )
            }

            else -> {
                DialogueResult(
                    intent = intent,
                    context = currentContext,
                    message = "Would you like to start your tour now? Please answer yes or no.",
                    isRandom = selectedTourTypeIsRandom
                )
            }
        }
    }

    suspend fun handleProfilePreferenceResult(userId: Long, didSave: Boolean): DialogueResult = withContext(Dispatchers.IO) {
        currentContext = ConversationContext.PRETOUR
        pendingStep = STEP_START_CONFIRMATION
        selectedTourTypeIsRandom = false

        val prefsText = getPreferenceSummary(userId)

        val reply = if (didSave) {
            "Your preferences have been updated to: $prefsText. Would you like to start your ordered tour now?"
        } else {
            "No changes were made. Your current preferences are: $prefsText. Would you like to start your ordered tour now?"
        }

        DialogueResult(
            intent = IntentType.EDIT_PREFS,
            context = currentContext,
            message = reply,
            isRandom = false
        )
    }

    private fun detectIntent(msg: String): IntentType {
        return when {
            msg.contains("hi") || msg.contains("hello") -> IntentType.GREETING
            msg.contains("edit preference") || msg.contains("edit preferences") ||
                    msg.contains("change preference") || msg.contains("change preferences") ||
                    msg.contains("update preference") || msg.contains("update preferences") -> IntentType.EDIT_PREFS
            msg.contains("done") || msg.contains("final") -> IntentType.FINALIZE_PREFS
            msg.contains("change floor") || msg.contains("go to floor") || msg.contains("another floor") -> IntentType.CHANGE_FLOOR
            msg.contains("more info") || msg.contains("tell me more") || msg.contains("more about") -> IntentType.MORE_INFO
            msg.contains("next stop") || msg.contains("next location") || msg.contains("next place") -> IntentType.NEXT_STOP
            msg.contains("info") || msg.contains("tell me about") -> IntentType.ASK_INFO
            msg.contains("thank") -> IntentType.FEEDBACK
            else -> IntentType.UNKNOWN
        }
    }

    private fun parseTourType(msg: String): Boolean? {
        return when {
            msg.contains("random") || msg.contains("surprise") -> true
            msg.contains("ordered") || msg.contains("order") -> false
            else -> null
        }
    }

    private fun isYes(msg: String): Boolean {
        return msg == "yes" || msg == "y" || msg.contains("sure") || msg.contains("okay")
    }

    private fun isNo(msg: String): Boolean {
        return msg == "no" || msg == "n" || msg.contains("not now")
    }

    private suspend fun getPreferenceSummary(userId: Long): String {
        val prefs = db.userPreferencesDao().getPreferencesByUser(userId)
        val currentPrefs = prefs?.interests ?: emptyList()
        return if (currentPrefs.isEmpty()) "none yet" else currentPrefs.joinToString(", ")
    }


    // fallback replies
    private fun generateSmallTalk(intent: IntentType, msg: String): String {
        return when (intent) {
            IntentType.ASK_INFO -> "Let me fetch that info for you..."
            IntentType.FEEDBACK -> "Thanks! Glad to help."
            IntentType.CHANGE_FLOOR -> "Okay, let's change floors."
            IntentType.MORE_INFO -> "Sure, here's more information."
            IntentType.NEXT_STOP -> "Alright, moving to the next stop."
            IntentType.UNKNOWN -> "I'm not sure I understood that."
            else -> "Alright!"
        }
    }
}
