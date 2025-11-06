package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_interaction_time",
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
data class UserInteractionTimeEntity(
    @PrimaryKey(autoGenerate = true)
    val userInteractionTimeId: Long = 0,
    val poiId: Long,
    val userId: Long,
    val duration: Long, // Duration in seconds or milliseconds
    val timestamp: Long
)