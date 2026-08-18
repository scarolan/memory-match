package xyz.oddforge.memorymatch

object GameEngine {

    enum class TileTheme(val label: String, val emojis: List<String>) {
        ANIMALS("Animals", listOf("🐶", "🐱", "🐸", "🦊", "🐼", "🐨", "🦁", "🐯")),
        FOOD("Food", listOf("🍎", "🍊", "🍋", "🍇", "🍓", "🍒", "🍑", "🥝")),
        SPACE("Space", listOf("🚀", "🌙", "⭐", "🪐", "☄️", "🛸", "🌍", "🔭")),
    }

    fun generatePairs(count: Int): List<Int> {
        require(count in 1..18) { "Pair count must be between 1 and 18" }
        val ids = (1..count).toList()
        return (ids + ids).shuffled()
    }

    fun createBoard(theme: TileTheme = TileTheme.ANIMALS): List<Card> {
        val pairs = (theme.emojis + theme.emojis).shuffled()
        return pairs.mapIndexed { index, emoji ->
            Card(id = index, emoji = emoji)
        }
    }

    fun createInitialState(theme: TileTheme = TileTheme.ANIMALS): GameState =
        GameState(cards = createBoard(theme), theme = theme)

    fun reduce(state: GameState, event: GameEvent): GameState = when (event) {
        is GameEvent.CardTapped -> handleCardTapped(state, event.index)
        is GameEvent.ResetGame -> createInitialState(state.theme)
        is GameEvent.HideUnmatched -> hideUnmatched(state)
        is GameEvent.ChangeTheme -> createInitialState(event.theme)
    }

    private fun handleCardTapped(state: GameState, index: Int): GameState {
        if (state.isGameOver) return state
        if (state.pendingHideIndices.isNotEmpty()) return state

        val card = state.cards[index]
        if (card.isFaceUp || card.isMatched) return state

        val firstIndex = state.firstFlippedIndex

        if (firstIndex == null) {
            val newCards = state.cards.mapIndexed { i, c ->
                if (i == index) c.copy(isFaceUp = true) else c
            }
            return state.copy(cards = newCards, firstFlippedIndex = index)
        }

        val firstCard = state.cards[firstIndex]
        val newMovesCount = state.movesCount + 1

        return if (firstCard.emoji == card.emoji) {
            val newCards = state.cards.mapIndexed { i, c ->
                if (i == firstIndex || i == index) c.copy(isFaceUp = true, isMatched = true) else c
            }
            val allMatched = newCards.all { it.isMatched }
            state.copy(
                cards = newCards,
                movesCount = newMovesCount,
                firstFlippedIndex = null,
                isGameOver = allMatched
            )
        } else {
            val newCards = state.cards.mapIndexed { i, c ->
                if (i == index) c.copy(isFaceUp = true) else c
            }
            state.copy(
                cards = newCards,
                movesCount = newMovesCount,
                firstFlippedIndex = null,
                pendingHideIndices = listOf(firstIndex, index)
            )
        }
    }

    private fun hideUnmatched(state: GameState): GameState {
        if (state.pendingHideIndices.isEmpty()) return state
        val toHide = state.pendingHideIndices.toSet()
        val newCards = state.cards.mapIndexed { i, c ->
            if (i in toHide) c.copy(isFaceUp = false) else c
        }
        return state.copy(cards = newCards, pendingHideIndices = emptyList())
    }
}
