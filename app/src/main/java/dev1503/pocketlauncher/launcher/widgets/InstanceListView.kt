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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import dev1503.pocketlauncher.InstanceInfo
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import dev1503.pocketlauncher.Utils.dp2px
import dev1503.pocketlauncher.modloader.ModInfo

class InstanceListView: ScrollView {
    val TAG = "InstanceListView"

    var instanceList: List<InstanceInfo>? = emptyList()
        get() = field
        set(value) {
            container.removeAllViews()
            value?.forEach { instanceInfo ->
                addInstance(instanceInfo)
            }
        }
    var onInstanceSelectedListener: ((InstanceInfo) -> Unit)? = null

    val container: LinearLayout = LinearLayout(context).apply {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }
    val instanceListItems: MutableList<InstanceListItem> = mutableListOf()

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
        }
        instanceListItems.add(view)
        container.addView(view)
    }

    private fun _onInstanceChecked(instanceInfo: InstanceInfo) {
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
                val processArch = Utils.getProcessArch()
                if (value?.arch != processArch) {
                    archChip.visibility = VISIBLE
                    archChipTextView.setText(value?.arch)
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