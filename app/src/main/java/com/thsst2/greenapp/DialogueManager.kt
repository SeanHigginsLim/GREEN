package com.thsst2.greenapp

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class IntentType {
    GREETING,
    START_TOUR,
    ADD_PREFERENCE,
    DELETE_PREFERENCE,
    FINALIZE_PREFS,
    ASK_INFO,
    FEEDBACK,
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
    val message: String = ""
)

class DialogueManager(private val context: Context) {

    private val db = MyAppDatabase.getInstance(context)
    private var currentContext = ConversationContext.GREETING
    private var tempPreferences = mutableListOf<String>()

    suspend fun processMessage(userId: Long, input: String): DialogueResult = withContext(Dispatchers.IO) {
        val message = input.lowercase().trim()
        val intent = detectIntent(message)

        Log.d("DialogueManager", "Intent=$intent, Context=$currentContext")

        when (currentContext) {
            ConversationContext.GREETING -> {
                return@withContext when {
                    intent == IntentType.GREETING -> DialogueResult(
                        intent, currentContext,
                        "Hi there! Ready to explore? (please type yes or no)"
                    )
                    message == "yes" -> {
                        currentContext = ConversationContext.PRETOUR
                        val prefs = db.userPreferencesDao().getPreferencesByUser(userId)
                        val currentPrefs = prefs?.interests ?: emptyList()
                        tempPreferences = currentPrefs.toMutableList()
                        val prefText = if (currentPrefs.isEmpty()) "none yet" else currentPrefs.joinToString(", ")
                        DialogueResult(
                            IntentType.START_TOUR, currentContext,
                            "Great! Your current preferences are: $prefText.\nWould you like to add or delete any? (yes/no)"
                        )
                    }
                    message == "no" -> DialogueResult(
                        intent, currentContext,
                        "Alright! Let me know when you’re ready to start your tour."
                    )
                    else -> DialogueResult(intent, currentContext, "Please say 'hi' to start the tour setup.")
                }
            }

            ConversationContext.PRETOUR -> {
                return@withContext when {
                    message == "yes" -> DialogueResult(
                        IntentType.ADD_PREFERENCE, currentContext,
                        "Sure! You can type 'add <preference>' or 'delete <preference>'."
                    )
                    message == "no" -> {
                        saveFinalPreferences(userId)
                        currentContext = ConversationContext.TOUR_READY
                        DialogueResult(
                            IntentType.FINALIZE_PREFS, currentContext,
                            "Perfect! Your preferences are finalized. Let’s start your tour!"
                        )
                    }
                    intent == IntentType.ADD_PREFERENCE -> {
                        val additions = message.replace("add", "").trim()
                            .split(",", "and", " ")
                            .filter { it.isNotBlank() }
                        tempPreferences.addAll(additions)
                        DialogueResult(
                            intent, currentContext,
                            "Added ${additions.joinToString(", ")}. Current preferences: ${tempPreferences.joinToString(", ")}.\nAdd or delete more? (yes/no)"
                        )
                    }
                    intent == IntentType.DELETE_PREFERENCE -> {
                        val deletions = message.replace("delete", "").trim()
                            .split(",", "and", " ")
                            .filter { it.isNotBlank() }
                        tempPreferences.removeAll(deletions)
                        DialogueResult(
                            intent, currentContext,
                            "Removed ${deletions.joinToString(", ")}. Current preferences: ${tempPreferences.joinToString(", ")}.\nModify more? (yes/no)"
                        )
                    }
                    else -> DialogueResult(
                        intent, currentContext,
                        "You can add or delete preferences, or type 'no' when you’re ready to start the tour."
                    )
                }
            }
            ConversationContext.TOUR_READY -> {
                return@withContext DialogueResult(
                    intent, currentContext,
                    "Generating your personalized tour now! Please wait."
                )
            }

            else -> DialogueResult(
                intent, ConversationContext.GENERAL,
                generateSmallTalk(intent, message)
            )
        }
    }


    private fun detectIntent(msg: String): IntentType {
        return when {
            msg.contains("hi") || msg.contains("hello") -> IntentType.GREETING
            msg == "yes" || msg.contains("start") -> IntentType.START_TOUR
            msg.contains("add") -> IntentType.ADD_PREFERENCE
            msg.contains("delete") || msg.contains("remove") -> IntentType.DELETE_PREFERENCE
            msg.contains("done") || msg.contains("final") -> IntentType.FINALIZE_PREFS
            msg.contains("info") || msg.contains("tell me about") -> IntentType.ASK_INFO
            msg.contains("thank") -> IntentType.FEEDBACK
            else -> IntentType.UNKNOWN
        }
    }

    private suspend fun saveFinalPreferences(userId: Long) {
        val dao = db.userPreferencesDao()
        val prefs = dao.getPreferencesByUser(userId)

        if (prefs != null) {
            // existing + new preferences
            val updatedInterests = (prefs.interests + tempPreferences).distinct()
            val updatedEntity = prefs.copy(interests = updatedInterests)

            dao.update(updatedEntity)

            Log.d("DialogueManager", "Updated preferences for user $userId: $updatedInterests")
        } else {
            Log.w("DialogueManager", "No existing UserPreferencesEntity found for user $userId.")
        }
    }


    // fallback replies
    private fun generateSmallTalk(intent: IntentType, msg: String): String {
        return when (intent) {
            IntentType.ASK_INFO -> "Let me fetch that info for you..."
            IntentType.FEEDBACK -> "Thanks! Glad to help."
            IntentType.UNKNOWN -> "I’m not sure I understood that."
            else -> "Alright!"
        }
    }
}
