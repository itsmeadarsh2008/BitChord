package com.music.bitchord.shared

/**
 * Platform identity. `androidMain` / `iosMain` provide the actuals.
 * Keeps BuildConfig / UIDevice out of commonMain.
 */
expect fun getPlatformName(): String

expect fun randomUUID(): String

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
