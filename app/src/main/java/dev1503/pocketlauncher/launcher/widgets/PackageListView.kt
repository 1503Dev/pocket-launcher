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
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils.dp2px

class PackageListView: ScrollView {
    var packageList: List<PackageInfo>? = emptyList()
        get() = field
        set(value) {
            container.removeAllViews()
            value?.forEach { packageInfo ->
                addPackage(packageInfo)
            }
        }
    var onPackageSelected: ((PackageInfo) -> Unit)? = null

    val container: LinearLayout = LinearLayout(context).apply {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
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
        addView(container)
        container.setPadding(0, dp2px(context, 8f), 0, dp2px(context,8f))
    }

    fun addPackage(packageInfo: PackageInfo) {
        val view: PackageListItem = PackageListItem(context).apply {
            this.packageInfo = packageInfo
        }
        view.setOnClickListener {
            _onPackageSelected(packageInfo)
        }
        container.addView(view)
    }

    private fun _onPackageSelected(packageInfo: PackageInfo) {
        onPackageSelected?.invoke(packageInfo)
    }

    class PackageListItem: LinearLayout {
        val iconView: ImageView
            get() = findViewWithTag<ImageView>("icon")!!
        val titleView: TextView
            get() = findViewWithTag<TextView>("title")!!
        val descriptionView: TextView
            get() = findViewWithTag<TextView>("description")!!

        var packageInfo: PackageInfo? = null
            @SuppressLint("SetTextI18n")
            set(value) {
                iconView.setImageDrawable(value?.applicationInfo?.loadIcon(context.packageManager))
                titleView.text = (value?.applicationInfo?.loadLabel(context.packageManager)?.toString() ?: "Unknown Label") + " v" + (value?.versionName ?: "Unknown Version")
                descriptionView.text = value?.packageName ?: "Unknown Package"
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
            inflate(context, R.layout.layout_package_list_view_item, this)
            orientation = HORIZONTAL
        }
    }
}