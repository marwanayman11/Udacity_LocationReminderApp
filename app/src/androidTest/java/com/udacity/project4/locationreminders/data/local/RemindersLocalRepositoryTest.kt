package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        remindersLocalRepository =
            RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_retrieveReminder() = runTest {
        // GIVEN - A new reminder saved in the database.
        val newReminder = ReminderDTO("title", "description", "location", 1.0, 1.0)
        remindersLocalRepository.saveReminder(newReminder)
        // WHEN  - reminder retrieved by ID.
        val result = remindersLocalRepository.getReminder(newReminder.id)
        // THEN - Same reminder is returned.
        assertThat(result, `is`(notNullValue()))
        result as Result.Success
        assertThat(result.data.title, `is`("title"))
        assertThat(result.data.description, `is`("description"))
        assertThat(result.data.location, `is`("location"))
        assertThat(result.data.latitude, `is`(1.0))
        assertThat(result.data.longitude, `is`(1.0))
    }

    @Test
    fun deleteAllReminders_retrieveReminders() = runTest {
        // GIVEN - A new reminder saved in the database then delete all reminders.
        val newReminder = ReminderDTO("title", "description", "location", 1.0, 1.0)
        remindersLocalRepository.saveReminder(newReminder)
        remindersLocalRepository.deleteAllReminders()
        // WHEN  - get reminders.
        val result = remindersLocalRepository.getReminders()
        result as Result.Success
        // THEN - The retrieved list of reminders is empty
        assertThat(result.data.isEmpty(), `is`(true))
    }
    @Test
    fun dataNotFound_error() = runTest {
        // GIVEN - A new reminder saved in the database then delete all reminders.
        val newReminder = ReminderDTO("title", "description", "location", 1.0, 1.0)
        remindersLocalRepository.saveReminder(newReminder)
        remindersLocalRepository.deleteAllReminders()
        // WHEN  - get reminder by id
        val result = remindersLocalRepository.getReminder(newReminder.id)
        result as Result.Error
        // THEN - The retrieved list of reminders is empty
        assertThat(result.message, `is`("Reminder not found!"))
    }

}