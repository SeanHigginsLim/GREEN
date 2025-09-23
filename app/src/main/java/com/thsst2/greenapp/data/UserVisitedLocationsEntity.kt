package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_visited_locations",
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
        )
    ]
)
data class UserVisitedLocationsEntity(
    @PrimaryKey(autoGenerate = true)
    val userVisitedLocationsId: Long = 0,
    val userId: Long,
    val poiId: Long,
    val timestamp: Long,
    val duration: Long
)
