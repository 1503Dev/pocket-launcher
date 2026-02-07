package dev1503.pocketlauncher.launcher.widgets

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import dev1503.pocketlauncher.R
import dev1503.pocketlauncher.Utils

class ColumnLayout : LinearLayout {
    val SLIDE_ANIM_DURATION = 125L

    lateinit var itemsContainer: LinearLayout
    lateinit var contentContainer: ViewGroup
    lateinit var layoutLeft: ViewGroup

     var lastContentLayout: ViewGroup? = null

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
        inflate(context, R.layout.layout_column_layout, this)
        orientation = HORIZONTAL
        itemsContainer = findViewWithTag("items_container")!!
        layoutLeft = findViewWithTag("layout_left")!!
        contentContainer = findViewWithTag("content")!!
    }

    fun setContentLayout(view: ViewGroup) {
        contentContainer.removeAllViews()
        if (lastContentLayout != null) {
            view.translationY = 144f
            view.alpha = 0.2f
        }
        contentContainer.addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        if (lastContentLayout != null) {
            val translateAnimator = ObjectAnimator.ofFloat(view, "translationY", 0f)
            translateAnimator.interpolator = AccelerateDecelerateInterpolator()

            val alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f)
            alphaAnimator.interpolator = AccelerateDecelerateInterpolator()

            AnimatorSet().apply {
                playTogether(translateAnimator, alphaAnimator)
                duration = 125
                start()
            }
        }
        lastContentLayout = view
    }

    fun setContentLayout(layoutId: Int) {
        setContentLayout(View.inflate(context, layoutId, null) as ViewGroup)
    }

    fun addDivider(text: CharSequence) {
        val divider = View.inflate(context, R.layout.layout_column_layout_divider, null)
        divider.findViewWithTag<TextView>("text")?.text = text
        itemsContainer.addView(divider, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    fun addItem(name: String = "", icon: Int = 0, selectable: Boolean = false, description: String = ""): ColumnLayoutItem {
        val view = ColumnLayoutItem(context)
        view.checkable = selectable
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
        val iconView: ImageView
            get() = findViewWithTag<ImageView>("icon")!!
        val titleView: TextView
            get() = findViewWithTag<TextView>("title")!!
        var checked: Boolean = false
            set(value) {
                if (!checkable) return
                if (value) {
                    setBackgroundColor(Utils.applyAlpha(Utils.getColorFromAttr(context, androidx.appcompat.R.attr.colorPrimary), 0.2f))
                    titleView.setTextColor(Utils.getColorFromAttr(context, androidx.appcompat.R.attr.colorPrimary))
                    titleView.setTypeface(titleView.typeface, Typeface.BOLD)
                    iconView.setColorFilter(Utils.getColorFromAttr(context, androidx.appcompat.R.attr.colorPrimary))
                } else {
                    background = Utils.getDrawableFromAttr(context, android.R.attr.selectableItemBackground)
                    titleView.setTextColor(Utils.getColorFromAttr(context, com.google.android.material.R.attr.colorOnPrimary))
                    titleView.typeface = originType
                    iconView.setColorFilter(Utils.getColorFromAttr(context, com.google.android.material.R.attr.colorOnPrimary))
                }
            }
        var checkable: Boolean = false
        var onClick: OnClickListener? = null

        lateinit var originType: Typeface

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
            inflate(context, R.layout.layout_column_layout_item_card, this)
            originType = titleView.typeface
            orientation = HORIZONTAL
            setOnClickListener {
                onClick?.onClick(this)
                if (checkable) {
                    checked = true
                }
            }
        }

        fun setTitle(title: CharSequence) {
            findViewWithTag<TextView>("title")?.text = title
        }

        fun setIcon(icon: Bitmap) {
            iconView.layoutParams.height = Utils.dp2px(context, 28f)
            iconView.setImageBitmap(icon)
            iconView.colorFilter = null
        }
        fun setIcon(icon: Int) {
            iconView.layoutParams.height = Utils.dp2px(context, 28f)
            iconView.setImageResource(icon)
            iconView.setColorFilter(Utils.getColorFromAttr(context, com.google.android.material.R.attr.colorOnPrimary))
        }
        fun setIconBig(icon: Bitmap) {
            setIcon(icon)
            iconView.layoutParams.height = Utils.dp2px(context, 42f)
            iconView.colorFilter = null
        }
        fun setIconBig(icon: Int) {
            setIcon(icon)
            iconView.layoutParams.height = Utils.dp2px(context, 42f)
            iconView.setColorFilter(Utils.getColorFromAttr(context, com.google.android.material.R.attr.colorOnPrimary))
        }

        fun setDescription(description: CharSequence) {
            if (description.isEmpty()) {
                findViewWithTag<TextView>("description")?.visibility = View.GONE
            } else findViewWithTag<TextView>("description")?.visibility = View.VISIBLE
            findViewWithTag<TextView>("description")?.text = description
        }
    }
}