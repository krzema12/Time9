package it.krzeminski.time9.model

import com.soywiz.klock.DateTime

data class WorkItem(
    val type: WorkType,
    val startTime: DateTime
)
