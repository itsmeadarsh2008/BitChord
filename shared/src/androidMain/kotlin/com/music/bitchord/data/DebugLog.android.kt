package com.music.bitchord.data

import android.util.Log

internal actual fun platformLog(level: Char, tag: String, message: String, error: Throwable?) {
    when (level) {
        'D' -> Log.d(tag, message)
        'I' -> Log.i(tag, message)
        'W' -> if (error == null) Log.w(tag, message) else Log.w(tag, message, error)
        else -> if (error == null) Log.e(tag, message) else Log.e(tag, message, error)
    }
}
