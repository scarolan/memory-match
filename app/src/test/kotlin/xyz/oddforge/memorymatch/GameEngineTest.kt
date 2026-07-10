package xyz.oddforge.memorymatch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {
    @Test
    fun generatePairs_returnsCorrectCount() {
        val cards = GameEngine.generatePairs(6)
        assertEquals(12, cards.size)
    }

    @Test
    fun generatePairs_containsExactlyTwoOfEach() {
        val cards = GameEngine.generatePairs(6)
        for (id in 1..6) {
            assertEquals(2, cards.count { it == id })
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
        val cards = GameEngine.generatePairs(1)
        assertEquals(listOf(1, 1), cards.sorted())
    }

    @Test
    fun generatePairs_boundaryEighteen() {
        val cards = GameEngine.generatePairs(18)
        assertEquals(36, cards.size)
        assertTrue(cards.all { it in 1..18 })
    }
}
