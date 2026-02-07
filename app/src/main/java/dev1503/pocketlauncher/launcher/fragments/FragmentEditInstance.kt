package dev1503.pocketlauncher.launcher.fragments

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev1503.pocketlauncher.InstanceInfo
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.launcher.widgets.ColumnLayout
import dev1503.pocketlauncher.launcher.widgets.ModListView

class FragmentEditInstance (self: AppCompatActivity, val instanceInfo: InstanceInfo) : Fragment(self, ColumnLayout(self), "FragmentEditInstance") {
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

        fun initInstanceIcon() {
            val instanceIcon = l.findViewWithTag<ViewGroup>("instance_icon").apply {
                setOnClickListener { _ ->
                    Snackbar.make(layout, "TODO", Snackbar.LENGTH_SHORT).show()
                }
            }
            val instanceIconView = instanceIcon.findViewWithTag<ImageView>("instance_icon_icon").apply {
                setImageBitmap(instanceInfo.iconBitmap)
            }
        }
        fun initWorkingDirectory() {
            l.findViewWithTag<ViewGroup>("working_directory").apply {
                setOnClickListener { _ ->
                    MaterialAlertDialogBuilder(self)
                        .setTitle(R.string.working_directory)
                        .setItems(arrayOf(self.getString(R.string.global_game_storage), self.getString(R.string.isolated_game_storage))) { _, which ->
                            when (which) {
                                0 -> {
                                    instanceInfo.dataIsolation = false
                                }
                                1 -> {
                                    instanceInfo.dataIsolation = true
                                    instanceInfo.dataStorageDir = ":INSTANCE/data/"
                                }
                            }
                            Snackbar.make(layout, self.getString(R.string.working_directory_set_to, instanceInfo.dataStorageDirParsed), Snackbar.LENGTH_SHORT).show()
                        }
                        .show()
                }
            }
        }

        initInstanceIcon()
        initWorkingDirectory()

        return l
    }

    fun initModsView(): ScrollView {
        val l = LinearLayout.inflate(self, R.layout.layout_launcher_edit_instance_mods, null) as ScrollView
        val modListView = l.findViewWithTag<ModListView>("mod_list")
        modListView.modList = Utils.getModsSupported(self, instanceInfo.versionName)
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