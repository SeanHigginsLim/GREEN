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
    val poiId: Long,
    val userId: Long,
    val rating: Int?,                   // e.g., 1-5 stars
    val comments: String?,
    val timestamp: Long
)