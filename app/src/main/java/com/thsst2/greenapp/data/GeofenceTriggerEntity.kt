package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "geofence_trigger",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PoiEntity::class,
            parentColumns = ["poiId"],
            childColumns = ["poiId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserLogEntity::class,
            parentColumns = ["userLogId"],
            childColumns = ["userLogId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GeofenceTriggerEntity(
    @PrimaryKey(autoGenerate = true)
    val geofenceTriggerId: Long = 0,
    val userId: Long,
    val poiId: Long,
    val userLogId: Long?,
    val entryTime: String,
    val exitTime: String,
    val triggerType: String
)
