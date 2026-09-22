package com.music.bitchord.shared

import platform.Foundation.NSUUID
import platform.UIKit.UIDevice

actual fun getPlatformName(): String =
    UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

actual fun randomUUID(): String = NSUUID().UUIDString()
