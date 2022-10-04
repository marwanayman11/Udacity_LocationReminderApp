package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@MediumTest
@ExperimentalCoroutinesApi

class SaveReminderFragmentTest : KoinTest {
    private lateinit var remindersDataSource: ReminderDataSource

    @Before
    fun initRepository() {
        stopKoin()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get())as ReminderDataSource }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(myModule))
        }
        remindersDataSource = get()
    }

    @After
    fun cleanUp() {
        stopKoin()
    }

    @Test
    fun clickSelectLocation_navigateToSelectLocationFragment() {
        //Given - on the SaveRemindersFragment
        val scenario = launchFragmentInContainer<SaveReminderFragment>(
            Bundle(),
            R.style.Theme_LocationReminder
        )
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        //when - click on SelectLocation
        onView(withId(R.id.selectLocation)).perform(
            ViewActions.click()

        )
        //Then - Verify that we navigate to SelectLocationFragment
        verify(navController).navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        Thread.sleep(3000)
    }
}