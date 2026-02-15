package dev1503.pocketlauncher.launcher.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.util.AttributeSet
import android.view.Gravity
import android.view.Menu
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import dev1503.pocketlauncher.InstanceInfo
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.Utils.dp2px
import dev1503.pocketlauncher.Utils.kvLauncherSettings
import dev1503.pocketlauncher.launcher.MainActivity
import dev1503.pocketlauncher.launcher.dialogs.DialogLoading
import dev1503.pocketlauncher.modloader.ModInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InstanceListView: ScrollView {
    val TAG = "InstanceListView"

    private var _selectedInstance: InstanceInfo? = null

    var instanceList: List<InstanceInfo>? = emptyList()
        get() = field
        set(value) {
            container.removeAllViews()
            value?.forEach { instanceInfo ->
                addInstance(instanceInfo)
            }
        }
    var selectedInstance: InstanceInfo?
        get() = _selectedInstance
        set(value) {
            _selectedInstance = value
            instanceListItems.forEach { item ->
                item.radioButton.isChecked = item.instanceInfo?.name == value?.name
            }
        }
    var onInstanceSelectedListener: ((InstanceInfo) -> Unit)? = null

    val container: LinearLayout = LinearLayout(context).apply {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }
    val instanceListItems: MutableList<InstanceListItem> = mutableListOf()
    var activity: AppCompatActivity? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        addView(container)
        container.setPadding(0, dp2px(context, 8f), 0, dp2px(context,8f))
    }

    fun addInstance(instanceInfo: InstanceInfo) {
        val view: InstanceListItem = InstanceListItem(context).apply {
            this.instanceInfo = instanceInfo
            this.radioButton.setOnClickListener { _ ->
                _onInstanceChecked(instanceInfo)
            }
            this.moreIcon.setOnClickListener { _ ->
                val popup = PopupMenu(context, moreIcon).apply {
                    gravity = Gravity.END or Gravity.BOTTOM
                }
                popup.menu.add(Menu.NONE, 0, 0, R.string.delete_instance)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        0 -> {
                            MaterialAlertDialogBuilder(context)
                                .setTitle(R.string.delete_instance)
                                .setMessage(context.getString(R.string.delete_instance_confirm, instanceInfo.name))
                                .setPositiveButton(R.string.delete_and_abandon_saves) { _, _ ->
                                    removeInstance(instanceInfo)
                                }
                                .show()
                        }
                    }
                    true
                }
                popup.show()
            }
        }
        instanceListItems.add(view)
        container.addView(view)
    }

    fun removeInstance(instanceInfo: InstanceInfo) {
        val dialog = DialogLoading(context, context.getString(R.string.delete_instance),
            DialogLoading.TYPE_CIRCULAR).init().apply {
                this.text = context.getString(R.string.removing_files)
            }
        dialog.show()
        activity?.lifecycleScope?.launch(Dispatchers.IO) {
            if (selectedInstance?.name == instanceInfo.name) {
                _selectedInstance = null
                Utils.setSelectedInstance("")
            }
            activity?.runOnUiThread {
                instanceListItems.forEach { item ->
                    if (item.instanceInfo?.name == instanceInfo.name) {
                        container.removeView(item)
                    }
                }
                instanceListItems.removeIf { it.instanceInfo?.name == instanceInfo.name }
            }

            val all = Utils.getInstancesByEntity(context, instanceInfo.entity)
            if (all.size == 1 && instanceInfo.apkPath != null) {
                Utils.fileRemove(instanceInfo.apkPath?:"")
            }
            Utils.fileRemove(instanceInfo.dirPath)
            activity?.runOnUiThread {
                dialog.cancel()
                if (activity!! is MainActivity) {
                    (activity as MainActivity).snack(context.getString(R.string.instance_deleted, instanceInfo.name))
                }
            }
        }
    }

    private fun _onInstanceChecked(instanceInfo: InstanceInfo) {
        selectedInstance = instanceInfo
        onInstanceSelectedListener?.invoke(instanceInfo)
    }

    class InstanceListItem: LinearLayout {
        val TAG = "InstanceListView.InstanceListItem"

        val radioButton: MaterialRadioButton
            get() = findViewWithTag<MaterialRadioButton>("radio_button")!!
        val iconView: ImageView
            get() = findViewWithTag<ImageView>("icon")!!
        val titleView: TextView
            get() = findViewWithTag<TextView>("title")!!
        val descriptionView: TextView
            get() = findViewWithTag<TextView>("description")!!
        val archChip: MaterialCardView
            get() = findViewWithTag<MaterialCardView>("arch")!!
        val archChipTextView: TextView
            get() = archChip.findViewWithTag<TextView>("text")!!
        val moreIcon: ImageView
            get() = findViewWithTag<ImageView>("more")!!

        var instanceInfo: InstanceInfo? = null
            @SuppressLint("SetTextI18n")
            set(value) {
                field = value
                if (value?.iconBitmap != null) {
                    iconView.setImageBitmap(value.iconBitmap)
                }
                titleView.text = value?.name ?: "Unknown Instance"
                if (value?.versionName != null) {
                    descriptionView.text = "v" + value.versionName
                }
                if (value?.isArchAvailable == false) {
                    archChip.visibility = VISIBLE
                    archChipTextView.setText(value.arch.split(" ").joinToString("/"))
                } else {
                    archChip.visibility = GONE
                }
            }

        constructor(context: Context) : super(context) {
            init()
        }

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            init()
        }

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
            init()
        }

        private fun init() {
            inflate(context, R.layout.layout_instance_list_view_item, this)
            orientation = HORIZONTAL
        }
    }
}