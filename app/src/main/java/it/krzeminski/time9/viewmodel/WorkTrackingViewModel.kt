package it.krzeminski.time9.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.soywiz.klock.TimeProvider
import it.krzeminski.time9.model.WorkItem
import it.krzeminski.time9.model.WorkType
import it.krzeminski.time9.storage.WorkHistoryStorage

class WorkTrackingViewModel(private val workHistoryStorage: WorkHistoryStorage,
                            private val timeProvider: TimeProvider) : ViewModel() {
    val currentWorkType = MutableLiveData<WorkType>()
    val numberOfWorkHistoryEntries = MutableLiveData<Int>()
    private val workHistory = MutableLiveData<List<WorkItem>>()
    private val offWorkType = WorkType("Off Work")

    fun initializeHistory() {
        workHistory.value = workHistoryStorage.load()
        currentWorkType.value = workHistory.value?.lastOrNull()?.type ?: offWorkType
        numberOfWorkHistoryEntries.value = workHistory.value?.size
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
    }

    fun changeToOffWork() {
        changeCurrentWorkType(offWorkType)
    }

    class Factory(private val workHistoryStorage: WorkHistoryStorage,
                  private val timeProvider: TimeProvider) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return WorkTrackingViewModel(workHistoryStorage, timeProvider) as T
        }
    }
}
