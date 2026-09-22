package com.music.bitchord.download

/**
 * Explicit-charset encode/decode for the MP4/WebM taggers, replacing the
 * JVM-only `String(bytes, charset)` / `toByteArray(charset)` overloads so
 * the taggers compile in `commonMain`.
 *
 * - UTF-8 / ASCII: stdlib `encodeToByteArray` (identical for ASCII-range
 *   input, which is all these call sites ever pass).
 * - Latin-1: a direct byte↔codepoint map, which is exactly what ISO-8859-1
 *   is — this preserves the 0xA9 bytes the taggers depend on.
 */
internal fun String.encodeUtf8(): ByteArray = encodeToByteArray()

internal fun String.encodeAscii(): ByteArray = encodeToByteArray()

internal fun String.encodeLatin1(): ByteArray =
    ByteArray(length) { i -> (this[i].code and 0xFF).toByte() }

internal fun ByteArray.decodeLatin1(offset: Int = 0, length: Int = size - offset): String =
    buildString(length) {
        for (i in offset until offset + length) append((this@decodeLatin1[i].toInt() and 0xFF).toChar())
    }
