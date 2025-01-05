package com.example.aichat

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface AiService {

    @POST("start-ai-agent")
    suspend fun startAiAgent(@Body request: AiAgentRequest): AiAgentResponse

    @POST("stop-ai-agent")
    suspend fun stopAiAgent(@Body request: AiAgentRequest): AiAgentResponse

}

@Serializable
data class AiAgentRequest(
    val channel_id: String,
    val channel_type: String = "messaging"
)

@Serializable
data class AiAgentResponse(
    val message: String,
    val data: List<String>
)

object NetworkModule {

    private lateinit var retrofit : Retrofit
    private lateinit var _aiService: AiService

    val aiService: AiService
        get() = _aiService

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Increased timeout
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun init(context: Context){
        retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.6:3000/")
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .client(client)
            .build()

        _aiService = retrofit.create<AiService>()
    }
}