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

    @Test
    fun titleAndMovesCounterClearStatusBarArea() {
        val oldTopPaddingPx = dpToPx(16f)

        val titleTop = composeTestRule
            .onNodeWithText("Memory Match")
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

    @Test
    fun titleTextExists() {
        composeTestRule.onNodeWithText("Memory Match").assertExists()
    }
}
