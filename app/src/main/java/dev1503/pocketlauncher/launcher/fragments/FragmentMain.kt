package dev1503.pocketlauncher.launcher.fragments

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.HttpUtils
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.XboxAPI
import dev1503.pocketlauncher.launcher.MainActivity
import dev1503.pocketlauncher.launcher.MainActivity.Companion.TAG
import dev1503.pocketlauncher.launcher.widgets.ColumnLayout
import java.io.File

class FragmentMain (self: AppCompatActivity) : Fragment(self, ColumnLayout(self), "FragmentMain") {
    private lateinit var itemAccount: ColumnLayout.ColumnLayoutItem
    private val activity: MainActivity = self as MainActivity
    val columnLayout: ColumnLayout = layout as ColumnLayout

    @Override
    override fun init(): Boolean {
        if (!super.init()) return false

        columnLayout.addDivider(self.getString(R.string.accounts))
        itemAccount = columnLayout.addItem(
            self.getString(R.string.accounts),
            R.drawable.person_24px,
            self.getString(R.string.not_logged_in)
        )
        columnLayout.addDivider(self.getString(R.string.games))
        columnLayout.addItem(
            self.getString(R.string.download),
            R.drawable.download_24px,
        ).setOnClickListener {
            activity.switchFragment("download")
        }

        columnLayout.setContentLayout(View.inflate(self, R.layout.layout_launcher_main, null) as ViewGroup)

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

            try {
                val jsonObject = Gson().fromJson(xalId, JsonObject::class.java)
                val id = jsonObject["default"].asString

                Utils.searchFilesWithContent(
                    Utils.getXalDirPath(self),
                    id,
                    object : Utils.FilesSearchWithContentListener {
                        override fun onSearchComplete(files: List<File>, fileContents: List<String>) {
                            fun extractGamerTag(): String? {
                                for (content in fileContents) {
                                    try {
                                        val contentJson = Gson().fromJson(content, JsonObject::class.java)
                                        val tokens = contentJson["tokens"].asJsonArray

                                        for (tokenElement in tokens) {
                                            try {
                                                val tokenData = tokenElement.asJsonObject["TokenData"].asJsonObject
                                                val displayClaims = tokenData["DisplayClaims"].asJsonObject
                                                val xuiArray = displayClaims["xui"].asJsonArray

                                                for (xuiElement in xuiArray) {
                                                    val gtg = xuiElement.asJsonObject["gtg"].asString
                                                    if (gtg.isNotEmpty()) {
                                                        return gtg
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                continue
                                            }
                                        }
                                    } catch (e: Exception) {
                                        continue
                                    }
                                }
                                return null
                            }

                            val gamerTag = extractGamerTag()
                            if (gamerTag.isNullOrEmpty()) return

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

                        override fun onSearchError(error: Throwable) {
                            Log.e(TAG, "Search error", error)
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse XAL ID", e)
            }
        }
    }
}