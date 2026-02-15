package dev1503.pocketlauncher.launcher.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Build
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import dev1503.pocketlauncher.InstanceInfo
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.launcher.MainActivity
import dev1503.pocketlauncher.launcher.dialogs.DialogLoading
import dev1503.pocketlauncher.launcher.pickers.PackagePicker
import dev1503.pocketlauncher.launcher.pickers.TextPicker
import dev1503.pocketlauncher.launcher.widgets.ColumnLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.util.Locale.getDefault
import java.util.UUID
import kotlin.random.Random

class FragmentDownload (self: MainActivity) : Fragment(self, ColumnLayout(self), "FragmentDownload") {
    val mainActivity: MainActivity = self
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
            columnLayout.findViewWithTag<View>("btn_install_from_device_installed").setOnClickListener {
                installFromDeviceInstalled()
            }
            columnLayout.findViewWithTag<View>("btn_install_from_apk_file").setOnClickListener {
                installFromApkFile()
            }
        }
        itemGames.checked = true

        return superResult
    }

    fun installFromDeviceInstalled() {
        var packageInfoList = Utils.getAllMCPossiblePackages(self)
        if (packageInfoList == null) {
            packageInfoList = Utils.getAllUserPackages(self)!!
        }
        var dialog: DialogLoading? = null
        if (packageInfoList.size > 15) {
            dialog = DialogLoading(self, self.getString(R.string.loading), DialogLoading.TYPE_CIRCULAR).init()
            dialog.text = self.getString(R.string.loading_packages)
            dialog.show()
        }
        PackagePicker(self, packageInfoList).apply {
            onPackageSelected = { packageInfo ->
                val appName = packageInfo.applicationInfo?.loadLabel(self.packageManager).toString()
                val appVersion = packageInfo.versionName
                TextPicker(self, self.getString(R.string.instance_name), "${appName} v${appVersion}".trim()).apply {
                    onInputFinish = { instanceName ->
                        if (!Utils.testFileName(instanceName)) {
                            mainActivity.snack(R.string.invalid_instance_name, Snackbar.LENGTH_LONG)
                        } else if (Utils.isInstanceExist(context, instanceName)) {
                            mainActivity.snack(R.string.instance_name_is_used, Snackbar.LENGTH_LONG)
                        } else {
                            installFromDeviceInstalled(packageInfo, instanceName)
                        }
                    }
                }.show()
            }
            onShow = {
                dialog?.cancel()
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
                        val apkArch = Utils.getApkArch(sourcePath?: "")
                        val instanceInfo = InstanceInfo(
                            name = instanceName,
                            versionName = versionName?:"UNKNOWN",
                            versionCode = versionCode,
                            installTime = System.currentTimeMillis(),
                            source = "device_installed",
                            entityType = "full_apk",
                            entity = sourceSha1,
                            arch = apkArch,
                            context = context,
                            dirPath = "$root/",
                            initKv = false,
                        )

                        val config = JsonObject()
                        config.addProperty("version", 1)
                        config.addProperty("data_isolation", true)
                        config.addProperty("data_storage_dir", ":INSTANCE/data/")
                        if (Utils.fileWriteString("$root/manifest.json", instanceInfo.toJsonString()) &&
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
                Utils.fileCopy(sourcePath, destPath, { progress, copiedSize, totalSize ->
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

    fun installFromApkFile() {
        var permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permission = Manifest.permission.MANAGE_EXTERNAL_STORAGE
        }
        mainActivity.requestPermission(self, permission, { code ->
            if (code >= 1 || Utils.checkPermission(self, permission)) {
                installFromApkFileNext()
            } else {
                Log.w(TAG, "Failed to request permission")
                alert(R.string.you_need_to_grant_permission_to_continue, R.string.information)
            }
        })

        val cacheDir = Utils.getADirIPath(self, "cache/launcher/file_stream")
        Utils.fileRemove(cacheDir)
    }

    fun installFromApkFileNext() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/vnd.android.package-archive", "application/octet-stream"))
        }
        val code = Random.nextInt(10001, 20000)
        mainActivity.requestCodeFileCallback[code] = {fn, inputStream ->
            if (fn.lowercase().endsWith(".apk") || fn.lowercase().endsWith(".apk.1")) {
                installFromApkFileNext(inputStream)
            } else {
                alert(R.string.invalid_file_type, R.string.information)
            }
        }
        self.startActivityForResult(intent, code)
    }

    fun installFromApkFileNext(inputStream: InputStream) {
        val dialogLoading = DialogLoading(
            self, self.getString(R.string.installing),
            indicatorType = DialogLoading.TYPE_LINEAR_INDETERMINATE_PROGRESS
        ).init()
        dialogLoading.progress = 0
        dialogLoading.text = self.getString(R.string.init_installation_program)
        dialogLoading.show()
        self.lifecycleScope.launch(Dispatchers.IO) {
            try {
                uiRun { dialogLoading.text = self.getString(R.string.caching_apk) }
                val cacheDir = Utils.getADirIPath(self, "cache/launcher/file_stream")
                var apkPath = "$cacheDir${UUID.randomUUID()}.apk"
                Utils.fileCopy(inputStream, apkPath)
                uiRun {
                    dialogLoading.text = self.getString(R.string.verifying_entity_files)
                    dialogLoading.linearProgressIndicator.isIndeterminate = false
                }
                val sourceSha1 = Utils.fileSHA1(apkPath, { progress ->
                    uiRun {
                        dialogLoading.progress = (progress * 20).toInt()
                    }
                })
                Log.d(TAG, "sourceSha1: $sourceSha1")

                fun installFinish(failed: Boolean = false, apkInfo: Utils.ApkInfo?, instanceName: String? = null, isDialogCanceled: Boolean = true) {
                    if (!failed && apkInfo != null && instanceName != null) {
                        val root = Utils.getInstancesDirPath(self) + instanceName
                        try {
                            if (!isDialogCanceled) {
                                val path = Utils.getInstanceEntitiesDirPath(self, "apk")
                                self.lifecycleScope.launch(Dispatchers.IO) {
                                    var lastProgress = -1
                                    Utils.fileCopy(apkPath, "$path$sourceSha1.apk", { progress, copiedSize, totalSize ->
                                        if (progress >= 100) {
                                            apkPath = "$path$sourceSha1.apk"
                                            Utils.fileRemove(cacheDir)
                                            uiRun { dialogLoading.cancel() }
                                            installFinish(false, apkInfo, instanceName)
                                        } else if (progress != lastProgress) {
                                            uiRun {
                                                dialogLoading.progress = 20 + (progress / 100f * 79).toInt()
                                                dialogLoading.text = self.getString(R.string.copying) + " ${copiedSize/1024/1024} MB/${totalSize/1024/1024} MB"
                                            }
                                        }
                                        lastProgress = progress
                                    })
                                }
                                return
                            }
                            val apkArch = Utils.getApkArch(apkPath)
                            val instanceInfo = InstanceInfo(
                                name = instanceName,
                                versionName = apkInfo.versionName,
                                versionCode = apkInfo.versionCode,
                                installTime = System.currentTimeMillis(),
                                source = "apk_file",
                                entityType = "full_apk",
                                entity = sourceSha1,
                                arch = apkArch,
                                context = context,
                                dirPath = "$root/",
                                initKv = false,
                            )

                            val config = JsonObject()
                            config.addProperty("version", 1)
                            config.addProperty("data_isolation", true)
                            config.addProperty("data_storage_dir", ":INSTANCE/data/")
                            if (Utils.fileWriteString("$root/manifest.json", instanceInfo.toJsonString()) &&
                                Utils.fileWriteString("$root/config.json", config.toString()) &&
                                Utils.isInstanceEntityExist(context, "apk", sourceSha1)
                            ) {
                                uiRun {
                                    MaterialAlertDialogBuilder(self)
                                        .setTitle(R.string.install_success)
                                        .setNegativeButton(R.string.ok, { dialog, which -> })
                                        .show()
                                }
                                return
                            }
                        } catch (e: Exception) {
                            Utils.fileRemove(root)
                            Log.e(TAG, e)
                        }
                    }
                    Log.e(TAG, "Failed to install")
                    Utils.fileRemove(cacheDir)
                    Utils.fileRemove(apkPath)
                    uiRun {
                        MaterialAlertDialogBuilder(self)
                            .setTitle(R.string.install_failed)
                            .setNegativeButton(R.string.ok, { dialog, which -> })
                            .show()
                    }
                }

                if (Utils.isInstanceEntityExist(context, "apk", sourceSha1)) {
                    val apkPath = Utils.getInstanceEntitiesDirPath(self, "apk") + sourceSha1 + ".apk"
                    val apkInfo = Utils.getApkInfo(apkPath, self.packageManager)
                    uiRun {
                        dialogLoading.cancel()
                        TextPicker(self, self.getString(R.string.instance_name), "${apkInfo.appName} v${apkInfo.versionName}".trim()).apply {
                            onInputFinish = { instanceName ->
                                if (!Utils.testFileName(instanceName)) {
                                    mainActivity.snack(R.string.invalid_instance_name, Snackbar.LENGTH_LONG)
                                    dialogLoading.cancel()
                                } else if (Utils.isInstanceExist(context, instanceName)) {
                                    mainActivity.snack(R.string.instance_name_is_used, Snackbar.LENGTH_LONG)
                                    dialogLoading.cancel()
                                } else {
                                    installFinish(false, apkInfo, instanceName)
                                }
                            }
                        }.show()
                    }
                    return@launch
                } else {
                    val apkInfo = Utils.getApkInfo(apkPath, self.packageManager)
                    uiRun {
                        TextPicker(self, self.getString(R.string.instance_name), "${apkInfo.appName} v${apkInfo.versionName}".trim()).apply {
                            onInputFinish = { instanceName ->
                                if (!Utils.testFileName(instanceName)) {
                                    mainActivity.snack(R.string.invalid_instance_name, Snackbar.LENGTH_LONG)
                                    dialogLoading.cancel()
                                } else if (Utils.isInstanceExist(context, instanceName)) {
                                    mainActivity.snack(R.string.instance_name_is_used, Snackbar.LENGTH_LONG)
                                    dialogLoading.cancel()
                                } else {
                                    installFinish(false, apkInfo, instanceName, false)
                                }
                            }
                            onInputCanceled = {
                                dialogLoading.cancel()
                            }
                        }.show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e)
                try {
                    dialogLoading.cancel()
                } catch (_: Exception) {}
                alert(Log.getStackTraceString(e), R.string.install_failed)
            }
        }
    }
}