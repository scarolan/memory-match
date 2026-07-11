package xyz.oddforge.memorymatch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    private fun deterministicBoard(): List<Card> = listOf(
        Card(0, "🐶"), Card(1, "🐱"), Card(2, "🐸"), Card(3, "🦊"),
        Card(4, "🐼"), Card(5, "🐨"), Card(6, "🦁"), Card(7, "🐯"),
        Card(8, "🐶"), Card(9, "🐱"), Card(10, "🐸"), Card(11, "🦊"),
        Card(12, "🐼"), Card(13, "🐨"), Card(14, "🦁"), Card(15, "🐯"),
    )

    private fun initialState(): GameState = GameState(cards = deterministicBoard())

    // --- generatePairs ---

    @Test
    fun generatePairs_returnsCorrectCount() {
        assertEquals(12, GameEngine.generatePairs(6).size)
    }

    @Test
    fun generatePairs_containsExactlyTwoOfEach() {
        val ids = GameEngine.generatePairs(6)
        for (id in 1..6) {
            assertEquals(2, ids.count { it == id })
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun generatePairs_rejectsZero() {
        GameEngine.generatePairs(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun generatePairs_rejectsTooMany() {
        GameEngine.generatePairs(19)
    }

    @Test
    fun generatePairs_boundaryOne() {
        val ids = GameEngine.generatePairs(1)
        assertEquals(2, ids.size)
        assertEquals(2, ids.count { it == 1 })
    }

    @Test
    fun generatePairs_boundaryEighteen() {
        val ids = GameEngine.generatePairs(18)
        assertEquals(36, ids.size)
        assertTrue(ids.all { it in 1..18 })
    }

    // --- createBoard ---

    @Test
    fun createBoard_returns16Cards() {
        assertEquals(16, GameEngine.createBoard().size)
    }

    @Test
    fun createBoard_has8PairsOfEmoji() {
        val grouped = GameEngine.createBoard().groupBy { it.emoji }
        assertEquals(8, grouped.size)
        assertTrue(grouped.all { it.value.size == 2 })
    }

    @Test
    fun createBoard_allCardsFaceDown() {
        val board = GameEngine.createBoard()
        assertTrue(board.none { it.isFaceUp })
        assertTrue(board.none { it.isMatched })
    }

    // --- CardTapped: first card ---

    @Test
    fun tapFirstCard_flipsItFaceUp() {
        val state = initialState()
        val result = GameEngine.reduce(state, GameEvent.CardTapped(0))
        assertTrue(result.cards[0].isFaceUp)
        assertEquals(0, result.firstFlippedIndex)
        assertEquals(0, result.movesCount)
    }

    @Test
    fun tapFirstCard_otherCardsUnchanged() {
        val state = initialState()
        val result = GameEngine.reduce(state, GameEvent.CardTapped(3))
        for (i in state.cards.indices) {
            if (i != 3) assertEquals(state.cards[i], result.cards[i])
        }
    }

    // --- CardTapped: second card, match ---

    @Test
    fun tapMatchingPair_marksBothMatched() {
        val state = initialState()
        var s = GameEngine.reduce(state, GameEvent.CardTapped(0))
        s = GameEngine.reduce(s, GameEvent.CardTapped(8))
        assertTrue(s.cards[0].isMatched)
        assertTrue(s.cards[8].isMatched)
        assertTrue(s.cards[0].isFaceUp)
        assertTrue(s.cards[8].isFaceUp)
        assertNull(s.firstFlippedIndex)
        assertEquals(1, s.movesCount)
        assertTrue(s.pendingHideIndices.isEmpty())
    }

    // --- CardTapped: second card, no match ---

    @Test
    fun tapNonMatchingPair_setsPendingHide() {
        val state = initialState()
        var s = GameEngine.reduce(state, GameEvent.CardTapped(0))
        s = GameEngine.reduce(s, GameEvent.CardTapped(1))
        assertTrue(s.cards[0].isFaceUp)
        assertTrue(s.cards[1].isFaceUp)
        assertFalse(s.cards[0].isMatched)
        assertFalse(s.cards[1].isMatched)
        assertEquals(listOf(0, 1), s.pendingHideIndices)
        assertNull(s.firstFlippedIndex)
        assertEquals(1, s.movesCount)
    }

    // --- HideUnmatched ---

    @Test
    fun hideUnmatched_flipsPendingCardsDown() {
        val state = initialState()
        var s = GameEngine.reduce(state, GameEvent.CardTapped(0))
        s = GameEngine.reduce(s, GameEvent.CardTapped(1))
        s = GameEngine.reduce(s, GameEvent.HideUnmatched)
        assertFalse(s.cards[0].isFaceUp)
        assertFalse(s.cards[1].isFaceUp)
        assertTrue(s.pendingHideIndices.isEmpty())
    }

    @Test
    fun hideUnmatched_noopWhenNothingPending() {
        val state = initialState()
        val result = GameEngine.reduce(state, GameEvent.HideUnmatched)
        assertEquals(state, result)
    }

    // --- Tap blocked while pending hide ---

    @Test
    fun tapBlockedWhilePendingHide() {
        val state = initialState()
        var s = GameEngine.reduce(state, GameEvent.CardTapped(0))
        s = GameEngine.reduce(s, GameEvent.CardTapped(1))
        val blocked = GameEngine.reduce(s, GameEvent.CardTapped(2))
        assertEquals(s, blocked)
    }

    // --- Tapping matched or face-up card is ignored ---

    @Test
    fun tapMatchedCard_ignored() {
        val state = initialState()
        var s = GameEngine.reduce(state, GameEvent.CardTapped(0))
        s = GameEngine.reduce(s, GameEvent.CardTapped(8))
        val before = s
        s = GameEngine.reduce(s, GameEvent.CardTapped(0))
        assertEquals(before, s)
    }

    @Test
    fun tapAlreadyFlippedCard_ignored() {
        val state = initialState()
        var s = GameEngine.reduce(state, GameEvent.CardTapped(0))
        val before = s
        s = GameEngine.reduce(s, GameEvent.CardTapped(0))
        assertEquals(before, s)
    }

    // --- Move count increments on second tap only ---

    @Test
    fun moveCountIncrementsOnSecondTapOnly() {
        val state = initialState()
        var s = GameEngine.reduce(state, GameEvent.CardTapped(0))
        assertEquals(0, s.movesCount)
        s = GameEngine.reduce(s, GameEvent.CardTapped(1))
        assertEquals(1, s.movesCount)
    }

    // --- Game over ---

    @Test
    fun gameOver_whenAllPairsMatched() {
        var s = initialState()
        for (i in 0 until 8) {
            s = GameEngine.reduce(s, GameEvent.CardTapped(i))
            s = GameEngine.reduce(s, GameEvent.CardTapped(i + 8))
        }
        assertTrue(s.isGameOver)
        assertEquals(8, s.movesCount)
        assertTrue(s.cards.all { it.isMatched })
    }

    @Test
    fun tapIgnoredWhenGameOver() {
        var s = initialState()
        for (i in 0 until 8) {
            s = GameEngine.reduce(s, GameEvent.CardTapped(i))
            s = GameEngine.reduce(s, GameEvent.CardTapped(i + 8))
        }
        val before = s
        s = GameEngine.reduce(s, GameEvent.CardTapped(0))
        assertEquals(before, s)
    }

    // --- ResetGame ---

    @Test
    fun resetGame_clearsEverything() {
        var s = initialState()
        s = GameEngine.reduce(s, GameEvent.CardTapped(0))
        s = GameEngine.reduce(s, GameEvent.CardTapped(8))
        s = GameEngine.reduce(s, GameEvent.ResetGame)
        assertEquals(0, s.movesCount)
        assertFalse(s.isGameOver)
        assertNull(s.firstFlippedIndex)
        assertTrue(s.pendingHideIndices.isEmpty())
        assertTrue(s.cards.none { it.isFaceUp })
        assertTrue(s.cards.none { it.isMatched })
    }

    // --- Purity: reduce returns new state, input unchanged ---

    @Test
    fun reduce_doesNotMutateInputState() {
        val original = initialState()
        val originalCards = original.cards.toList()
        GameEngine.reduce(original, GameEvent.CardTapped(0))
        assertEquals(originalCards, original.cards)
        assertNull(original.firstFlippedIndex)
        assertEquals(0, original.movesCount)
    }
}
