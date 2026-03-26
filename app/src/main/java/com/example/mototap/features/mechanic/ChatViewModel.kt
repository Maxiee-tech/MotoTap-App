package com.example.mototap.features.mechanic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mototap.core.model.ChatMessage
import com.example.mototap.core.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val jobId: String,
    private val currentUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.observeMessages(jobId).collectLatest { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            val result = chatRepository.sendMessage(jobId, currentUserId, text)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(error = "Failed to send message")
            }
        }
    }
}
