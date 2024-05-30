package com.gotcha.narandee.src.models

data class ChatMessage(
    val type: String,
    val name: String,
    val gptScript: String?,
    val userScript: String,
    var isTypingComplete: Boolean = false,  // 타이핑 효과 완료 여부 플래그,
    var placeAddress: String? = null
)
