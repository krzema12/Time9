package it.krzeminski.time9.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.TimeSpan
import it.krzeminski.time9.logic.calculateWorkDuration
import it.krzeminski.time9.logic.lastIncludingDay
import it.krzeminski.time9.model.WorkItem
import it.krzeminski.time9.model.WorkType
import it.krzeminski.time9.storage.WorkHistoryStorage

class WorkTrackingViewModel(private val workHistoryStorage: WorkHistoryStorage,
                            private val timeProvider: TimeProvider) : ViewModel() {
    val workTypes = MutableLiveData<List<WorkType>>()
    val currentWorkType = MutableLiveData<WorkType>()
    val currentWorkTypeTime = MutableLiveData<TimeSpan>()
    val numberOfWorkHistoryEntries = MutableLiveData<Int>()
    val timeWorkedToday = MutableLiveData<TimeSpan>()
    val workHistory = MutableLiveData<List<WorkItem>>()
    private val offWorkType = WorkType("Off Work")
    // It's needed to hold a reference to this object because in SharedPreferences' implementation,
    // it's held in a WeakHashMap. If this object didn't have a strong reference (below), it would
    // be collected by the garbage collector.
    private lateinit var preferencesOnChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

    fun initializeHistory() {
        workHistory.value = workHistoryStorage.load()
        currentWorkType.value = workHistory.value?.lastOrNull()?.type ?: offWorkType
        numberOfWorkHistoryEntries.value = workHistory.value?.size
        recalculateTimes()
    }

    fun initializeWorkTypes(sharedPreferences: SharedPreferences) {
        workTypes.value = getWorkTypesFromPreferences(sharedPreferences)

        preferencesOnChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            workTypes.value = getWorkTypesFromPreferences(sharedPreferences)
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesOnChangeListener)
    }

    fun changeCurrentWorkType(newWorkType: WorkType) {
        if (newWorkType == currentWorkType.value) {
            return
        }

        workHistory.value = workHistory.value!! + WorkItem(
            type = newWorkType,
            startTime = timeProvider.now().local)
        numberOfWorkHistoryEntries.value = workHistory.value!!.size
        currentWorkType.value = newWorkType

        workHistoryStorage.store(workHistory.value!!)
        recalculateTimes()
    }

    fun changeToOffWork() {
        changeCurrentWorkType(offWorkType)
    }

    fun recalculateTimes() {
        timeWorkedToday.value = (workHistory.value ?: emptyList())
            .lastIncludingDay(timeProvider.now().date)
            .calculateWorkDuration(timeProvider.now().local)
        currentWorkTypeTime.value = workHistory.value?.lastOrNull()?.startTime?.let {
            timeProvider.now().local - it
        } ?: TimeSpan.ZERO
    }

    private fun getWorkTypesFromPreferences(preferences: SharedPreferences): List<WorkType> {
        return (1..9)
            .map { preferences.getString("work_type_slot_$it", "Unset") }
            .map { preferenceValue: String -> WorkType(preferenceValue) }
    }

    class Factory(private val workHistoryStorage: WorkHistoryStorage,
                  private val timeProvider: TimeProvider) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return WorkTrackingViewModel(workHistoryStorage, timeProvider) as T
        }
    }
}
