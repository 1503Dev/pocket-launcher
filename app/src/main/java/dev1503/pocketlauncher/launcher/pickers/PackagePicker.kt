package dev1503.pocketlauncher.launcher.pickers

import android.content.pm.PackageInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.launcher.widgets.PackageListView

class PackagePicker (val activity: AppCompatActivity, val packageInfoList: List<PackageInfo>) {
    lateinit var dialog: AlertDialog
    var onPackageSelected: ((PackageInfo) -> Unit)? = null

    fun show() {
        val view = PackageListView(activity)
        view.packageList = packageInfoList
        view.onPackageSelected = { packageInfo ->
            onPackageSelected?.invoke(packageInfo)
            cancel()
        }
        dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(activity.getString(R.string.select_an_application))
            .setView(view)
            .show()
    }

    fun cancel() {
        if (::dialog.isInitialized) dialog.dismiss()
    }
}
