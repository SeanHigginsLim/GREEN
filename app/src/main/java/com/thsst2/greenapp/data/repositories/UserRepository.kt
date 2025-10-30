package com.thsst2.greenapp.data.repositories

import com.thsst2.greenapp.MyAppDatabase
import com.thsst2.greenapp.data.UserEntity

class UserRepository(private val db: MyAppDatabase) : BaseRepository() {

    suspend fun insertUser(user: UserEntity, syncToFirebase: Boolean = true) {
        db.userDao().insert(user)
        if (syncToFirebase) uploadToFirebase("user", user.userId.toString(), user)
    }

    suspend fun getAll(): List<UserEntity> = db.userDao().getAll()

    suspend fun deleteAll() = db.userDao().deleteAll()
}