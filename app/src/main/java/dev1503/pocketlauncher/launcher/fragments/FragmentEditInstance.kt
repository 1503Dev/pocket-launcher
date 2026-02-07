package dev1503.pocketlauncher.launcher.fragments

import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import dev1503.pocketlauncher.InstanceInfo
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.launcher.widgets.ColumnLayout

class FragmentEditInstance (self: AppCompatActivity, instanceInfo: InstanceInfo) : Fragment(self, ColumnLayout(self), "FragmentEditInstance") {
    private val col = layout as ColumnLayout
    val checkableItems = ArrayList<ColumnLayout.ColumnLayoutItem>()

    init {
        col.setContentLayout(initSettingsView())
        val itemSettings = col.addItem(self.getString(R.string.settings), R.drawable.settings_24px, true).apply {
            checked = true
            onClick = View.OnClickListener {
                if (checked) return@OnClickListener
                getCheckableItemOnClick().onClick(it)
                col.setContentLayout(initSettingsView())
            }
        }
        val itemMods = col.addItem(self.getString(R.string.mods), R.drawable.extension_24px, true).apply {
            onClick = View.OnClickListener {
                if (checked) return@OnClickListener
                getCheckableItemOnClick().onClick(it)
                col.setContentLayout(initModsView())
            }
        }
        checkableItems.add(itemSettings)
        checkableItems.add(itemMods)

    }

    fun initSettingsView(): ScrollView {
        val l = LinearLayout.inflate(self, R.layout.layout_launcher_edit_instance_settings, null) as ScrollView
        return l
    }

    fun initModsView(): ScrollView {
        val l = LinearLayout.inflate(self, R.layout.layout_launcher_edit_instance_mods, null) as ScrollView
        return l
    }

    fun getCheckableItemOnClick(): View.OnClickListener {
        return View.OnClickListener {
            checkableItems.forEach { item ->
                if (item != it) item.checked = false
            }
        }
    }
}