package com.example.junglenav

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MvpSmokeTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun createWaypointAndStartRecordingFromMainShell() {
        rule.onNodeWithText("Waypoints").performClick()
        rule.onNodeWithText("Add Waypoint").performClick()
        rule.onAllNodesWithText("Waypoint 1").assertCountEquals(2)

        rule.onNodeWithText("Tracks").performClick()
        rule.onNodeWithText("Start Recording").performClick()
        rule.onAllNodesWithText("Recording").assertCountEquals(2)
    }

    @Test
    fun shellChromeAndTopLevelRoutesStayUsable() {
        rule.onNodeWithText("MapLibre OpenGL").assertIsDisplayed()
        rule.onNodeWithText("Add Waypoint").assertIsDisplayed()
        rule.onNodeWithText("Return To Base").assertIsDisplayed()
        rule.onNodeWithTag("compact_target_summary").assertIsDisplayed()

        val rootBounds = rule.onRoot().fetchSemanticsNode().boundsInRoot
        val bottomBounds = rule.onNodeWithTag("route_bar").fetchSemanticsNode().boundsInRoot

        assertTrue(bottomBounds.bottom < rootBounds.bottom)

        rule.onNodeWithText("Packages").performClick()
        waitForText("Remote Catalog")
        rule.onNodeWithTag("package_search_field").assertIsDisplayed()

        rule.onNodeWithText("Diagnostics").performClick()
        waitForText("Current state")
        rule.onNodeWithText("Current state").assertIsDisplayed()

        rule.onNodeWithText("Settings").performClick()
        waitForText("Operation profile")
        rule.onNodeWithText("Operation profile").assertIsDisplayed()
        rule.onNodeWithText("Emergency").performClick()
        rule.onNodeWithText("Current mode EMERGENCY").assertIsDisplayed()

        rule.onNodeWithText("Waypoints").performClick()
        waitForText("Drop fast field markers and keep them readable offline.")
        rule.onNodeWithText("Drop fast field markers and keep them readable offline.").assertIsDisplayed()

        rule.onNodeWithText("Tracks").performClick()
        waitForText("Capture movement with a calmer, clearer recording workflow.")
        rule.onNodeWithText("Capture movement with a calmer, clearer recording workflow.").assertIsDisplayed()

        rule.onNodeWithText("Map").performClick()
        waitForTag("map_full_view")
        rule.onNodeWithTag("map_full_view").assertIsDisplayed()
    }

    @Test
    fun mapRouteUsesFullViewLayout() {
        rule.onNodeWithTag("map_full_view").assertIsDisplayed()
        rule.onNodeWithTag("map_bottom_sheet").assertIsDisplayed()
        rule.onNodeWithTag("compact_target_summary").assertIsDisplayed()
        rule.onNodeWithTag("center_gps_button").assertIsDisplayed()
        rule.onNodeWithTag("offline_region_toggle").assertIsDisplayed()
        rule.onNodeWithText("MapLibre OpenGL").assertIsDisplayed()
        rule.onNodeWithText("PATROL").assertIsDisplayed()
        waitForAnyText("Fallback style", "Offline style", "Remote package style")
        rule.onAllNodesWithText("Field View").assertCountEquals(0)
    }

    @Test
    fun mapRouteCanShowAndHideOfflineRegionDetails() {
        rule.onAllNodesWithTag("offline_region_details").assertCountEquals(0)
        rule.onNodeWithTag("offline_region_toggle").performClick()
        rule.onNodeWithTag("offline_region_details").assertIsDisplayed()
        rule.onNodeWithTag("offline_region_toggle").performClick()
        rule.onAllNodesWithTag("offline_region_details").assertCountEquals(0)
    }

    @Test
    fun diagnosticsShowsNavigationSourceAndCompactShellChrome() {
        rule.onNodeWithText("Diagnostics").performClick()
        rule.onNodeWithTag("field_status_compact").assertIsDisplayed()
        rule.onNodeWithText("Navigation source FUSED").assertIsDisplayed()
    }

    @Test
    fun bottomNavShowsIconRoutes() {
        rule.onNodeWithTag("route_single_row").assertIsDisplayed()
        rule.onNodeWithTag("route_icon_map").assertIsDisplayed()
        rule.onNodeWithTag("route_icon_waypoints").assertIsDisplayed()
        rule.onNodeWithTag("route_icon_tracks").assertIsDisplayed()
        rule.onNodeWithTag("route_icon_packages").assertIsDisplayed()
    }

    @Test
    fun packageLibraryCanSearchDownloadAndRenderOfflineBundle() {
        rule.onNodeWithText("Packages").performClick()
        waitForText("Remote Catalog")
        waitForText("Mission catalog ready")
        rule.onNodeWithTag("package_search_field").performTextInput("Bogor")
        Espresso.closeSoftKeyboard()
        rule.onNodeWithTag("package_list")
            .performScrollToNode(hasTestTag("catalog_download_bogor-trails"))
        rule.onNodeWithText("Bogor Trails").assertIsDisplayed()
        rule.onNodeWithTag("catalog_download_bogor-trails").performScrollTo()
        rule.onNodeWithTag("catalog_download_bogor-trails").assertIsDisplayed()
        rule.onNodeWithTag("catalog_download_bogor-trails").performClick()

        rule.waitUntil(timeoutMillis = 20_000L) {
            rule.onAllNodesWithText("Bogor Trails ready for offline use")
                .fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithText("Map").performClick()
        waitForAnyText("Offline style", "Remote package style")

        rule.onNodeWithTag("offline_region_toggle").performClick()
        rule.onNodeWithText("Bogor Trails").assertIsDisplayed()
        rule.onNodeWithText("Style source: Offline jnavpack").assertIsDisplayed()
        rule.onNodeWithTag("hillshade_toggle_chip").assertIsDisplayed()
        rule.onNodeWithTag("imagery_toggle_chip").assertIsDisplayed()
    }

    private fun waitForTag(tag: String) {
        rule.waitUntil(timeoutMillis = 5_000L) {
            rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForText(text: String) {
        rule.waitUntil(timeoutMillis = 5_000L) {
            rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForAnyText(vararg texts: String) {
        rule.waitUntil(timeoutMillis = 5_000L) {
            texts.any { text ->
                rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
            }
        }
    }
}
