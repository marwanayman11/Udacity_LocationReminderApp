@file:Suppress("DEPRECATION")

package com.udacity.project4.locationreminders.reminderslist

import android.os.Build.VERSION_CODES.Q
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
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

@Config(sdk = [Q])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@SmallTest
class RemindersListViewModelTest {
    private lateinit var viewModel: RemindersListViewModel
    private lateinit var remindersRepository: FakeDataSource

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        remindersRepository = FakeDataSource()
        val reminder1 = ReminderDTO(
            "title1",
            "description1",
            "location1",
            1.0,
            1.0,
        )
        val reminder2 = ReminderDTO(
            "title2",
            "description2",
            "location2",
            2.0,
            2.0,
        )
        val reminder3 = ReminderDTO(
            "title3",
            "description3",
            "location3",
            3.0,
            3.0,
        )
        remindersRepository.addReminders(reminder1, reminder2, reminder3)
        //Given a fresh TasksViewModel
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
    }
    @After
    fun cleanUp(){
        stopKoin()
    }

    @Test
    fun loadRemindersWhenRemindersAreUnavailable_callErrorToDisplay() =
        mainCoroutineRule.runBlockingTest {
            // When make the repository return errors.
            remindersRepository.setReturnError(true)
            viewModel.loadReminders()
            // Then showSnackBar get new value
            assertThat(
                viewModel.showSnackBar.getOrAwaitValue(),
                `is`("java.lang.Exception: Test exception")
            )
        }

    @Test
    fun loadReminders_loading() = mainCoroutineRule.runBlockingTest {
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()
        // When loadReminders
        viewModel.loadReminders()
        // Then showLoading is true
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))
        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()
        // Then showLoading is false
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

}