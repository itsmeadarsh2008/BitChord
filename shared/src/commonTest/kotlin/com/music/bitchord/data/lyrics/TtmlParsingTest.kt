package com.music.bitchord.data.lyrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Behaviour-parity guard for the KMP migration: `TtmlLyrics` moved from
 * `javax.xml` DOM to the internal [XmlNode] DOM with the walk logic
 * otherwise verbatim, so Apple Music documents must parse identically on
 * JVM, Android and iOS.
 */
class TtmlParsingTest {
    private val doc = """
        <tt xmlns="http://www.w3.org/ns/ttml" xmlns:ttm="http://www.w3.org/ns/ttml#metadata">
          <head><metadata>
            <ttm:agent xml:id="v1" type="person"/>
            <ttm:agent xml:id="v2" type="person"/>
          </metadata></head>
          <body><div>
            <p begin="1.00" end="1.90" ttm:agent="v1"><span begin="1.00" end="1.20">He</span> <span begin="1.20" end="1.50">llo</span></p>
            <p begin="2.00" end="2.95" ttm:agent="v2"><span begin="2.00" end="2.20">e</span><span begin="2.20" end="2.60">nough</span><span begin="2.60" end="2.90" ttm:role="x-bg">ooh</span></p>
            <p begin="3.10" end="3.50">plain line</p>
          </div></body>
        </tt>
    """.trimIndent()

    @Test
    fun wordLinesAndBackgroundVoice() {
        val lines = TtmlLyrics.parse(doc)
        assertEquals(3, lines.size)

        val first = lines[0]
        assertEquals(1_000L, first.timeMs)
        assertEquals("He llo", first.text)
        assertEquals(listOf("He", "llo"), first.words.map { it.text })

        // Adjacent syllable spans glue into one word; the bg span splits off.
        val second = lines[1]
        assertEquals("enough", second.text)
        assertEquals(1, second.words.size)
        val bg = second.background
        assertEquals("ooh", bg?.text)
        assertEquals(2_600L, bg?.timeMs)

        // Bare paragraph: line-synced, carrying the paragraph's own end.
        val third = lines[2]
        assertEquals("plain line", third.text)
        assertTrue(third.sungUntilMs == 3_500L)
    }

    @Test
    fun duetSidesAlternate() {
        val lines = TtmlLyrics.parse(doc)
        // First voice starts left, voice change flips to right, voiceless stays.
        assertEquals(LyricAlignment.Start, lines[0].alignment)
        assertEquals(LyricAlignment.End, lines[1].alignment)
        assertEquals(LyricAlignment.Start, lines[2].alignment)
    }

    @Test
    fun malformedIsEmpty() {
        assertEquals(emptyList(), TtmlLyrics.parse("not xml at all"))
        assertEquals(emptyList(), TtmlLyrics.parse("<p>unclosed"))
        assertEquals(emptyList(), TtmlLyrics.parse(""))
    }

    @Test
    fun clockFormats() {
        assertEquals(27_395L, TtmlLyrics.time("27.395"))
        assertEquals(65_200L, TtmlLyrics.time("1:05.20"))
        assertEquals(3_723_400L, TtmlLyrics.time("1:02:03.4"))
        assertEquals(500L, TtmlLyrics.time("500ms"))
        assertEquals(null, TtmlLyrics.time(null))
        assertEquals(null, TtmlLyrics.time(""))
    }
}
