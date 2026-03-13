package com.thsst2.greenapp

import androidx.room.*
import com.thsst2.greenapp.data.*
import com.thsst2.greenapp.data.connections.*

// Generic Base DAO
interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: T): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: T)

    @Delete
    suspend fun delete(entity: T)
}

// DialogueHistory DAO
@Dao
interface DialogueHistoryDao: BaseDao<DialogueHistoryEntity> {
    @Query("SELECT * FROM dialogue_history WHERE dialogueHistoryId = :id")
    suspend fun getDialogueHistoryById(id: Long): DialogueHistoryEntity?

    @Query("SELECT * FROM dialogue_history")
    suspend fun getAllDialogueHistories(): List<DialogueHistoryEntity>

    @Query("SELECT * FROM dialogue_history WHERE userId = :userId ORDER BY turnNumber ASC")
    suspend fun getDialogueHistoryByUser(userId: Long): List<DialogueHistoryEntity>

    @Transaction
    @Query("SELECT * FROM dialogue_history WHERE userId = :userId")
    suspend fun getDialogueHistoryWithUser(userId: Long): List<DialogueHistoryWithUser>

    @Query("SELECT * FROM dialogue_history")
    suspend fun getAll(): List<DialogueHistoryEntity>

    @Query("DELETE FROM dialogue_history")
    suspend fun deleteAll()
}

// GeneratedPath DAO
@Dao
interface GeneratedPathDao: BaseDao<GeneratedPathEntity> {
    @Query("SELECT * FROM generated_path WHERE generatedPathId = :id")
    suspend fun getGeneratedPathById(id: Long): GeneratedPathEntity?

    @Query("SELECT * FROM generated_path")
    suspend fun getAllGeneratedPaths(): List<GeneratedPathEntity>

    @Query("SELECT * FROM generated_path WHERE userId = :userId")
    suspend fun getGeneratedPathsByUser(userId: Long): List<GeneratedPathEntity>

//    @Query("SELECT * FROM generated_path WHERE userId = :userId")
//    suspend fun getGeneratedPathsByUserLog(userId: Long): List<GeneratedPathEntity>

    @Transaction
    @Query("SELECT * FROM generated_path WHERE generatedPathId = :id")
    suspend fun getGeneratedPathWithUser(id: Long): GeneratedPathWithUser

    @Transaction
    @Query("SELECT * FROM generated_path WHERE generatedPathId = :id")
    suspend fun getGeneratedPathWithPois(id: Long): GeneratedPathWithPoi

    @Query("SELECT * FROM generated_path")
    suspend fun getAll(): List<GeneratedPathEntity>

    @Query("DELETE FROM generated_path")
    suspend fun deleteAll()
}

// GeofenceTrigger DAO
@Dao
interface GeofenceTriggerDao: BaseDao<GeofenceTriggerEntity> {
    @Query("SELECT * FROM geofence_trigger WHERE geofenceTriggerId = :id")
    suspend fun getGeofenceTriggerById(id: Long): GeofenceTriggerEntity?

    @Query("SELECT * FROM geofence_trigger")
    suspend fun getAllGeofenceTriggers(): List<GeofenceTriggerEntity>

    @Query("SELECT * FROM geofence_trigger WHERE userId = :userId")
    suspend fun getGeofenceTriggersByUser(userId: Long): List<GeofenceTriggerEntity>

    @Query("SELECT * FROM geofence_trigger WHERE poiId = :poiId")
    suspend fun getGeofenceTriggersByPoi(poiId: Long): List<GeofenceTriggerEntity>

    @Query("SELECT * FROM geofence_trigger WHERE userId = :userId")
    suspend fun getGeofenceTriggersByUserLog(userId: Long): List<GeofenceTriggerEntity>

    @Transaction
    @Query("SELECT * FROM geofence_trigger WHERE geofenceTriggerId = :id")
    suspend fun getGeofenceTriggerWithUser(id: Long): GeofenceTriggerWithUser

    @Transaction
    @Query("SELECT * FROM geofence_trigger WHERE geofenceTriggerId = :id")
    suspend fun getGeofenceTriggerWithPois(id: Long): GeofenceTriggerWithPoi

    @Query("SELECT * FROM geofence_trigger")
    suspend fun getAll(): List<GeofenceTriggerEntity>

    @Query("DELETE FROM geofence_trigger")
    suspend fun deleteAll()
}

// IntentLog DAO
@Dao
interface IntentLogDao: BaseDao<IntentLogEntity> {
    @Query("SELECT * FROM intent_log WHERE intentLogId = :id")
    suspend fun getIntentLogById(id: Long): IntentLogEntity?

