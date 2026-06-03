package com.example.mototap.core.data.firebase

import android.util.Log
import com.example.mototap.core.model.ChatMessage
import com.example.mototap.core.repository.ChatRepository
import com.example.mototap.core.repository.ChatSummary
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreChatRepository(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    private val chats = firestore.collection("chats")

    override fun observeMessages(jobId: String): Flow<List<ChatMessage>> = callbackFlow {
        val chatId = jobId.trim()
        if (chatId.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        // Listen to the messages subcollection
        val subscription = chats.document(chatId)
            .collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreChatRepo", "Error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    val senderId = doc.getString("senderId") ?: return@mapNotNull null
                    ChatMessage(
                        id = doc.id,
                        senderId = senderId,
                        text = doc.getString("text") ?: "",
                        timestampMillis = doc.getLong("timestampMillis") ?: 0L,
                        read = doc.getBoolean("read") ?: false
                    )
                }?.sortedBy { it.timestampMillis }.orEmpty()

                trySend(messages)
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun sendMessage(jobId: String, senderId: String, text: String): Result<Unit> = runCatching {
        val chatId = jobId.trim()
        
        // 1. Ensure the parent chat document exists (needed for summaries/typing)
        chats.document(chatId).set(
            mapOf("lastActive" to System.currentTimeMillis()),
            SetOptions.merge()
        ).await()

        // 2. Add message to subcollection
        chats.document(chatId).collection("messages").add(
            mapOf(
                "senderId" to senderId,
                "text" to text,
                "timestampMillis" to System.currentTimeMillis(),
                "read" to false
            )
        ).await()
    }

    override suspend fun markAsRead(jobId: String, currentUserId: String): Result<Unit> = runCatching {
        val chatId = jobId.trim()
        val messages = chats.document(chatId)
            .collection("messages")
            .get()
            .await()

        val unreadMessages = messages.filter { doc ->
            val senderId = doc.getString("senderId") ?: ""
            val read = doc.getBoolean("read") ?: false
            senderId != currentUserId && !read
        }

        if (unreadMessages.isEmpty()) return@runCatching

        val batch = firestore.batch()
        unreadMessages.forEach { doc ->
            batch.update(doc.reference, "read", true)
        }
        
        batch.set(chats.document(chatId), mapOf("lastReadUpdate" to System.currentTimeMillis()), SetOptions.merge())
        batch.commit().await()
    }

    override fun observeChatSummaries(userId: String): Flow<List<ChatSummary>> = callbackFlow {
        val subscription = chats.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            
            val docs = snapshot?.documents ?: emptyList()
            if (docs.isEmpty()) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val summaries = mutableMapOf<String, ChatSummary>()
            val targetDocs = docs.filter { it.id.contains(userId) }
            
            if (targetDocs.isEmpty()) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            targetDocs.forEach { chatDoc ->
                val chatId = chatDoc.id
                val parts = chatId.split("_")
                if (parts.size >= 3) {
                    val otherId = if (parts[1] == userId) parts[2] else parts[1]
                    
                    // Real-time listener for the last message in THIS specific chat
                    chatDoc.reference.collection("messages")
                        .orderBy("timestampMillis", Query.Direction.DESCENDING)
                        .limit(1)
                        .addSnapshotListener { msgSnapshot, _ ->
                            val lastMsgDoc = msgSnapshot?.documents?.firstOrNull()
                            if (lastMsgDoc != null) {
                                // Now fetch the user name (we can assume this is static for now)
                                firestore.collection("users").document(otherId).get()
                                    .addOnSuccessListener { userDoc ->
                                        val otherName = userDoc.getString("name") ?: "MotoTap User"
                                        val summary = ChatSummary(
                                            jobId = chatId,
                                            lastMessage = ChatMessage(
                                                id = lastMsgDoc.id,
                                                senderId = lastMsgDoc.getString("senderId") ?: "",
                                                text = lastMsgDoc.getString("text") ?: "",
                                                timestampMillis = lastMsgDoc.getLong("timestampMillis") ?: 0L,
                                                read = lastMsgDoc.getBoolean("read") ?: false
                                            ),
                                            otherUserName = otherName
                                        )
                                        summaries[chatId] = summary
                                        trySend(summaries.values.sortedByDescending { it.lastMessage.timestampMillis })
                                    }
                            }
                        }
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun setTypingStatus(jobId: String, userId: String, isTyping: Boolean): Result<Unit> = runCatching {
        chats.document(jobId.trim())
            .update("typingStatus.$userId", isTyping)
            .await()
    }

    override fun observeTypingStatus(jobId: String, currentUserId: String): Flow<Boolean> = callbackFlow {
        val subscription = chats.document(jobId.trim())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(false)
                    return@addSnapshotListener
                }
                
                val typingMap = snapshot?.get("typingStatus") as? Map<*, *>
                val isOtherTyping = typingMap?.entries?.any { it.key != currentUserId && it.value == true } ?: false
                trySend(isOtherTyping)
            }
        awaitClose { subscription.remove() }
    }
}
