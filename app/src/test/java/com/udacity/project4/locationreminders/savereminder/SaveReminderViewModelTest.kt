@file:Suppress("DEPRECATION")

package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.Q])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@SmallTest
class SaveReminderViewModelTest {
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var remindersRepository: FakeDataSource

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        remindersRepository = FakeDataSource()
        //Given a fresh TasksViewModel
        viewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
    }
    @After
    fun cleanUp(){
        stopKoin()
    }
    @Test
    fun validateAndSaveReminder_callErrorToDisplay() = mainCoroutineRule.runBlockingTest {
        // When make the repository return errors.
        remindersRepository.setReturnError(true)
        viewModel.validateAndSaveReminder(
            reminderData = ReminderDataItem(
                title = null,
                description = "description",
                location = "location",
                latitude = 1.0,
                longitude = 1.0
            )
        )
        // Then showSnackBarInt get new value
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun saveReminders_loading() = mainCoroutineRule.runBlockingTest {
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()
        // When save a reminder
        viewModel.saveReminder(
            reminderData = ReminderDataItem(
                title = "title",
                description = "description",
                location = "location",
                latitude = 1.0,
                longitude = 1.0
            )
        )
        // Then showLoading is true
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))
        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()
        // Then showLoading is false
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))

    }
}