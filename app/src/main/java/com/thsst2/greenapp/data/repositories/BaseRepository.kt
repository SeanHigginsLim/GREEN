package com.thsst2.greenapp.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

open class BaseRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun <T> uploadToFirebase(collection: String, documentId: String, data: T) {
        firestore.collection(collection).document(documentId).set(data!!).await()
    }

    suspend fun deleteFromFirebase(collection: String, documentId: String) {
        firestore.collection(collection).document(documentId).delete().await()
    }
}