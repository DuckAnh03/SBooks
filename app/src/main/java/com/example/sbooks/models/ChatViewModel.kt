package com.example.sbooks.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sbooks.database.dao.BookDao
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatViewModel(
    private val bookDao: BookDao
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isChatOpen = MutableStateFlow(false)
    val isChatOpen: StateFlow<Boolean> = _isChatOpen.asStateFlow()

    private var generativeModel: GenerativeModel? = null
    private var chat: com.google.ai.client.generativeai.Chat? = null

    // Định nghĩa function cho Gemini
    private val searchBooksFunction = defineFunction(
        name = "search_books",
        description = "Tìm kiếm sách trong cơ sở dữ liệu theo tên, tác giả hoặc thể loại",
        parameters = listOf(
            Schema.str("query", "Từ khóa tìm kiếm (tên sách, tác giả, thể loại)"),
            Schema.int("limit", "Số lượng kết quả tối đa (mặc định 5)")
        )
    )

    private val getBookDetailsFunction = defineFunction(
        name = "get_book_details",
        description = "Lấy thông tin chi tiết của một cuốn sách theo ID",
        parameters = listOf(
            Schema.str("bookId", "ID của sách cần xem chi tiết")
        )
    )

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
            },
            tools = listOf(Tool(listOf(searchBooksFunction, getBookDetailsFunction)))
        )

        chat = generativeModel?.startChat(
            history = listOf(
                content("user") {
                    text("""
                        Bạn là trợ lý tư vấn sách của cửa hàng sách trực tuyến.
                        
                        NHIỆM VỤ:
                        - Khi khách hỏi về sách, SỬ DỤNG function search_books để tìm trong database
                        - Giới thiệu sách có sẵn, đưa ra lý do phù hợp
                        - Nếu không tìm thấy, gợi ý sách tương tự
                        - Trả lời ngắn gọn, thân thiện bằng tiếng Việt
                        
                        QUY TẮC:
                        - LUÔN tìm kiếm database trước khi trả lời
                        - Chỉ giới thiệu sách có trong kết quả tìm kiếm
                        - Hiển thị giá, tác giả, đánh giá nếu có
                    """.trimIndent())
                },
                content("model") {
                    text("Xin chào! Tôi sẽ giúp bạn tìm sách phù hợp từ kho sách của chúng tôi. Bạn đang tìm sách gì? 📚")
                }
            )
        )

        _messages.value = listOf(
            Message(
                content = "Xin chào! Tôi sẽ giúp bạn tìm sách từ kho của chúng tôi.\n\n" +
                        "Hãy hỏi tôi:\n" +
                        "• \"Có bán sách Lão Hạc không?\"\n" +
                        "• \"Sách về lập trình Android\"\n" +
                        "• \"Truyện ngắn Nam Cao\" 📚",
                isUser = false
            )
        )
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
                var response = chat?.sendMessage(userMessage)

                // Xử lý function calling
                while (response?.functionCalls?.isNotEmpty() == true) {
                    val functionCall = response.functionCalls.first()
                    val functionResponse = handleFunctionCall(functionCall)

                    // Gửi kết quả function về cho Gemini
                    response = chat?.sendMessage(
                        content("function") {
                            part(FunctionResponsePart(functionCall.name, functionResponse))
                        }
                    )
                }

                val botMessage = response?.text ?: "Xin lỗi, tôi không thể trả lời lúc này."

                _messages.value = _messages.value + Message(
                    content = botMessage,
                    isUser = false
                )
            } catch (e: Exception) {
                _messages.value = _messages.value + Message(
                    content = "⚠️ Lỗi: ${e.message}",
                    isUser = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun handleFunctionCall(functionCall: FunctionCallPart): JSONObject {
        return when (functionCall.name) {
            "search_books" -> {
                val query = functionCall.args?.get("query") as? String ?: ""

                val limit = (functionCall.args?.get("limit") as? Any)?.toString()?.toIntOrNull() ?: 5

                val books = bookDao.BOTsearchBooks(query, limit)

                JSONObject().apply {
                    put("success", true)
                    put("count", books.size)
                    put("books", books.map { book ->
                        JSONObject().apply {
                            put("id", book.id)
                            put("title", book.title)
                            put("author", book.author)
                            put("price", book.price)
                            put("category", book.categoryName)
                            put("rating", book.rating)
                            put("inStock", book.stock)
                        }
                    })
                }
            }

            "get_book_details" -> {
                val bookId = functionCall.args?.get("bookId") as? String ?: ""
                val book = bookDao.getBookById(bookId)

                if (book != null) {
                    JSONObject().apply {
                        put("success", true)
                        put("book", JSONObject().apply {
                            put("id", book.id)
                            put("title", book.title)
                            put("author", book.author)
                            put("description", book.description)
                            put("price", book.price)
                            put("category", book.categoryName)
                            put("rating", book.rating)
                            put("inStock", book.stock)
                        })
                    }
                } else {
                    JSONObject().apply {
                        put("success", false)
                        put("error", "Không tìm thấy sách")
                    }
                }
            }

            else -> JSONObject().apply {
                put("error", "Unknown function")
            }
        }
    }

    fun toggleChat() {
        _isChatOpen.value = !_isChatOpen.value
    }

    fun closeChat() {
        _isChatOpen.value = false
    }

    fun clearChat() {
        chat = generativeModel?.startChat()
        _messages.value = listOf(
            Message(
                content = "Chat đã được làm mới. Hãy hỏi tôi về sách!",
                isUser = false
            )
        )
    }
}