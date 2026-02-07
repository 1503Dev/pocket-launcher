package dev1503.pocketlauncher

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.text.SpannableString
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.isVisible

class GlobalDebugWindow(application: Application) {
    companion object {
        @JvmField
        @SuppressLint("StaticFieldLeak")
        var instance: GlobalDebugWindow? = null
    }

    val TAG = "GlobalDebugWindow"

    var windowManager: WindowManager? = null
    var ball: LinearLayout
    var layoutLogger: ScrollView
    var activity: Activity? = null
    val loggerTextView: TextView
        get() {
            return layoutLogger.findViewWithTag<TextView>("text")!!
        }

    init {
        instance = this

        ball = LinearLayout(application).apply {
            addView(ImageView(application).apply {
                setImageResource(R.drawable.bug_report_24px)
            })
            setOnClickListener { _ ->
                layoutLogger.isVisible = !layoutLogger.isVisible
            }
        }

        layoutLogger = LinearLayout.inflate(application, R.layout.layout_gdw_logger, null).apply {
            visibility = View.GONE
        } as ScrollView

        Log.i(TAG, "Init")
    }

    fun addAllViews(){
        windowManager?.addView(layoutLogger, getNormalParams().apply {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        })

        windowManager?.addView(ball, getNormalParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = 200
        })
    }
    fun removeAllViews(){
        try {
            windowManager?.removeView(ball)
        } catch (e: Exception) {
        }
        try {
            windowManager?.removeView(layoutLogger)
        } catch (e: Exception) {
        }
    }

    fun updateActivity(activity: Activity) {
        this.activity = activity
        this.windowManager = activity.getSystemService(Application.WINDOW_SERVICE) as WindowManager
        refreshViews()
    }
    fun onActivityDestroyed(activity: Activity) {
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

    fun uiRun(runnable: () -> Unit) {
        activity?.runOnUiThread(runnable)
    }
}