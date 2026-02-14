package dev1503.pocketlauncher.launcher.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev1503.pocketlauncher.HttpUtils
import dev1503.pocketlauncher.InstanceInfo
import dev1503.pocketlauncher.KVConfig
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
//import dev1503.pocketlauncher.common.R as CR
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.Utils.kvGlobalGameConfig
import dev1503.pocketlauncher.Utils.kvLauncherSettings
import dev1503.pocketlauncher.XboxAPI
import dev1503.pocketlauncher.dexbridge.BridgeA
import dev1503.pocketlauncher.dexbridge.BridgeB
import dev1503.pocketlauncher.dexbridge.MinecraftActivity
import dev1503.pocketlauncher.launcher.MainActivity
import dev1503.pocketlauncher.launcher.MainActivity.Companion.TAG
import dev1503.pocketlauncher.launcher.widgets.ColumnLayout
import dev1503.pocketlauncher.modloader.ModInfo
import dev1503.pocketlauncher.modloader.ModLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.util.zip.ZipFile

class FragmentMain (self: AppCompatActivity) : Fragment(self, ColumnLayout(self), "FragmentMain") {
    val TAG = "FragmentMain"

    private lateinit var itemAccount: ColumnLayout.ColumnLayoutItem
    private lateinit var itemEditInstance: ColumnLayout.ColumnLayoutItem
    private val activity: MainActivity = self as MainActivity
    val columnLayout: ColumnLayout = layout as ColumnLayout
    lateinit var btnLaunchInstanceName: TextView

