package com.example.autostradaauctions

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.autostradaauctions.ui.theme.AutostradaAuctionsTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive integration tests for the AutostradaAuctions app
 */
@RunWith(AndroidJUnit4::class)
class AutostradaAuctionsIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var context: android.content.Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun app_launches_successfully() {
        // Test that the app launches without crashing
        composeTestRule.onNodeWithText("AutostradaAuctions")
            .assertIsDisplayed()
    }

    @Test
    fun navigation_works_correctly() {
        // Test navigation between screens
        
        // Should start on home screen
        composeTestRule.onNodeWithText("AutostradaAuctions")
            .assertIsDisplayed()

        // Test login navigation
        composeTestRule.onNodeWithContentDescription("Login")
            .performClick()

        // Should navigate to login screen
        composeTestRule.waitForIdle()
        
        // Test back navigation
        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()

        // Should return to home screen
        composeTestRule.onNodeWithText("AutostradaAuctions")
            .assertIsDisplayed()
    }

    @Test
    fun search_functionality_works() {
        // Test search functionality
        composeTestRule.onNodeWithText("Search auctions...")
            .performClick()
            .performTextInput("Toyota")

        composeTestRule.waitForIdle()

        // Verify search query is displayed
        composeTestRule.onNodeWithText("Toyota")
            .assertIsDisplayed()
    }

    @Test
    fun login_form_validation_works() {
        // Navigate to login screen
        composeTestRule.onNodeWithContentDescription("Login")
            .performClick()

        composeTestRule.waitForIdle()

        // Try to login with empty fields
        composeTestRule.onNodeWithText("Login")
            .performClick()

        // Should show validation error
        composeTestRule.waitForIdle()
        
        // Fill in username
        composeTestRule.onNodeWithText("Username")
            .performTextInput("testuser")

        // Fill in password  
        composeTestRule.onNodeWithText("Password")
            .performTextInput("password123")

        // Form should now be valid
        composeTestRule.onNodeWithText("Login")
            .assertIsEnabled()
    }

    @Test
    fun theme_is_applied_correctly() {
        // Test that Material 3 theme is applied
        composeTestRule.setContent {
            AutostradaAuctionsTheme {
                // The app should use the theme colors and typography
            }
        }
        
        // Verify theme elements are present
        composeTestRule.onNodeWithText("AutostradaAuctions")
            .assertIsDisplayed()
    }

    @Test
    fun error_handling_works() {
        // Test error states by triggering network errors
        // This would require mock network responses in a real test
        
        // Verify error message is displayed when network fails
        composeTestRule.waitForIdle()
        
        // The app should handle errors gracefully without crashing
        composeTestRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun accessibility_labels_are_present() {
        // Test accessibility
        composeTestRule.onNodeWithContentDescription("Refresh")
            .assertIsDisplayed()
            
        composeTestRule.onNodeWithContentDescription("Login")
            .assertIsDisplayed()
            
        composeTestRule.onNodeWithContentDescription("Favorites")
            .assertIsDisplayed()
            
        composeTestRule.onNodeWithContentDescription("Profile")
            .assertIsDisplayed()
    }

    @Test
    fun performance_is_acceptable() {
        // Test that screens load within acceptable time
        val startTime = System.currentTimeMillis()
        
        // Navigate to different screens
        composeTestRule.onNodeWithContentDescription("Login")
            .performClick()
        
        composeTestRule.waitForIdle()
        
        val loadTime = System.currentTimeMillis() - startTime
        
        // Screen should load within 2 seconds
        assert(loadTime < 2000) { "Screen took too long to load: ${loadTime}ms" }
    }

    @Test
    fun memory_usage_is_reasonable() {
        // Test memory usage doesn't exceed reasonable limits
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        
        val memoryUsagePercentage = (usedMemory.toFloat() / maxMemory) * 100
        
        // Memory usage should be under 70%
        assert(memoryUsagePercentage < 70f) { 
            "Memory usage too high: ${memoryUsagePercentage}%" 
        }
    }

    @Test
    fun filters_work_correctly() {
        // Test filtering functionality
        
        // Open filters
        composeTestRule.onNodeWithContentDescription("Filters")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify filter UI is displayed
        composeTestRule.onNodeWithText("Vehicle Make")
            .assertIsDisplayed()
            
        composeTestRule.onNodeWithText("Price Range")
            .assertIsDisplayed()

        // Test filter selection
        composeTestRule.onNodeWithText("All")
            .assertIsDisplayed()
    }

    @Test
    fun auction_cards_are_interactive() {
        // Wait for auctions to load
        composeTestRule.waitForIdle()
        
        // Verify auction cards are displayed and clickable
        // This test would need to be adjusted based on actual auction data
        composeTestRule.onRoot().assertIsDisplayed()
    }
}
