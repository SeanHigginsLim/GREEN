package com.thsst2.greenapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thsst2.greenapp.data.DialogueHistoryEntity
import com.thsst2.greenapp.data.GeneratedPathEntity
import com.thsst2.greenapp.data.GeofenceTriggerEntity
import com.thsst2.greenapp.data.IntentLogEntity
import com.thsst2.greenapp.data.PathDeviationAlertEntity
import com.thsst2.greenapp.data.PerformanceMetricsEntity
import com.thsst2.greenapp.data.PoiEntity
import com.thsst2.greenapp.data.ResponseJustificationEntity
import com.thsst2.greenapp.data.SessionEntity
import com.thsst2.greenapp.data.UserEntity
import com.thsst2.greenapp.data.UserFeedbackEntity
import com.thsst2.greenapp.data.UserInteractionTimeEntity
import com.thsst2.greenapp.data.UserLocationEntity
import com.thsst2.greenapp.data.UserLogEntity
import com.thsst2.greenapp.data.UserPreferencesEntity
import com.thsst2.greenapp.data.UserQueryEntity
import com.thsst2.greenapp.data.UserRoleEntity
import com.thsst2.greenapp.data.UserSkippedOrDislikedLocationEntity
import com.thsst2.greenapp.data.UserTourPathHistoryEntity
import com.thsst2.greenapp.data.UserVisitedLocationEntity

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
    version = 3,
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
                ).fallbackToDestructiveMigration(false).build()
                INSTANCE = instance
                instance
            }
        }
    }
}