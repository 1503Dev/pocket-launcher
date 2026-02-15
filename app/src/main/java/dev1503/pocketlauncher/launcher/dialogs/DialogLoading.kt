package dev1503.pocketlauncher.launcher.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils.dp2px

class DialogLoading(val context: Context, val title: String, val indicatorType: Int, val cancelable: Boolean = false) {
    companion object {
        const val TYPE_LINEAR_DETERMINATE_PROGRESS = 0
        const val TYPE_LINEAR_INDETERMINATE_PROGRESS = 1
        const val TYPE_CIRCULAR = 2
    }

    lateinit var layout: LinearLayout
    lateinit var textView: TextView
    lateinit var detailsTextView: TextView
    lateinit var linearProgressIndicator: LinearProgressIndicator
    lateinit var circularProgressIndicator: CircularProgressIndicator

    var text: String = ""
        set(value) {
            field = value
            textView.text = value
        }
    var details: String = ""
        set(value) {
            field = value
            detailsTextView.text = value
        }
    var progress: Int = 0
        set(value) {
            field = value
            linearProgressIndicator.progress = value
        }
    var onCancel: (() -> Unit)? = null

    val dialogBuilder = MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setCancelable(false)
    lateinit var dialog: AlertDialog

    @SuppressLint("InflateParams")
    fun init(): DialogLoading {
        when (indicatorType) {
            TYPE_LINEAR_DETERMINATE_PROGRESS -> {
                layout = LinearLayout.inflate(context, R.layout.dialog_loading_linear_progress, null) as LinearLayout
                textView = layout.findViewWithTag<TextView>("text")
                linearProgressIndicator = layout.findViewWithTag<LinearProgressIndicator>("progress")
            }
            TYPE_LINEAR_INDETERMINATE_PROGRESS -> {
                layout = LinearLayout.inflate(context, R.layout.dialog_loading_linear_progress, null) as LinearLayout
                textView = layout.findViewWithTag<TextView>("text")
                linearProgressIndicator = layout.findViewWithTag<LinearProgressIndicator>("progress")
                linearProgressIndicator.isIndeterminate = true
            }
            TYPE_CIRCULAR -> {
                layout = LinearLayout.inflate(context, R.layout.dialog_loading_circular_progress, null) as LinearLayout
                textView = layout.findViewWithTag<TextView>("text")
                circularProgressIndicator = layout.findViewWithTag<CircularProgressIndicator>("progress")
                dialogBuilder.setTitle(null)
                layout.findViewWithTag<TextView>("title").text = title
            }
        }
        if (cancelable) {
            dialogBuilder.setNeutralButton(R.string.cancel, { dialog, which ->
                onCancel?.invoke()
            })
        } else {
            layout.setPadding(
                layout.paddingLeft,
                layout.paddingTop,
                layout.paddingRight,
                dp2px(context, 24f)
            )
        }
        dialogBuilder.setView(layout)
        return this
    }

    fun show() {
        dialog = dialogBuilder.show()
    }

    fun cancel() {
        dialog.cancel()
    }
}