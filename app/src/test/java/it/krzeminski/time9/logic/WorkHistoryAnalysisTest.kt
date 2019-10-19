package it.krzeminski.time9.logic

import com.soywiz.klock.*
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec
import it.krzeminski.time9.model.WorkItem
import it.krzeminski.time9.model.WorkType

class WorkHistoryAnalysisTest : FeatureSpec({
    val easyTimeFormat = DateFormat("yyyy-MM-dd HH:mm:ss")

    feature("${List<WorkItem>::calculateWorkDuration.name} function") {
        scenario("one day given, no off-work's") {
            // given
            val workHistory = listOf(
                WorkItem(type = WorkType("type1"), startTime = easyTimeFormat.parse("2019-10-12 16:00:00")),
                WorkItem(type = WorkType("type2"), startTime = easyTimeFormat.parse("2019-10-12 17:15:00"))
            )

            // when
            val result = workHistory.calculateWorkDuration(
                now = easyTimeFormat.parse("2019-10-12 17:15:24"))

            // then
            result shouldBe 1.hours + 15.minutes + 24.seconds
        }

        scenario("one day given, with off-work's") {
            // given
            val workHistory = listOf(
                WorkItem(type = WorkType("type1"),    startTime = easyTimeFormat.parse("2019-10-12 16:00:00")),
                WorkItem(type = WorkType("type2"),    startTime = easyTimeFormat.parse("2019-10-12 17:00:00")),
                WorkItem(type = WorkType("Off Work"), startTime = easyTimeFormat.parse("2019-10-12 18:00:00")),
                WorkItem(type = WorkType("type3"),    startTime = easyTimeFormat.parse("2019-10-12 19:00:00")),
                WorkItem(type = WorkType("Off Work"), startTime = easyTimeFormat.parse("2019-10-12 20:00:00")))

            // when
            val result = workHistory.calculateWorkDuration(
                now = easyTimeFormat.parse("2019-10-12 21:00:00"))

            // then
            result shouldBe 3.hours
        }

        scenario("multiple days given, with off-work's") {
            // given
            val workHistory = listOf(
                WorkItem(type = WorkType("type1"),    startTime = easyTimeFormat.parse("2019-10-02 22:00:00")),
                WorkItem(type = WorkType("Off Work"), startTime = easyTimeFormat.parse("2019-10-02 23:00:00")),
                WorkItem(type = WorkType("type3"),    startTime = easyTimeFormat.parse("2019-10-12 22:00:00")),
                WorkItem(type = WorkType("type4"),    startTime = easyTimeFormat.parse("2019-10-12 23:00:00")),
                WorkItem(type = WorkType("type5"),    startTime = easyTimeFormat.parse("2019-10-13 02:00:00")))

            // when
            val result = workHistory.calculateWorkDuration(
                now = easyTimeFormat.parse("2019-10-13 04:00:00"))

            // then
            result shouldBe 7.hours
        }

        scenario("empty work history") {
            // given
            val workHistory = emptyList<WorkItem>()

            // when
            val result = workHistory.calculateWorkDuration(
                now = easyTimeFormat.parse("2019-10-12 21:00:00"))

            // then
            result shouldBe TimeSpan.ZERO
        }
    }

    feature("${List<WorkItem>::lastIncludingDay.name} function") {
        scenario("multiple days given, given day matches earlier than last work type in history") {
            // given
            val workHistory = listOf(
                WorkItem(type = WorkType("type1"),    startTime = easyTimeFormat.parse("2019-10-02 22:00:00")),
                WorkItem(type = WorkType("Off Work"), startTime = easyTimeFormat.parse("2019-10-02 23:00:00")),
                WorkItem(type = WorkType("type3"),    startTime = easyTimeFormat.parse("2019-10-12 22:00:00")),
                WorkItem(type = WorkType("type4"),    startTime = easyTimeFormat.parse("2019-10-12 23:00:00")),
                WorkItem(type = WorkType("type5"),    startTime = easyTimeFormat.parse("2019-10-13 02:00:00")),
                WorkItem(type = WorkType("type6"),    startTime = easyTimeFormat.parse("2019-10-13 04:00:00")))

            // when
            val result = workHistory.lastIncludingDay(Date(2019, 10, 12))

            // then
            result shouldBe listOf(
                WorkItem(type = WorkType("type3"), startTime = easyTimeFormat.parse("2019-10-12 22:00:00")),
                WorkItem(type = WorkType("type4"), startTime = easyTimeFormat.parse("2019-10-12 23:00:00")),
                WorkItem(type = WorkType("type5"), startTime = easyTimeFormat.parse("2019-10-13 02:00:00")),
                WorkItem(type = WorkType("type6"), startTime = easyTimeFormat.parse("2019-10-13 04:00:00")))
        }

        scenario("multiple days given, given day matches last work type in history") {
            // given
            val workHistory = listOf(
                WorkItem(type = WorkType("type1"),    startTime = easyTimeFormat.parse("2019-10-02 22:00:00")),
                WorkItem(type = WorkType("Off Work"), startTime = easyTimeFormat.parse("2019-10-02 23:00:00")),
                WorkItem(type = WorkType("type3"),    startTime = easyTimeFormat.parse("2019-10-12 22:00:00")),
                WorkItem(type = WorkType("type4"),    startTime = easyTimeFormat.parse("2019-10-12 23:00:00")),
                WorkItem(type = WorkType("type5"),    startTime = easyTimeFormat.parse("2019-10-13 02:00:00")),
                WorkItem(type = WorkType("type6"),    startTime = easyTimeFormat.parse("2019-10-13 04:00:00")))

            // when
            val result = workHistory.lastIncludingDay(Date(2019, 10, 13))

            // then
            result shouldBe listOf(
                WorkItem(type = WorkType("type5"), startTime = easyTimeFormat.parse("2019-10-13 02:00:00")),
                WorkItem(type = WorkType("type6"), startTime = easyTimeFormat.parse("2019-10-13 04:00:00")))
        }

        scenario("multiple days given, given day is later than last work type in history") {
            // given
            val workHistory = listOf(
                WorkItem(type = WorkType("type1"),    startTime = easyTimeFormat.parse("2019-10-02 22:00:00")),
                WorkItem(type = WorkType("Off Work"), startTime = easyTimeFormat.parse("2019-10-02 23:00:00")),
                WorkItem(type = WorkType("type3"),    startTime = easyTimeFormat.parse("2019-10-12 22:00:00")),
                WorkItem(type = WorkType("type4"),    startTime = easyTimeFormat.parse("2019-10-12 23:00:00")),
                WorkItem(type = WorkType("type5"),    startTime = easyTimeFormat.parse("2019-10-13 02:00:00")),
                WorkItem(type = WorkType("type6"),    startTime = easyTimeFormat.parse("2019-10-13 04:00:00")))

            // when
            val result = workHistory.lastIncludingDay(Date(2019, 10, 16))

            // then
            result shouldBe emptyList()
        }

        scenario("empty work history") {
            // given
            val workHistory = emptyList<WorkItem>()

            // when
            val result = workHistory.lastIncludingDay(Date(2019, 10, 13))

            // then
            result shouldBe emptyList()
        }
    }
})
