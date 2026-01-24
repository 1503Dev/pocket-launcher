package dev1503.pocketlauncher.launcher.fragments

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import dev1503.pocketlauncher.Log

open class Fragment (val self: AppCompatActivity, val layout: ViewGroup, val fragmentName: String) {
    val context: Context = layout.context
    var isInit: Boolean = false

    open fun init(): Boolean {
        if (isInit) return false
        layout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        Log.i(fragmentName, "Init")
        isInit = true
        return true
    }

    fun findViewWithTag(tag: String): View {
        return layout.findViewWithTag(tag)!!
    }
    fun uiRun(block: () -> Unit) {
        self.runOnUiThread(block)
    }
}