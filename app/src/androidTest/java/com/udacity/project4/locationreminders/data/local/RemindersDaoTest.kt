package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java,
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runTest {
        //Given - Insert a reminder
        val reminder = ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 1.0,
            longitude = 1.0
        )
        database.reminderDao().saveReminder(reminder)
        //When - get the reminder by id from database
        val loaded = database.reminderDao().getReminderById(reminder.id)
        //Then - The loaded data contains the expected values.
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun dataNotFound_predictableErrors() = runTest {
        //Given - insert a reminder then delete all reminders
        val reminder = ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 1.0,
            longitude = 1.0
        )
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().deleteAllReminders()
        //When - get all reminders and get reminder by id
        val loadedData = database.reminderDao().getReminders()
        val loaded = database.reminderDao().getReminderById(reminder.id)
        //Then - the reminders list will be empty
        assertThat(loadedData.isEmpty(), `is`(true))
        assertThat(loaded, `is`(nullValue()))
    }
}