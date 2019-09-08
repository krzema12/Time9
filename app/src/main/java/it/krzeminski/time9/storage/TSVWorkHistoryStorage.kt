package it.krzeminski.time9.storage

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import it.krzeminski.time9.model.WorkItem
import it.krzeminski.time9.model.WorkType
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TSVWorkHistoryStorage(val filePath: String) : WorkHistoryStorage() {
    override fun store(workHistory: List<WorkItem>) {
        println(filePath)
        with(FileWriter(filePath)) {
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
        if (!File(filePath).exists()) {
            return emptyList()
        }

        val tsvData = with(FileReader(filePath)) {
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

private val dateTimeSpreadsheetFriendlyFormat =
    DateFormat("yyyy-MM-dd HH:mm:ss")

private fun DateTime.toSpreadsheetFriendlyFormat() =
    this.format(dateTimeSpreadsheetFriendlyFormat)

private fun String.fromSpreadsheetFriendlyFormatToInstant() =
    dateTimeSpreadsheetFriendlyFormat.tryParse(this)?.local
        ?: throw IllegalArgumentException("The time format $this couldn't be parsed!")
