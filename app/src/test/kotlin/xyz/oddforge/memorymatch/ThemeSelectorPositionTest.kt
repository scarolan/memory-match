package xyz.oddforge.memorymatch

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
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
    fun themeSelectorExists() {
        composeTestRule.onNodeWithText("Animals").assertExists()
        composeTestRule.onNodeWithText("Food").assertExists()
        composeTestRule.onNodeWithText("Space").assertExists()
    }

    @Test
    fun currentThemeButtonIsDisabled() {
        composeTestRule.onNodeWithText("Animals").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Food").assertIsEnabled()
        composeTestRule.onNodeWithText("Space").assertIsEnabled()
    }
}
