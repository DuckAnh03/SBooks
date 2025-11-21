package com.example.sbooks.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isChatOpen = MutableStateFlow(false)
    val isChatOpen: StateFlow<Boolean> = _isChatOpen.asStateFlow()

    private var generativeModel: GenerativeModel? = null
    private var chat: com.google.ai.client.generativeai.Chat? = null

    fun initializeChat(apiKey: String) {
        if (generativeModel != null) return

        generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash-exp",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 2048
            }
        )

        // Khởi tạo chat với history để set system prompt
        chat = generativeModel?.startChat(
            history = listOf(
                content("user") {
                    text("Bạn là chuyên gia tư vấn sách. Hãy gợi ý sách phù hợp, giải thích lý do. Trả lời ngắn gọn bằng tiếng Việt, thân thiện.")
                },
                content("model") {
                    text("Xin chào! Tôi là trợ lý tư vấn sách AI. Hãy cho tôi biết sở thích của bạn! 📚")
                }
            )
        )

        _messages.value = listOf(
            Message(
                content = "Xin chào! Tôi là trợ lý tư vấn sách AI. Hãy cho tôi biết:\n\n" +
                        "• Bạn thích đọc thể loại gì?\n" +
                        "• Tâm trạng hiện tại của bạn?\n" +
                        "• Mục đích đọc sách?\n\n" +
                        "Hoặc hỏi trực tiếp về một cuốn sách nhé! 📚",
                isUser = false
            )
        )
    }

    fun toggleChat() {
        _isChatOpen.value = !_isChatOpen.value
    }

    fun closeChat() {
        _isChatOpen.value = false
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank() || chat == null) return

        _messages.value = _messages.value + Message(
            content = userMessage,
            isUser = true
        )

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = chat?.sendMessage(userMessage)
                val botMessage = response?.text ?: "Xin lỗi, tôi không thể trả lời lúc này. Vui lòng thử lại."

                _messages.value = _messages.value + Message(
                    content = botMessage,
                    isUser = false
                )
            } catch (e: Exception) {
                _messages.value = _messages.value + Message(
                    content = "⚠️ Đã xảy ra lỗi: ${e.message}\n\nVui lòng thử lại sau.",
                    isUser = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearChat() {
        chat = generativeModel?.startChat()
        _messages.value = listOf(
            Message(
                content = "Chat đã được làm mới. Hãy hỏi tôi về sách bạn muốn tìm!",
                isUser = false
            )
        )
    }
}