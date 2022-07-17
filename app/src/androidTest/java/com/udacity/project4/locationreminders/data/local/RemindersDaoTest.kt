package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    private lateinit var db: RemindersDatabase

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun insertReminder() = runBlockingTest {
        // GIVEN
        val reminder = ReminderDTO(TITLE, DESCRIPTION, LOCATION, LONG, LAT, ID)
        db.reminderDao().saveReminder(reminder)

        // WHEN
        val loadedRemind = db.reminderDao().getReminderById(ID)

        // THEN
        assertThat(loadedRemind as ReminderDTO, notNullValue())
        assertThat(reminder.id, `is`(loadedRemind.id))
        assertThat(reminder.description, `is`(loadedRemind.description))
        assertThat(reminder.location, `is`(loadedRemind.location))
        assertThat(reminder.latitude, `is`(loadedRemind.latitude))
        assertThat(reminder.longitude, `is`(loadedRemind.longitude))
    }

    @Test
    fun deleteReminders() = runBlockingTest {
        // GIVEN
        val reminder = ReminderDTO(TITLE, DESCRIPTION, LOCATION, LONG, LAT, ID)
        db.reminderDao().saveReminder(reminder)
        val reminder2 = ReminderDTO(TITLE, DESCRIPTION, LOCATION, LONG, LAT, ID)
        db.reminderDao().saveReminder(reminder2)

        db.reminderDao().deleteAllReminders()

        // WHEN
        val loadedRemind = db.reminderDao().getReminders()

        // THEN
        assertThat(loadedRemind, `is`(emptyList()))
    }

    companion object {
        const val LAT = 25.2048
        const val LONG = 55.2708
        const val TITLE = "MansourTitle"
        const val DESCRIPTION = "MansourDescription"
        const val LOCATION = "DUBAI"
        const val ID = "1"
    }

}