package dev1503.pocketlauncher

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.text.SpannableString
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import dev1503.pocketlauncher.common.R
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class GlobalDebugWindow(application: Application) {
    companion object {
        @JvmField
        @SuppressLint("StaticFieldLeak")
        var instance: GlobalDebugWindow? = null
    }

    val TAG = "GlobalDebugWindow"

    val config = KVConfig(application, Utils.getDataDirPath(application) + "debugger_config.json")

    var windowManager: WindowManager? = null
    var ball: LinearLayout
    var layoutLogger: ScrollView
    var layoutKVConfigs: LinearLayout
    var activity: Activity? = null
    val kvConfigStatus: MutableMap<KVConfig, Boolean> = mutableMapOf()
    val loggerTextView: TextView
        get() {
            return layoutLogger.findViewWithTag<TextView>("text")!!
        }
    val kvConfigsActiveTextView: TextView
        get() {
            return layoutKVConfigs.findViewWithTag<TextView>("active")!!
        }
    val kvConfigsReleasedTextView: TextView
        get() {
            return layoutKVConfigs.findViewWithTag<TextView>("released")!!
        }

    init {
        instance = this

        val themedContext = ContextThemeWrapper(application, R.style.Theme_PocketLauncher)

        ball = (LinearLayout.inflate(themedContext, R.layout.layout_gdw_ball, null) as LinearLayout).apply {
            val icon = findViewWithTag<ImageView>("icon")!!
            val container = findViewWithTag<LinearLayout>("container")!!.apply {
                isVisible = config.getBoolean("expended", false)
            }
            val logger = container.findViewWithTag<CheckBox>("logger")!!.apply {
                isChecked = config.getBoolean("logger_enabled", false)
            }
            val kvConfigs = container.findViewWithTag<CheckBox>("kv_configs")!!.apply {
                isChecked = config.getBoolean("kv_configs_enabled", false)
            }

            logger.setOnCheckedChangeListener { _, isChecked ->
                layoutLogger.visibility = if (isChecked) View.VISIBLE else View.GONE
                config.set("logger_enabled", isChecked)
            }
            kvConfigs.setOnCheckedChangeListener { _, isChecked ->
                layoutKVConfigs.visibility = if (isChecked) View.VISIBLE else View.GONE
                config.set("kv_configs_enabled", isChecked)
            }

            var lastX = 0f
            var lastY = 0f
            var dx = 0f
            var dy = 0f
            var totalDx = 0f
            var totalDy = 0f

            icon.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.rawX
                        lastY = event.rawY
                        totalDx = 0f
                        totalDy = 0f
                    }
                    MotionEvent.ACTION_MOVE -> {
                        dx = event.rawX - lastX
                        dy = event.rawY - lastY
                        lastX = event.rawX
                        lastY = event.rawY
                        totalDx += dx
                        totalDy += dy
                        updatePositionRelatively(ball, dx, dy)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (abs(totalDx) < 6 && abs(totalDy) < 6) {
                            container.isVisible = !container.isVisible
                            config.set("expended", container.isVisible)
                        } else {
                            config.set("ball_x", (ball.layoutParams as WindowManager.LayoutParams).x.toFloat())
                            config.set("ball_y", (ball.layoutParams as WindowManager.LayoutParams).y.toFloat())
                        }
                    }
                }
                true
            }
        }
        layoutKVConfigs = LinearLayout.inflate(themedContext, R.layout.layout_gdw_kv_configs, null).apply {
            visibility = View.GONE
        } as LinearLayout
        layoutLogger = LinearLayout.inflate(themedContext, R.layout.layout_gdw_logger, null).apply {
            visibility = View.GONE
        } as ScrollView

        Log.i(TAG, "Init")
    }

    fun addAllViews(){
        val flagsNoTouch = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL

        try {
            windowManager?.addView(layoutKVConfigs, getNormalParams().apply {
                flags = flags or flagsNoTouch
            })
            layoutKVConfigs.isVisible = config.getBoolean("kv_configs_enabled", false)
        } catch (_: Exception) {
        }
        try {
            windowManager?.addView(layoutLogger, getNormalParams().apply {
                flags = flags or flagsNoTouch
            })
            layoutLogger.isVisible = config.getBoolean("logger_enabled", false)
        } catch (_: Exception) {
        }

        try {
            windowManager?.addView(ball, getNormalParams().apply {
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
                x = config.getFloat("ball_x", 200f).toInt()
                y = config.getFloat("ball_y", 0f).toInt()
            })
        } catch (_: Exception) {
        }
    }
    fun removeAllViews(){
        try {
            windowManager?.removeView(ball)
        } catch (_: Exception) {
        }
        try {
            windowManager?.removeView(layoutLogger)
        } catch (_: Exception) {
        }
        try {
            windowManager?.removeView(layoutKVConfigs)
        } catch (_: Exception) {
        }
    }
    fun updatePositionRelatively(view: View, dx: Float, dy: Float) {
        val params = view.layoutParams as WindowManager.LayoutParams
        params.x = (params.x + dx).toInt()
        params.y = (params.y + dy).toInt()
        if (params.x < 0) params.x = 0
        if (params.y < 0) params.y = 0
        if (params.x + view.width > (activity?.windowManager?.defaultDisplay?.width ?: 0)) {
            params.x = (activity?.windowManager?.defaultDisplay?.width ?: 0) - view.width
        }
        if (params.y + view.height > (activity?.windowManager?.defaultDisplay?.height ?: 0)) {
            params.y = (activity?.windowManager?.defaultDisplay?.height ?: 0) - view.height
        }
        windowManager?.updateViewLayout(view, params)
    }
    fun updateActivity(activity: Activity) {
        this.activity = activity
        this.windowManager = activity.getSystemService(Application.WINDOW_SERVICE) as WindowManager
        refreshViews()
    }
    fun onActivityDestroyed(activity: Activity) {
        for (kvConfig in kvConfigStatus.keys) {
           if (kvConfig.context == activity || kvConfig.context == activity.baseContext) {
               kvConfig.release("force by lifecycle")
           }
        }
        if (this.activity == activity) {
            this.activity = null
            removeAllViews()
            this.windowManager = null
        }
    }
    fun refreshViews() {
        removeAllViews()
        addAllViews()
    }

    fun getNormalParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    fun setLoggerText(text: SpannableString) {
        uiRun {
            loggerTextView.text = text
            Handler().postDelayed({
                layoutLogger.smoothScrollTo(0, loggerTextView.height)
            }, 50)
        }
    }
    fun updateKVConfigStatus(c: KVConfig, isActive: Boolean) {
        kvConfigStatus[c] = isActive
        var activeStr = ""
        var releasedStr = ""
        for (kvConfig in kvConfigStatus.keys) {
            if (kvConfigStatus[kvConfig] == true) {
                activeStr += "${kvConfig.hashCode()}@${kvConfig.file.name}: ${!kvConfig.isReleased}\n"
            } else {
                releasedStr += "${kvConfig.hashCode()}@${kvConfig.file.name}: ${!kvConfig.isReleased}\n"
            }
        }
        uiRun {
            kvConfigsActiveTextView.text = activeStr
            kvConfigsReleasedTextView.text = releasedStr
        }
    }

    fun uiRun(runnable: () -> Unit) {
        activity?.runOnUiThread(runnable)
    }
}