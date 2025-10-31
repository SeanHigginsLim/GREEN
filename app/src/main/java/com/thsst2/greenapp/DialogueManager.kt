package com.thsst2.greenapp

import android.util.Log
import com.thsst2.greenapp.data.UserPreferencesEntity

class DialogueManager {

    fun processUserIntent(userMessage: String, prefs: UserPreferencesEntity): String {
        val lower = userMessage.lowercase()

        return when {
            lower.contains("tour") -> "generate_tour"
            lower.contains("info") || lower.contains("tell me about") -> "fetch_poi_info"
            lower.contains("recommend") -> "recommend_poi"
            lower.contains("history") -> "show_history"
            else -> "unknown_intent"
        }
    }

    fun handleAction(action: String, prefs: UserPreferencesEntity): String {
        return when (action) {
            "generate_tour" -> "Generating a tour based on your interests: ${prefs.interests.joinToString()}"
            "fetch_poi_info" -> "Let me pull up information on that location."
            "recommend_poi" -> "Here are some POIs you might like."
            else -> "I’m not sure what you mean yet, could you rephrase?"
        }
    }
}
