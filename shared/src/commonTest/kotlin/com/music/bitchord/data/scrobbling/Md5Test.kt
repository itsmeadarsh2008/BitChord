package com.music.bitchord.data.scrobbling

import kotlin.test.Test
import kotlin.test.assertEquals

/** RFC 1321 vectors — Last.fm's api_sig must match the JVM build exactly. */
class Md5Test {
    @Test
    fun emptyString() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", md5Hex(""))
    }

    @Test
    fun abc() {
        assertEquals("900150983cd24fb0d6963f7d28e17f72", md5Hex("abc"))
    }

    @Test
    fun fox() {
        assertEquals(
            "9e107d9d372bb6826bd81d3542a419d6",
            md5Hex("The quick brown fox jumps over the lazy dog"),
        )
    }
}
