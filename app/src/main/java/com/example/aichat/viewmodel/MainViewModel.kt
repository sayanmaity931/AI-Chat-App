package com.example.aichat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.log.streamLog
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModel : ViewModel() {

    private val chatClient by lazy { ChatClient.instance() }

    fun createChannel() {
        viewModelScope.launch {
            val number = Random.nextInt(10000)
            chatClient.createChannel(
                channelType = "messaging",
                channelId = "channel$number",
                memberIds = listOf(chatClient.getCurrentUser()?.id.orEmpty()),
                extraData = mapOf()
            ).await().onSuccess {
                streamLog { "Created a new channel" }
            }.onError {
                streamLog { "error: $it" }
            }
        }
    }

}