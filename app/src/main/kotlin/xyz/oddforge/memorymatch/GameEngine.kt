package xyz.oddforge.memorymatch

object GameEngine {
    fun generatePairs(count: Int): List<Int> {
        require(count in 1..18) { "Pair count must be between 1 and 18" }
        val ids = (1..count).toList()
        return (ids + ids).shuffled()
    }
}
