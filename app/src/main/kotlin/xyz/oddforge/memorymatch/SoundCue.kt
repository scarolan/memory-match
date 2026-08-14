package xyz.oddforge.memorymatch

enum class SoundCue { FLIP, MATCH, MISMATCH, GAME_COMPLETE }

fun detectSoundCue(old: GameState, new: GameState): SoundCue? {
    if (!old.isGameOver && new.isGameOver) return SoundCue.GAME_COMPLETE
    if (new.cards.count { it.isMatched } > old.cards.count { it.isMatched }) return SoundCue.MATCH
    if (new.pendingHideIndices.isNotEmpty() && old.pendingHideIndices.isEmpty()) return SoundCue.MISMATCH
    if (new.cards.count { it.isFaceUp } > old.cards.count { it.isFaceUp }) return SoundCue.FLIP
    return null
}
