package it.krzeminski.time9

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule
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
}
