package it.krzeminski.time9.viewmodel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import it.krzeminski.time9.R
import it.krzeminski.time9.model.WorkItem
import kotlinx.android.synthetic.main.work_item.view.*

class WorkItemAdapter(private var workHistory: List<WorkItem>) : RecyclerView.Adapter<WorkItemAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workType: TextView = itemView.work_type
        val startTime: TextView = itemView.start_time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        with (LayoutInflater.from(parent.context)) {
            return ViewHolder(inflate(R.layout.work_item, parent, false))
        }
    }

    override fun getItemCount() = workHistory.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workItemAtPosition = workHistory[workHistory.size - position - 1]
        holder.workType.text = workItemAtPosition.type.name
        holder.startTime.text = workItemAtPosition.startTime.toHumanFriendlyFormat()
    }

    fun setWorkHistory(workHistory: List<WorkItem>) {
        this.workHistory = workHistory
        this.notifyDataSetChanged()
    }

    private fun DateTimeTz.toHumanFriendlyFormat() = this.format(humanFriendlyDateTimeFormat)

    private val humanFriendlyDateTimeFormat = DateFormat("yyyy-MM-dd HH:mm:ss")
}
