package it.krzeminski.time9

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.krzeminski.time9.storage.TSVWorkHistoryStorage
import kotlinx.android.synthetic.main.activity_main.*
import android.view.Menu
import android.content.Intent
import androidx.core.content.FileProvider
import android.view.MenuItem
import it.krzeminski.time9.model.WorkType
import it.krzeminski.time9.preferences.MyPreferenceActivity
import android.preference.PreferenceManager
import androidx.lifecycle.Observer
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.toTimeString
import it.krzeminski.time9.utils.PeriodicViewRefresher
import it.krzeminski.time9.viewmodel.WorkTrackingViewModel
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var workTrackingViewModel: WorkTrackingViewModel
    private lateinit var workHistoryFile: File
    private lateinit var periodicViewRefresher: PeriodicViewRefresher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        workHistoryFile = filesDir.resolve("work_history.tsv")
        val workHistoryStorage = TSVWorkHistoryStorage(
            filePath = workHistoryFile.toString(),
            timezoneToRestore = TimeProvider.now().localOffset)
        val workTrackingViewModelFactory = WorkTrackingViewModel.Factory(workHistoryStorage, TimeProvider)

        workTrackingViewModel = workTrackingViewModelFactory.create(
            WorkTrackingViewModel::class.java)

        periodicViewRefresher = PeriodicViewRefresher {
            workTrackingViewModel.recalculateTimes()
        }

        val thisActivity = this
        with(workTrackingViewModel) {
            currentWorkType.observe(thisActivity, Observer { workType ->
                current_work_type.text = workType.name
            })
            numberOfWorkHistoryEntries.observe(thisActivity, Observer { numberOfEntries ->
                number_of_history_entries.text = "Number of history entries: $numberOfEntries"
            })
            workTypes.observe(thisActivity, Observer { workTypes ->
                configureButtons(workTypes, this)
            })
            timeWorkedToday.observe(thisActivity, Observer { timeWorkedToday ->
                time_worked_today.text = timeWorkedToday.toTimeString(components = 3)
            })
        }

        workTrackingViewModel.initializeHistory()
        workTrackingViewModel.initializeWorkTypes(PreferenceManager.getDefaultSharedPreferences(this))
    }

    override fun onResume() {
        super.onResume()
        periodicViewRefresher.start(1000)
    }

    override fun onPause() {
        super.onPause()
        periodicViewRefresher.stop()
    }

    private fun configureButtons(workTypes: List<WorkType>, workTrackingViewModel: WorkTrackingViewModel) {
        val changeWorkTypeButtonIds = listOf(
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
                    workTrackingViewModel.changeCurrentWorkType(workType)
                }
            }
        }

        button_off_work.setOnClickListener {
            workTrackingViewModel.changeToOffWork()
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
}
