package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(
    tableName = "user",
    foreignKeys = [
        ForeignKey(
            entity = UserRoleEntity::class,
            parentColumns = ["userRoleId"],
            childColumns = ["userRoleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserEntity (
    @PrimaryKey val userId: Long,
    val userRoleId: Long?,           // FK → roles table (student, faculty, guest)
)