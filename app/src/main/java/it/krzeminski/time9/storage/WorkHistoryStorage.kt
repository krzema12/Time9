package it.krzeminski.time9.storage

import it.krzeminski.time9.model.WorkItem

abstract class WorkHistoryStorage {
    abstract fun store(workHistory: List<WorkItem>)
}
