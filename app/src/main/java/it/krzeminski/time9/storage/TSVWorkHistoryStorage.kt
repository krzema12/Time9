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
                "type",
                "startTime"
            ).joinToString("\t"))

            workHistory.forEach { workItem ->
                appendln(listOf(
                    workItem.type,
                    workItem.startTime.toSpreadsheetFriendlyFormat()
                ).joinToString("\t"))
            }

            flush()
            close()
        }
    }
}

private fun Instant.toSpreadsheetFriendlyFormat() =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(this)
