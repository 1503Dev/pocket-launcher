package dev1503.pocketlauncher.launcher.fragments

import android.content.pm.PackageInfo
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.launcher.dialogs.DialogLoading
import dev1503.pocketlauncher.launcher.pickers.PackagePicker
import dev1503.pocketlauncher.launcher.pickers.TextPicker
import dev1503.pocketlauncher.launcher.widgets.ColumnLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.Locale.getDefault

class FragmentDownload (self: AppCompatActivity) : Fragment(self, ColumnLayout(self), "FragmentDownload") {
    val TAG = "FragmentDownload"
    val columnLayout: ColumnLayout = layout as ColumnLayout

    lateinit var itemGames: ColumnLayout.ColumnLayoutItem

    @Override
    override fun init(): Boolean {
        val superResult = super.init()

        if (superResult) {
            columnLayout.addDivider(self.getString(R.string.new_game))
            itemGames = columnLayout.addItem(self.getString(R.string.games), R.drawable.stadia_controller_24px, true)

            columnLayout.setContentLayout(R.layout.layout_launcher_download_games)
            val btnInstallFromDeviceInstalled = columnLayout.findViewWithTag<View>("btn_install_from_device_installed")
            btnInstallFromDeviceInstalled.setOnClickListener {
                installFromDeviceInstalled()
            }
        }
        itemGames.checked = true

        return superResult
    }

    fun installFromDeviceInstalled() {
        val packageInfoList = Utils.getAllMCPossiblePackages(self)
        if (packageInfoList == null) {
            Snackbar.make(layout, "Error", Snackbar.LENGTH_SHORT).show()
            return
        }
        PackagePicker(self, packageInfoList).apply {
            onPackageSelected = { packageInfo ->
                val appName = packageInfo.applicationInfo?.loadLabel(self.packageManager).toString()
                val appVersion = packageInfo.versionName
                TextPicker(self, self.getString(R.string.instance_name), "${appName} v${appVersion}".trim()).apply {
                    onInputFinish = { instanceName ->
                        if (!Utils.testFileName(instanceName)) {
                            Snackbar.make(layout, self.getString(R.string.invalid_instance_name), Snackbar.LENGTH_LONG).show()
                        } else if (Utils.isInstanceExist(context, instanceName)) {
                            Snackbar.make(layout, self.getString(R.string.instance_name_is_used), Snackbar.LENGTH_LONG).show()
                        } else {
                            installFromDeviceInstalled(packageInfo, instanceName)
                        }
                    }
                }.show()
            }
        }.show()
    }

    fun installFromDeviceInstalled(packageInfo: PackageInfo, instanceName: String) {
        val versionName = packageInfo.versionName
        var versionCode: Long = 0
        if (Build.VERSION.SDK_INT >= 28) {
            versionCode = packageInfo.longVersionCode
        } else versionCode = packageInfo.versionCode.toLong()
        val dialogLoading = DialogLoading(
            self, self.getString(R.string.installing),
            indicatorType = DialogLoading.TYPE_LINEAR_DETERMINATE_PROGRESS
        ).init()
        dialogLoading.progress = 0
        dialogLoading.text = self.getString(R.string.init_installation_program)
        dialogLoading.show()
        var lastProgress = -1
        self.lifecycleScope.launch (Dispatchers.IO) {
            val root = Utils.getInstancesDirPath(context) + instanceName
            File(root).mkdirs()
            val sourcePath = packageInfo.applicationInfo?.publicSourceDir
            dialogLoading.text = self.getString(R.string.verifying_entity_files)
            val sourceSha1 = Utils.fileSHA1(sourcePath, { progress ->
                        uiRun {
                            dialogLoading.progress = (progress * 20).toInt()
                        }
                    }).lowercase(getDefault())
            Log.i(TAG, "sourcePath: $sourcePath, sourceSha1: $sourceSha1")

            fun installFinish(failed: Boolean = false) {
                if (!failed) {
                    uiRun {
                        dialogLoading.progress = 99
                        dialogLoading.text = self.getString(R.string.creating_manifest)
                    }
                    try {
                        val manifest = JsonObject()
                        manifest.addProperty("version", 1)
                        val manifestInstance = JsonObject()
                        manifestInstance.addProperty("type", "full_apk")
                        manifestInstance.addProperty("entity", sourceSha1)
                        manifestInstance.addProperty("version_name",versionName)
                        manifestInstance.addProperty("version_code",versionCode)
                        manifestInstance.addProperty("install_time",System.currentTimeMillis())
                        manifestInstance.addProperty("source", "device_installed")
                        manifest.add("instance", manifestInstance)

                        val config = JsonObject()
                        config.addProperty("version", 1)
                        config.addProperty("data_isolation", true)
                        config.addProperty("data_storage_dir", ":INSTANCE/data/")
                        if (Utils.fileWriteString("$root/manifest.json", manifest.toString()) &&
                            Utils.fileWriteString("$root/config.json", config.toString()) &&
                            Utils.isInstanceEntityExist(context, "apk", sourceSha1)
                        ) {
                            uiRun {
                                dialogLoading.cancel()
                                MaterialAlertDialogBuilder(self)
                                    .setTitle(R.string.install_success)
                                    .setNegativeButton(R.string.ok, { dialog, which -> })
                                    .show()
                            }
                            return
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, e)
                    }
                }
                Log.e(TAG, "Failed to install")
                Utils.fileRemove(root)
                uiRun {
                    dialogLoading.cancel()
                    MaterialAlertDialogBuilder(self)
                        .setTitle(R.string.install_failed)
                        .setNegativeButton(R.string.ok, { dialog, which -> })
                        .show()
                }
            }

            if (Utils.isInstanceEntityExist(context, "apk", sourceSha1)) {
                installFinish()
                return@launch
            }
            val destPath = Utils.getInstanceEntitiesDirPath(context, "apk") + sourceSha1 + ".apk"
            uiRun { dialogLoading.progress = 20 }
            try {
                Utils.copyFile(sourcePath, destPath, { progress, copiedSize, totalSize ->
                    if (progress >= 100) {
                        installFinish()
                    } else if (progress != lastProgress) {
                        uiRun {
                            dialogLoading.progress = 20 + (progress / 100f * 79).toInt()
                            dialogLoading.text = self.getString(R.string.copying) + " ${copiedSize/1024/1024} MB/${totalSize/1024/1024} MB"
                        }
                    }
                    lastProgress = progress
                })
            } catch (e: Exception) {
                Log.e(TAG, e)
                installFinish(true)
                Utils.fileRemove(destPath)
            }
        }
    }
}