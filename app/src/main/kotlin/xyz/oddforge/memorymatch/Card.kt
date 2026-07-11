package xyz.oddforge.memorymatch

data class Card(
    val id: Int,
    val emoji: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)
