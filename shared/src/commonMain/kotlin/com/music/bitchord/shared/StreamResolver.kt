package com.music.bitchord.shared

/**
 * Stream-resolution seam. :app currently uses NewPipeExtractor + Rhino
 * (app/build.gradle.kts) which are JVM-only and cannot compile for iOS.
 *
 * Do NOT move NewPipe code into :shared — it will break the iOS target.
 * Phase 2 is either a pure-Kotlin Innertube decipher in commonMain, or
 * server-side resolution. Until then this stub keeps both targets green.
 */
interface StreamResolver {
    suspend fun resolveStreamUrl(videoId: String): String?
}

class StubStreamResolver : StreamResolver {
    override suspend fun resolveStreamUrl(videoId: String): String? = null
}
