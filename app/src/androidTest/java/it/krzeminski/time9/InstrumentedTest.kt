package it.krzeminski.time9

import android.text.InputType
import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class InstrumentedTest {
    @get:Rule
    var activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun addsNewWorkItemWithDefaultWorkTypeAndCorrectTime() {
        // When
        onView(withId(R.id.button_change_work_type_1)).perform(click())

        // Then
        val workHistoryFilePath = activityRule.activity.filesDir.resolve("work_history.tsv")
        val workHistoryLines = Files.readAllLines(workHistoryFilePath.toPath(), Charsets.UTF_8)

        assertThat(workHistoryLines.size, `is`(2))
        assertThat(workHistoryLines[0], `is`("type\tstartTime"))

        val entry = workHistoryLines[1].split('\t')
        assertThat(entry[0], `is`("Please"))
        val parsedTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(entry[1]).toInstant()
        assertThat(Duration.between(parsedTime, Instant.now()).abs().seconds, lessThan(5L))
    }

    @Test
    fun updatesTimeWorkedTodayAndCurrentWorkItemTime() {
        // Allowing 1-second tolerance everywhere where elapsed time is checked.

        // Preconditions
        onView(withId(R.id.time_worked_today))
            .check(matches(withText("00:00:00")))

        // Test flow
        onView(withId(R.id.button_change_work_type_1)).perform(click())
        Thread.sleep(5000)
        onView(withId(R.id.time_worked_today))
            .check(matches(anyOf(withText("00:00:04"), withText("00:00:05"), withText("00:00:06"))))
        onView(withId(R.id.current_work_type_time))
            .check(matches(anyOf(withText("00:00:04"), withText("00:00:05"), withText("00:00:06"))))

        onView(withId(R.id.button_off_work)).perform(click())
        Thread.sleep(10000)
        onView(withId(R.id.time_worked_today))
            .check(matches(anyOf(withText("00:00:04"), withText("00:00:05"), withText("00:00:06"))))
        onView(withId(R.id.current_work_type_time))
            .check(matches(anyOf(withText("00:00:09"), withText("00:00:10"), withText("00:00:11"))))

        onView(withId(R.id.button_change_work_type_2)).perform(click())
        Thread.sleep(15000)
        onView(withId(R.id.time_worked_today))
            .check(matches(anyOf(withText("00:00:19"), withText("00:00:20"), withText("00:00:21"))))
        onView(withId(R.id.current_work_type_time))
            .check(matches(anyOf(withText("00:00:14"), withText("00:00:15"), withText("00:00:16"))))
    }

    @Test
    fun changesWorkTypePreferenceAndAddsNewWorkItemWithNewWorkType() {
        // When
        onView(withId(R.id.action_preferences)).perform(click())

        onView(withText("Work type slot 1"))
            .perform(click())

        onView(withInputType(InputType.TYPE_CLASS_TEXT))
            .perform(click())
            .perform(pressKey(KeyEvent.KEYCODE_DEL))
            .perform(typeText("New work type"))

        onView(withText("OK"))
            .perform(click())

        onView(isRoot())
            .perform(pressBack())

        onView(withId(R.id.button_change_work_type_1)).perform(click())

        // Then
        onView(allOf(withId(R.id.button_change_work_type_1), withText("New work type")))
            .check(matches(isDisplayed()))

        val workHistoryFilePath = activityRule.activity.filesDir.resolve("work_history.tsv")
        val workHistoryLines = Files.readAllLines(workHistoryFilePath.toPath(), Charsets.UTF_8)

        assertThat(workHistoryLines.size, `is`(2))
        assertThat(workHistoryLines[0], `is`("type\tstartTime"))

        val entry = workHistoryLines[1].split('\t')
        assertThat(entry[0], `is`("New work type"))
    }
}
