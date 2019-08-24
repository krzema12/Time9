package it.krzeminski.time9.storage

import it.krzeminski.time9.model.WorkItem
import it.krzeminski.time9.model.WorkType
import java.io.FileReader
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
                    workItem.type.name,
                    workItem.startTime.toSpreadsheetFriendlyFormat()
                ).joinToString("\t"))
            }

            flush()
            close()
        }
    }

    override fun load(): List<WorkItem> {
        if (!filePath.toFile().exists()) {
            return emptyList()
        }

        val tsvData = with(FileReader(filePath.toFile())) {
            readLines().drop(1) // Header.
        }

        return tsvData.map { line ->
            val fields = line.split("\t")
            WorkItem(
                type = WorkType(name = fields[0]),
                startTime = fields[1].fromSpreadsheetFriendlyFormatToInstant())
        }
    }
}

private val dateTimeSpreadsheetFriendlyFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

private fun Instant.toSpreadsheetFriendlyFormat() =
    dateTimeSpreadsheetFriendlyFormatter.format(this)

private fun String.fromSpreadsheetFriendlyFormatToInstant() =
    Instant.from(dateTimeSpreadsheetFriendlyFormatter.parse(this))
