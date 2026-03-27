package com.example.mototap.core.data.firebase

import android.util.Log
import com.example.mototap.core.model.ChatMessage
import com.example.mototap.core.repository.ChatRepository
import com.example.mototap.core.repository.ChatSummary
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

    override fun observeChatSummaries(userId: String): Flow<List<ChatSummary>> = callbackFlow {
        // This is a simplified version. In a real app, you might want a "chats" collection
        // or to query jobs where the user is either the driver or the mechanic.
        val subscription = firestore.collection("jobs")
            .whereIn("status", listOf("REQUESTED", "ASSIGNED", "IN_PROGRESS", "COMPLETED"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreChatRepo", "Error observing jobs for chats: ${error.message}")
                    return@addSnapshotListener
                }

                val jobs = snapshot?.documents ?: emptyList()
                val summaries = mutableListOf<ChatSummary>()

                // For each job, check if it has messages and if the user is involved
                jobs.forEach { jobDoc ->
                    val driverId = jobDoc.getString("driverId")
                    val mechanicId = jobDoc.getString("mechanicId")
                    
                    if (driverId == userId || mechanicId == userId) {
                        val jobId = jobDoc.id
                        val otherUserId = if (driverId == userId) mechanicId else driverId
                        
                        // We need to fetch the last message and the other user's name
                        // Note: This is inefficient in a real production app without a dedicated chats collection
                        // but serves the purpose for this implementation.
                        firestore.collection("jobs")
                            .document(jobId)
                            .collection("messages")
                            .orderBy("timestampMillis", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { msgSnapshot ->
                                val lastMsgDoc = msgSnapshot.documents.firstOrNull()
                                if (lastMsgDoc != null) {
                                    val lastMsg = ChatMessage(
                                        id = lastMsgDoc.id,
                                        senderId = lastMsgDoc.getString("senderId") ?: "",
                                        text = lastMsgDoc.getString("text") ?: "",
                                        timestampMillis = lastMsgDoc.getLong("timestampMillis") ?: 0L,
                                        read = lastMsgDoc.getBoolean("read") ?: false
                                    )
                                    
                                    if (otherUserId != null) {
                                        firestore.collection("users").document(otherUserId).get()
                                            .addOnSuccessListener { userDoc ->
                                                summaries.add(ChatSummary(
                                                    jobId = jobId,
                                                    lastMessage = lastMsg,
                                                    otherUserName = userDoc.getString("name") ?: "MotoTap User"
                                                ))
                                                // Emit the list once we've processed as much as we can
                                                trySend(summaries.sortedByDescending { it.lastMessage.timestampMillis }.toList())
                                            }
                                    }
                                }
                            }
                    }
                }
            }

        awaitClose { subscription.remove() }
    }
}
