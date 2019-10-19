package it.krzeminski.time9.logic

import com.soywiz.klock.*
import it.krzeminski.time9.model.WorkItem
import it.krzeminski.time9.model.WorkType

/**
 * For the given work history, returns a sum of worked time until [now], taking into consideration "Off Work".
 */
fun List<WorkItem>.calculateWorkDuration(now: DateTimeTz): TimeSpan {
    // Adding "now" at the end, to be able to calculate duration of the last work history
    // item that is actually passed to the function, in a convenient way.
    val workHistoryWithNow = this + WorkItem(WorkType("Now"), now)

    return TimeSpan(workHistoryWithNow
        .zipWithNext()
        .filter { (first, _) -> first.type != WorkType("Off Work") }
        .map { (first, second) -> second.startTime - first.startTime }
        .sumByDouble { it.milliseconds })
}

/**
 * For the given work history, returns work items from the end, so that the include work items started at [day] and
 * any later ones.
 */
fun List<WorkItem>.lastIncludingDay(day: Date) =
    takeLastWhile { it.startTime.local.date >= day }
