package dev1503.pocketlauncher.launcher.fragments

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dev1503.pocketlauncher.InstanceInfo
import dev1503.pocketlauncher.KVConfig
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.launcher.widgets.ColumnLayout
import dev1503.pocketlauncher.launcher.widgets.ModListView

class FragmentEditInstance (self: AppCompatActivity, val instanceInfo: InstanceInfo) : Fragment(self, ColumnLayout(self), "FragmentEditInstance") {
    private val col = layout as ColumnLayout
    val checkableItems = ArrayList<ColumnLayout.ColumnLayoutItem>()
    val instanceConfig = KVConfig(self, instanceInfo.dirPath + "config.json")

    val TAG = "FragmentEditInstance/${instanceInfo.name}"

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
        fun initDeviceModel() {
            l.findViewWithTag<ViewGroup>("device_model").apply {
                setOnClickListener { _ ->
                    val inputLayout = TextInputLayout(self)
                    val inputField = TextInputEditText(self)
                    inputLayout.addView(inputField)
                    MaterialAlertDialogBuilder(self)
                        .setTitle(R.string.working_directory)
                        .setView(inputLayout)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            instanceInfo.deviceModel = inputField.text.toString()
                        }
                        .setNegativeButton(R.string.cancel) { d, _ ->
                            d.cancel()
                        }
                        .setNeutralButton(R.string.reset) { d, _ ->
                            inputField.text = Utils.getDeviceModelName()
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
        val modListCard = l.findViewWithTag<CardView>("mods")
        val noModsCard = l.findViewWithTag<CardView>("info_no_mods")
        val supportedMods = Utils.getModsSupported(self, instanceInfo.versionName)
        if (supportedMods.isEmpty()) {
            modListCard.visibility = View.GONE
            Log.w(TAG, "No supported mods found for version ${instanceInfo.versionName}")
            return l
        }
        noModsCard.visibility = View.GONE
        modListView.modList = supportedMods
        val mods = instanceConfig.getArray("mods", mutableListOf()).toMutableSet()
        modListView.setEnabledMods(mods.toList() as List<String>)
        modListView.onModCheckListener = { mod, checked ->
            val modPackage = mod.packageName
            val modVersion = mod.version

            if (!Utils.testModPackageName(modPackage)) {
                alert(self.getString(R.string.invalid_mod_package_name), self.getString(R.string.failed_to_change_mod_status))
            } else if (!Utils.testModVersionName(modVersion)) {
                alert(self.getString(R.string.invalid_mod_version), self.getString(R.string.failed_to_change_mod_status))
            } else {
                if (checked) {
                    mods.add(mod.id)
                } else {
                    mods.remove(mod.id)
                }
                instanceConfig.setArray("mods", mods.toList())
            }
        }
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