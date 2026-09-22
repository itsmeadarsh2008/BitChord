package com.music.bitchord.data.scrobbling

import kotlin.math.abs
import kotlin.math.sin

/**
 * Pure-Kotlin MD5 (RFC 1321) for Last.fm's `api_sig`, replacing
 * `java.security.MessageDigest` so scrobbling compiles in `commonMain`.
 *
 * One code path for both platforms also means the signature can never
 * diverge between Android and iOS. Verified against the RFC vectors in
 * [Md5Test].
 */
internal fun md5Hex(input: String): String =
    Md5.digest(input.encodeToByteArray())
        .joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }

internal object Md5 {
    private val SHIFTS = intArrayOf(
        7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
        5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
        4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
        6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21,
    )

    fun digest(message: ByteArray): ByteArray {
        val bitLen = message.size.toLong() * 8
        val padded = message.toMutableList().apply {
            add(0x80.toByte())
            while (size % 64 != 56) add(0)
            for (i in 0 until 8) add(((bitLen ushr (8 * i)) and 0xFF).toByte())
        }.toByteArray()

        var a0 = 0x67452301
        var b0 = 0xefcdab89.toInt()
        var c0 = 0x98badcfe.toInt()
        var d0 = 0x10325476

        for (chunk in 0 until padded.size / 64) {
            val m = IntArray(16) { i ->
                val o = chunk * 64 + i * 4
                (padded[o].toInt() and 0xFF) or
                    ((padded[o + 1].toInt() and 0xFF) shl 8) or
                    ((padded[o + 2].toInt() and 0xFF) shl 16) or
                    ((padded[o + 3].toInt() and 0xFF) shl 24)
            }
            var a = a0
            var b = b0
            var c = c0
            var d = d0
            for (i in 0 until 64) {
                val (f, g) = when (i) {
                    in 0..15 -> ((b and c) or (b.inv() and d)) to i
                    in 16..31 -> ((d and b) or (d.inv() and c)) to (5 * i + 1) % 16
                    in 32..47 -> (b xor c xor d) to (3 * i + 5) % 16
                    else -> (c xor (b or d.inv())) to (7 * i) % 16
                }
                val k = ((1L shl 32) * abs(sin((i + 1).toDouble()))).toLong()
                val sum = (a.toLong() and 0xFFFFFFFFL) + (f.toLong() and 0xFFFFFFFFL) + k +
                    (m[g].toLong() and 0xFFFFFFFFL)
                val rotated = ((sum and 0xFFFFFFFFL).toInt() shl SHIFTS[i]) or
                    ((sum and 0xFFFFFFFFL).toInt() ushr (32 - SHIFTS[i]))
                a = d
                d = c
                c = b
                b = (b.toLong() + rotated.toLong()).toInt()
            }
            a0 += a
            b0 += b
            c0 += c
            d0 += d
        }

        val out = ByteArray(16)
        val state = intArrayOf(a0, b0, c0, d0)
        for (i in 0 until 4) {
            for (j in 0 until 4) out[i * 4 + j] = ((state[i] ushr (8 * j)) and 0xFF).toByte()
        }
        return out
    }
}
