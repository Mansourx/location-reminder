package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValueTest
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setup() {
        stopKoin()
        val remindersList = mutableListOf(
            ReminderDTO(TITLE, DESCRIPTION, LOCATION, LAT, LONG),
            ReminderDTO(TITLE, DESCRIPTION, LOCATION, LAT, LONG),
            ReminderDTO(TITLE, DESCRIPTION, LOCATION, LAT, LONG),
        )

        fakeDataSource = FakeDataSource(remindersList)
        viewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

    }

    @Test
    fun onClear() {
        viewModel.onClear()
        assertThat(viewModel.reminderTitle.getOrAwaitValueTest(), nullValue())
        assertThat(viewModel.reminderDescription.getOrAwaitValueTest(), nullValue())
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValueTest(), nullValue())
        assertThat(viewModel.selectedPOI.getOrAwaitValueTest(), nullValue())
        assertThat(viewModel.latitude.getOrAwaitValueTest(), nullValue())
        assertThat(viewModel.longitude.getOrAwaitValueTest(), nullValue())
    }

    @Test
    fun saveReminder_dataSource() = mainCoroutineRule.runBlockingTest {
        fakeDataSource.deleteAllReminders()
        val reminderData =
            ReminderDataItem(TITLE, DESCRIPTION, LOCATION, LAT, LONG)
        viewModel.saveReminder(reminderData)
        val reminderFromDataSource = (fakeDataSource.getReminders() as Result.Success).data
        assertThat(reminderData.title, equalTo(reminderFromDataSource[0].title))
    }

    companion object {
        private const val LAT = 25.2048
        private const val LONG = 55.2708
        private const val TITLE = "MansourTitle"
        private const val DESCRIPTION = "MansourDescription"
        private const val LOCATION = "DUBAI"
    }

}