package it.krzeminski.time9.viewmodel

import android.content.SharedPreferences
import com.soywiz.klock.*
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.*
import it.krzeminski.time9.model.WorkItem
import it.krzeminski.time9.model.WorkType
import it.krzeminski.time9.storage.WorkHistoryStorage

class WorkTrackingViewModelTest : StringSpec({
    "loads history and sets fields values when initializeHistory() is called" {
        // given
        val workHistoryStorage = mockk<WorkHistoryStorage>()
        val timeProvider = spyk<TimeProvider>(object : TimeProvider {
            override fun now() = DateTime.fromString("Sat, 08 Sep 2018 04:09:30 Z").local
        })
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        every { workHistoryStorage.load() } returns listOf(
            WorkItem(type = WorkType("first"), startTime = DateTime.fromString("Sat, 08 Sep 2018 04:08:00 Z")),
            WorkItem(type = WorkType("second"), startTime = DateTime.fromString("Sat, 08 Sep 2018 04:09:00 Z")))

        // when
        workTrackingViewModel.initializeHistory()

        // then
        workTrackingViewModel.numberOfWorkHistoryEntries.value shouldBe 2
        workTrackingViewModel.currentWorkType.value shouldBe WorkType("second")
        workTrackingViewModel.timeWorkedToday.value shouldBe 1.minutes + 30.seconds
        verifyAll {
            workHistoryStorage.load()
        }
    }

    "loads history and sets fields values when initializeHistory() is called and last work type is Off Work" {
        // given
        val workHistoryStorage = mockk<WorkHistoryStorage>()
        val timeProvider = spyk<TimeProvider>(object : TimeProvider {
            override fun now() = DateTime.fromString("Sat, 08 Sep 2018 04:09:30 Z").local
        })
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        every { workHistoryStorage.load() } returns listOf(
            WorkItem(type = WorkType("first"), startTime = DateTime.fromString("Sat, 08 Sep 2018 04:08:00 Z")),
            WorkItem(type = WorkType("Off Work"), startTime = DateTime.fromString("Sat, 08 Sep 2018 04:09:00 Z")))

        // when
        workTrackingViewModel.initializeHistory()

        // then
        workTrackingViewModel.numberOfWorkHistoryEntries.value shouldBe 2
        workTrackingViewModel.currentWorkType.value shouldBe WorkType("Off Work")
        workTrackingViewModel.timeWorkedToday.value shouldBe 1.minutes
        verifyAll {
            workHistoryStorage.load()
        }
    }

    "does not load history and set fields values when initializeHistory() is not called" {
        // given
        val workHistoryStorage = mockk<WorkHistoryStorage>()
        val timeProvider = mockk<TimeProvider>()
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        every { workHistoryStorage.load() } returns listOf(
            WorkItem(type = WorkType("first"), startTime = DateTimeTz.nowLocal()),
            WorkItem(type = WorkType("second"), startTime = DateTimeTz.nowLocal()))

        // then
        workTrackingViewModel.numberOfWorkHistoryEntries.value shouldBe null
        workTrackingViewModel.currentWorkType.value shouldBe null
        verifyAll {
            workHistoryStorage wasNot called
        }
    }

    "empty history" {
        // given
        val workHistoryStorage = mockk<WorkHistoryStorage>()
        val timeProvider = spyk<TimeProvider>(object : TimeProvider {
            override fun now() = DateTime.fromString("Sat, 08 Sep 2018 04:09:30 Z").local
        })
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        every { workHistoryStorage.load() } returns emptyList()

        // when
        workTrackingViewModel.initializeHistory()

        // then
        workTrackingViewModel.numberOfWorkHistoryEntries.value shouldBe 0
        workTrackingViewModel.currentWorkType.value shouldBe WorkType("Off Work")
        workTrackingViewModel.timeWorkedToday.value shouldBe TimeSpan.ZERO
        verifyAll {
            workHistoryStorage.load()
        }
    }

    "changeCurrentWorkType when empty history" {
        // given
        val workHistoryStorage = mockk<WorkHistoryStorage>()
        val timeProvider = spyk<TimeProvider>(object : TimeProvider {
            override fun now() = DateTime.fromUnix(123)
        })
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        every { workHistoryStorage.load() } returns emptyList()
        every { workHistoryStorage.store(any()) } just runs
        workTrackingViewModel.initializeHistory()

        // when
        workTrackingViewModel.changeCurrentWorkType(WorkType("newWorkItemType"))

        // then
        workTrackingViewModel.numberOfWorkHistoryEntries.value shouldBe 1
        workTrackingViewModel.currentWorkType.value shouldBe WorkType("newWorkItemType")
        verifyAll {
            workHistoryStorage.load()
            workHistoryStorage.store(listOf(
                WorkItem(type = WorkType("newWorkItemType"), startTime = DateTime.fromUnix(123).local)))
            timeProvider.now()
        }
    }

    "changeCurrentWorkType when non-empty history" {
        // given
        val workHistoryStorage = mockk<WorkHistoryStorage>()
        val timeProvider = spyk<TimeProvider>(object : TimeProvider {
            override fun now() = DateTime.fromUnix(123)
        })
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        every { workHistoryStorage.load() } returns listOf(
            WorkItem(type = WorkType("first"), startTime = DateTimeTz.fromUnixLocal(1)),
            WorkItem(type = WorkType("second"), startTime = DateTimeTz.fromUnixLocal(2)))
        every { workHistoryStorage.store(any()) } just runs
        workTrackingViewModel.initializeHistory()

        // when
        workTrackingViewModel.changeCurrentWorkType(WorkType("newWorkItemType"))

        // then
        workTrackingViewModel.numberOfWorkHistoryEntries.value shouldBe 3
        workTrackingViewModel.currentWorkType.value shouldBe WorkType("newWorkItemType")
        verifyAll {
            workHistoryStorage.load()
            workHistoryStorage.store(listOf(
                WorkItem(type = WorkType("first"), startTime = DateTimeTz.fromUnixLocal(1)),
                WorkItem(type = WorkType("second"), startTime = DateTimeTz.fromUnixLocal(2)),
                WorkItem(type = WorkType("newWorkItemType"), startTime = DateTime.fromUnix(123).local)))
            timeProvider.now()
        }
    }

    "changeCurrentWorkType to the same value as last history item" {
        // given
        val workHistoryStorage = mockk<WorkHistoryStorage>()
        val timeProvider = spyk<TimeProvider>(object : TimeProvider {
            override fun now() = DateTime.fromUnix(123)
        })
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        every { workHistoryStorage.load() } returns listOf(
            WorkItem(type = WorkType("first"), startTime = DateTimeTz.fromUnixLocal(1)),
            WorkItem(type = WorkType("LastHistoryItem"), startTime = DateTimeTz.fromUnixLocal(2)))
        every { workHistoryStorage.store(any()) } just runs
        workTrackingViewModel.initializeHistory()

        // when
        workTrackingViewModel.changeCurrentWorkType(WorkType("LastHistoryItem"))

        // then
        workTrackingViewModel.numberOfWorkHistoryEntries.value shouldBe 2
        workTrackingViewModel.currentWorkType.value shouldBe WorkType("LastHistoryItem")
        verifyAll {
            workHistoryStorage.load()
        }
    }

    "changeToOffWork when non-empty history" {
        // given
        val workHistoryStorage = mockk<WorkHistoryStorage>()
        val timeProvider = spyk<TimeProvider>(object : TimeProvider {
            override fun now() = DateTime.fromUnix(123)
        })
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        every { workHistoryStorage.load() } returns listOf(
            WorkItem(type = WorkType("first"), startTime = DateTimeTz.fromUnixLocal(1)),
            WorkItem(type = WorkType("second"), startTime = DateTimeTz.fromUnixLocal(2)))
        every { workHistoryStorage.store(any()) } just runs
        workTrackingViewModel.initializeHistory()

        // when
        workTrackingViewModel.changeToOffWork()

        // then
        workTrackingViewModel.numberOfWorkHistoryEntries.value shouldBe 3
        workTrackingViewModel.currentWorkType.value shouldBe WorkType("Off Work")
        verifyAll {
            workHistoryStorage.load()
            workHistoryStorage.store(listOf(
                WorkItem(type = WorkType("first"), startTime = DateTimeTz.fromUnixLocal(1)),
                WorkItem(type = WorkType("second"), startTime = DateTimeTz.fromUnixLocal(2)),
                WorkItem(type = WorkType("Off Work"), startTime = DateTime.fromUnix(123).local)))
            timeProvider.now()
        }
    }

    "recalculateTimes when time flows within the same day" {
        val workHistoryStorage = mockk<WorkHistoryStorage>()
        class MockableTimeProvider(var timeToReturn: DateTime) : TimeProvider {
            override fun now() = timeToReturn
        }
        val timeProvider = MockableTimeProvider(timeToReturn = DateTime.fromString("Sat, 08 Sep 2018 04:09:30 Z").local)
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        every { workHistoryStorage.load() } returns listOf(
            WorkItem(type = WorkType("second"), startTime = DateTime.fromString("Sat, 08 Sep 2018 04:09:00 Z")))
        workTrackingViewModel.initializeHistory()
        workTrackingViewModel.timeWorkedToday.value shouldBe 30.seconds

        // When
        timeProvider.timeToReturn = DateTime.fromString("Sat, 08 Sep 2018 04:11:30 Z").local
        workTrackingViewModel.recalculateTimes()

        // Then
        workTrackingViewModel.timeWorkedToday.value shouldBe 2.minutes + 30.seconds
    }

    "recalculateTimes when time flows and a new day comes" {
        val workHistoryStorage = mockk<WorkHistoryStorage>()
        class MockableTimeProvider(var timeToReturn: DateTime) : TimeProvider {
            override fun now() = timeToReturn
        }
        val timeProvider = MockableTimeProvider(timeToReturn = DateTime.fromString("Sat, 08 Sep 2018 23:55:00 Z").local)
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        every { workHistoryStorage.load() } returns listOf(
            WorkItem(type = WorkType("second"), startTime = DateTime.fromString("Sat, 08 Sep 2018 23:30:00 Z")),
            WorkItem(type = WorkType("second"), startTime = DateTime.fromString("Sat, 09 Sep 2018 00:05:00 Z")),
            WorkItem(type = WorkType("second"), startTime = DateTime.fromString("Sat, 09 Sep 2018 00:10:00 Z")))
        workTrackingViewModel.initializeHistory()
        workTrackingViewModel.timeWorkedToday.value shouldBe 25.minutes

        // When
        timeProvider.timeToReturn = DateTime.fromString("Sat, 09 Sep 2018 00:15:00 Z").local
        workTrackingViewModel.recalculateTimes()

        // Then
        // When a new day comes, the work item that started previous day and continues the current day is not divided
        // into two to count the current day's part - it's just not counted. It's a conscious simplificatin for now and
        // should affect only people working at nights. Fixing it is in scope of #22.
        workTrackingViewModel.timeWorkedToday.value shouldBe 10.minutes
    }

    "loads work types from preferences" {
        // given
        val workHistoryStorage = mockk<WorkHistoryStorage>()
        val timeProvider = mockk<TimeProvider>()
        val sharedPreferences = mockk<SharedPreferences>()
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        val preferenceKeySlot = slot<String>()
        every { sharedPreferences.getString(capture(preferenceKeySlot), any()) } answers {
            "Some preference value for ${preferenceKeySlot.captured}" }
        every { sharedPreferences.registerOnSharedPreferenceChangeListener(any()) } just runs

        // when
        workTrackingViewModel.initializeWorkTypes(sharedPreferences)

        // then
        workTrackingViewModel.workTypes.value shouldBe listOf(
            WorkType("Some preference value for work_type_slot_1"),
            WorkType("Some preference value for work_type_slot_2"),
            WorkType("Some preference value for work_type_slot_3"),
            WorkType("Some preference value for work_type_slot_4"),
            WorkType("Some preference value for work_type_slot_5"),
            WorkType("Some preference value for work_type_slot_6"),
            WorkType("Some preference value for work_type_slot_7"),
            WorkType("Some preference value for work_type_slot_8"),
            WorkType("Some preference value for work_type_slot_9"))
        verifyAll {
            sharedPreferences.getString("work_type_slot_1", "Unset")
            sharedPreferences.getString("work_type_slot_2", "Unset")
            sharedPreferences.getString("work_type_slot_3", "Unset")
            sharedPreferences.getString("work_type_slot_4", "Unset")
            sharedPreferences.getString("work_type_slot_5", "Unset")
            sharedPreferences.getString("work_type_slot_6", "Unset")
            sharedPreferences.getString("work_type_slot_7", "Unset")
            sharedPreferences.getString("work_type_slot_8", "Unset")
            sharedPreferences.getString("work_type_slot_9", "Unset")
            sharedPreferences.registerOnSharedPreferenceChangeListener(any())
        }
    }

    "loads work types from preferences if defaults are unset" {
        // given
        val workHistoryStorage = mockk<WorkHistoryStorage>()
        val timeProvider = mockk<TimeProvider>()
        val sharedPreferences = mockk<SharedPreferences>()
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        val preferenceKeySlot = slot<String>()
        every { sharedPreferences.getString(capture(preferenceKeySlot), any()) } answers { "Unset" }
        every { sharedPreferences.registerOnSharedPreferenceChangeListener(any()) } just runs

        // when
        workTrackingViewModel.initializeWorkTypes(sharedPreferences)

        // then
        workTrackingViewModel.workTypes.value shouldBe listOf(
            WorkType("Unset"),
            WorkType("Unset"),
            WorkType("Unset"),
            WorkType("Unset"),
            WorkType("Unset"),
            WorkType("Unset"),
            WorkType("Unset"),
            WorkType("Unset"),
            WorkType("Unset"))
        verifyAll {
            sharedPreferences.getString("work_type_slot_1", "Unset")
            sharedPreferences.getString("work_type_slot_2", "Unset")
            sharedPreferences.getString("work_type_slot_3", "Unset")
            sharedPreferences.getString("work_type_slot_4", "Unset")
            sharedPreferences.getString("work_type_slot_5", "Unset")
            sharedPreferences.getString("work_type_slot_6", "Unset")
            sharedPreferences.getString("work_type_slot_7", "Unset")
            sharedPreferences.getString("work_type_slot_8", "Unset")
            sharedPreferences.getString("work_type_slot_9", "Unset")
            sharedPreferences.registerOnSharedPreferenceChangeListener(any())
        }
    }
})
