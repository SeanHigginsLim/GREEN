package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "session",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SessionEntity (
    @PrimaryKey
    val sessionId: Long = 0,
    val userId: Long,
    val componentsUsed: String?,                 // e.g. ["Chat","Map","Trivia"]
    val sessionStartedAt: String,                // ISO timestamp
    val sessionEndedAt: String? = null
)