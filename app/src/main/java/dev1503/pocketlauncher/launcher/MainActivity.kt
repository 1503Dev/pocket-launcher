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
import dev1503.pocketlauncher.launcher.fragments.Fragment
import dev1503.pocketlauncher.launcher.fragments.FragmentMain
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

    // Title bar elements
    private lateinit var titleBarIcon: ImageView
    private lateinit var titleBarBack: ImageView
    private lateinit var titleBarExit: ImageView
    private lateinit var titleBarMinimize: ImageView
    private lateinit var titleBarTitle: TextView

    // Fragment
    private lateinit var fragments: MutableMap<String, Fragment>
    private var currentFragmentName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = View.inflate(self, R.layout.activity_launcher, null) as ViewGroup
        setContentView(contentView)
        layoutContainer = findViewWithTag("container") as ViewGroup
        initLayouts()
    }

    private fun initLayouts() {
        initTitleBar()
        fragments = mutableMapOf()

        layoutMain = View.inflate(self, R.layout.layout_launcher_main, null) as ViewGroup
        fragments.put("main", FragmentMain(self, layoutMain))

        switchFragment("main")

        findViewWithTag("main_action_download").setOnClickListener { v ->
            switchFragment("download");
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

    private fun findViewWithTag(tag: String): View {
        return window.decorView.findViewWithTag(tag)!!
    }

    private fun switchFragment(string: String) {
        val fragment = fragments[string]
        if (fragment != null && string != currentFragmentName) {
            layoutContainer.removeAllViews()
            fragment.init()
            layoutContainer.addView(fragment.layout)
            currentFragmentName = string
        }
    }
}