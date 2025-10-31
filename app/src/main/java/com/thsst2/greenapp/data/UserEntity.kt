package com.thsst2.greenapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(tableName = "user")
data class UserEntity (
    @PrimaryKey val userId: Long,
)