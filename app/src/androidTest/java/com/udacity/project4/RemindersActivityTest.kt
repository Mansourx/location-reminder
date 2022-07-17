package com.udacity.project4

import android.app.Application
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private var decorView: View? = null
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun setup() {
        stopKoin()
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }

        startKoin {
            modules(listOf(myModule))
        }

        repository = get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResources() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResources() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun getRemindersList() = runBlocking {

        // Set initial state.
        repository.saveReminder(ReminderDTO(TITLE, DESCRIPTION, LOCATION, null, null))

        // Start up Tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the task on the list and verify that all the data is correct.
        onView(withId(R.id.title)).check(matches(withText(TITLE)))
        onView(withId(R.id.description)).check(matches(withText(DESCRIPTION)))
        onView(withId(R.id.location)).check(matches(withText(LOCATION)))

        // Make sure the activity is closed before resetting the db.
        activityScenario.close()
    }

    @Test
    fun saveToRemindersList() = runBlocking {

        repository.saveReminder(ReminderDTO(TITLE, DESCRIPTION, LOCATION, null, null))


        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.title)).check(matches(withText(TITLE)))
        onView(withId(R.id.description)).check(matches(withText(DESCRIPTION)))
        onView(withId(R.id.location)).check(matches(withText(LOCATION)))
        onView(withId(R.id.add_reminder_fab)).perform(click())
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.buttonSave)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText(NEW_TITLE))
        onView(withId(R.id.reminderDescription)).perform(replaceText(NEW_DESCRIPTION))
        onView(withId(R.id.saveReminder)).perform(click())

        activityScenario.onActivity {
            decorView = it.window.decorView
        }

        onView(withText("Reminder Saved !"))
            .inRoot(withDecorView(not(decorView)))
            .check(matches(isDisplayed()));

        onView(withText(NEW_TITLE)).check(matches(isDisplayed()))

        activityScenario.close()
    }


    @Test
    fun saveToRemindersList_ErrMessage() = runBlocking {

        repository.saveReminder(ReminderDTO(TITLE, DESCRIPTION, LOCATION, null, null))

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.title)).check(matches(withText("MansourTitle")))
        onView(withId(R.id.description)).check(matches(withText("MansourDescription")))
        onView(withId(R.id.location)).check(matches(withText("DUBAI")))

        onView(withId(R.id.add_reminder_fab)).perform(click())
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.buttonSave)).perform(click())
        onView(withId(R.id.reminderDescription)).perform(replaceText(NEW_DESCRIPTION))
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(R.id.snackbar_text)).check(matches(withText(R.string.err_enter_title)))

        activityScenario.close()
    }

    companion object {
        const val LAT = 25.2048
        const val LONG = 55.2708
        const val TITLE = "MansourTitle"
        const val NEW_TITLE = "new title"
        const val DESCRIPTION = "MansourDescription"
        const val NEW_DESCRIPTION = "new description"
        const val LOCATION = "DUBAI"
        const val NEW_LOCATION = "new Dubai"
    }
}
