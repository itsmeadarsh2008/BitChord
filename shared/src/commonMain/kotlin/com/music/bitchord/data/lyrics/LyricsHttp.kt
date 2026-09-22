package com.music.bitchord.data.lyrics

import com.music.bitchord.shared.createHttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

/**
 * Shared plumbing for the lyric providers, ported from `:app`'s
 * `data/lyrics/LyricsHttp.kt` (OkHttp) to Ktor so it compiles for iOS.
 *
 * Same contract, same deadlines: providers are raced against each other by
 * the lyrics lookup, so a provider that hangs holds up the whole lookup.
 * [LYRICS_TIMEOUT_MS] stays far shorter than any stream-oriented timeout —
 * a lyric that arrives after the second chorus is of no use to anyone.
 *
 * Suspend instead of blocking: every caller already runs inside
 * `withContext(ioDispatcher)`, so call sites compile unchanged.
 */
private const val LYRICS_TIMEOUT_MS = 6_000L
private const val LYRICS_CONNECT_TIMEOUT_MS = 3_000L
private const val AUTH_TIMEOUT_MS = 15_000L
private const val AUTH_CONNECT_TIMEOUT_MS = 10_000L

internal const val LYRICS_AGENT = "BitChord (https://github.com/bitchord)"

internal val lyricsJson = Json { ignoreUnknownKeys = true; isLenient = true }

private val client by lazy { createHttpClient() }

/** Body of a successful GET, or null for any failure at all. */
internal suspend fun lyricsGet(url: String, userAgent: String = LYRICS_AGENT): String? = runCatching {
    client.get(url) {
        header("User-Agent", userAgent)
        header("Accept", "application/json")
        timeout {
            requestTimeoutMillis = LYRICS_TIMEOUT_MS
            connectTimeoutMillis = LYRICS_CONNECT_TIMEOUT_MS
        }
    }.takeIf { it.status.isSuccess() }?.bodyAsText()
}.getOrNull()

/** Body of an authenticated provider GET without service-specific browser headers. */
internal suspend fun lyricsGetBearer(url: String, bearer: String): String? = runCatching {
    if (bearer.isBlank()) return null
    client.get(url) {
        header("User-Agent", LYRICS_AGENT)
        header("Accept", "application/json, text/plain, */*")
        header("Authorization", "Bearer $bearer")
        timeout {
            requestTimeoutMillis = AUTH_TIMEOUT_MS
            connectTimeoutMillis = AUTH_CONNECT_TIMEOUT_MS
        }
    }.takeIf { it.status.isSuccess() }?.bodyAsText()
}.getOrNull()

/**
 * [lyricsGet], with a bearer token and the headers Apple's own web player
 * sends alongside one — `amp-api.music.apple.com` answers a token with no
 * `Origin` at all the same way it answers a wrong one, with a 403.
 */
internal suspend fun lyricsGetAuthorized(url: String, bearer: String): String? = runCatching {
    client.get(url) {
        header("User-Agent", LYRICS_AGENT)
        header("Accept", "application/json")
        header("Authorization", "Bearer $bearer")
        header("Origin", "https://music.apple.com")
        header("Referer", "https://music.apple.com/")
        timeout {
            requestTimeoutMillis = LYRICS_TIMEOUT_MS
            connectTimeoutMillis = LYRICS_CONNECT_TIMEOUT_MS
        }
    }.takeIf { it.status.isSuccess() }?.bodyAsText()
}.getOrNull()
