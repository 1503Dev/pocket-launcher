package dev1503.pocketlauncher.launcher.pickers

import android.content.pm.PackageInfo
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils.dp2px
import dev1503.pocketlauncher.launcher.widgets.PackageListView

class TextPicker (val activity: AppCompatActivity, val title: String, val defaultText: String = "") {
    lateinit var dialog: AlertDialog
    var onInputFinish: ((String) -> Unit)? = null
    var isSingleLine: Boolean = true

    fun show() {
        val layout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp2px(activity, 22f),
                dp2px(activity, 12f),
                dp2px(activity, 22f),
                0
            )
        }
        val inputLayout = TextInputLayout(activity).apply {
//            setHint(title)
        }
        val input = TextInputEditText(activity).apply {
            setText(defaultText)
            isSingleLine = this@TextPicker.isSingleLine
        }
        inputLayout.addView(input)
        layout.addView(inputLayout)
        dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton(R.string.ok) { _, _ ->
                onInputFinish?.invoke(input.text.toString())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun cancel() {
        if (::dialog.isInitialized) dialog.dismiss()
    }
}
