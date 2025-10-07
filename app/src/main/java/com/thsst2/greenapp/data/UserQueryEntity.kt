package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserQueryEntity(
    @PrimaryKey(autoGenerate = true)
    val userQueryId: Long = 0,
    val userId: Long,
    val text: String,
    val timestamp: Long,
    val intentDetected: String?,
    val responseType: String?
)