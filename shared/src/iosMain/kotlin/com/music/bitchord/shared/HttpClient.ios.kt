package com.music.bitchord.shared

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json

fun createHttpClient(): HttpClient = HttpClient(Darwin) {
    install(ContentNegotiation) { json() }
    install(WebSockets)
}
