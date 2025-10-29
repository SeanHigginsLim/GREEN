package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_role")
data class UserRoleEntity(
    @PrimaryKey(autoGenerate = true)
    val userRoleId: Long = 0,
    val roles: List<String>
)
