package it.krzeminski.time9.model

import java.time.Instant

data class WorkItem(
    val type: WorkType,
    val startTime: Instant)
