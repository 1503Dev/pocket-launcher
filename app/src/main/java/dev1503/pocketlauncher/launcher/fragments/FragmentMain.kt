package dev1503.pocketlauncher.launcher.fragments

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev1503.Log
import dev1503.pocketlauncher.HttpUtils
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.XboxAPI
import dev1503.pocketlauncher.launcher.MainActivity.Companion.TAG
import io.reactivex.rxjava3.core.Single
import java.io.File

class FragmentMain (self: AppCompatActivity, layout: ViewGroup) : Fragment(self, layout, "FragmentMain") {
    private lateinit var mainAccountName: TextView
    private lateinit var mainLoginMethod: TextView
    private lateinit var mainAccountIcon: ImageView

    @Override
    override fun init() {
        super.init()
        mainAccountName = findViewWithTag("main_account_name") as TextView
        mainLoginMethod = findViewWithTag("main_login_method") as TextView
        mainAccountIcon = findViewWithTag("main_account_icon") as ImageView
        initMsAccount()
    }

    @SuppressLint("CheckResult")
    private fun initMsAccount() {
        mainAccountIcon.setImageResource(R.drawable.person_24px)
        mainAccountName.setText(R.string.accounts)
        mainLoginMethod.setText(R.string.not_logged_in)

        val single: Single<*>? = Utils.getCurrentXalIdRx(context)
        single?.subscribe { str ->
            if (str != null) {
                try {
                    val jsonObject = Gson().fromJson(str as String, JsonObject::class.java)
                    val id = jsonObject.get("default").asString
                    Utils.searchFilesWithContent(
                        Utils.getXalDirPath(self),
                        id,
                        object : Utils.FilesSearchWithContentListener {
                            override fun onSearchComplete(files: List<File>, fileContents: List<String>) {
                                Thread {
                                    for (i in files.indices) {
                                        try {
                                            val content = fileContents[i]
                                            val jsonObject =
                                                Gson().fromJson(content, JsonObject::class.java)
                                            val tokens: JsonArray = jsonObject.get("tokens").asJsonArray
                                            for (j in tokens.toList().indices) {
                                                Log.d(TAG, "token: ${tokens[j].asJsonObject}")
                                                try {
                                                    val token = tokens[j].asJsonObject
                                                    val tokenData =
                                                        token.get("TokenData").asJsonObject
                                                    val displayClaims =
                                                        tokenData.get("DisplayClaims").asJsonObject
                                                    val xui = displayClaims.get("xui").asJsonArray
                                                    for (k in xui.toList().indices) {
                                                        try {
                                                            val xuiObj = xui[k].asJsonObject
                                                            val gtg = xuiObj.get("gtg").asString
                                                            Log.d(TAG, "gtg: $gtg")
                                                            if (gtg.isNotEmpty()) {
                                                                uiRun {
                                                                    mainAccountName.text = gtg
                                                                    mainLoginMethod.setText(R.string.microsoft_account)
                                                                }
                                                                XboxAPI.getSimpleProfileByName(
                                                                    gtg,
                                                                    object :
                                                                        HttpUtils.HttpCallback {
                                                                        override fun onSuccess(
                                                                            code: Int,
                                                                            body: String
                                                                        ) {
                                                                            try {
                                                                                val jsonObject =
                                                                                    Gson().fromJson(
                                                                                        body,
                                                                                        JsonObject::class.java
                                                                                    )
                                                                                        .get("people").asJsonArray[0].asJsonObject
                                                                                val avatar =
                                                                                    jsonObject.get("displayPicRaw").asString
                                                                                uiRun {
                                                                                    Glide.with(self)
                                                                                        .load(avatar)
                                                                                        .into(
                                                                                            mainAccountIcon
                                                                                        )
                                                                                }
                                                                            } catch (e: Exception) {
                                                                                Log.w(TAG, e)
                                                                            }
                                                                        }

                                                                        override fun onError(error: String) {
                                                                            Log.w(TAG, error)
                                                                        }
                                                                    })
                                                                return@Thread
                                                            }
                                                        } catch (e: Exception) {
                                                            Log.w(TAG, e)
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    Log.w(TAG, e)
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.w(TAG, e)
                                        }
                                    }
                                }.start()
                            }

                            override fun onSearchError(error: Throwable) {
                                // Handle error
                            }
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, e)
                }
            }
        }
    }
}