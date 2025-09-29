package com.thsst2.greenapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class ChatRequest(val prompt: String)
data class ChatResponse(val response: String)

interface ChatApi {
    @POST("generate")
    fun generate(@Body request: ChatRequest): Call<ChatResponse>
}
