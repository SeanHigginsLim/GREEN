package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_log",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GeneratedPathEntity::class,
            parentColumns = ["generatedPathId"],
            childColumns = ["generatedPathId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserLogEntity(
    @PrimaryKey(autoGenerate = true)
    val userLogId: Long = 0,
    val userId: Long,
    val generatedPathId: Long = 0
)