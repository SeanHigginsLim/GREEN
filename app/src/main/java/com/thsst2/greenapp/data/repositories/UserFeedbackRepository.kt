package com.thsst2.greenapp.data.repositories

import com.thsst2.greenapp.data.MyAppDatabase
import com.thsst2.greenapp.data.UserFeedbackEntity

class UserFeedbackRepository(private val db: MyAppDatabase) : BaseRepository() {

    suspend fun insertFeedback(userFeedback: UserFeedbackEntity, syncToFirebase: Boolean = true) {
        db.userFeedbackDao().insert(userFeedback)
        if (syncToFirebase) uploadToFirebase("user_feedback", userFeedback.userFeedbackId.toString(), userFeedback)
    }

    suspend fun getAll() = db.userFeedbackDao().getAll()

    suspend fun deleteAll() = db.userFeedbackDao().deleteAll()
}
