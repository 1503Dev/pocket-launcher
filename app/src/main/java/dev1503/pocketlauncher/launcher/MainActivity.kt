package dev1503.pocketlauncher.launcher

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.launcher.fragments.Fragment
import dev1503.pocketlauncher.launcher.fragments.FragmentDownload
import dev1503.pocketlauncher.launcher.fragments.FragmentMain
import dev1503.pocketlauncher.launcher.widgets.ColumnLayout
import okio.Utf8

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private val self: MainActivity = this
    private lateinit var contentView: ViewGroup
    private lateinit var layoutContainer: ViewGroup

    // Title bar elements
    private lateinit var titleBarIcon: ImageView
    private lateinit var titleBarBack: ImageView
    private lateinit var titleBarExit: ImageView
    private lateinit var titleBarMinimize: ImageView
    private lateinit var titleBarTitle: TextView

    // Fragment
    private var fragments: MutableMap<String, Fragment> = mutableMapOf()
    private var currentFragmentName: String = ""
    private var lastFragmentName: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = View.inflate(self, R.layout.activity_launcher, null) as ViewGroup
        setContentView(contentView)
        layoutContainer = findViewWithTag("container") as ViewGroup
        initLayouts()

        if (Build.VERSION.SDK_INT >= 35) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                switchFragment("main")
            }
        }
    }

    private fun initLayouts() {
        initTitleBar()
        fragments.put("main", FragmentMain(self))
        fragments.put("download", FragmentDownload(self))

        switchFragment("main")

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
                .setPositiveButton(R.string.yes) { dialog, which ->
                    System.exit(0)
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }
        titleBarBack.setOnClickListener { v ->
            switchFragment(lastFragmentName)
        }
    }

    private fun findViewWithTag(tag: String): View {
        return window.decorView.findViewWithTag(tag)!!
    }
    @SuppressLint("SetTextI18n")
    fun switchFragment(string: String) {
        val fragment = fragments[string]
        if (fragment != null && string != currentFragmentName) {
            val curFragment: Fragment? = fragments[currentFragmentName]
            if (curFragment != null) {
                (curFragment.layout as ColumnLayout).slideOut {
                    layoutContainer.removeAllViews()
                    fragment.init()
                    Utils.setTimeout({
                        runOnUiThread {
                            layoutContainer.removeAllViews()
                            layoutContainer.addView(fragment.layout)
                            (fragment.layout as ColumnLayout).slideIn()
                        }
                    }, 10)
                }
                if (string != "main") {
                    titleBarIcon.visibility = View.GONE
                    titleBarBack.visibility = View.VISIBLE
                } else {
                    titleBarIcon.visibility = View.VISIBLE
                    titleBarBack.visibility = View.GONE
                }
                when (string) {
                    "main" -> {
                        titleBarTitle.text = "${getString(R.string.app_name)} v${Utils.getAppVersionName(self)}"
                    }
                    "download" -> {
                        titleBarTitle.text = getString(R.string.download)
                    }
                }
            } else {
                layoutContainer.removeAllViews()
                fragment.init()
                layoutContainer.addView(fragment.layout)
            }
            lastFragmentName = currentFragmentName
            currentFragmentName = string
        }
    }
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        switchFragment("main")
    }
}