    @Query("SELECT * FROM intent_log")
    suspend fun getAllIntentLogs(): List<IntentLogEntity>

    @Query("SELECT * FROM intent_log WHERE userId = :userId")
    suspend fun getIntentLogsByUser(userId: Long): List<IntentLogEntity>

    @Transaction
    @Query("SELECT * FROM intent_log WHERE intentLogId = :id")
    suspend fun getIntentLogWithUser(id: Long): IntentLogWithUser

    @Query("SELECT * FROM intent_log")
    suspend fun getAll(): List<IntentLogEntity>

    @Query("DELETE FROM intent_log")
    suspend fun deleteAll()
}

// PathDeviationAlert DAO
@Dao
interface PathDeviationAlertDao: BaseDao<PathDeviationAlertEntity> {
    @Query("SELECT * FROM path_deviation_alert WHERE pathDeviationAlertId = :id")
    suspend fun getPathDeviationAlertById(id: Long): PathDeviationAlertEntity?

    @Query("SELECT * FROM path_deviation_alert")
    suspend fun getAllPathDeviationAlerts(): List<PathDeviationAlertEntity>

    @Query("SELECT * FROM path_deviation_alert WHERE userId = :userId")
    suspend fun getPathDeviationAlertsByUser(userId: Long): List<PathDeviationAlertEntity>

    @Transaction
    @Query("SELECT * FROM path_deviation_alert WHERE pathDeviationAlertId = :id")
    suspend fun getPathDeviationAlertWithUser(id: Long): PathDeviationAlertWithUser

    @Query("SELECT * FROM path_deviation_alert")
    suspend fun getAll(): List<PathDeviationAlertEntity>

    @Query("DELETE FROM path_deviation_alert")
    suspend fun deleteAll()
}

// PerformanceMetrics DAO
@Dao
interface PerformanceMetricsDao: BaseDao<PerformanceMetricsEntity> {
    @Query("SELECT * FROM performance_metrics WHERE performanceMetricsId = :id")
    suspend fun getPerformanceMetricsById(id: Long): PerformanceMetricsEntity?

    @Query("SELECT * FROM performance_metrics WHERE sessionId = :sessionId")
    suspend fun getMetricsBySession(sessionId: Long): List<PerformanceMetricsEntity>

    @Query("SELECT * FROM performance_metrics")
    suspend fun getAll(): List<PerformanceMetricsEntity>

    @Query("DELETE FROM performance_metrics")
    suspend fun deleteAll()
}

// Poi DAO
@Dao
interface PoiDao: BaseDao<PoiEntity> {
    @Query("SELECT * FROM poi WHERE poiId = :id")
    suspend fun getPoiById(id: String): PoiEntity?

    @Query("SELECT * FROM poi")
    suspend fun getAllPois(): List<PoiEntity>

    @Query("SELECT * FROM poi")
    suspend fun getAll(): List<PoiEntity>

    @Query("DELETE FROM poi")
    suspend fun deleteAll()
}

// ResponseJustification DAO
@Dao
interface ResponseJustificationDao: BaseDao<ResponseJustificationEntity> {
    @Query("SELECT * FROM response_justification WHERE responseJustificationId = :id")
    suspend fun getResponseJustificationById(id: Long): ResponseJustificationEntity?

    @Query("SELECT * FROM response_justification WHERE userQueryId = :userQueryId")
    suspend fun getResponseJustificationsByQuery(userQueryId: Long): List<ResponseJustificationEntity>

    @Query("SELECT * FROM response_justification")
    suspend fun getAll(): List<ResponseJustificationEntity>

    @Query("DELETE FROM response_justification")
    suspend fun deleteAll()
}

// Session DAO
@Dao
interface SessionDao: BaseDao<SessionEntity> {
    @Query("SELECT * FROM session WHERE sessionId = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Query("SELECT * FROM session WHERE userId = :userId")
    suspend fun getSessionsByUser(userId: Long): List<SessionEntity>

    @Query("SELECT * FROM session")
    suspend fun getAll(): List<SessionEntity>

    @Query("DELETE FROM session")
    suspend fun deleteAll()

    @Query("SELECT * FROM session WHERE userId = :userId ORDER BY sessionId DESC LIMIT 1")
    suspend fun getLatestSessionForUser(userId: Long): SessionEntity?

    @Query("UPDATE session SET sessionEndedAt = :endedAt WHERE sessionId = :sessionId")
    suspend fun setSessionEndedAt(sessionId: Long, endedAt: String)
}

// Session Log DAO
@Dao
interface SessionLogDao: BaseDao<SessionLogEntity> {
    @Query("SELECT * FROM session_log WHERE sessionLogId = :id")
    suspend fun getSessionLogById(id: Long): SessionLogEntity?

