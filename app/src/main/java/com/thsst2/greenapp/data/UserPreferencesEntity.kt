package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_preferences",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserPreferencesEntity(
    @PrimaryKey(autoGenerate = true)
    val userPreferencesId: Long = 0,
    val userId: Long,
    val interests: List<String>,
    val disinterests: List<String>?,
    val tourPace: String?
)
