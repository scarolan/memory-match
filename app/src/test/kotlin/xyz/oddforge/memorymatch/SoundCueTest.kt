package xyz.oddforge.memorymatch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundCueTest {

    private fun deterministicBoard(): List<Card> = listOf(
        Card(0, "🐶"), Card(1, "🐱"), Card(2, "🐸"), Card(3, "🦊"),
        Card(4, "🐼"), Card(5, "🐨"), Card(6, "🦁"), Card(7, "🐯"),
        Card(8, "🐶"), Card(9, "🐱"), Card(10, "🐸"), Card(11, "🦊"),
        Card(12, "🐼"), Card(13, "🐨"), Card(14, "🦁"), Card(15, "🐯"),
    )

    private fun initialState(): GameState = GameState(cards = deterministicBoard())

    @Test
    fun flipFirstCard_returnsFlip() {
        val oldState = initialState()
        val newState = GameEngine.reduce(oldState, GameEvent.CardTapped(0))
        assertEquals(SoundCue.FLIP, detectSoundCue(oldState, newState))
    }

    @Test
    fun matchingPair_returnsMatch() {
        val state0 = initialState()
        val state1 = GameEngine.reduce(state0, GameEvent.CardTapped(0))
        val state2 = GameEngine.reduce(state1, GameEvent.CardTapped(8))
        assertEquals(SoundCue.MATCH, detectSoundCue(state1, state2))
    }

    @Test
    fun nonMatchingPair_returnsMismatch() {
        val state0 = initialState()
        val state1 = GameEngine.reduce(state0, GameEvent.CardTapped(0))
        val state2 = GameEngine.reduce(state1, GameEvent.CardTapped(1))
        assertEquals(SoundCue.MISMATCH, detectSoundCue(state1, state2))
    }

    @Test
    fun allPairsMatched_returnsGameComplete() {
        var state = initialState()
        for (i in 0 until 7) {
            state = GameEngine.reduce(state, GameEvent.CardTapped(i))
            state = GameEngine.reduce(state, GameEvent.CardTapped(i + 8))
        }
        val beforeLastTap = GameEngine.reduce(state, GameEvent.CardTapped(7))
        val finalState = GameEngine.reduce(beforeLastTap, GameEvent.CardTapped(15))
        assertEquals(SoundCue.GAME_COMPLETE, detectSoundCue(beforeLastTap, finalState))
    }

    @Test
    fun gameCompleteOverridesMatch() {
        var state = initialState()
        for (i in 0 until 7) {
            state = GameEngine.reduce(state, GameEvent.CardTapped(i))
            state = GameEngine.reduce(state, GameEvent.CardTapped(i + 8))
        }
        val beforeLastMatch = GameEngine.reduce(state, GameEvent.CardTapped(7))
        val afterLastMatch = GameEngine.reduce(beforeLastMatch, GameEvent.CardTapped(15))
        assertTrue(afterLastMatch.isGameOver)
        assertEquals(SoundCue.GAME_COMPLETE, detectSoundCue(beforeLastMatch, afterLastMatch))
    }

    @Test
    fun hideUnmatched_returnsNull() {
        val state0 = initialState()
        val state1 = GameEngine.reduce(state0, GameEvent.CardTapped(0))
        val state2 = GameEngine.reduce(state1, GameEvent.CardTapped(1))
        val state3 = GameEngine.reduce(state2, GameEvent.HideUnmatched)
        assertNull(detectSoundCue(state2, state3))
    }

    @Test
    fun resetGame_returnsNull() {
        val state0 = initialState()
        val state1 = GameEngine.reduce(state0, GameEvent.CardTapped(0))
        val state2 = GameEngine.reduce(state1, GameEvent.ResetGame)
        assertNull(detectSoundCue(state1, state2))
    }

    @Test
    fun tapMatchedCard_returnsNull() {
        val state0 = initialState()
        val state1 = GameEngine.reduce(state0, GameEvent.CardTapped(0))
        val state2 = GameEngine.reduce(state1, GameEvent.CardTapped(8))
        val state3 = GameEngine.reduce(state2, GameEvent.CardTapped(0))
        assertNull(detectSoundCue(state2, state3))
    }

    @Test
    fun tapFaceUpCard_returnsNull() {
        val state0 = initialState()
        val state1 = GameEngine.reduce(state0, GameEvent.CardTapped(0))
        val state2 = GameEngine.reduce(state1, GameEvent.CardTapped(0))
        assertNull(detectSoundCue(state1, state2))
    }
}
