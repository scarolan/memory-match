package xyz.oddforge.memorymatch

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ThemeSelectorPositionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun themeSelectorIsRenderedBelowTheCardGrid() {
        val selectorBounds = composeTestRule
            .onNodeWithText("Animals")
            .fetchSemanticsNode()
            .boundsInRoot
        val selectorCenter = (selectorBounds.top + selectorBounds.bottom) / 2f

        val cardBounds = composeTestRule
            .onAllNodesWithText("?")
            .fetchSemanticsNodes()
            .map { it.boundsInRoot }
        val gridTop = cardBounds.minOf { it.top }
        val gridBottom = cardBounds.maxOf { it.bottom }
        val gridCenter = (gridTop + gridBottom) / 2f

        assertTrue(
            "theme selector (center=$selectorCenter) must be below the card grid (center=$gridCenter)",
            selectorCenter > gridCenter
        )
    }

    @Test
    fun themeSelectorIsStillInteractiveBelowTheBoard() {
        composeTestRule.onNodeWithText("Food").assertIsEnabled()
        composeTestRule.onNodeWithText("Animals").assertIsNotEnabled()

        composeTestRule.onNodeWithText("Food").performClick()

        composeTestRule.onNodeWithText("Food").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Animals").assertIsEnabled()
    }
}
