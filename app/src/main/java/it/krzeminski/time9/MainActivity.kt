package it.krzeminski.time9

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import it.krzeminski.time9.model.WorkItem
import it.krzeminski.time9.storage.TSVWorkHistoryStorage
import it.krzeminski.time9.storage.WorkHistoryStorage
import kotlinx.android.synthetic.main.activity_main.*
import android.view.Menu
import android.content.Intent
import android.support.v4.content.FileProvider
import android.view.MenuItem
import android.widget.Button
import it.krzeminski.time9.model.WorkType
import it.krzeminski.time9.preferences.MyPreferenceActivity
import android.preference.PreferenceManager
import com.soywiz.klock.DateTime
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var changeWorkTypeButtonIds: List<Button>
    private lateinit var workHistoryFile: File
    private lateinit var workHistoryStorage: WorkHistoryStorage
    private var workHistory: List<WorkItem> = emptyList()

    private var workTypes: List<WorkType> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        workTypes = (1..9)
            .map { prefs.getString("work_type_slot_$it", null) }
            .map { WorkType(it) }

        configureButtons()

        workHistoryFile = filesDir.resolve("work_history.tsv")
        workHistoryStorage = TSVWorkHistoryStorage(filePath = workHistoryFile.toString())
        workHistory = workHistoryStorage.load()

        number_of_history_entries.text = "Number of history entries: ${workHistory.size}"
        current_work_type.text = workHistory.lastOrNull()?.type?.name ?: "(history empty)"
    }

    private fun configureButtons() {
        changeWorkTypeButtonIds = listOf(
            button_change_work_type_1,
            button_change_work_type_2,
            button_change_work_type_3,
            button_change_work_type_4,
            button_change_work_type_5,
            button_change_work_type_6,
            button_change_work_type_7,
            button_change_work_type_8,
            button_change_work_type_9)

        (workTypes zip changeWorkTypeButtonIds).forEach { (workType, button) ->
            with(button) {
                text = workType.name
                isEnabled = true
                setOnClickListener {
                    onWorkItemChange(workType)
                }
            }
        }

        button_off_work.setOnClickListener {
            onWorkItemChange(WorkType(name = "Off Work"))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export_work_history -> {
                val sendIntent = with(Intent(Intent.ACTION_SEND)) {
                    type = "text/tab-separated-values"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    val workHistoryFileUri = FileProvider.getUriForFile(
                        applicationContext, "${applicationContext.packageName}.fileprovider", workHistoryFile)
                    putExtra(Intent.EXTRA_STREAM, workHistoryFileUri)
                }
                startActivity(Intent.createChooser(sendIntent, "Export work history"))
                true
            }
            R.id.action_preferences -> {
                startActivity(Intent(this, MyPreferenceActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onWorkItemChange(workType: WorkType) {
        workHistory = workHistory + WorkItem(
            type = workType,
            startTime = DateTime.now().local)

        number_of_history_entries.text = "Number of history entries: ${workHistory.size}"
        current_work_type.text = workType.name
        workHistoryStorage.store(workHistory)
    }
}
