package com.thsst2.greenapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thsst2.greenapp.DialogueHistoryDao
import com.thsst2.greenapp.GeneratedPathDao
import com.thsst2.greenapp.GeofenceTriggerDao
import com.thsst2.greenapp.IntentLogDao
import com.thsst2.greenapp.PathDeviationAlertDao
import com.thsst2.greenapp.PerformanceMetricsDao
import com.thsst2.greenapp.PoiDao
import com.thsst2.greenapp.ResponseJustificationDao
import com.thsst2.greenapp.SessionDao
import com.thsst2.greenapp.TypeConverter
import com.thsst2.greenapp.UserDao
import com.thsst2.greenapp.UserFeedbackDao
import com.thsst2.greenapp.UserInteractionTimeDao
import com.thsst2.greenapp.UserLocationDao
import com.thsst2.greenapp.UserLogDao
import com.thsst2.greenapp.UserPreferencesDao
import com.thsst2.greenapp.UserQueryDao
import com.thsst2.greenapp.UserRoleDao
import com.thsst2.greenapp.UserSkippedOrDislikedLocationDao
import com.thsst2.greenapp.UserTourPathHistoryDao
import com.thsst2.greenapp.UserVisitedLocationDao

@Database(
    entities = [
        // Entities
        UserEntity::class,
        UserRoleEntity::class,
        UserPreferencesEntity::class,
        SessionEntity::class,
        DialogueHistoryEntity::class,
        GeneratedPathEntity::class,
        GeofenceTriggerEntity::class,
        IntentLogEntity::class,
        PathDeviationAlertEntity::class,
        PerformanceMetricsEntity::class,
        PoiEntity::class,
        ResponseJustificationEntity::class,
        UserQueryEntity::class,
        UserLogEntity::class,
        UserFeedbackEntity::class,
        UserInteractionTimeEntity::class,
        UserLocationEntity::class,
        UserTourPathHistoryEntity::class,
        UserVisitedLocationEntity::class,
        UserSkippedOrDislikedLocationEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(TypeConverter::class)
abstract class MyAppDatabase : RoomDatabase() {
    // DAOs
    abstract fun userDao(): UserDao
    abstract fun userRoleDao(): UserRoleDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun sessionDao(): SessionDao
    abstract fun dialogueHistoryDao(): DialogueHistoryDao
    abstract fun generatedPathDao(): GeneratedPathDao
    abstract fun geofenceTriggerDao(): GeofenceTriggerDao
    abstract fun intentLogDao(): IntentLogDao
    abstract fun pathDeviationAlertDao(): PathDeviationAlertDao
    abstract fun performanceMetricsDao(): PerformanceMetricsDao
    abstract fun poiDao(): PoiDao
    abstract fun responseJustificationDao(): ResponseJustificationDao
    abstract fun userQueryDao(): UserQueryDao
    abstract fun userLogDao(): UserLogDao
    abstract fun userFeedbackDao(): UserFeedbackDao
    abstract fun userInteractionTimeDao(): UserInteractionTimeDao
    abstract fun userLocationDao(): UserLocationDao
    abstract fun userTourPathHistoryDao(): UserTourPathHistoryDao
    abstract fun userVisitedLocationDao(): UserVisitedLocationDao
    abstract fun userSkippedOrDislikedLocationDao(): UserSkippedOrDislikedLocationDao

    companion object {
        @Volatile
        private var INSTANCE: MyAppDatabase? = null

        fun getInstance(context: Context): MyAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyAppDatabase::class.java,
                    "greenapp_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}