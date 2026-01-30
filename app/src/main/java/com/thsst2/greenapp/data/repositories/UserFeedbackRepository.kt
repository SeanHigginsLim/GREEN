package com.thsst2.greenapp.data.repositories

import com.thsst2.greenapp.MyAppDatabase
import com.thsst2.greenapp.data.UserFeedbackEntity

class UserFeedbackRepository(private val db: MyAppDatabase) : BaseRepository() {

    suspend fun insertFeedback(userFeedback: UserFeedbackEntity, syncToFirebase: Boolean = true) {
        db.userFeedbackDao().insert(userFeedback)
        if (syncToFirebase) uploadToFirebase("user_feedback", userFeedback.userFeedbackId.toString(), userFeedback)
    }

    suspend fun getFeedbackByUserLog(userId: Long): List<UserFeedbackEntity> = 
        db.userFeedbackDao().getFeedbackByUserLog(userId)

    suspend fun getFeedbackByPoi(poiId: Long): List<UserFeedbackEntity> = 
        db.userFeedbackDao().getFeedbackByPoi(poiId)

    suspend fun getAll(): List<UserFeedbackEntity> = db.userFeedbackDao().getAll()

    suspend fun deleteAll() = db.userFeedbackDao().deleteAll()
    
    // Get average ratings for analysis
    suspend fun getAverageFeedback(): Map<String, Double> {
        val allFeedback = getAll()
        if (allFeedback.isEmpty()) return emptyMap()
        
        return mapOf(
            "avgRating" to allFeedback.mapNotNull { it.rating }.average(),
            "avgExperience" to allFeedback.mapNotNull { it.experienceRating }.average(),
            "avgPersonalization" to allFeedback.mapNotNull { it.personalizationRating }.average(),
            "avgNavigationEase" to allFeedback.mapNotNull { it.navigationEaseRating }.average()
        )
    }
}
