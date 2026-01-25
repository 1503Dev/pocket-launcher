package dev1503.pocketlauncher.launcher.fragments

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev1503.pocketlauncher.HttpUtils
import dev1503.pocketlauncher.InstanceInfo
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.Utils.kvConfig
import dev1503.pocketlauncher.XboxAPI
import dev1503.pocketlauncher.launcher.MainActivity
import dev1503.pocketlauncher.launcher.MainActivity.Companion.TAG
import dev1503.pocketlauncher.launcher.widgets.ColumnLayout
import java.io.File

class FragmentMain (self: AppCompatActivity) : Fragment(self, ColumnLayout(self), "FragmentMain") {
    private lateinit var itemAccount: ColumnLayout.ColumnLayoutItem
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
                val instanceInfo = Utils.getInstanceInfo(self, kvConfig?.getString("instance", ""))
                if (instanceInfo == null) {
                    Snackbar.make(layout, R.string.no_instance_selected, Snackbar.LENGTH_SHORT).show()
                } else {
                    launch(instanceInfo)
                }
            } catch (e: Exception) {
                Log.e(TAG, e)
            }
        }
        btnLaunchInstanceName = btnLaunch.findViewWithTag("instance_name")
        if (kvConfig?.getString("instance", "") != "") {
            btnLaunchInstanceName.text = kvConfig?.getString("instance", "")
        }

        initMsAccount()
        return true
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
            kvConfig?.set("instance", mInstance.name)
            btnLaunchInstanceName.text = mInstance.name
            true
        }
        popup.show()
    }

    fun alert(message: String, title: String = "") {
        val dialog = MaterialAlertDialogBuilder(self)
            .setMessage(message)
            .setNegativeButton(R.string.ok, {_,_ -> })
            .setCancelable(false)
        if (title.isNotEmpty()) dialog.setTitle(title)
        dialog.show()
    }
    fun alert(message: Int, title: Int = 0) {
        val dialog = MaterialAlertDialogBuilder(self)
            .setMessage(message)
            .setNegativeButton(R.string.ok, {_,_ -> })
            .setCancelable(false)
        if (title != 0) dialog.setTitle(title)
        dialog.show()
    }

    fun launch (instanceInfo: InstanceInfo) {
        val dialogView: LinearLayout = LinearLayout.inflate(self, R.layout.dialog_launching, null) as LinearLayout
        val iconCheckInstance = dialogView.findViewWithTag<View>("icon_check_instance") as ImageView
        val iconLoadMods = dialogView.findViewWithTag<View>("icon_load_mods") as ImageView
        val iconLaunchGame = dialogView.findViewWithTag<View>("icon_launch_game") as ImageView
        val progress = dialogView.findViewWithTag<View>("progress") as LinearProgressIndicator
        val dialog = MaterialAlertDialogBuilder(self)
            .setTitle(R.string.launch_game)
            .setView(dialogView)
            .setCancelable(true)
            .show()
        if (!File(instanceInfo.dirPath + instanceInfo.entityRelativePath).exists() ||
            !File(instanceInfo.dirPath + instanceInfo.entityRelativePath).isFile ||
            !File(instanceInfo.dirPath + "manifest.json").exists() ||
            !File(instanceInfo.dirPath + "manifest.json").isFile){
            dialog.dismiss()
            alert(R.string.launch_failed, R.string.failed_to_check_instance)
            return
        }
        progress.progress = 33
        iconCheckInstance.setImageResource(R.drawable.check_24px)
        iconLoadMods.setImageResource(R.drawable.arrow_forward_24px)
        progress.progress = 66
        iconLoadMods.setImageResource(R.drawable.skip_next_24px)
        iconLaunchGame.setImageResource(R.drawable.arrow_forward_24px)
    }
}