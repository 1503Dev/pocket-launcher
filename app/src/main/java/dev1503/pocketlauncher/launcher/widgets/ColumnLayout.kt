package dev1503.pocketlauncher.launcher.widgets

import android.animation.Animator
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import dev1503.pocketlauncher.Log
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils
import okhttp3.internal.wait

class ColumnLayout : LinearLayout {
    val SLIDE_ANIM_DURATION = 125L

    lateinit var itemsContainer: LinearLayout
    lateinit var contentContainer: ViewGroup
    lateinit var layoutLeft: ViewGroup

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        inflate(context, R.layout.layout_column_layout, this)
        orientation = HORIZONTAL
        itemsContainer = findViewWithTag<LinearLayout>("items_container")!!
        layoutLeft = findViewWithTag<ViewGroup>("layout_left")!!
        contentContainer = findViewWithTag<ViewGroup>("content")!!
    }

    fun setContentLayout(view: ViewGroup) {
        contentContainer.removeAllViews()
        contentContainer.addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun addDivider(text: CharSequence) {
        val divider = View.inflate(context, R.layout.layout_column_layout_divider, null)
        divider.findViewWithTag<TextView>("text")?.text = text
        itemsContainer.addView(divider, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    fun addItem(name: String = "", icon: Int = 0, description: String = ""): ColumnLayoutItem {
        val view = ColumnLayoutItem(context)
        view.setTitle(name)
        view.setDescription(description)
        if (description != "") {
            view.setIconBig(icon)
        } else view.setIcon(icon)
        itemsContainer.addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        return view
    }

    fun slideOut(onEnd: () -> Unit = {}) {
        layoutLeft.animate().translationX(-layoutLeft.width.toFloat() / 3)
            .setDuration(SLIDE_ANIM_DURATION)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    visibility = GONE
                    onEnd.invoke()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            }).start()
        layoutLeft.animate().alpha(0f)
            .setDuration(SLIDE_ANIM_DURATION).start()
        contentContainer.animate().translationX(layoutLeft.width.toFloat() / 3)
            .setDuration(SLIDE_ANIM_DURATION).start()
        contentContainer.animate().alpha(0f)
            .setDuration(SLIDE_ANIM_DURATION).start()
    }
    fun slideIn(onEnd: () -> Unit = {}) {
        layoutLeft.alpha = 0f
        layoutLeft.translationX = -layoutLeft.width.toFloat() / 3
        contentContainer.alpha = 0f
        contentContainer.translationX = layoutLeft.width.toFloat() / 3
        visibility = VISIBLE

        layoutLeft.animate().translationX(0f)
            .setDuration(SLIDE_ANIM_DURATION)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    onEnd.invoke()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            }).start()
        layoutLeft.animate().alpha(1f)
            .setDuration(SLIDE_ANIM_DURATION).start()
        contentContainer.animate().translationX(0f)
            .setDuration(SLIDE_ANIM_DURATION).start()
        contentContainer.animate().alpha(1f)
            .setDuration(SLIDE_ANIM_DURATION).start()
    }

    class ColumnLayoutItem: LinearLayout {
        public val iconView: ImageView
            get() = findViewWithTag<ImageView>("icon")!!

        constructor(context: Context) : super(context) {
            init()
        }

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            init()
        }

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
            init()
        }

        constructor(
            context: Context,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int
        ) : super(context, attrs, defStyleAttr, defStyleRes) {
            init()
        }

        private fun init() {
            inflate(context, R.layout.layout_column_layout_item_card, this)
            orientation = HORIZONTAL
        }

        fun setTitle(title: CharSequence) {
            findViewWithTag<TextView>("title")?.text = title
        }

        fun setIcon(icon: Bitmap) {
            iconView.layoutParams.height = Utils.dp2px(context, 32f)
            iconView.setImageBitmap(icon)
        }
        fun setIcon(icon: Int) {
            iconView.layoutParams.height = Utils.dp2px(context, 32f)
            iconView.setImageResource(icon)
        }
        fun setIconBig(icon: Bitmap) {
            setIcon(icon)
            iconView.layoutParams.height = Utils.dp2px(context, 42f)
        }
        fun setIconBig(icon: Int) {
            setIcon(icon)
            iconView.layoutParams.height = Utils.dp2px(context, 42f)
        }

        fun setDescription(description: CharSequence) {
            if (description.isEmpty()) {
                findViewWithTag<TextView>("description")?.visibility = View.GONE
            } else findViewWithTag<TextView>("description")?.visibility = View.VISIBLE
            findViewWithTag<TextView>("description")?.text = description
        }
    }
}