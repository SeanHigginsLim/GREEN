package com.thsst2.greenapp.data.repositories

import com.thsst2.greenapp.data.MyAppDatabase
import com.thsst2.greenapp.data.UserLocationEntity

class UserLocationRepository(private val db: MyAppDatabase) : BaseRepository() {

    suspend fun insertLocation(userLocation: UserLocationEntity, syncToFirebase: Boolean = true) {
        db.userLocationDao().insert(userLocation)
        if (syncToFirebase) uploadToFirebase("user_locations", userLocation.userLocationId.toString(), userLocation)
    }

    suspend fun getAll(): List<UserLocationEntity> = db.userLocationDao().getAll()

    suspend fun deleteAll() = db.userLocationDao().deleteAll()
}
