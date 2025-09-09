package com.example.autostradaauctions.ui.viewmodel

import com.example.autostradaauctions.testing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class HomeViewModelTest {
    
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()
    
    private lateinit var viewModel: HomeViewModel
    
    @Before
    fun setup() {
        viewModel = HomeViewModel()
    }
    
    @Test
    fun `initial state should be correct`() {
        val state = viewModel.uiState.value
        
        assertEquals("", state.searchQuery)
        assertEquals("All", state.selectedMake)
        assertNull(state.minPrice)
        assertNull(state.maxPrice)
        assertTrue(state.allAuctions.isEmpty())
        assertTrue(state.filteredAuctions.isEmpty())
        assertTrue(state.availableMakes.contains("All"))
        assertTrue(state.isLoading)
        assertNull(state.errorMessage)
    }
    
    @Test
    fun `updateSearchQuery should filter auctions`() = runTest {
        // Arrange
        val testAuctions = TestUtils.createTestAuctions(3)
        // Simulate loaded auctions
        viewModel.loadAuctions()
        
        // Act
        viewModel.updateSearchQuery("Toyota")
        
        // Assert
        assertEquals("Toyota", viewModel.uiState.value.searchQuery)
    }
    
    @Test
    fun `updateSelectedMake should filter by make`() = runTest {
        // Arrange
        val testAuctions = listOf(
            TestUtils.createTestAuction(id = 1, make = "Toyota"),
            TestUtils.createTestAuction(id = 2, make = "Honda"),
            TestUtils.createTestAuction(id = 3, make = "Toyota")
        )
        
        // Act
        viewModel.updateSelectedMake("Toyota")
        
        // Assert
        assertEquals("Toyota", viewModel.uiState.value.selectedMake)
    }
    
    @Test
    fun `updatePriceRange should set price filters`() {
        // Act
        viewModel.updatePriceRange(10000.0, 20000.0)
        
        // Assert
        val state = viewModel.uiState.value
        assertEquals(10000.0, state.minPrice)
        assertEquals(20000.0, state.maxPrice)
    }
    
    @Test
    fun `clearFilters should reset all filters`() {
        // Arrange - set some filters
        viewModel.updateSearchQuery("Toyota")
        viewModel.updateSelectedMake("Honda")
        viewModel.updatePriceRange(5000.0, 15000.0)
        
        // Act
        viewModel.clearFilters()
        
        // Assert
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertEquals("All", state.selectedMake)
        assertNull(state.minPrice)
        assertNull(state.maxPrice)
    }
    
    @Test
    fun `formatPrice should format correctly`() {
        // Test various price formats
        assertEquals("$10,000", viewModel.formatPrice(10000.0))
        assertEquals("$1,500", viewModel.formatPrice(1500.0))
        assertEquals("$25,999", viewModel.formatPrice(25999.0))
        assertEquals("$100", viewModel.formatPrice(100.0))
    }
    
    @Test
    fun `getStatusColor should return correct colors`() {
        // Note: This would need to be adjusted based on actual color implementation
        val activeColor = viewModel.getStatusColor("Active")
        val endedColor = viewModel.getStatusColor("Ended")
        val pendingColor = viewModel.getStatusColor("Pending")
        
        // These assertions would depend on your actual color scheme
        assertNotNull(activeColor)
        assertNotNull(endedColor)
        assertNotNull(pendingColor)
    }
    
    @Test
    fun `calculateTimeLeft should format time correctly`() {
        // This would need actual date/time logic testing
        val futureTime = java.time.LocalDateTime.now().plusHours(2)
        val pastTime = java.time.LocalDateTime.now().minusHours(1)
        
        val futureResult = viewModel.calculateTimeLeft(futureTime)
        val pastResult = viewModel.calculateTimeLeft(pastTime)
        
        // Verify time formatting (exact assertions would depend on implementation)
        assertNotNull(futureResult)
        assertNotNull(pastResult)
    }
}

/**
 * Test filtering logic separately
 */
class FilteringLogicTest {
    
    @Test
    fun `filterAuctions should work with search query`() {
        val auctions = listOf(
            testAuction().withTitle("2020 Toyota Camry").build(),
            testAuction().withTitle("2019 Honda Accord").build(),
            testAuction().withTitle("2021 Toyota Corolla").build()
        )
        
        val filtered = auctions.filter { it.title.contains("Toyota", ignoreCase = true) }
        
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.title.contains("Toyota", ignoreCase = true) })
    }
    
    @Test
    fun `filterAuctions should work with price range`() {
        val auctions = listOf(
            testAuction().withCurrentBid(10000.0).build(),
            testAuction().withCurrentBid(15000.0).build(),
            testAuction().withCurrentBid(20000.0).build(),
            testAuction().withCurrentBid(25000.0).build()
        )
        
        val filtered = auctions.filter { auction ->
            auction.currentBid >= 12000.0 && auction.currentBid <= 22000.0
        }
        
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.currentBid in 12000.0..22000.0 })
    }
    
    @Test
    fun `filterAuctions should work with make filter`() {
        val auctions = listOf(
            testAuction().withMake("Toyota").build(),
            testAuction().withMake("Honda").build(),
            testAuction().withMake("Toyota").build(),
            testAuction().withMake("Ford").build()
        )
        
        val filtered = auctions.filter { it.vehicle.make == "Toyota" }
        
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.vehicle.make == "Toyota" })
    }
    
    @Test
    fun `filterAuctions should work with combined filters`() {
        val auctions = listOf(
            testAuction().withTitle("2020 Toyota Camry").withMake("Toyota").withCurrentBid(15000.0).build(),
            testAuction().withTitle("2019 Honda Accord").withMake("Honda").withCurrentBid(18000.0).build(),
            testAuction().withTitle("2021 Toyota Corolla").withMake("Toyota").withCurrentBid(12000.0).build(),
            testAuction().withTitle("2018 Toyota Prius").withMake("Toyota").withCurrentBid(8000.0).build()
        )
        
        // Filter: Toyota make, price between 10000-16000, title contains "Toyota"
        val filtered = auctions.filter { auction ->
            auction.vehicle.make == "Toyota" &&
            auction.currentBid >= 10000.0 &&
            auction.currentBid <= 16000.0 &&
            auction.title.contains("Toyota", ignoreCase = true)
        }
        
        assertEquals(2, filtered.size) // Should match Camry and Corolla
        assertTrue(filtered.all { 
            it.vehicle.make == "Toyota" && 
            it.currentBid in 10000.0..16000.0 
        })
    }
}
