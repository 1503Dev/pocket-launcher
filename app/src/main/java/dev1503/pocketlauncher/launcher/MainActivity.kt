package dev1503.pocketlauncher.launcher

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev1503.Log
import dev1503.pocketlauncher.HttpUtils
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.XboxAPI
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.lang.Thread;

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private val self: MainActivity = this
    private lateinit var contentView: ViewGroup
    private lateinit var layoutContainer: ViewGroup
    private lateinit var layoutMain: ViewGroup

    private lateinit var titleBarIcon: ImageView
    private lateinit var titleBarBack: ImageView
    private lateinit var titleBarExit: ImageView
    private lateinit var titleBarMinimize: ImageView
    private lateinit var titleBarTitle: TextView

    private lateinit var mainAccountName: TextView
    private lateinit var mainLoginMethod: TextView
    private lateinit var mainAccountIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = View.inflate(self, R.layout.activity_launcher, null) as ViewGroup
        setContentView(contentView)
        layoutContainer = findViewWithTag("container") as ViewGroup
        initLayouts()
        initMsAccount()
    }

    private fun initLayouts() {
        initTitleBar()
        layoutMain = View.inflate(self, R.layout.layout_launcher_main, layoutContainer) as ViewGroup
        mainAccountName = findViewWithTag("main_account_name") as TextView
        mainLoginMethod = findViewWithTag("main_login_method") as TextView
        mainAccountIcon = findViewWithTag("main_account_icon") as ImageView

        findViewWithTag("main_action_download").setOnClickListener { v ->
            // Add your click listener logic here
        }

        Utils.setAllTextColor(
            contentView,
            Utils.getColorFromAttr(self, com.google.android.material.R.attr.colorOnPrimary)
        )
    }

    @SuppressLint("SetTextI18n")
    private fun initTitleBar() {
        titleBarIcon = findViewWithTag("title_bar_icon") as ImageView
        titleBarBack = findViewWithTag("title_bar_back") as ImageView
        titleBarExit = findViewWithTag("title_bar_exit") as ImageView
        titleBarMinimize = findViewWithTag("title_bar_minimize") as ImageView
        titleBarTitle = findViewWithTag("title_bar_title") as TextView

        titleBarTitle.text = "${getString(R.string.app_name)} v${Utils.getAppVersionName(self)}"

        titleBarMinimize.setOnClickListener { v ->
            moveTaskToBack(true)
        }
        titleBarExit.setOnClickListener { v ->
            MaterialAlertDialogBuilder(self)
                .setTitle(R.string.are_you_sure_exit)
                .setPositiveButton(android.R.string.yes) { dialog, which ->
                    System.exit(0)
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
        }
    }

    @SuppressLint("CheckResult")
    private fun initMsAccount() {
        mainAccountIcon.setImageResource(R.drawable.person_24px)
        mainAccountName.setText(R.string.accounts)
        mainLoginMethod.setText(R.string.not_logged_in)

        val single: Single<*>? = Utils.getCurrentXalIdRx(self)
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
                                                                runOnUiThread {
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
                                                                                runOnUiThread {
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

    private fun findViewWithTag(tag: String): View {
        return window.decorView.findViewWithTag(tag)!!
    }
}