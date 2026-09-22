package com.music.bitchord.data

/**
 * Platform log minus the release build, ported from `:app`'s
 * `data/DebugLog.kt` (android.util.Log).
 *
 * Same API and call-site shape (`import ...DebugLog as Log`), so consumers
 * move unchanged. [isDebug] is set once at startup — from BuildConfig on
 * Android, false (or a settings flag) on iOS.
 */
object DebugLog {
    var isDebug: Boolean = false

    fun d(tag: String, message: String) {
        if (isDebug) platformLog('D', tag, message, null)
    }

    fun i(tag: String, message: String) {
        if (isDebug) platformLog('I', tag, message, null)
    }

    fun w(tag: String, message: String) {
        if (isDebug) platformLog('W', tag, message, null)
    }

    fun w(tag: String, message: String, error: Throwable) {
        if (isDebug) platformLog('W', tag, message, error)
    }

    fun e(tag: String, message: String) {
        if (isDebug) platformLog('E', tag, message, null)
    }

    fun e(tag: String, message: String, error: Throwable) {
        if (isDebug) platformLog('E', tag, message, error)
    }
}

internal expect fun platformLog(level: Char, tag: String, message: String, error: Throwable?)