    @Query("SELECT * FROM session_log WHERE userId = :userId")
    suspend fun getSessionLogsByUser(userId: Long): List<SessionLogEntity>

    @Query("SELECT * FROM session_log")
    suspend fun getAll(): List<SessionLogEntity>

    @Query("DELETE FROM session_log")
    suspend fun deleteAll()
}

// Transition DAO
@Dao
interface TransitionDao: BaseDao<TransitionEntity> {
    @Query("SELECT * FROM transition WHERE transitionId = :id")
    suspend fun getTransitionById(id: Long): TransitionEntity?

    @Query("SELECT * FROM transition")
    suspend fun getAll(): List<TransitionEntity>

    @Query("DELETE FROM transition")
    suspend fun deleteAll()
}

// User DAO
@Dao
interface UserDao: BaseDao<UserEntity> {
    @Query("SELECT * FROM user WHERE userId = :id")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM user")
    suspend fun getAll(): List<UserEntity>

    @Query("DELETE FROM user")
    suspend fun deleteAll()
}

// UserRole DAO
@Dao
interface UserRoleDao: BaseDao<UserRoleEntity> {
    @Query("SELECT * FROM user_role WHERE userId = :id")
    suspend fun getUserRoleById(id: Long): UserRoleEntity?

    @Query("SELECT * FROM user_role")
    suspend fun getAllUserRoles(): List<UserRoleEntity>

    @Query("SELECT * FROM user_role")
    suspend fun getAll(): List<UserRoleEntity>

    @Query("DELETE FROM user_role")
    suspend fun deleteAll()
}

// UserPreferences DAO
@Dao
interface UserPreferencesDao: BaseDao<UserPreferencesEntity> {
    @Query("SELECT * FROM user_preferences WHERE userPreferencesId = :id")
    suspend fun getUserPreferencesById(id: Long): UserPreferencesEntity?

    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    suspend fun getPreferencesByUser(userId: Long): UserPreferencesEntity?

    @Query("SELECT * FROM user_preferences")
    suspend fun getAll(): List<UserPreferencesEntity>

    @Query("DELETE FROM user_preferences")
    suspend fun deleteAll()
}

// UserQuery DAO
@Dao
interface UserQueryDao: BaseDao<UserQueryEntity> {
    @Query("SELECT * FROM user_query WHERE userQueryId = :id")
    suspend fun getUserQueryById(id: Long): UserQueryEntity?

    @Query("SELECT * FROM user_query WHERE userId = :userId")
    suspend fun getUserQueriesByUser(userId: Long): List<UserQueryEntity>

    @Query("SELECT * FROM user_query")
    suspend fun getAll(): List<UserQueryEntity>

    @Query("DELETE FROM user_query")
    suspend fun deleteAll()
}

// UserFeedback DAO
@Dao
interface UserFeedbackDao: BaseDao<UserFeedbackEntity> {
    @Query("SELECT * FROM user_feedback WHERE userFeedbackId = :id")
    suspend fun getUserFeedbackById(id: Long): UserFeedbackEntity?

    @Query("SELECT * FROM user_feedback WHERE userId = :userId")
    suspend fun getFeedbackByUserLog(userId: Long): List<UserFeedbackEntity>

    @Query("SELECT * FROM user_feedback WHERE poiId = :poiId")
    suspend fun getFeedbackByPoi(poiId: Long): List<UserFeedbackEntity>

    @Query("SELECT * FROM user_feedback")
    suspend fun getAll(): List<UserFeedbackEntity>

    @Query("DELETE FROM user_feedback")
    suspend fun deleteAll()
}

// UserInteractionTime DAO
@Dao
interface UserInteractionTimeDao: BaseDao<UserInteractionTimeEntity> {
    @Query("SELECT * FROM user_interaction_time WHERE userInteractionTimeId = :id")
    suspend fun getInteractionTimeById(id: Long): UserInteractionTimeEntity?

    @Query("SELECT * FROM user_interaction_time WHERE userId = :userId")
    suspend fun getInteractionTimesByUserLog(userId: Long): List<UserInteractionTimeEntity>

    @Query("SELECT * FROM user_interaction_time WHERE poiId = :poiId")
    suspend fun getInteractionTimesByPoi(poiId: Long): List<UserInteractionTimeEntity>

    @Query("SELECT * FROM user_interaction_time")
    suspend fun getAll(): List<UserInteractionTimeEntity>

