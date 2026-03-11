package com.example.junglenav.feature.package_manager

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import com.example.junglenav.MainActivity
import org.junit.Rule
import org.junit.Test

class RemoteCatalogSmokeTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun remoteCatalogShowsMissionBundlesAndActivationWarningForUnverifiedPack() {
        rule.onNodeWithText("Packages").performClick()
        waitForText("Remote Catalog")
        waitForText("Mission catalog ready")
        rule.onNodeWithTag("package_search_field").performTextInput("Gunung")
        Espresso.closeSoftKeyboard()
        rule.onNodeWithTag("package_list")
            .performScrollToNode(hasTestTag("catalog_download_gunung-gede-mission-a"))
        rule.onNodeWithText("Gunung Gede Mission A").assertIsDisplayed()
        rule.onNodeWithTag("catalog_download_gunung-gede-mission-a").performScrollTo()
        rule.onNodeWithTag("catalog_download_gunung-gede-mission-a").assertIsDisplayed()
        rule.onNodeWithTag("catalog_download_gunung-gede-mission-a").performClick()

        rule.waitUntil(timeoutMillis = 20_000L) {
            rule.onAllNodesWithTag("activation_warning_dialog").fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("activation_warning_dialog").assertIsDisplayed()
        rule.onNodeWithText("Activate").performClick()
        waitForText("Gunung Gede Mission A activated for map use")
    }

    private fun waitForText(text: String) {
        rule.waitUntil(timeoutMillis = 10_000L) {
            rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
