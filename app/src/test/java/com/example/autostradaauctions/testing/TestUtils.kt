package com.example.autostradaauctions.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Test utilities and helpers for consistent testing across the app
 */
object TestUtils {
    
    /**
     * Create test auction data for testing
     */
    fun createTestAuction(
        id: Int = 1,
        title: String = "Test Auction",
        make: String = "Toyota",
        model: String = "Camry",
        year: Int = 2020,
        currentBid: Double = 15000.0,
        status: String = "Active"
    ) = com.example.autostradaauctions.data.model.Auction(
        id = id,
        title = title,
        description = "Test auction description",
        startingBid = 10000.0,
        currentBid = currentBid,
        endTime = java.time.LocalDateTime.now().plusDays(1),
        status = status,
        imageUrls = listOf("https://example.com/image1.jpg"),
        vehicle = com.example.autostradaauctions.data.model.Vehicle(
            id = id,
            make = make,
            model = model,
            year = year,
            mileage = 50000,
            vin = "TEST123456789",
            color = "Blue",
            fuelType = "Gasoline",
            transmission = "Automatic",
            drivetrain = "FWD",
            engine = "2.0L I4"
        ),
        seller = com.example.autostradaauctions.data.model.User(
            id = id,
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User"
        ),
        bids = emptyList()
    )
    
    /**
     * Create test user data
     */
    fun createTestUser(
        id: Int = 1,
        username: String = "testuser",
        email: String = "test@example.com"
    ) = com.example.autostradaauctions.data.model.User(
        id = id,
        username = username,
        email = email,
        firstName = "Test",
        lastName = "User"
    )
    
    /**
     * Create test login request
     */
    fun createTestLoginRequest(
        username: String = "testuser",
        password: String = "password123"
    ) = com.example.autostradaauctions.data.model.LoginRequest(
        username = username,
        password = password
    )
    
    /**
     * Create test login response
     */
    fun createTestLoginResponse(
        token: String = "test-jwt-token",
        user: com.example.autostradaauctions.data.model.User = createTestUser()
    ) = com.example.autostradaauctions.data.model.LoginResponse(
        token = token,
        user = user
    )
    
    /**
     * Create a list of test auctions
     */
    fun createTestAuctions(count: Int = 5): List<com.example.autostradaauctions.data.model.Auction> {
        return (1..count).map { i ->
            createTestAuction(
                id = i,
                title = "Test Auction $i",
                make = listOf("Toyota", "Honda", "Ford", "BMW", "Audi")[i % 5],
                currentBid = 15000.0 + (i * 1000)
            )
        }
    }
    
    /**
     * Assert that two doubles are approximately equal (for price comparisons)
     */
    fun assertApproximatelyEqual(
        expected: Double,
        actual: Double,
        tolerance: Double = 0.01
    ) {
        if (kotlin.math.abs(expected - actual) > tolerance) {
            throw AssertionError("Expected $expected but was $actual (tolerance: $tolerance)")
        }
    }
    
    /**
     * Wait for a condition to be true (useful for async testing)
     */
    suspend fun waitUntil(
        timeoutMillis: Long = 5000,
        intervalMillis: Long = 100,
        condition: suspend () -> Boolean
    ) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (condition()) return
            kotlinx.coroutines.delay(intervalMillis)
        }
        throw AssertionError("Condition was not met within ${timeoutMillis}ms")
    }
}

/**
 * JUnit rule for coroutine testing
 */
@ExperimentalCoroutinesApi
class MainCoroutineRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    
    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }
    
    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}

/**
 * Mock implementations for testing
 */
class MockAuthRepository : com.example.autostradaauctions.data.repository.AuthRepository {
    
    private var shouldSucceed = true
    private var mockUser: com.example.autostradaauctions.data.model.User? = null
    
    fun setResponse(success: Boolean, user: com.example.autostradaauctions.data.model.User? = null) {
        shouldSucceed = success
        mockUser = user
    }
    
    override suspend fun login(
        request: com.example.autostradaauctions.data.model.LoginRequest
    ): Result<com.example.autostradaauctions.data.model.LoginResponse> {
        return if (shouldSucceed) {
            Result.success(
                TestUtils.createTestLoginResponse(
                    user = mockUser ?: TestUtils.createTestUser()
                )
            )
        } else {
            Result.failure(Exception("Login failed"))
        }
    }
    
    override suspend fun register(
        request: com.example.autostradaauctions.data.model.RegisterRequest
    ): Result<com.example.autostradaauctions.data.model.LoginResponse> {
        return if (shouldSucceed) {
            Result.success(
                TestUtils.createTestLoginResponse(
                    user = mockUser ?: TestUtils.createTestUser()
                )
            )
        } else {
            Result.failure(Exception("Registration failed"))
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return Result.success(Unit)
    }
    
    override suspend fun getCurrentUser(): Result<com.example.autostradaauctions.data.model.User> {
        return if (shouldSucceed && mockUser != null) {
            Result.success(mockUser!!)
        } else {
            Result.failure(Exception("No user"))
        }
    }
    
    override suspend fun updateProfile(
        request: com.example.autostradaauctions.data.model.UpdateProfileRequest
    ): Result<com.example.autostradaauctions.data.model.User> {
        return if (shouldSucceed) {
            Result.success(mockUser ?: TestUtils.createTestUser())
        } else {
            Result.failure(Exception("Update failed"))
        }
    }
    
    override suspend fun isLoggedIn(): Boolean = mockUser != null
}

/**
 * Test data builders for fluent test setup
 */
class AuctionTestBuilder {
    private var auction = TestUtils.createTestAuction()
    
    fun withId(id: Int) = apply { auction = auction.copy(id = id) }
    fun withTitle(title: String) = apply { auction = auction.copy(title = title) }
    fun withCurrentBid(bid: Double) = apply { auction = auction.copy(currentBid = bid) }
    fun withStatus(status: String) = apply { auction = auction.copy(status = status) }
    fun withMake(make: String) = apply { 
        auction = auction.copy(vehicle = auction.vehicle.copy(make = make))
    }
    
    fun build() = auction
}

/**
 * Extension functions for easier testing
 */
fun testAuction() = AuctionTestBuilder()

/**
 * Assertion helpers
 */
fun assertAuctionEquals(
    expected: com.example.autostradaauctions.data.model.Auction,
    actual: com.example.autostradaauctions.data.model.Auction
) {
    assert(expected.id == actual.id) { "IDs don't match: ${expected.id} vs ${actual.id}" }
    assert(expected.title == actual.title) { "Titles don't match" }
    assert(expected.currentBid == actual.currentBid) { "Current bids don't match" }
    assert(expected.status == actual.status) { "Statuses don't match" }
}
