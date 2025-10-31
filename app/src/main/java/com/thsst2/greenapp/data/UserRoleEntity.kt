package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "user_role",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserRoleEntity(
    @PrimaryKey(autoGenerate = true)
    val userRoleId: Long = 0,
    val userId: Long,
    val role: String
)
