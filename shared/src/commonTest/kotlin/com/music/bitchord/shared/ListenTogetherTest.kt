package com.music.bitchord.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class ListenTogetherTest {
    @Test
    fun pausedHoldsPosition() {
        val s = PartyState(positionMs = 42_000, anchorMs = 1_757_630_001_234, isPlaying = false)
        assertEquals(42_000, s.playheadAt(1_757_630_005_000))
    }

    @Test
    fun playingAdvancesFromAnchor() {
        val s = PartyState(positionMs = 42_000, anchorMs = 1_757_630_001_234, isPlaying = true)
        assertEquals(43_000, s.playheadAt(1_757_630_002_234))
    }
}
