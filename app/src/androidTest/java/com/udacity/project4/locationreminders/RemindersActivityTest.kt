package com.udacity.project4.locationreminders

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.DataBindingIdlingResource
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.utils.monitorActivity
import com.google.android.material.internal.ContextUtils.getActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
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


@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : KoinTest {
    private lateinit var remindersDataSource: ReminderDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()

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
        runBlocking {
            remindersDataSource.deleteAllReminders()
        }
    }

    @After
    fun cleanUp() {
        stopKoin()
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun allApp_functionality() = runBlocking {
        // Start up Tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        val decorView = getActivity(ApplicationProvider.getApplicationContext())?.window?.decorView
        // click on add reminder FAB
        onView(withId(R.id.addReminderFAB)).perform(click())
        // click on save reminder FAB without typing title edit text
        onView(withId(R.id.saveReminder)).perform(click())
        // snackBar is appeared
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_enter_title)))
        delay(3000)
        // type the title
        onView(withId(R.id.reminderTitle)).perform(typeText("title"), closeSoftKeyboard())
        // click on save reminder FAB without typing description edit text
        onView(withId(R.id.saveReminder)).perform(click())
        // snackBar is appeared
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_select_description)))
        delay(3000)
        // type the description
        onView(withId(R.id.reminderDescription)).perform(
            typeText("description"),
            closeSoftKeyboard()
        )
        // click on save reminder FAB without select location
        onView(withId(R.id.saveReminder)).perform(click())
        // snackBar is appeared
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_select_location)))
        delay(3000)
        // click on select location
        onView(withId(R.id.selectLocation)).perform(click())
        delay(3000)
        //select location from map
        onView(withId(R.id.map)).perform(longClick())
        delay(3000)
        // click on save location button
        onView(withId(R.id.save_button)).perform(click())
        delay(3000)
        // verify title, description and location
        onView(withId(R.id.reminderTitle)).check(matches(withText("title")))
        onView(withId(R.id.reminderDescription)).check(matches(withText("description")))
        onView(withId(R.id.selectedLocation)).check(matches(not(withText(""))))
        // click on save reminder FAB
        onView(withId(R.id.saveReminder)).perform(click())
        // toast is appeared
        onView(withText(R.string.geofence_added)).inRoot(withDecorView(not(`is`(decorView))))
            .check(matches(isDisplayed()))
        delay(3000)
        // toast is appeared
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(decorView))))
            .check(matches(isDisplayed()))
        delay(3000)
        // check item in reminders list
        onView(withId(R.id.title)).check(matches(withText("title")))
        onView(withId(R.id.description)).check(matches(withText("description")))
        activityScenario.close()
    }

}