package com.example.mototap.core.data.firebase

import com.example.mototap.core.model.ChatMessage
import com.example.mototap.core.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreChatRepository(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun observeMessages(jobId: String): Flow<List<ChatMessage>> = callbackFlow {
        val subscription = firestore.collection("jobs")
            .document(jobId)
            .collection("messages")
            .orderBy("timestampMillis", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    ChatMessage(
                        id = doc.id,
                        senderId = doc.getString("senderId") ?: "",
                        text = doc.getString("text") ?: "",
                        timestampMillis = doc.getLong("timestampMillis") ?: 0L,
                        read = doc.getBoolean("read") ?: false
                    )
                }.orEmpty()

                trySend(messages)
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun sendMessage(jobId: String, senderId: String, text: String): Result<Unit> = runCatching {
        firestore.collection("jobs")
            .document(jobId)
            .collection("messages")
            .add(
                mapOf(
                    "senderId" to senderId,
                    "text" to text,
                    "timestampMillis" to System.currentTimeMillis(),
                    "read" to false
                )
            ).await()
        Unit
    }
}
