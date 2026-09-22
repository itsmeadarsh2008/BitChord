package com.music.bitchord.shared

import kotlinx.serialization.Serializable

/**
 * Pure-Kotlin mirror of backend/app party state (see backend/README.md).
 * No platform APIs — safe to share and unit-test in commonTest.
 *
 * Server holds a position + the server time it was true at; each device
 * answers "where should I be right now" locally:
 *   playhead = positionMs + max(0, serverNow - anchorMs) while playing
 */
@Serializable
data class PartyState(
    val positionMs: Long,
    val anchorMs: Long,
    val isPlaying: Boolean,
)

fun PartyState.playheadAt(serverNowMs: Long): Long {
    if (!isPlaying) return positionMs
    return positionMs + maxOf(0L, serverNowMs - anchorMs)
}

fun greeting(): String = "BitChord shared · ${getPlatformName()}"
