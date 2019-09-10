package it.krzeminski.time9.model

import com.soywiz.klock.DateTimeTz

data class WorkItem(
    val type: WorkType,
    val startTime: DateTimeTz
)
