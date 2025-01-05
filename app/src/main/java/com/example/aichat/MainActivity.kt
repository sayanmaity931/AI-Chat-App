package com.example.aichat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.aichat.ui.theme.AIChatTheme
import com.example.aichat.viewmodel.MainViewModel
import io.getstream.chat.android.client.BuildConfig
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.channels.SearchMode
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.ConnectionData
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.log.AndroidStreamLogger
import io.getstream.log.streamLog
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var chatClient: ChatClient
//    private var clientInitialisationState by mutableStateOf(InitializationState.NOT_INITIALIZED)

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // initialise the Stream logger
        AndroidStreamLogger.installOnDebuggableApp(application)

        NetworkModule.init(applicationContext)

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
            id = "tutorial-droid",
            name = "AI Android Stream",
            image = "https://picsum.photos/id/${Random.nextInt(1000)}/300/300"
        )

        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidHV0b3JpYWwtZHJvaWQifQ.WwfBzU1GZr0brt_fXnqKdKhz3oj0rbDUm2DqJO_SS5U"

        enableEdgeToEdge()
        setContent {

            var clientInitialisationState by remember { mutableStateOf(InitializationState.INITIALIZING) }
            var userState by remember { mutableStateOf<User?>(null)}

            lifecycleScope.launch {
                chatClient.connectUser(user, token).enqueue(object : Call.Callback<ConnectionData> {
                    override fun onResult(result: Result<ConnectionData>) {
                        if (result.isSuccess){
                            clientInitialisationState = InitializationState.COMPLETE
                            userState = user
                        }
                        else{
                            streamLog {
                                "Can't connect user. Please check the app README.md and ensure " +
                                        "**Disable Auth Checks** is ON in the Dashboard"
                            }
                            clientInitialisationState = InitializationState.NOT_INITIALIZED
                            Log.d("result", "onResult: ${result.toUnitResult()}")
                            }
                        }
                    }
                )
            }
            AIChatTheme {
                when (clientInitialisationState) {
                    InitializationState.COMPLETE -> {
                        if (userState != null) {
                            ChatTheme {
                                ChannelsScreen(
                                    title = stringResource(id = R.string.app_name),
                                    isShowingHeader = false,
                                    onHeaderActionClick = { mainViewModel.createChannel() },
                                    onChannelClick = { channel ->
                                        startActivity(
                                            ChannelActivity.getIntent(this, channel.cid)
                                                )
                                            },
                                            onBackPressed = { finish() },
                                            searchMode = SearchMode.Channels
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            text = "User data unavailable...",
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                        }
                    InitializationState.INITIALIZING -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    InitializationState.NOT_INITIALIZED -> {
                        Text(text = "Not initialized...")
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun onDestroy() {
        super.onDestroy()
        if (::chatClient.isInitialized) {
            chatClient.disconnect(
                flushPersistence = true
            )
        }
    }
}
