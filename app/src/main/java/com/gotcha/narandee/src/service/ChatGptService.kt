package com.gotcha.narandee.src.service

import com.gotcha.narandee.config.ApplicationClass
import com.gotcha.narandee.src.models.ChatGptRequest
import com.gotcha.narandee.src.models.ChatGptResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatGptService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun getChatResponse(@Body request: ChatGptRequest): ChatGptResponse
}

object GptApi {
    val chatGptService: ChatGptService by lazy {
        ApplicationClass.GPTRetrofit.create(ChatGptService::class.java)
    }
}