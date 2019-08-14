package it.krzeminski.time9

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
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
    }

    override fun onClick(view: View?) {
        if (view is Button) {
            val button: Button = view
            val tag = button.tag
            getWorkItemFromTag(tag)?.let { workItemType ->
                println("Button clicked, tag: $tag, workItemType: $workItemType")
            }
        }
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
