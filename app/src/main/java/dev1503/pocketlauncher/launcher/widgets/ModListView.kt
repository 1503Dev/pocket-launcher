package dev1503.pocketlauncher.launcher.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils.dp2px
import dev1503.pocketlauncher.modloader.ModInfo

class ModListView: ScrollView {
    val TAG = "ModListView"

    var modList: List<ModInfo>? = emptyList()
        get() = field
        set(value) {
            container.removeAllViews()
            value?.forEach { modInfo ->
                addMod(modInfo)
            }
        }
    var onModCheckListener: ((ModInfo, Boolean) -> Unit)? = null

    val container: LinearLayout = LinearLayout(context).apply {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }
    val modListItems: MutableList<ModListItem> = mutableListOf()

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

    fun addMod(modInfo: ModInfo) {
        val view: ModListItem = ModListItem(context).apply {
            this.modInfo = modInfo
            this.checkbox.addOnCheckedStateChangedListener { _, isChecked ->
                _onModChecked(modInfo, isChecked == MaterialCheckBox.STATE_CHECKED)
            }
        }
        modListItems.add(view)
        container.addView(view)
    }

    private fun _onModChecked(modInfo: ModInfo, isChecked: Boolean) {
        onModCheckListener?.invoke(modInfo, isChecked)
    }

    class ModListItem: LinearLayout {
        val TAG = "ModListView.ModListItem"

        val checkbox: MaterialCheckBox
            get() = findViewWithTag<MaterialCheckBox>("checkbox")!!
        val iconView: ImageView
            get() = findViewWithTag<ImageView>("icon")!!
        val titleView: TextView
            get() = findViewWithTag<TextView>("title")!!
        val descriptionView: TextView
            get() = findViewWithTag<TextView>("description")!!

        var modInfo: ModInfo? = null
            @SuppressLint("SetTextI18n")
            set(value) {
                field = value
                if (value?.icon != null) {
                    iconView.setImageBitmap(value.icon)
                }
                titleView.text = value?.name ?: value?.packageName ?: "Unknown Label"
                if (value?.version != null) {
                    descriptionView.text = "v" + value.version
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
            inflate(context, R.layout.layout_mod_list_view_item, this)
            orientation = HORIZONTAL
        }
    }
    fun setEnabledMods(modIds: List<String>) {
        modListItems.forEach { modListItem ->
            modListItem.checkbox.isChecked = modIds.contains(modListItem.modInfo?.id)
        }
    }
}