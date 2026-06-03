package com.example.mototap.features.mechanic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mototap.core.model.ChatMessage
import com.example.mototap.core.repository.AuthRepository
import com.example.mototap.core.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val otherParticipantName: String = "Chat",
    val isOtherTyping: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val jobId: String,
    private val currentUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        observeMessages()
        observeTypingStatus()
        fetchParticipantName()
    }

    private fun fetchParticipantName() {
        viewModelScope.launch {
            // Extract other userId from jobId (chat_userId_mechanicId)
            val parts = jobId.split("_")
            if (parts.size >= 3) {
                val otherId = if (parts[1] == currentUserId) parts[2] else parts[1]
                val profile = authRepository.getUserProfile(otherId)
                if (profile != null) {
                    _uiState.value = _uiState.value.copy(otherParticipantName = profile.name)
                }
            }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.observeMessages(jobId).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
                
                val unreadCount = messages.count { !it.read && it.senderId != currentUserId }
                if (unreadCount > 0) {
                    Log.d("ChatViewModel", "Marking $unreadCount messages as read in room $jobId")
                    chatRepository.markAsRead(jobId, currentUserId)
                }
            }
        }
    }

    private fun observeTypingStatus() {
        viewModelScope.launch {
            chatRepository.observeTypingStatus(jobId, currentUserId).collectLatest { isTyping ->
                _uiState.value = _uiState.value.copy(isOtherTyping = isTyping)
            }
        }
    }

    fun setTypingStatus(isTyping: Boolean) {
        viewModelScope.launch {
            chatRepository.setTypingStatus(jobId, currentUserId, isTyping)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        // OPTIMISTIC UPDATE: Add message to list immediately so it doesn't "disappear"
        val tempId = UUID.randomUUID().toString()
        val optimisticMessage = ChatMessage(
            id = tempId,
            senderId = currentUserId,
            text = text,
            timestampMillis = System.currentTimeMillis(),
            read = false
        )
        
        val currentList = _uiState.value.messages.toMutableList()
        currentList.add(optimisticMessage)
        _uiState.value = _uiState.value.copy(messages = currentList)
        
        viewModelScope.launch {
            val result = chatRepository.sendMessage(jobId, currentUserId, text)
            if (result.isFailure) {
                // If it really failed, we can show an error or remove the message
                _uiState.value = _uiState.value.copy(error = "Failed to send message")
            }
        }
    }
}
