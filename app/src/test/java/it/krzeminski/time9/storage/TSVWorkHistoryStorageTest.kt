package it.krzeminski.time9.storage

import com.soywiz.klock.*
import io.kotlintest.specs.FeatureSpec
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import it.krzeminski.time9.model.WorkItem
import it.krzeminski.time9.model.WorkType
import java.io.File

class TSVWorkHistoryStorageTest : FeatureSpec({
    feature("store-and-load flow") {
        scenario("the same timezone is used to store and load") {
            // Given
            val tempStorageFile = File.createTempFile("time9", "unittest")
            val storage = TSVWorkHistoryStorage(tempStorageFile.path, TimezoneOffset.invoke(2.hours))
            val storedWorkHistory = listOf(
                WorkItem(type = WorkType("Foo"), startTime = DateTime.fromString("Sat, 08 Sep 2018 04:08:09 GMT+2")),
                WorkItem(type = WorkType("Bar Baz"), startTime = DateTime.fromString("Sat, 08 Dec 2018 14:35:13 GMT+2")))

            // When
            storage.store(storedWorkHistory)
            val loadedWorkHistory = storage.load()

            // Then
            loadedWorkHistory shouldBe storedWorkHistory
        }

        scenario("different timezone is used to store and load") {
            // Given
            val tempStorageFile = File.createTempFile("time9", "unittest")
            val storage = TSVWorkHistoryStorage(tempStorageFile.path, TimezoneOffset.invoke(4.hours))
            val storedWorkHistory = listOf(
                WorkItem(type = WorkType("Foo"), startTime = DateTime.fromString("Sat, 08 Sep 2018 04:08:09 GMT+2")),
                WorkItem(type = WorkType("Bar Baz"), startTime = DateTime.fromString("Sat, 08 Dec 2018 14:35:13 GMT+2")))

            // When
            storage.store(storedWorkHistory)
            val loadedWorkHistory = storage.load()

            // Then
            loadedWorkHistory shouldBe listOf(
                WorkItem(type = WorkType("Foo"), startTime = DateTime.fromString("Sat, 08 Sep 2018 04:08:09 GMT+4")),
                WorkItem(type = WorkType("Bar Baz"), startTime = DateTime.fromString("Sat, 08 Dec 2018 14:35:13 GMT+4")))
        }
    }

    feature("${TSVWorkHistoryStorage::store.name} function") {
        scenario("generic case") {
            // Given
            val tempStorageFile = File.createTempFile("time9", "unittest")
            val storage = TSVWorkHistoryStorage(tempStorageFile.path, TimezoneOffset.invoke(2.hours))
            val workHistory = listOf(
                WorkItem(type = WorkType("Foo"), startTime = DateTime.fromString("Sat, 08 Sep 2018 04:08:09 UTC")),
                WorkItem(type = WorkType("Bar Baz"), startTime = DateTime.fromString("Sat, 08 Dec 2018 14:35:13 GMT+2")))

            // When
            storage.store(workHistory)

            // Then
            tempStorageFile.readLines() shouldBe listOf(
                "type\tstartTime",
                "Foo\t2018-09-08 04:08:09",
                "Bar Baz\t2018-12-08 14:35:13")
        }
    }

    feature("${TSVWorkHistoryStorage::load.name} function") {
        scenario("generic case") {
            // Given
            val tempStorageFile = File.createTempFile("time9", "unittest")
            tempStorageFile.writeText(listOf(
                "type\tstartTime",
                "Foo\t2018-09-08 04:08:09",
                "Bar Baz\t2018-12-08 14:35:13").joinToString("\n"))
            val storage = TSVWorkHistoryStorage(tempStorageFile.path, TimezoneOffset(2.hours))

            // When
            val loadedWorkHistory = storage.load()

            // Then
            loadedWorkHistory shouldBe listOf(
                WorkItem(type = WorkType("Foo"), startTime = DateTime.fromString("Sat, 08 Sep 2018 04:08:09 GMT+2")),
                WorkItem(type = WorkType("Bar Baz"), startTime = DateTime.fromString("Sat, 08 Dec 2018 14:35:13 GMT+2")))
        }

        scenario("file is empty") {
            // Given
            val tempStorageFile = File.createTempFile("time9", "unittest")
            val storage = TSVWorkHistoryStorage(tempStorageFile.path, TimezoneOffset(2.hours))

            // When
            val loadedWorkHistory = storage.load()

            // Then
            loadedWorkHistory shouldBe emptyList()
        }

        scenario("file does not exist") {
            // Given
            val storage = TSVWorkHistoryStorage("some-nonexistent-path", TimezoneOffset(2.hours))

            // When
            val loadedWorkHistory = storage.load()

            // Then
            loadedWorkHistory shouldBe emptyList()
        }

        scenario("incorrect time format is used") {
            // Given
            val tempStorageFile = File.createTempFile("time9", "unittest")
            tempStorageFile.writeText(listOf(
                "type\tstartTime",
                "Bar Baz\t2018-12-08").joinToString("\n"))
            val storage = TSVWorkHistoryStorage(tempStorageFile.path, TimezoneOffset(2.hours))

            // Then
            val exception = shouldThrow<IllegalArgumentException> {
                // When
                storage.load()
            }
            exception.message shouldBe "The time format 2018-12-08 couldn't be parsed!"
        }
    }
})
