package com.mohitatray.verticallayout

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.util.LayoutDirection.LTR
import android.util.LayoutDirection.RTL
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.mohitatray.verticallayout.VerticalLayout.LayoutParams.Companion.DIRECTION_END_TO_START
import com.mohitatray.verticallayout.VerticalLayout.LayoutParams.Companion.DIRECTION_START_TO_END
import kotlin.math.max

/**
 * Shows the views inside it vertically (either rotated +90 degrees or -90 degrees). If more than 1
 * child is added, all of them are shown one over the other (just like [android.widget.FrameLayout])
 * vertically.
 */
class VerticalLayout : ViewGroup {

    /**
     * LayoutParams for VerticalLayout.
     */
    class LayoutParams : MarginLayoutParams {
        companion object {
            /**
             * Direction value which arranges the views inside this layout having their top towards
             * start of parent and bottom towards end of parent.
             */
            const val DIRECTION_START_TO_END = 0

            /**
             * Direction value which arranges the views inside this layout having their top towards
             * end of parent and bottom towards start of parent.
             */
            const val DIRECTION_END_TO_START = 1
        }

        /**
         * Gravity for child view. Start top is the default.
         */
        var gravity = Gravity.START.or(Gravity.TOP)

        /**
         * Direction in which child view is arranged. This determines whether the child is rotated
         * +90 degrees or -90 degrees. Must be either [DIRECTION_START_TO_END] or
         * [DIRECTION_END_TO_START]. Default value: [DIRECTION_START_TO_END].
         */
        var direction: Int = DIRECTION_START_TO_END

        constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet) {
            val ta = context.obtainStyledAttributes(attributeSet, R.styleable.VerticalLayout_Layout)
            gravity = ta.getInt(R.styleable.VerticalLayout_Layout_android_layout_gravity, gravity)
            direction = ta.getInt(R.styleable.VerticalLayout_Layout_layout_direction, direction)
            ta.recycle()
        }

        constructor(width: Int, height: Int): super(width, height)

        constructor(source: ViewGroup.LayoutParams?): super(source) {
            if (source is LayoutParams) {
                gravity = source.gravity
                direction = source.direction
            }
        }
    }

    /**
     * Temp rect used by [onLayout] call which will represent frame in which this layout is laid
     * out.
     */
    private val myLayoutRect = Rect()

    /**
     * Temp rect used by [onLayout] call which will represent frame in which child layout is laid
     * out.
     */
    private val childLayoutRect = Rect()

    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int)
            : super(context, attributeSet, defStyleAttr)

    override fun shouldDelayChildPressedState() = false

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (child != null) {
            val direction = (params as LayoutParams).direction
            val layoutDirection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                context.resources.configuration.layoutDirection else View.LAYOUT_DIRECTION_LTR

            // Set rotation of child
            child.rotation =
                if (
                    (direction == DIRECTION_START_TO_END && layoutDirection == LTR)
                    || (direction == DIRECTION_END_TO_START && layoutDirection == RTL)
                ) -90f else 90f
        }
        super.addView(child, index, params)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Iterate children
        val childCount = childCount
        var childState = 0
        var maxWidth = 0
        var maxHeight = 0
        for (i in 0 until childCount) {
            // Get child
            val childView = getChildAt(i)

            if (childView.visibility != View.GONE) {
                val childLayoutParams = childView.layoutParams as LayoutParams
                val childMarginWidth = childLayoutParams.run { leftMargin + rightMargin }
                val childMarginHeight = childLayoutParams.run { topMargin + bottomMargin }

                // Measure child
                measureChildWithMargins(childView, heightMeasureSpec, 0, widthMeasureSpec, 0)

                // Calculate measured width and height
                val childMeasuredWidth = childView.measuredWidth
                val childMeasuredHeight = childView.measuredHeight

                // Combine to maxWidth and maxHeight
                maxWidth = max(maxWidth, childMeasuredHeight + childMarginHeight)
                maxHeight = max(maxHeight, childMeasuredWidth + childMarginWidth)

                // Combine to childState
                childState = combineMeasuredStates(childState, childView.measuredState)
            }
        }

        // Check against minimum width and height
        maxWidth = max(maxWidth, suggestedMinimumWidth)
        maxHeight = max(maxHeight, suggestedMinimumHeight)

        setMeasuredDimension(
            resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            resolveSizeAndState(maxHeight, heightMeasureSpec, childState.shl(MEASURED_HEIGHT_STATE_SHIFT))
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val layoutLeft = paddingLeft
        val layoutTop = paddingTop
        val layoutRight = r - l - paddingRight
        val layoutBottom = b - t - paddingBottom

        val childCount = childCount
        for (i in 0 until childCount) {
            val childView = getChildAt(i)

            if (childView.visibility != View.GONE) {
                val childMeasuredWidth = childView.measuredWidth
                val childMeasuredHeight = childView.measuredHeight
                val layoutParams = childView.layoutParams as LayoutParams
                val isChildRotationClockwise = childView.rotation > 0

                layoutParams.run { myLayoutRect.set(
                    layoutLeft + if (isChildRotationClockwise) bottomMargin else topMargin,
                    layoutTop + if (isChildRotationClockwise) leftMargin else rightMargin,
                    layoutRight - if (isChildRotationClockwise) topMargin else bottomMargin,
                    layoutBottom - if (isChildRotationClockwise) rightMargin else leftMargin
                )}

                Gravity.apply(
                    layoutParams.gravity,
                    childMeasuredHeight,
                    childMeasuredWidth,
                    myLayoutRect,
                    childLayoutRect
                )

                // Consider myLayoutRect & childLayoutRect width as height and height as width
                // and then center myLayoutRect in original myLayoutRect by applying offset making a
                // plus but give childLayoutRect to childView.layout()
                val childLeft = childLayoutRect.run { ((width() - height()) / 2) + left }
                val childTop = childLayoutRect.run { ((height() - width()) / 2) + top }
                childView.layout(
                    childLeft,
                    childTop,
                    childLeft + childMeasuredWidth,
                    childTop + childMeasuredHeight
                )
            }
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

}