package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transition"
)
data class TransitionEntity(
    @PrimaryKey(autoGenerate = true)
    val transitionId: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
)
