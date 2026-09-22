package com.music.bitchord.shared

/**
 * Playback seam. Media3 (ExoPlayer) is Android-only, AVPlayer is iOS-only,
 * so neither can live in commonMain.
 *
 * Phase 1 (this scaffold): [StubPlayerController] so the shared framework
 * compiles on both platforms and iosApp links.
 *
 * Phase 2: replace with expect/actual backed by ExoPlayer / AVPlayer, or a
 * library that already wraps both (KMP-Player, KrossPlay,
 * ComposeMediaPlayer). Keep this interface common so ViewModels never import
 * androidx.media3 directly.
 */
interface PlayerController {
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun setPlaybackSpeed(speed: Float)
    val isPlaying: Boolean
}

class StubPlayerController : PlayerController {
    override val isPlaying: Boolean = false
    override fun play() = Unit
    override fun pause() = Unit
    override fun seekTo(positionMs: Long) = Unit
    override fun setPlaybackSpeed(speed: Float) = Unit
}
