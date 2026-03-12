package com.thsst2.greenapp.data.connections

import androidx.room.Embedded
import androidx.room.Relation
import com.thsst2.greenapp.data.DialogueHistoryEntity
import com.thsst2.greenapp.data.PerformanceMetricsEntity
import com.thsst2.greenapp.data.SessionEntity
import com.thsst2.greenapp.data.UserEntity
import com.thsst2.greenapp.data.UserLocationEntity
import com.thsst2.greenapp.data.UserLogEntity
import com.thsst2.greenapp.data.UserPreferencesEntity
import com.thsst2.greenapp.data.UserRoleEntity
import com.thsst2.greenapp.data.UserVisitedLocationEntity
import com.thsst2.greenapp.data.UserSkippedOrDislikedLocationEntity
import com.thsst2.greenapp.data.UserTourPathHistoryEntity
import com.thsst2.greenapp.data.GeneratedPathEntity
import com.thsst2.greenapp.data.GeofenceTriggerEntity
import com.thsst2.greenapp.data.IntentLogEntity
import com.thsst2.greenapp.data.PathDeviationAlertEntity
import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.data.UserFeedbackEntity
import com.thsst2.greenapp.data.UserInteractionTimeEntity
import com.thsst2.greenapp.data.UserQueryEntity
import com.thsst2.greenapp.data.ResponseJustificationEntity

// User
data class UserWithDialogueHistory(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val dialogueHistories: List<DialogueHistoryEntity>
)
data class UserWithGeneratedPath(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val generatedPaths: List<GeneratedPathEntity>
)
data class UserWithGeofenceTrigger(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
        )
    val geofenceTriggers: List<GeofenceTriggerEntity>
)
data class UserWithIntentLog(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val intentLogs: List<IntentLogEntity>
)
data class UserWithPathDeviationAlert(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val pathDeviationAlerts: List<PathDeviationAlertEntity>
)
data class UserWithSession(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val session: SessionEntity
)
data class UserWithUserLocation(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val userLocation: UserLocationEntity
)
data class UserWithUserLog(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val userLogs: UserLogEntity
)
data class UserWithUserPreferences(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val userPreferences: UserPreferencesEntity
)
data class UserWithUserQuery(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val userQueries: List<UserQueryEntity>
)

// Poi
data class PoiWithGeneratedPath(
    @Embedded val poi: PoiEntity,
    @Relation(
        parentColumn = "poiId",
        entityColumn = "poiId"
    )
    val generatedPaths: List<GeneratedPathEntity>
)
data class PoiWithGeofenceTrigger(
    @Embedded val poi: PoiEntity,
    @Relation(
        parentColumn = "poiId",
        entityColumn = "poiId"
    )
    val geofenceTriggers: List<GeofenceTriggerEntity>
)
data class PoiWithUserFeedback(
    @Embedded val poi: PoiEntity,
    @Relation(
        parentColumn = "poiId",
        entityColumn = "poiId"
    )
    val userFeedback: UserFeedbackEntity
)
data class PoiWithUserInteractionTime(
    @Embedded val poi: PoiEntity,
    @Relation(
        parentColumn = "poiId",
        entityColumn = "poiId"
    )
    val userInteractionTime: UserInteractionTimeEntity
)
data class PoiWithUserSkippedOrDislikedLocation(
    @Embedded val poi: PoiEntity,
    @Relation(
        parentColumn = "poiId",
        entityColumn = "poiId"
    )
    val userSkippedOrDislikedLocation: UserSkippedOrDislikedLocationEntity
)
data class PoiWithUserVisitedLocation(
    @Embedded val poi: PoiEntity,
    @Relation(
        parentColumn = "poiId",
        entityColumn = "poiId"
    )
    val userVisitedLocations: List<UserVisitedLocationEntity>
)

// User Query
data class UserQueryWithIntentLog(
    @Embedded val userQuery: UserQueryEntity,
    @Relation(
        parentColumn = "userQueryId",
        entityColumn = "userQueryId"
    )
    val intentLog: IntentLogEntity
)
data class UserQueryWithResponseJustification(
    @Embedded val userQuery: UserQueryEntity,
    @Relation(
        parentColumn = "userQueryId",
        entityColumn = "userQueryId"
    )
    val responseJustification: ResponseJustificationEntity
)
data class UserQueryWithUser(
    @Embedded val userQuery: UserQueryEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val user: UserEntity
)


// Session
data class SessionWithPerformanceMetrics(
    @Embedded val session: SessionEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val performanceMetrics: PerformanceMetricsEntity
)
data class SessionWithUserLocation(
    @Embedded val session: SessionEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val userLocation: UserLocationEntity
)
data class SessionWithUserSkippedOrDislikedLocation(
    @Embedded val session: SessionEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val userSkippedOrDislikedLocations: List<UserSkippedOrDislikedLocationEntity>
)
data class SessionWithUserTourPathHistory(
    @Embedded val session: SessionEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val userTourPathHistories: List<UserTourPathHistoryEntity>
)
data class SessionWithUserVisitedLocation(
    @Embedded val session: SessionEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val userVisitedLocations: List<UserVisitedLocationEntity>
)

