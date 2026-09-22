package com.music.bitchord.shared

import android.os.Build
import java.util.UUID

actual fun getPlatformName(): String = "Android ${Build.VERSION.SDK_INT}"

actual fun randomUUID(): String = UUID.randomUUID().toString()
