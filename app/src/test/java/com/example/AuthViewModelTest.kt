package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAuthSheetVisibility() {
        assertFalse(viewModel.isAuthSheetVisible.value)
        viewModel.showAuthSheet()
        assertTrue(viewModel.isAuthSheetVisible.value)
        viewModel.dismissAuthSheet()
        assertFalse(viewModel.isAuthSheetVisible.value)
    }

    @Test
    fun testSignInWithEmailValidation() = runTest {
        var receivedMessage: String? = null
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.userMessage.collect { receivedMessage = it }
        }
        viewModel.signInWithEmail("", "")
        assertEquals("Please enter both email and password", receivedMessage)
        job.cancel()
    }

    @Test
    fun testSignUpWithEmailValidation() = runTest {
        var receivedMessage: String? = null
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.userMessage.collect { receivedMessage = it }
        }
        viewModel.signUpWithEmail("test@example.com", "123", "Test User")
        assertEquals("Password must be at least 6 characters", receivedMessage)
        job.cancel()
    }

    @Test
    fun testGetCurrentUserId() {
        val uid = viewModel.getCurrentUserId()
        assertTrue(uid.isNotEmpty())
    }
}
