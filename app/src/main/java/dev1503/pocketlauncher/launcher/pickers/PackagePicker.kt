package dev1503.pocketlauncher.launcher.pickers

import android.content.pm.PackageInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.launcher.widgets.PackageListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PackagePicker (val activity: AppCompatActivity, val packageInfoList: List<PackageInfo>) {
    var dialog: AlertDialog? = null
    var onPackageSelected: ((PackageInfo) -> Unit)? = null
    var onShow: (() -> Unit)? = null

    fun show() {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val view = PackageListView(activity)
            view.packageList = packageInfoList
            view.onPackageSelected = { packageInfo ->
                onPackageSelected?.invoke(packageInfo)
                cancel()
            }
            activity.runOnUiThread {
                dialog = MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.select_an_application))
                    .setView(view)
                    .show()
                onShow?.invoke()
            }
        }
    }

    fun cancel() {
        dialog?.dismiss()
    }
}
