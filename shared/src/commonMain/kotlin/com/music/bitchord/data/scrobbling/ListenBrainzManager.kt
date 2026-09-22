package com.music.bitchord.data.scrobbling

import com.music.bitchord.data.DebugLog as Log
import com.music.bitchord.data.model.Song
import com.music.bitchord.shared.createHttpClient
import com.music.bitchord.shared.ioDispatcher
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object ListenBrainzManager {
    private const val TAG = "ListenBrainzManager"
    private const val API_URL = "https://api.listenbrainz.org/1/submit-listens"

    private val client by lazy { createHttpClient() }

    @OptIn(ExperimentalTime::class)
    private suspend fun postListen(bodyJson: String, token: String, logTag: String): Boolean = runCatching {
        val resp = client.post(API_URL) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Token $token")
            setBody(bodyJson)
        }
        if (resp.status.isSuccess()) {
            true
        } else {
            val bodyText = runCatching { resp.bodyAsText() }.getOrNull().orEmpty()
            Log.w(TAG, "$logTag submit failed: ${resp.status.value} - $bodyText")
            false
        }
    }.getOrElse {
        Log.e(TAG, "$logTag failed", it)
        false
    }

    suspend fun submitPlayingNow(
        token: String,
        song: Song?,
        positionMs: Long,
        durationMsOverride: Long? = null,
        primaryArtistOnly: Boolean = false,
    ): Boolean {
        if (token.isBlank() || song == null) return false
        return withContext(ioDispatcher) {
            val durationMs = durationMsOverride ?: parseDurationMs(song.durationText)
            // The API rejects a zero/negative duration_ms, and it is
            // optional — so only send it when it is actually known.
            val durationPart = if (durationMs > 0) "\"duration_ms\":$durationMs," else ""
            val releaseName = song.albumName.orEmpty()
            val releasePart = if (releaseName.isBlank()) "" else "\"release_name\":\"${escapeJson(releaseName)}\","
            val artist = if (primaryArtistOnly) song.artist.primaryArtist() else song.artist
            val trackMetadata = """{"track_metadata":{"artist_name":"${escapeJson(artist)}","track_name":"${escapeJson(song.title)}",$releasePart"additional_info":{${durationPart}"position_ms":$positionMs,"submission_client":"BitChord"}}}"""
            val bodyJson = "{\"listen_type\":\"playing_now\",\"payload\":[$trackMetadata]}"
            Log.d(TAG, "submitPlayingNow: $bodyJson")
            if (postListen(bodyJson, token, "playing_now")) {
                Log.d(TAG, "playing_now submitted for ${song.title}")
                true
            } else {
                false
            }
        }
    }

    suspend fun submitFinished(
        token: String,
        song: Song?,
        startMs: Long,
        endMs: Long,
        durationMsOverride: Long? = null,
        primaryArtistOnly: Boolean = false,
    ): Boolean {
        if (token.isBlank() || song == null) return false
        return withContext(ioDispatcher) {
            val durationMs = durationMsOverride ?: parseDurationMs(song.durationText)
            val durationPart = if (durationMs > 0) "\"duration_ms\":$durationMs," else ""
            val releaseName = song.albumName.orEmpty()
            val releasePart = if (releaseName.isBlank()) "" else "\"release_name\":\"${escapeJson(releaseName)}\","
            var listenedAtStart = startMs / 1000L
            val minListenTs = 1033430400L
            if (listenedAtStart < minListenTs) {
                listenedAtStart = nowEpochSeconds()
            }
                val artist = if (primaryArtistOnly) song.artist.primaryArtist() else song.artist
                val trackMetadata = """{"listened_at":$listenedAtStart,"track_metadata":{"artist_name":"${escapeJson(artist)}","track_name":"${escapeJson(song.title)}",$releasePart"additional_info":{${durationPart}"start_ms":$startMs,"end_ms":$endMs,"submission_client":"BitChord"}}}"""
                val bodyJson = "{\"listen_type\":\"single\",\"payload\":[$trackMetadata]}"
                Log.d(TAG, "submitFinished: $bodyJson")
                if (postListen(bodyJson, token, "finished listen")) {
                    Log.d(TAG, "finished listen submitted for ${song.title}")
                    true
                } else {
                    false
                }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun nowEpochSeconds(): Long = Clock.System.now().epochSeconds

    private fun parseDurationMs(text: String?): Long {
        if (text == null) return 0L
        val parts = text.split(":")
        if (parts.size != 2) return 0L
        val minutes = parts[0].toLongOrNull() ?: return 0L
        val seconds = parts[1].toLongOrNull() ?: return 0L
        return (minutes * 60 + seconds) * 1000
    }

    private fun escapeJson(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
}
