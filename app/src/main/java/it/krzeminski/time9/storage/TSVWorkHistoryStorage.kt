package it.krzeminski.time9.storage

import it.krzeminski.time9.model.WorkItem
import java.io.FileWriter
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TSVWorkHistoryStorage(val filePath: Path) : WorkHistoryStorage() {
    override fun store(workHistory: List<WorkItem>) {
        println(filePath)
        with(FileWriter(filePath.toFile())) {
            appendln(listOf(
                "id", // To be able to refer to some work item from another work item.
                "type",
                "startTime",
                "nextWorkItemId",
                "endTime" // For convenience when analyzing the data in e.g. a spreadsheet.
            ).joinToString("\t"))

            workHistory.forEachIndexed { index, workItem ->
                val nextWorkItemIndex = workItem.nextWorkItem?.let {
                    workHistory.indexOfFirst {
                        it.type == workItem.nextWorkItem.type
                                && it.startTime == workItem.nextWorkItem.startTime
                    }
                }
                val nextWorkItemIndexOrNull = if (nextWorkItemIndex != -1) {
                    nextWorkItemIndex
                } else {
                    null
                }
                appendln(listOf(
                    index.toString(),
                    workItem.type,
                    workItem.startTime.toSpreadsheetFriendlyFormat(),
                    nextWorkItemIndexOrNull ?: "",
                    workItem.nextWorkItem?.startTime?.toSpreadsheetFriendlyFormat() ?: ""
                ).joinToString("\t"))
            }

            flush()
            close()
        }
    }
}

private fun Instant.toSpreadsheetFriendlyFormat() =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(this)
