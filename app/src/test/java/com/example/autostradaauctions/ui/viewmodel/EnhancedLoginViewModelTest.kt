package com.example.autostradaauctions.ui.viewmodel

import com.example.autostradaauctions.testing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class EnhancedLoginViewModelTest {
    
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()
    
    private lateinit var mockAuthRepository: MockAuthRepository
    private lateinit var viewModel: EnhancedLoginViewModel
    
    @Before
    fun setup() {
        mockAuthRepository = MockAuthRepository()
        viewModel = EnhancedLoginViewModel(mockAuthRepository)
    }
    
    @Test
    fun `initial state should be correct`() {
        val state = viewModel.uiState.value
        
        assertEquals("", state.username)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertFalse(state.isLoginSuccessful)
    }
    
    @Test
    fun `updateUsername should update state`() {
        val testUsername = "testuser"
        
        viewModel.updateUsername(testUsername)
        
        assertEquals(testUsername, viewModel.uiState.value.username)
    }
    
    @Test
    fun `updatePassword should update state`() {
        val testPassword = "password123"
        
        viewModel.updatePassword(testPassword)
        
        assertEquals(testPassword, viewModel.uiState.value.password)
    }
    
    @Test
    fun `login with valid credentials should succeed`() = runTest {
        // Arrange
        mockAuthRepository.setResponse(success = true, user = TestUtils.createTestUser())
        viewModel.updateUsername("testuser")
        viewModel.updatePassword("password123")
        
        // Act
        viewModel.login()
        
        // Assert
        assertTrue(viewModel.uiState.value.isLoginSuccessful)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }
    
    @Test
    fun `login with invalid credentials should fail`() = runTest {
        // Arrange
        mockAuthRepository.setResponse(success = false)
        viewModel.updateUsername("wronguser")
        viewModel.updatePassword("wrongpassword")
        
        // Act
        viewModel.login()
        
        // Assert
        assertFalse(viewModel.uiState.value.isLoginSuccessful)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }
    
    @Test
    fun `login with empty username should show validation error`() = runTest {
        // Arrange
        viewModel.updateUsername("")
        viewModel.updatePassword("password123")
        
        // Act
        viewModel.login()
        
        // Assert
        assertFalse(viewModel.uiState.value.isLoginSuccessful)
        assertEquals("Username cannot be empty", viewModel.uiState.value.errorMessage)
    }
    
    @Test
    fun `login with empty password should show validation error`() = runTest {
        // Arrange
        viewModel.updateUsername("testuser")
        viewModel.updatePassword("")
        
        // Act
        viewModel.login()
        
        // Assert
        assertFalse(viewModel.uiState.value.isLoginSuccessful)
        assertEquals("Password cannot be empty", viewModel.uiState.value.errorMessage)
    }
    
    @Test
    fun `isFormValid should return true for valid input`() {
        viewModel.updateUsername("testuser")
        viewModel.updatePassword("password123")
        
        assertTrue(viewModel.isFormValid())
    }
    
    @Test
    fun `isFormValid should return false for invalid input`() {
        // Empty username
        viewModel.updateUsername("")
        viewModel.updatePassword("password123")
        assertFalse(viewModel.isFormValid())
        
        // Empty password
        viewModel.updateUsername("testuser")
        viewModel.updatePassword("")
        assertFalse(viewModel.isFormValid())
        
        // Both empty
        viewModel.updateUsername("")
        viewModel.updatePassword("")
        assertFalse(viewModel.isFormValid())
    }
    
    @Test
    fun `clearError should reset error state`() = runTest {
        // Arrange - create error state
        mockAuthRepository.setResponse(success = false)
        viewModel.updateUsername("testuser")
        viewModel.updatePassword("password123")
        viewModel.login()
        
        // Verify error exists
        assertNotNull(viewModel.uiState.value.errorMessage)
        
        // Act
        viewModel.clearError()
        
        // Assert
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
