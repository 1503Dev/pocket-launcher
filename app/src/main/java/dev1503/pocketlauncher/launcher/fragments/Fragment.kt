package dev1503.pocketlauncher.launcher.fragments

import android.content.Context
import android.view.ViewGroup

open class Fragment (val layout: ViewGroup) {
    val context: Context = layout.context
}