package com.thsst2.greenapp.data.repositories

import com.thsst2.greenapp.data.MyAppDatabase
import com.thsst2.greenapp.data.DialogueHistoryEntity

class DialogueHistoryRepository(private val db: MyAppDatabase) : BaseRepository() {
    suspend fun insertDialogue(dialogueHistory: DialogueHistoryEntity, syncToFirebase: Boolean = true) {
        db.dialogueHistoryDao().insert(dialogueHistory)
        if (syncToFirebase) uploadToFirebase("dialogue_history", dialogueHistory.dialogueHistoryId.toString(), dialogueHistory)
    }

    suspend fun getAll(): List<DialogueHistoryEntity> = db.dialogueHistoryDao().getAll()

    suspend fun deleteAll() = db.dialogueHistoryDao().deleteAll()
}
