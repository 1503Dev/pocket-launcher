package dev1503.pocketlauncher.launcher

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev1503.pocketlauncher.GlobalDebugWindow
import dev1503.pocketlauncher.KVConfig
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.Utils.isPermissionDeclared
import dev1503.pocketlauncher.launcher.fragments.Fragment
import dev1503.pocketlauncher.launcher.fragments.FragmentAllInstances
import dev1503.pocketlauncher.launcher.fragments.FragmentDownload
import dev1503.pocketlauncher.launcher.fragments.FragmentMain
import dev1503.pocketlauncher.launcher.widgets.ColumnLayout
import kotlin.random.Random
import androidx.core.net.toUri
import java.io.InputStream

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

        if (Build.VERSION.SDK_INT <= 27) {
            MaterialAlertDialogBuilder(self)
                .setTitle(R.string.unsupported_android_system)
                .setMessage(getString(R.string.android_system_version_not_supported, Build.VERSION.RELEASE, Build.VERSION.SDK_INT.toString()))
                .setCancelable(true)
                .show()
        }

        Utils.kvGlobalGameConfig = KVConfig(self, Utils.getGlobalGameStorageDirPath(self) + "config.json")
        Utils.kvLauncherSettings = KVConfig(self, Utils.getDataDirPath(self) + "launcher_settings.json")
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
        fragments["main"] = FragmentMain(self)
        fragments["download"] = FragmentDownload(self)
        fragments["all_instances"] = FragmentAllInstances(self)

        switchFragment("main")
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
        var fragment: Fragment? = null
        fragment = fragments[string]
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
                    "all_instances" -> {
                        titleBarTitle.text = getString(R.string.all_instances)
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
    @SuppressLint("SetTextI18n")
    fun switchFragment(fragment: Fragment, pageName: String) {
        fragments.put(fragment.fragmentName, fragment)
        val string = fragment.fragmentName
        val curFragment: Fragment? = fragments[currentFragmentName]
        (curFragment?.layout as ColumnLayout).slideOut {
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
        titleBarIcon.visibility = View.GONE
        titleBarBack.visibility = View.VISIBLE
        titleBarTitle.text = pageName
        lastFragmentName = currentFragmentName
        currentFragmentName = string
    }
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        switchFragment("main")
    }

    override fun onDestroy() {
        Utils.kvLauncherSettings?.release()
        Utils.kvGlobalGameConfig?.release()
        super.onDestroy()
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        Log.d(TAG, "onWindowFocusChanged: $hasFocus")
        if (hasFocus) {
            hideSystemBars()
        } else if (GlobalDebugWindow.instance != null) {
//            GlobalDebugWindow.instance?.refreshViews()
        }
    }
    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.navigationBars())
                controller.hide(WindowInsets.Type.statusBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }
    }

    val requestCodePermissionCallback = mutableMapOf<Int, (Int) -> Unit>()
    val requestCodeFileCallback = mutableMapOf<Int, (String, InputStream) -> Unit>()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCodePermissionCallback.containsKey(requestCode)) {
            requestCodePermissionCallback[requestCode]?.invoke(
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) 1 else 0
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: $requestCode $resultCode $data")
        if (requestCodePermissionCallback.containsKey(requestCode)) {
            requestCodePermissionCallback[requestCode]?.invoke(-2)
        }
        if (resultCode == RESULT_OK) {
            if (requestCodeFileCallback.containsKey(requestCode)) {
                val uri = data?.data ?: return
                val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        cursor.moveToFirst()
                        cursor.getString(nameIndex)
                    } else null
                }
                requestCodeFileCallback[requestCode]?.invoke(fileName ?: "", contentResolver.openInputStream(uri) ?: return)
            }
        }
    }

    fun requestPermission(activity: AppCompatActivity, permission: String, callback: (Int) -> Unit) {
        if (!isPermissionDeclared(activity, permission)) return callback(-1)
        if (Utils.checkPermission(activity, permission)) return callback(2)
        val code = Random.nextInt(1, 10000)
        requestCodePermissionCallback[code] = callback
        if (permission == Manifest.permission.MANAGE_EXTERNAL_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    callback(2)
                    return
                }

                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = "package:${activity.packageName}".toUri()
                }

                try {
                    activity.startActivityForResult(intent, code)
                } catch (e: Exception) {
                    Utils.openAppSettings(activity)
                }
            }
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), code)
        }
    }
}