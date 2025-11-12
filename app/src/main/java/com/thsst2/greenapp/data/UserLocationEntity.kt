package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_location",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val userLocationId: Long = 0,
    val userId: Long,
    val sessionId: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val accuracyRadius: Float,
    val floor: Int? = null
)
