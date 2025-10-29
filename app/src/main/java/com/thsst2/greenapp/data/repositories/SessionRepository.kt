package com.thsst2.greenapp.data.repositories

import com.thsst2.greenapp.data.MyAppDatabase
import com.thsst2.greenapp.data.SessionEntity

class SessionRepository(private val db: MyAppDatabase) : BaseRepository() {

    suspend fun insertSession(session: SessionEntity, syncToFirebase: Boolean = true) {
        db.sessionDao().insert(session)
        if (syncToFirebase) uploadToFirebase("sessions", session.sessionId.toString(), session)
    }

    suspend fun getAllSessions() = db.sessionDao().getAll()

    suspend fun deleteAll() = db.sessionDao().deleteAll()
}