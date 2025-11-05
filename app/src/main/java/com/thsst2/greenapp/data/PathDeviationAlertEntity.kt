package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "path_deviation_alert",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PathDeviationAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val pathDeviationAlertId: Long = 0,
    val userId: Long,
    val deviationLocation: String,
    val timeStamp: String,
    val noticeSent: Boolean
)
