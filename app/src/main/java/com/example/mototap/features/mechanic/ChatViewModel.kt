package com.example.mototap.features.mechanic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mototap.core.model.ChatMessage
import com.example.mototap.core.repository.AuthRepository
import com.example.mototap.core.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
        ensureRoom()
    }

    private fun ensureRoom() {
        viewModelScope.launch {
            val parts = jobId.split("_")
            if (parts.size < 3) return@launch
            val otherId = if (parts[1] == currentUserId) parts[2] else parts[1]
            val myProfile = authRepository.getUserProfile(currentUserId)
            val otherProfile = authRepository.getUserProfile(otherId)
            chatRepository.ensureConversation(
                currentUserId,
                otherId,
                buildMap {
                    myProfile?.name?.takeIf { it.isNotBlank() }?.let { put(currentUserId, it) }
                    otherProfile?.name?.takeIf { it.isNotBlank() }?.let { put(otherId, it) }
                },
            )
        }
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
            chatRepository.observeMessages(jobId, currentUserId).collect { messages ->
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

    private var typingJob: Job? = null
    private var isCurrentlyTyping = false

    fun onInputChanged(text: String) {
        val isBlank = text.isBlank()
        
        if (!isBlank && !isCurrentlyTyping) {
            isCurrentlyTyping = true
            viewModelScope.launch {
                chatRepository.setTypingStatus(jobId, currentUserId, true)
            }
        }

        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(2000)
            if (isCurrentlyTyping) {
                isCurrentlyTyping = false
                chatRepository.setTypingStatus(jobId, currentUserId, false)
            }
        }
    }

    fun setTypingStatus(isTyping: Boolean) {
        typingJob?.cancel()
        isCurrentlyTyping = isTyping
        viewModelScope.launch {
            chatRepository.setTypingStatus(jobId, currentUserId, isTyping)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        // Immediately clear typing status on send
        setTypingStatus(false)
        
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
            val myName = authRepository.getUserProfile(currentUserId)?.name
            val parts = jobId.split("_")
            val otherId = if (parts.size >= 3) {
                if (parts[1] == currentUserId) parts[2] else parts[1]
            } else {
                null
            }
            val otherName = otherId?.let { authRepository.getUserProfile(it)?.name }
            val result = chatRepository.sendMessage(
                jobId,
                currentUserId,
                text,
                senderName = myName,
                recipientName = otherName,
            )
            if (result.isFailure) {
                // If it really failed, we can show an error or remove the message
                _uiState.value = _uiState.value.copy(error = "Failed to send message")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Ensure typing status is cleared when leaving the chat
        viewModelScope.launch {
            chatRepository.setTypingStatus(jobId, currentUserId, false)
        }
    }
}
