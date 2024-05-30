package com.gotcha.narandee.src.models

data class ChatGptRequest(
    val model: String,
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class ChatGptResponse(
    val id: String,
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

