package dev1503.pocketlauncher.launcher.fragments

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R

open class Fragment (open var self: AppCompatActivity, val layout: ViewGroup, val fragmentName: String) {
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
    fun alert(message: String, title: String = "") {
        val dialog = MaterialAlertDialogBuilder(self)
            .setMessage(message)
            .setNegativeButton(R.string.ok, {_,_ -> })
            .setCancelable(false)
        if (title.isNotEmpty()) dialog.setTitle(title)
        dialog.show()
    }
    fun alert(message: Int, title: Int = 0) {
        uiRun {
            val dialog = MaterialAlertDialogBuilder(self)
                .setMessage(message)
                .setNegativeButton(R.string.ok, {_,_ -> })
                .setCancelable(false)
            if (title != 0) dialog.setTitle(title)
            dialog.show()
        }
    }
    fun alert(message: Int, title: String) {
        uiRun {
            val dialog = MaterialAlertDialogBuilder(self)
                .setMessage(message)
                .setNegativeButton(R.string.ok, {_,_ -> })
                .setCancelable(false)
            if (title.isNotEmpty()) dialog.setTitle(title)
            dialog.show()
        }
    }
    fun alert(message: String, title: Int = 0) {
        uiRun {
            val dialog = MaterialAlertDialogBuilder(self)
                .setMessage(message)
//                .setNegativeButton(R.string.ok, {_,_ -> })
                .setCancelable(true)
            if (title != 0) dialog.setTitle(title)
            dialog.show()
        }
    }
}