// User Role
data class UserRoleWithUser(
    @Embedded val userRole: UserRoleEntity,
    @Relation(
        parentColumn = "userRoleId",
        entityColumn = "userRoleId"
    )
    val user: UserEntity
)

// User Log
data class UserLogWithGeneratedPath(
    @Embedded val userLog: UserLogEntity,
    @Relation(
        parentColumn = "generatedPathId",
        entityColumn = "generatedPathId"
    )
    val generatedPaths: List<GeneratedPathEntity>
)
data class UserLogWithGeofenceTrigger(
    @Embedded val userLog: UserLogEntity,
    @Relation(
        parentColumn = "geofenceTriggerId",
        entityColumn = "geofenceTriggerId"
    )
    val geofenceTriggers: List<GeofenceTriggerEntity>
)
data class UserLogWithPathDeviationAlert(
    @Embedded val userLog: UserLogEntity,
    @Relation(
        parentColumn = "pathDeviationAlertId",
        entityColumn = "pathDeviationAlertId"
    )
    val pathDeviationAlerts: List<PathDeviationAlertEntity>
)
data class UserLogWithUserFeedback(
    @Embedded val userLog: UserLogEntity,
    @Relation(
        parentColumn = "userFeedbackId",
        entityColumn = "userFeedbackId"
    )
    val userFeedbacks: List<UserFeedbackEntity>
)
data class UserLogWithUserInteractionTime(
    @Embedded val userLog: UserLogEntity,
    @Relation(
        parentColumn = "userInteractionTimeId",
        entityColumn = "userInteractionTimeId"
    )
    val userInteractionTimes: List<UserInteractionTimeEntity>
)

// Dialogue History
data class DialogueHistoryWithUser(
    @Embedded val dialogueHistory: DialogueHistoryEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val user: UserEntity
)

// Generated Path
data class GeneratedPathWithUser(
    @Embedded val generatedPath: GeneratedPathEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val user: UserEntity
)
data class GeneratedPathWithPoi(
    @Embedded val generatedPath: GeneratedPathEntity,
    @Relation(
        parentColumn = "generatedPathId",
        entityColumn = "generatedPathId"
    )
    val pois: List<PoiEntity>
)
data class GeneratedPathWithUserLog(
    @Embedded val generatedPath: GeneratedPathEntity,
    @Relation(
        parentColumn = "generatedPathId",
        entityColumn = "generatedPathId"
    )
    val userLog: UserLogEntity
)

// Geofence Trigger
data class GeofenceTriggerWithUser(
    @Embedded val geofenceTrigger: GeofenceTriggerEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val user: UserEntity
)
data class GeofenceTriggerWithPoi(
    @Embedded val geofenceTrigger: GeofenceTriggerEntity,
    @Relation(
        parentColumn = "poiId",
        entityColumn = "poiId"
    )
    val poi: PoiEntity?
)
data class GeofenceTriggerWithUserLog(
    @Embedded val geofenceTrigger: GeofenceTriggerEntity,
    @Relation(
        parentColumn = "geofenceTriggerId",
        entityColumn = "geofenceTriggerId"
    )
    val userLog: UserLogEntity
)

// Intent Log
data class IntentLogWithUser(
    @Embedded val intentLog: IntentLogEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val user: UserEntity
)

// Path Deviation Alert
data class PathDeviationAlertWithUser(
    @Embedded val pathDeviationAlert: PathDeviationAlertEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val user: UserEntity
)
data class PathDeviationAlertWithUserLog(
    @Embedded val pathDeviationAlert: PathDeviationAlertEntity,
    @Relation(
        parentColumn = "pathDeviationAlertId",
        entityColumn = "pathDeviationAlertId"
    )
    val userLog: UserLogEntity
)

// User Visited Location
data class UserVisitedLocationWithPoi(
    @Embedded val userVisitedLocation: UserVisitedLocationEntity,
    @Relation(
        parentColumn = "poiId",
        entityColumn = "poiId"
    )
    val poi: PoiEntity
)
data class UserVisitedLocationWithSession(
    @Embedded val userVisitedLocation: UserVisitedLocationEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val session: SessionEntity
)

// User Skipped Or Disliked Location
data class UserSkippedOrDislikedLocationWithSession(
    @Embedded val userSkippedOrDislikedLocation: UserSkippedOrDislikedLocationEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val session: SessionEntity
)

// User Tour Path History
data class UserTourPathHistoryWithSession(
    @Embedded val userTourPathHistory: UserTourPathHistoryEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val session: SessionEntity
)

// User Feedback
data class UserFeedbackWithUserLog(
    @Embedded val userFeedback: UserFeedbackEntity,
    @Relation(
        parentColumn = "userFeedbackId",
        entityColumn = "userFeedbackId"
    )
    val userLog: UserLogEntity
)

// User Interaction Time
data class UserInteractionTimeWithUserLog(
    @Embedded val userInteractionTime: UserInteractionTimeEntity,
    @Relation(
        parentColumn = "userInteractionTime",
        entityColumn = "userInteractionTime"
    )
    val userLog: UserLogEntity
)