    @Query("DELETE FROM user_interaction_time")
    suspend fun deleteAll()
}

// UserLocation DAO
@Dao
interface UserLocationDao: BaseDao<UserLocationEntity> {
    @Query("SELECT * FROM user_location WHERE userLocationId = :id")
    suspend fun getUserLocationById(id: Long): UserLocationEntity?

    @Query("SELECT * FROM user_location WHERE sessionId = :sessionId")
    suspend fun getLocationsBySession(sessionId: Long): List<UserLocationEntity>

    @Query("SELECT * FROM user_location")
    suspend fun getAll(): List<UserLocationEntity>

    @Query("DELETE FROM user_location")
    suspend fun deleteAll()
}

// UserLog DAO
@Dao
interface UserLogDao: BaseDao<UserLogEntity> {
    @Query("SELECT * FROM user_log WHERE userLogId = :id")
    suspend fun getUserLogById(id: Long): UserLogEntity?

    @Query("SELECT * FROM user_log WHERE userId = :userId")
    suspend fun getUserLogsByUser(userId: Long): List<UserLogEntity>

    @Query("SELECT * FROM user_log")
    suspend fun getAll(): List<UserLogEntity>

    @Query("DELETE FROM user_log")
    suspend fun deleteAll()
}

// UserSkippedOrDislikedLocation DAO
@Dao
interface UserSkippedOrDislikedLocationDao: BaseDao<UserSkippedOrDislikedLocationEntity> {
    @Query("SELECT * FROM user_skipped_or_disliked_location WHERE userSkippedOrDislikedLocationId = :id")
    suspend fun getById(id: Long): UserSkippedOrDislikedLocationEntity?

    @Query("SELECT * FROM user_skipped_or_disliked_location WHERE sessionId = :sessionId")
    suspend fun getBySession(sessionId: Long): List<UserSkippedOrDislikedLocationEntity>

    @Query("SELECT * FROM user_skipped_or_disliked_location WHERE poiId = :poiId")
    suspend fun getByPoi(poiId: Long): List<UserSkippedOrDislikedLocationEntity>

    @Query("SELECT * FROM user_skipped_or_disliked_location")
    suspend fun getAll(): List<UserSkippedOrDislikedLocationEntity>

    @Query("DELETE FROM user_skipped_or_disliked_location")
    suspend fun deleteAll()
}

// UserTourPathHistory DAO
@Dao
interface UserTourPathHistoryDao: BaseDao<UserTourPathHistoryEntity> {
    @Query("SELECT * FROM user_tour_path_history WHERE userTourPathHistoryId = :id")
    suspend fun getById(id: Long): UserTourPathHistoryEntity?

    @Query("SELECT * FROM user_tour_path_history WHERE sessionId = :sessionId")
    suspend fun getBySession(sessionId: Long): List<UserTourPathHistoryEntity>

    @Query("SELECT * FROM user_tour_path_history")
    suspend fun getAll(): List<UserTourPathHistoryEntity>

    @Query("DELETE FROM user_tour_path_history")
    suspend fun deleteAll()
}
@Dao
// UserVisitedLocation DAO

interface UserVisitedLocationDao: BaseDao<UserVisitedLocationEntity> {
    @Query("SELECT * FROM user_visited_location WHERE userVisitedLocationId = :id")
    suspend fun getById(id: Long): UserVisitedLocationEntity?

    @Query("SELECT * FROM user_visited_location WHERE sessionId = :sessionId")
    suspend fun getBySession(sessionId: Long): List<UserVisitedLocationEntity>

    @Query("SELECT * FROM user_visited_location WHERE poiId = :poiId")
    suspend fun getByPoi(poiId: Long): List<UserVisitedLocationEntity>

    @Transaction
    @Query("SELECT * FROM user_visited_location WHERE sessionId = :id")
    suspend fun getUserVisitedLocationWithSession(id: Long): List<UserVisitedLocationWithSession>

    @Transaction
    @Query("SELECT * FROM user_visited_location WHERE poiId = :id")
    suspend fun getUserVisitedLocationWithPoi(id: Long): List<UserVisitedLocationWithPoi>

    @Query("SELECT * FROM user_visited_location")
    suspend fun getAll(): List<UserVisitedLocationEntity>

    @Query("DELETE FROM user_visited_location")
    suspend fun deleteAll()
}

@Dao
interface LocalDataDao: BaseDao<LocalDataEntity> {
    @Query("SELECT * FROM local_data WHERE userId = :id")
    suspend fun getLocalData(id: Long): LocalDataEntity?

    @Query("SELECT * FROM local_data")
    suspend fun getAll(): List<LocalDataEntity>

    @Query("DELETE FROM local_data")
    suspend fun deleteAll()
}