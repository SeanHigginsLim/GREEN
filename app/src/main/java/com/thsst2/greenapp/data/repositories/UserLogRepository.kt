package com.thsst2.greenapp.data.repositories

import com.thsst2.greenapp.data.MyAppDatabase
import com.thsst2.greenapp.data.UserLogEntity

class UserLogRepository(private val db: MyAppDatabase) : BaseRepository() {

    suspend fun insertLog(userLog: UserLogEntity, syncToFirebase: Boolean = true) {
        db.userLogDao().insert(userLog)
        if (syncToFirebase) uploadToFirebase("user_logs", userLog.userLogId.toString(), userLog)
    }

    suspend fun getAll() = db.userLogDao().getAll()

    suspend fun deleteAll() = db.userLogDao().deleteAll()
}
