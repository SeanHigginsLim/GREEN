package com.thsst2.greenapp

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val suggestions: List<String> = emptyList()
)
