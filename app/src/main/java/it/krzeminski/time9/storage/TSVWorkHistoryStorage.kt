package it.krzeminski.time9.storage

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimezoneOffset
import it.krzeminski.time9.model.WorkItem
import it.krzeminski.time9.model.WorkType
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * Handles storing and loading work history to/from a text file given in [filePath].
 *
 * Because the file currently stores the start times only in spreadsheet-friendly format, it doesn't contain any
 * information about timezone. Because of this, when the times are stored, this piece of info is lost. When loading
 * the history, the times are first read as UTC and then [timezoneToRestore] is used to set desired timezone.
 * This may give incorrect behavior when the client works in multiple timezones.
 *
 * XXX [timezoneToRestore] is passed in the constructor, and not in [load] method because it's currently impossible
 * to mock inline classes in mockk library.
 */
class TSVWorkHistoryStorage(val filePath: String, val timezoneToRestore: TimezoneOffset) : WorkHistoryStorage() {
    override fun store(workHistory: List<WorkItem>) {
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
                startTime = fields[1].fromSpreadsheetFriendlyFormatToDateTimeTz(timezoneToRestore))
        }
    }
}

private val dateTimeSpreadsheetFriendlyFormat =
    DateFormat("yyyy-MM-dd HH:mm:ss")

private fun DateTimeTz.toSpreadsheetFriendlyFormat() =
    this.format(dateTimeSpreadsheetFriendlyFormat)

private fun String.fromSpreadsheetFriendlyFormatToDateTimeTz(timezoneToRestore: TimezoneOffset) =
    dateTimeSpreadsheetFriendlyFormat.tryParse(this)?.toOffsetUnadjusted(timezoneToRestore)
        ?: throw IllegalArgumentException("The time format $this couldn't be parsed!")
