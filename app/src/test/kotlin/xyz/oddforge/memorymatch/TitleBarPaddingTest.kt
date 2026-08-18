package xyz.oddforge.memorymatch

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TitleBarPaddingTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun dpToPx(dp: Float): Float =
        composeTestRule.activity.resources.displayMetrics.density * dp

    // The old layout gave the Column only 16dp of top padding, so the title
    // sat under the camera cutout on punch-hole phones. The new layout must
    // push the title (and the moves counter below it) further down.
    @Test
    fun titleAndMovesCounterClearStatusBarArea() {
        val oldTopPaddingPx = dpToPx(16f)

        val titleTop = composeTestRule
            .onNodeWithText("Memory")
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        assertTrue(
            "title top ($titleTop) must be below the old 16dp top padding ($oldTopPaddingPx)",
            titleTop > oldTopPaddingPx
        )

        val movesTop = composeTestRule
            .onNodeWithText("Moves: 0")
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        assertTrue(
            "moves counter top ($movesTop) must be below the old 16dp top padding ($oldTopPaddingPx)",
            movesTop > oldTopPaddingPx
        )
    }

    // The title words were running together; they must now be laid out as
    // separate pieces with visible whitespace between them.
    @Test
    fun titleWordsAreSeparateComposables() {
        composeTestRule.onNodeWithText("Memory").assertExists()
        composeTestRule.onNodeWithText("Match").assertExists()
    }

    @Test
    fun titleHasWhitespaceBetweenMemoryAndMatch() {
        val memoryBounds = composeTestRule
            .onNodeWithText("Memory")
            .fetchSemanticsNode()
            .boundsInRoot
        val matchBounds = composeTestRule
            .onNodeWithText("Match")
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(
            "'Memory' (right=${memoryBounds.right}) must sit to the left of 'Match' (left=${matchBounds.left})",
            memoryBounds.right < matchBounds.left
        )

        val gapPx = matchBounds.left - memoryBounds.right
        val minGapPx = dpToPx(8f)
        assertTrue(
            "gap between title words ($gapPx) must be at least 8dp ($minGapPx)",
            gapPx >= minGapPx
        )
    }
}
