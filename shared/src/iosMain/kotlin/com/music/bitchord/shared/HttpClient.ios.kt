package com.music.bitchord.shared

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun createHttpClient(): HttpClient = HttpClient(Darwin) {
    install(ContentNegotiation) { json() }
    install(WebSockets)
    install(HttpTimeout)
}

// Kotlin/Native exposes no Dispatchers.IO (internal there); Default is the
// correct pool for blocking-adjacent network/file work on iOS.
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
