package com.thsst2.greenapp

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import androidx.room.TypeConverter
import com.thsst2.greenapp.data.*

class TypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    // GeneratedPathEntity
    @TypeConverter
    fun fromGeneratedPathList(value: List<GeneratedPathEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toGeneratedPathList(value: String): List<GeneratedPathEntity> {
        val listType = object : TypeToken<List<GeneratedPathEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // GeofenceTriggerEntity
    @TypeConverter
    fun fromGeofenceTriggerList(value: List<GeofenceTriggerEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toGeofenceTriggerList(value: String): List<GeofenceTriggerEntity> {
        val listType = object : TypeToken<List<GeofenceTriggerEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // PathDeviationAlertEntity
    @TypeConverter
    fun fromPathDeviationAlertList(value: List<PathDeviationAlertEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toPathDeviationAlertList(value: String): List<PathDeviationAlertEntity> {
        val listType = object : TypeToken<List<PathDeviationAlertEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // DialogueHistoryEntity
    @TypeConverter
    fun fromDialogueHistoryList(value: List<DialogueHistoryEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toDialogueHistoryList(value: String): List<DialogueHistoryEntity> {
        val listType = object : TypeToken<List<DialogueHistoryEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // IntentLogEntity
    @TypeConverter
    fun fromIntentLogList(value: List<IntentLogEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toIntentLogList(value: String): List<IntentLogEntity> {
        val listType = object : TypeToken<List<IntentLogEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // TransitionEntity
    @TypeConverter
    fun fromTransitionList(value: List<TransitionEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toTransitionList(value: String): List<TransitionEntity> {
        val listType = object : TypeToken<List<TransitionEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // UserQueryEntity
    @TypeConverter
    fun fromUserQueryList(value: List<UserQueryEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toUserQueryList(value: String): List<UserQueryEntity> {
        val listType = object : TypeToken<List<UserQueryEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // UserFeedbackEntity
    @TypeConverter
    fun fromUserFeedbackList(value: List<UserFeedbackEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toUserFeedbackList(value: String): List<UserFeedbackEntity> {
        val listType = object : TypeToken<List<UserFeedbackEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // UserInteractionTimeEntity
    @TypeConverter
    fun fromUserInteractionTimeList(value: List<UserInteractionTimeEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toUserInteractionTimeList(value: String): List<UserInteractionTimeEntity> {
        val listType = object : TypeToken<List<UserInteractionTimeEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // PerformanceMetricsEntity
    @TypeConverter
    fun fromPerformanceMetricsList(value: List<PerformanceMetricsEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toPerformanceMetricsList(value: String): List<PerformanceMetricsEntity> {
        val listType = object : TypeToken<List<PerformanceMetricsEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // SessionEntity
    @TypeConverter
    fun fromSessionList(value: List<SessionEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toSessionList(value: String): List<SessionEntity> {
        val listType = object : TypeToken<List<SessionEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // UserLocationEntity
    @TypeConverter
    fun fromUserLocationList(value: List<UserLocationEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toUserLocationList(value: String): List<UserLocationEntity> {
        val listType = object : TypeToken<List<UserLocationEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // UserLogEntity
    @TypeConverter
    fun fromUserLogList(value: List<UserLogEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toUserLogList(value: String): List<UserLogEntity> {
        val listType = object : TypeToken<List<UserLogEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // UserSkippedOrDislikedLocationEntity
    @TypeConverter
    fun fromUserSkippedOrDislikedLocationList(value: List<UserSkippedOrDislikedLocationEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toUserSkippedOrDislikedLocationList(value: String): List<UserSkippedOrDislikedLocationEntity> {
        val listType = object : TypeToken<List<UserSkippedOrDislikedLocationEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // UserTourPathHistoryEntity
    @TypeConverter
    fun fromUserTourPathHistoryList(value: List<UserTourPathHistoryEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toUserTourPathHistoryList(value: String): List<UserTourPathHistoryEntity> {
        val listType = object : TypeToken<List<UserTourPathHistoryEntity>>() {}.type
        return gson.fromJson(value, listType)
    }

    // UserVisitedLocationEntity
    @TypeConverter
    fun fromUserVisitedLocationList(value: List<UserVisitedLocationEntity>): String = gson.toJson(value)

    @TypeConverter
    fun toUserVisitedLocationList(value: String): List<UserVisitedLocationEntity> {
        val listType = object : TypeToken<List<UserVisitedLocationEntity>>() {}.type
        return gson.fromJson(value, listType)
    }
}