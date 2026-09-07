package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
    fun `test auth sheet visibility toggle`() {
        assertFalse(viewModel.isAuthSheetVisible.value)
        viewModel.showAuthSheet()
        assertTrue(viewModel.isAuthSheetVisible.value)
        viewModel.dismissAuthSheet()
        assertFalse(viewModel.isAuthSheetVisible.value)
    }

    @Test
    fun `test initial auth state user profile`() {
        val user = viewModel.currentUserProfile.value
        assertNotNull(user)
    }

    @Test
    fun `test password reset empty email validation`() {
        var callbackCalled = false
        var successResult = false
        var resultMsg = ""

        viewModel.sendPasswordReset("") { success, msg ->
            callbackCalled = true
            successResult = success
            resultMsg = msg
        }

        assertTrue(callbackCalled)
        assertFalse(successResult)
        assertEquals("Please enter your email address.", resultMsg)
    }

    @Test
    fun `test current user id non empty`() {
        val uid = viewModel.getCurrentUserId()
        assertTrue(uid.isNotBlank())
    }
}
