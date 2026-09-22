package com.music.bitchord.shared

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json

/**
 * Android engine wiring. iOS uses Darwin (see iosMain). commonMain only
 * declares HttpClient { } without an engine.
 */
fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    install(ContentNegotiation) { json() }
    install(WebSockets)
}