    @Override
    override fun init(): Boolean {
        val superResult = super.init()
        if (!superResult) return false

        columnLayout.addDivider(self.getString(R.string.accounts))
        itemAccount = columnLayout.addItem(
            self.getString(R.string.accounts),
            R.drawable.person_24px,
            false,
            self.getString(R.string.not_logged_in)
        )
        columnLayout.addDivider(self.getString(R.string.games))
        itemEditInstance = columnLayout.addItem(
            self.getString(R.string.edit_instance),
            R.drawable.deployed_code_24px,
        )
        itemEditInstance.onClick = View.OnClickListener {
            editInstance()
        }
        columnLayout.addItem(
            self.getString(R.string.all_instances),
            R.drawable.format_list_bulleted_24px
        ).onClick = View.OnClickListener {
            activity.switchFragment("all_instances")
        }
        columnLayout.addItem(
            self.getString(R.string.download),
            R.drawable.download_24px,
        ).onClick = View.OnClickListener {
            activity.switchFragment("download")
        }

        columnLayout.setContentLayout(View.inflate(self, R.layout.layout_launcher_main, null) as ViewGroup)

        columnLayout.contentContainer.findViewWithTag<View>("btn_select_instance")?.setOnClickListener { v-> selectInstance(v)}
        val btnLaunch = columnLayout.contentContainer.findViewWithTag<View>("btn_launch")
        btnLaunch.setOnClickListener {
            try {
                val instanceInfo = Utils.getInstanceInfo(self, kvLauncherSettings?.getString("instance", ""), true)
                if (instanceInfo == null) {
                    Snackbar.make(layout, R.string.no_instance_selected, Snackbar.LENGTH_SHORT).show()
                } else {
                    launch(instanceInfo)
                }
            } catch (e: Exception) {
                Log.e(TAG, e)
            }
        }
        btnLaunchInstanceName = btnLaunch.findViewWithTag<TextView>("instance_name")!!

        updateInstances()
        initMsAccount()
        return true
    }
    fun updateInstances() {
        self.lifecycleScope.launch(Dispatchers.IO) {
            val instanceInfo = Utils.getSelectedInstance(self)
            uiRun {
                if (instanceInfo == null) {
                    btnLaunchInstanceName.text = self.getString(R.string.no_instance_selected)
                    itemEditInstance.setDescription(self.getString(R.string.no_instance_selected))
                    itemEditInstance.setIconBig(R.drawable.deployed_code_24px)
                } else {
                    btnLaunchInstanceName.text = instanceInfo.name
                    itemEditInstance.setDescription(instanceInfo.name ?: "")
                    val icon = instanceInfo.iconBitmap
                    if (icon != null) {
                        itemEditInstance.setIconBig(icon)
                    } else {
                        itemEditInstance.setIconBig(R.drawable.deployed_code_24px)
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun initMsAccount() {
        itemAccount.setIconBig(R.drawable.person_24px)
        itemAccount.setTitle(self.getString(R.string.accounts))
        itemAccount.setDescription(self.getString(R.string.not_logged_in))

        Utils.getCurrentXalIdRx(context)?.subscribe { xalId ->
            if (xalId == null) return@subscribe
            handleXalId(xalId)
        }
    }

    private fun handleXalId(xalId: String) {
        try {
            val id = Gson().fromJson(xalId, JsonObject::class.java)["default"].asString
            Utils.searchFilesWithContent(
                Utils.getXalDirPath(self),
                id,
                object : Utils.FilesSearchWithContentListener {
                    override fun onSearchComplete(files: List<File>, fileContents: List<String>) {
                        val gamerTag = extractGamerTag(fileContents)
                        if (gamerTag.isNullOrEmpty()) return
                        updateAccountWithGamerTag(gamerTag)
                    }
                    override fun onSearchError(error: Throwable) {
                        Log.e(TAG, "Search error", error)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse XAL ID", e)
        }
    }

    private fun updateAccountWithGamerTag(gamerTag: String) {
        uiRun {
            itemAccount.setTitle(gamerTag)
            itemAccount.setDescription(self.getString(R.string.microsoft_account))
        }

        XboxAPI.getSimpleProfileByName(gamerTag, object : HttpUtils.HttpCallback {
            override fun onSuccess(code: Int, body: String) {
                try {
                    val profileJson = Gson().fromJson(body, JsonObject::class.java)
                    val peopleArray = profileJson["people"].asJsonArray
                    if (peopleArray.size() > 0) {
                        val avatarUrl = peopleArray[0].asJsonObject["displayPicRaw"].asString
                        uiRun {
                            Glide.with(self)
                                .load(avatarUrl)
                                .into(itemAccount.iconView)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse profile", e)
                }
            }

            override fun onError(error: String) {
                Log.w(TAG, "Failed to get profile: $error")
            }
        })
    }

    private fun extractGamerTag(fileContents: List<String>): String? {
        for (content in fileContents) {
            try {
                val contentJson = Gson().fromJson(content, JsonObject::class.java)
                val tokens = contentJson["tokens"].asJsonArray

                for (tokenElement in tokens) {
                    val tokenData = tokenElement.asJsonObject["TokenData"].asJsonObject
                    val displayClaims = tokenData["DisplayClaims"].asJsonObject
                    val xuiArray = displayClaims["xui"].asJsonArray

                    for (xuiElement in xuiArray) {
                        val gtg = xuiElement.asJsonObject["gtg"].asString
                        if (gtg.isNotEmpty()) return gtg
                    }
                }
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    fun selectInstance(a: View) {
        val instances = Utils.getAllInstances(self)
        if (instances.isEmpty()) {
            Snackbar.make(layout, self.getString(R.string.no_instance_installed), Snackbar.LENGTH_SHORT).show()
            return
        }

        val popup = PopupMenu(self, a).apply {
            gravity = Gravity.END or Gravity.BOTTOM
        }
        instances.forEachIndexed({i, info ->
            popup.menu.add(Menu.NONE, i, i, info.name)
        })
        popup.setOnMenuItemClickListener { menuItem ->
            val mInstance = instances[menuItem.itemId]
            kvLauncherSettings?.set("instance", mInstance.name)
            updateInstances()
            true
        }
        popup.show()
    }

    fun editInstance() {
        val ii = Utils.getSelectedInstance(self, true)
        if (ii == null) {
            Snackbar.make(layout, self.getString(R.string.no_instance_selected), Snackbar.LENGTH_SHORT).show()
            return
        }
        activity.switchFragment(FragmentEditInstance(self, ii), self.getString(R.string.edit_instance) + " - " + ii.name)
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode", "DiscouragedPrivateApi")
    fun launch (instanceInfo: InstanceInfo) {
        val dialogView: LinearLayout = LinearLayout.inflate(self, R.layout.dialog_launching, null) as LinearLayout
        val taskTextView = dialogView.findViewWithTag<View>("task") as TextView
        val iconCheckInstance = dialogView.findViewWithTag<View>("icon_check_instance") as ImageView
        val iconLoadMods = dialogView.findViewWithTag<View>("icon_load_mods") as ImageView
        val iconLaunchGame = dialogView.findViewWithTag<View>("icon_launch_game") as ImageView
        val iconPatchDex = dialogView.findViewWithTag<View>("icon_patch_dex") as ImageView
        val iconPatchAsset = dialogView.findViewWithTag<View>("icon_patch_asset") as ImageView
        val iconPatchSo = dialogView.findViewWithTag<View>("icon_patch_so") as ImageView
        val progress = dialogView.findViewWithTag<View>("progress") as LinearProgressIndicator
        val dialog = MaterialAlertDialogBuilder(self)
            .setTitle(R.string.launch_game)
            .setView(dialogView)
            .setCancelable(false)
            .show()

        fun updateProgress(p: Int, lastIcon: ImageView, nextIcon: ImageView?, isSkip: Boolean = false) {
            uiRun {
                lastIcon.setImageResource(if (isSkip) R.drawable.skip_next_24px else R.drawable.check_24px)
                nextIcon?.setImageResource(R.drawable.arrow_forward_24px)

                val totalSteps = 5
                if (p >= totalSteps) {
                    progress.isIndeterminate = true
                    return@uiRun
                }
                progress.progress = (p.toFloat() / totalSteps.toFloat() * 100).toInt()
            }
        }
        @SuppressLint("SetTextI18n")
        fun updateTaskText(text: String, message: String = "") {
            uiRun {
                if (message.isNotEmpty()) {
                    taskTextView.text = "$text: $message"
                } else {
                    taskTextView.text = text
                }
            }
        }

        self.lifecycleScope.launch(Dispatchers.IO) {
            if (!Utils.isInstanceEntityExist(self, "apk", instanceInfo.entity) ||
                !File(instanceInfo.dirPath + "manifest.json").exists() ||
                !File(instanceInfo.dirPath + "manifest.json").isFile){
                dialog.dismiss()
                alert(R.string.launch_failed, R.string.failed_to_check_instance)
                return@launch
            }
            updateProgress(1, iconCheckInstance, iconLoadMods)

            val classLoader = self.classLoader

            fun normalProcess() {
                try {
                    classLoader.loadClass(BridgeA::class.java.name)
                    classLoader.loadClass(BridgeB::class.java.name)

                    updateTaskText("Accessing pathList")
                    val pathListField = classLoader.javaClass.superclass.getDeclaredField("pathList")
                    pathListField.isAccessible = true
                    val pathList = pathListField.get(classLoader)
                    val addDexPath = pathList.javaClass.getDeclaredMethod("addDexPath", String::class.java, File::class.java)

                    updateTaskText("Clearing cache")
                    Utils.fileRemove(Utils.getADirIPath(self, "cache/launcher"))

                    val source = instanceInfo.apkPath

                    // Load dex files
                    updateTaskText("Extracting dex files")
                    val cacheDexDir = Utils.getADirIPath(self, "cache/launcher/dex")
                    ZipFile(source).use { zipFile ->
                        for (i in 4 downTo 0) {
                            val dexName = "classes" + (if (i == 0) "" else i.toString()) + ".dex"
                            val dexFile = zipFile.getEntry(dexName) ?: continue

                            val mcDex = File(cacheDexDir, dexName)

                            updateTaskText("Extracting dex file", dexName)
                            zipFile.getInputStream(dexFile).source().use { source ->
                                mcDex.sink().buffer().use { sink ->
                                    sink.writeAll(source)
                                }
                            }

                            Log.d(TAG, "Extracted $dexName to ${mcDex.absolutePath}")

                            if (mcDex.setReadOnly()) {
                                updateTaskText("Add dex file to pathList", dexName)
                                addDexPath.invoke(pathList, mcDex.absolutePath, null)
                            } else {
                                throw Exception("Failed to set read-only permission for $dexName")
                            }
                        }
                    }
                    // Load bridge
                    try {
                        val fileList = self.assets.list("pocket_launcher")
                        fileList?.forEach { fileName ->
                            if (fileName.matches(Regex("bridge.*\\.dex"))) {
                                val inputStream = self.assets.open("pocket_launcher/$fileName")
                                Utils.fileCopy(inputStream, cacheDexDir + fileName)
                                updateTaskText("Add dex file to pathList", fileName)
                                addDexPath.invoke(pathList, cacheDexDir + fileName, null)
                                Log.d(TAG, "Loaded $fileName")
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // Load assets
                    updateProgress(3, iconPatchDex, iconPatchAsset)
                    val am = self.getAssets()
                    try {
                        val addAssetPath = am.javaClass.getDeclaredMethod(
                            "addAssetPath", String::class.java
                        )
                        addAssetPath.invoke(am, source)
                    } catch (e: java.lang.Exception) {
                        Log.e(TAG, e)
                    }

                    // Load so files
                    updateProgress(4, iconPatchAsset, iconPatchSo)
                    updateTaskText("Extracting shared libraries")
                    val cacheLibDir = Utils.getADirIPath(self, "cache/launcher/native_libs")
                    ZipFile(source).use { zipFile ->
                        val libDir = "lib/arm64-v8a/"

                        zipFile.entries().asSequence().forEach { entry ->
                            if (entry.name.startsWith(libDir) &&
                                entry.name.substringAfterLast('/').startsWith("lib") &&
                                entry.name.endsWith(".so") &&
                                !entry.isDirectory) {
                                println(entry.name)

                                val soFileName = entry.name.substringAfterLast('/')
                                val targetSoFile = File(cacheLibDir, soFileName)

                                updateTaskText("Extracting shared library", soFileName)

                                zipFile.getInputStream(entry).source().use { source ->
                                    targetSoFile.sink().buffer().use { sink ->
                                        sink.writeAll(source)
                                    }
                                }

                                Log.d(TAG, "Extracted $soFileName to ${targetSoFile.absolutePath}")

                                if (!targetSoFile.setReadOnly()) {
                                    throw Exception("Failed to set read-only permission for $soFileName")
                                }
                            }
                        }
                    }
                    updateTaskText("Add shared object directory to pathList")
                    val libDirsField = pathList.javaClass.getDeclaredField("nativeLibraryDirectories")
                    libDirsField.isAccessible = true
                    val dirList = libDirsField.get(pathList) as MutableList<File>
                    dirList.add(File(cacheLibDir))
                    libDirsField.set(pathList, dirList)

                    val addNativePath = pathList.javaClass.getDeclaredMethod("addNativePath", Collection::class.java)
                    val dirList2 = arrayListOf(cacheLibDir)
                    addNativePath.invoke(pathList, dirList2)

                    updateProgress(5, iconPatchSo, iconLaunchGame)
                    updateTaskText("Loading shared object files")
                    File(cacheLibDir).listFiles()?.sortedWith { lib1, lib2 ->
                        val name1 = lib1.name.toString()
                        val name2 = lib2.name.toString()

                        val isPriority1 = name1 == "libc++_shared.so" || name1 == "libfmod.so"
                        val isPriority2 = name2 == "libc++_shared.so" || name2 == "libfmod.so"

                        when {
                            isPriority1 && !isPriority2 -> -1
                            !isPriority1 && isPriority2 -> 1
                            else -> name1.compareTo(name2)
                        }
                    }?.forEach {
                        if (!it.name.endsWith(".so")) return@forEach
                        updateTaskText("Loading shared object file", it.name)
                        System.load(it.absolutePath)
                        Log.d(TAG, "Loaded ${it.absolutePath}")
                    }

                    // Launch game
                    kvGlobalGameConfig?.release()
                    uiRun {
                        updateTaskText("Launching game")
                        try {
                            val intent = Intent(self, MinecraftActivity::class.java)
                            self.startActivity(intent)
                            Utils.fileRemove(cacheDexDir)
                            Log.d(TAG, "Removed $cacheDexDir")
                            self.finish()
                        } catch (e: Exception) {
                            Log.e(TAG, e)
                            throw e
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e)
                    dialog.dismiss()
                    if (e is NoSuchMethodException && e.message?.contains("addNativePath") == true) {
                        alert(
                            self.getString(
                                R.string.android_system_version_not_supported,
                                Build.VERSION.RELEASE,
                                Build.VERSION.SDK_INT.toString()
                            ),
                            R.string.unsupported_android_system
                        )
                        return
                    }
                    alert(Log.getStackTraceString(e), R.string.launch_failed)
                    return
                }
            }

            try {
                val modList = instanceInfo.enabledMods
                if (modList.isEmpty()) {
                    updateProgress(2, iconLoadMods, iconPatchDex, true)
                    normalProcess()
                } else {
                    ModLoader(self, classLoader).loadMods(modList, instanceInfo, {m, i ->
                        if (i == 0) updateTaskText("Loading mod", m.name)
                    }, {b, s ->
                        if (b) {
                            updateProgress(2, iconLoadMods, iconPatchDex)
                            normalProcess()
                        } else {
                            dialog.dismiss()
                            alert(s, R.string.launch_failed)
                        }
                    })
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load mod info", e)
                dialog.dismiss()
                alert(Log.getStackTraceString(e), R.string.launch_failed)
                return@launch
            }
        }
    }
}