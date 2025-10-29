package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "response_justification",
    foreignKeys = [
        ForeignKey(
            entity = UserQueryEntity::class,
            parentColumns = ["userQueryId"],
            childColumns = ["userQueryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ResponseJustificationEntity(
    @PrimaryKey(autoGenerate = true)
    val responseJustificationId: Long = 0,
    val userQueryId: Long,
    val explanation: String,
    val sourceDocs: List<String>, //list
)
