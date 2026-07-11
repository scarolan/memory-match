package xyz.oddforge.memorymatch

data class GameState(
    val cards: List<Card>,
    val movesCount: Int = 0,
    val firstFlippedIndex: Int? = null,
    val pendingHideIndices: List<Int> = emptyList(),
    val isGameOver: Boolean = false
)
