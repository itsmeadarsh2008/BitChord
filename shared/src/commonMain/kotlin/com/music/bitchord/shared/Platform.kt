package com.music.bitchord.shared

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.time.Clock

/**
 * Platform identity. `androidMain` / `iosMain` provide the actuals.
 * Keeps BuildConfig / UIDevice out of commonMain.
 */
expect fun getPlatformName(): String

expect fun randomUUID(): String

/** Engine-wired HTTP client: OkHttp on Android, Darwin on iOS. */
expect fun createHttpClient(): HttpClient

/**
 * Blocking-safe dispatcher for network/file work. Android uses Dispatchers.IO;
 * Kotlin/Native exposes no IO dispatcher (it is internal there), so iOS uses
 * Dispatchers.Default. Call sites must use this instead of Dispatchers.IO.
 */
expect val ioDispatcher: CoroutineDispatcher

/**
 * Build metadata that commonMain cannot see directly (BuildConfig on
 * Android, Info.plist on iOS). Bound per-platform, consumed in common.
 */
data class BuildInfo(
    val applicationId: String,
    val versionName: String,
    val versionCode: Int,
    val isDebug: Boolean,
)

/** Wall clock, common replacement for `System.currentTimeMillis()`. */
fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()

fun nowEpochSeconds(): Long = Clock.System.now().epochSeconds
