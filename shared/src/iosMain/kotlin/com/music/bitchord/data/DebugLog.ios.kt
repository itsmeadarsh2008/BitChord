package com.music.bitchord.data

import platform.Foundation.NSLog

internal actual fun platformLog(level: Char, tag: String, message: String, error: Throwable?) {
    val suffix = error?.let { "\n${it.message ?: it.toString()}" }.orEmpty()
    NSLog("[$level/$tag] $message$suffix")
}
