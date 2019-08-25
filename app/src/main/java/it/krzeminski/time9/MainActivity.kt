package it.krzeminski.time9

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import it.krzeminski.time9.model.WorkItem
import it.krzeminski.time9.storage.TSVWorkHistoryStorage
import it.krzeminski.time9.storage.WorkHistoryStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.time.Instant
import android.view.Menu
import android.content.Intent
import android.support.v4.content.FileProvider
import android.view.MenuItem
import java.nio.file.Path


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var workHistoryFilePath: Path
    private lateinit var workHistoryStorage: WorkHistoryStorage
    private var workHistory: List<WorkItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_sprint_work.setOnClickListener(::onClick)
        button_code_review.setOnClickListener(::onClick)
        button_meeting.setOnClickListener(::onClick)
        button_adhoc_request.setOnClickListener(::onClick)
        button_operational.setOnClickListener(::onClick)
        button_design_project_management.setOnClickListener(::onClick)
        button_scrum_mastering.setOnClickListener(::onClick)
        button_recruiting.setOnClickListener(::onClick)
        button_other.setOnClickListener(::onClick)
        button_off_work.setOnClickListener(::onClick)

        workHistoryFilePath = filesDir.toPath().resolve("work_history.tsv")
        workHistoryStorage = TSVWorkHistoryStorage(filePath = workHistoryFilePath)
        workHistory = workHistoryStorage.load()
        println("Loaded:")
        println(workHistory)
        number_of_history_entries.text = "Number of history entries: ${workHistory.size}"
        current_work_type.text = workHistory.lastOrNull()?.type?.split("_")?.joinToString(" ") ?: "(history empty)"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export_work_history -> {
                println("Export")
                val sendIntent = with(Intent(Intent.ACTION_SEND)) {
                    type = "text/tab-separated-values"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    val workHistoryFile = workHistoryFilePath.toFile()
                    val workHistoryFileUri = FileProvider.getUriForFile(
                        applicationContext, "${applicationContext.packageName}.fileprovider", workHistoryFile)
                    putExtra(Intent.EXTRA_STREAM, workHistoryFileUri)
                }
                startActivity(Intent.createChooser(sendIntent, "Export work history"))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(view: View?) {
        if (view is Button) {
            val button: Button = view
            val tag = button.tag
            getWorkItemFromTag(tag)?.let { workItemType ->
                onWorkItemChange(workItemType)
            }
        }
    }

    private fun onWorkItemChange(workItemType: String) {
        println("Work item changed to $workItemType")

        workHistory = workHistory + WorkItem(
            type = workItemType,
            startTime = Instant.now())
        println("Work history")
        println(workHistory)
        number_of_history_entries.text = "Number of history entries: ${workHistory.size}"
        current_work_type.text = workItemType
        workHistoryStorage.store(workHistory)
    }

    private fun getWorkItemFromTag(tag: Any?): String? {
        val tagAsString = tag as String? ?: return null
        val tagParts = tagAsString.split(":")

        return if (tagParts.size == 2 && tagParts[0] == "work_item_type") {
            tagParts[1]
        } else {
            null
        }
    }
}
