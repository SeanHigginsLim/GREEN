package com.thsst2.greenapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thsst2.greenapp.data.*
@Database(
    entities = [
        // Entities
        UserEntity::class,
        UserRoleEntity::class,
        UserPreferencesEntity::class,
        SessionEntity::class,
        SessionLogEntity::class,
        DialogueHistoryEntity::class,
        GeneratedPathEntity::class,
        GeofenceTriggerEntity::class,
        IntentLogEntity::class,
        LocalDataEntity::class,
        PathDeviationAlertEntity::class,
        PerformanceMetricsEntity::class,
        PoiEntity::class,
        ResponseJustificationEntity::class,
        TransitionEntity::class,
        UserQueryEntity::class,
        UserLogEntity::class,
        UserFeedbackEntity::class,
        UserInteractionTimeEntity::class,
        UserLocationEntity::class,
        UserTourPathHistoryEntity::class,
        UserVisitedLocationEntity::class,
        UserSkippedOrDislikedLocationEntity::class
    ],
    version = 16,
    exportSchema = true
)
@TypeConverters(TypeConverter::class)
abstract class MyAppDatabase : RoomDatabase() {
    // DAOs
    abstract fun userDao(): UserDao
    abstract fun userRoleDao(): UserRoleDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun sessionDao(): SessionDao
    abstract fun sessionLogDao(): SessionLogDao
    abstract fun dialogueHistoryDao(): DialogueHistoryDao
    abstract fun generatedPathDao(): GeneratedPathDao
    abstract fun geofenceTriggerDao(): GeofenceTriggerDao
    abstract fun intentLogDao(): IntentLogDao
    abstract fun localDataDao(): LocalDataDao
    abstract fun pathDeviationAlertDao(): PathDeviationAlertDao
    abstract fun performanceMetricsDao(): PerformanceMetricsDao
    abstract fun poiDao(): PoiDao
    abstract fun responseJustificationDao(): ResponseJustificationDao
    abstract fun transitionDao(): TransitionDao
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