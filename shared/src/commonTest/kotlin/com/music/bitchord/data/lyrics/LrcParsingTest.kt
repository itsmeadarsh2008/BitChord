package com.music.bitchord.data.lyrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Behaviour-parity guard for the KMP migration: these files moved verbatim
 * from `:app` (only their OkHttp/IO imports were ported), so parsing must
 * answer byte-for-byte the same on JVM, Android and iOS.
 */
class LrcParsingTest {
    @Test
    fun parsesLineStamps() {
        val lines = LrcLib.parseLrc("[00:01.00] hello\n[00:02.50] world\n")
        assertEquals(2, lines.size)
        assertEquals(1_000L, lines[0].timeMs)
        assertEquals("hello", lines[0].text)
        assertEquals(2_500L, lines[1].timeMs)
        assertEquals("world", lines[1].text)
    }

    @Test
    fun wordStampsBecomeWordRuns() {
        val lines = LrcLib.parseLrc("[00:01.00] <00:01.00>he <00:01.20>llo\n")
        assertEquals(1, lines.size)
        assertTrue(lines[0].isWordSynced)
        assertEquals("he llo", lines[0].text)
    }

    @Test
    fun metadataTagsFallOut() {
        val lines = LrcLib.parseLrc("[ti:Song]\n[ar:Artist]\n[00:05.00] sung\n")
        // Tags carry no stamp so they fall out; the late first line earns an
        // intro break ahead of it.
        assertEquals(2, lines.size)
        assertTrue(lines[0].isGap)
        assertEquals("sung", lines[1].text)
    }
}
