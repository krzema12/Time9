package it.krzeminski.time9.viewmodel

import android.content.SharedPreferences
import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimeProvider
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
        val timeProvider = mockk<TimeProvider>()
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        every { workHistoryStorage.load() } returns listOf(
            WorkItem(type = WorkType("first"), startTime = DateTimeTz.nowLocal()),
            WorkItem(type = WorkType("second"), startTime = DateTimeTz.nowLocal()))

        // when
        workTrackingViewModel.initializeHistory()

        // then
        workTrackingViewModel.numberOfWorkHistoryEntries.value shouldBe 2
        workTrackingViewModel.currentWorkType.value shouldBe WorkType("second")
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
        val timeProvider = mockk<TimeProvider>()
        val workTrackingViewModel = WorkTrackingViewModel(workHistoryStorage, timeProvider)
        cookArchitectureComponents()
        every { workHistoryStorage.load() } returns emptyList()

        // when
        workTrackingViewModel.initializeHistory()

        // then
        workTrackingViewModel.numberOfWorkHistoryEntries.value shouldBe 0
        workTrackingViewModel.currentWorkType.value shouldBe WorkType("Off Work")
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
