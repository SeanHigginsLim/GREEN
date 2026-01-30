package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_feedback",
    foreignKeys = [
        ForeignKey(
            entity = PoiEntity::class,
            parentColumns = ["poiId"],
            childColumns = ["poiId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserFeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val userFeedbackId: Long = 0,
    val poiId: String,
    val userId: Long,
    
    // Overall ratings (1-5 scale)
    val rating: Int?,                               // POI rating
    val experienceRating: Int? = null,              // Overall tour experience
    val personalizationRating: Int? = null,         // How well personalized
    val navigationEaseRating: Int? = null,          // Ease of navigation
    
    val comments: String?,
    val timestamp: Long = System.currentTimeMillis()
)