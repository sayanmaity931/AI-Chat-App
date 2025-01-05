package com.example.aichat

import android.app.Application
import io.getstream.chat.android.client.BuildConfig
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.models.ConnectionData
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.log.AndroidStreamLogger
import io.getstream.log.streamLog
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlin.random.Random

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // initialise the Stream logger
        AndroidStreamLogger.installOnDebuggableApp(this)

        /**
         * initialize a global instance of the [ChatClient].
         * The ChatClient is the main entry point for all low-level operations on chat.
         * e.g, connect/disconnect user to the server, send/update/pin message, etc.
         */
        val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING
        val offlinePluginFactory = StreamOfflinePluginFactory(
            appContext = applicationContext
        )
        val statePluginFactory = StreamStatePluginFactory(
            config = StatePluginConfig(
                backgroundSyncEnabled = true,
                userPresence = true
            ),
            appContext = applicationContext
        )
        val chatClient = ChatClient.Builder("gkpzhr9hypfk", applicationContext)
            .withPlugins(offlinePluginFactory, statePluginFactory)
            .logLevel(logLevel)
            .build()

        val user = User(
            id = "sayanmaity931@gmail.com",
            name = "AI Android Stream",
            image = "https://picsum.photos/id/${Random.nextInt(1000)}/300/300"
        )

        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoic2F5YW5tYWl0eTkzMUBnbWFpbC5jb20gIn0.O8v2IXsnhcI-CZ7kMJFbXLn4k4PM5X5DdYfPfPj3XIo"
        chatClient.connectUser(user, token).enqueue(object : Call.Callback<ConnectionData> {
            override fun onResult(result: Result<ConnectionData>) {
                if (result.isFailure) {
                    streamLog {
                        "Can't connect user. Please check the app README.md and ensure " +
                                "**Disable Auth Checks** is ON in the Dashboard"
                    }
                }
            }
        }
        )
    }
}