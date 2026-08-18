package xyz.oddforge.memorymatch

sealed class GameEvent {
    data class CardTapped(val index: Int) : GameEvent()
    data object ResetGame : GameEvent()
    data object HideUnmatched : GameEvent()
    data class ChangeTheme(val theme: GameEngine.TileTheme) : GameEvent()
}
