package com.music.bitchord.shared

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Android engine wiring. iOS uses Darwin (see iosMain). commonMain only
 * declares HttpClient { } without an engine.
 */
actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    install(ContentNegotiation) { json() }
    install(WebSockets)
    // Installed with no defaults so per-request timeouts (see LyricsHttp)
    // apply without imposing a global deadline on streams.
    install(HttpTimeout)
}

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
