package it.krzeminski.time9.model

import java.time.Instant

data class WorkItem(
    val type: String,
    val startTime: Instant,
    val nextWorkItem: WorkItem? = null)
