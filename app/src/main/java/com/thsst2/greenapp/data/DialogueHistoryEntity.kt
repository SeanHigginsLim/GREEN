package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DialogueHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val dialogueHistoryId: Long = 0,
    val userId: Long,
    val userText: String,
    val systemResponse: String,
    val contextSnapshot: String?,
    val turnNumber: Int
)