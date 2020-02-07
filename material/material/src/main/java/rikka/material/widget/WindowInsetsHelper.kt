package rikka.material.widget

import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import rikka.material.R

open class WindowInsetsHelper private constructor(
        private val view: View,
        private var initialPaddingLeft: Int = view.paddingLeft,
        private var initialPaddingTop: Int = view.paddingTop,
        private var initialPaddingRight: Int = view.paddingRight,
        private var initialPaddingBottom: Int = view.paddingBottom,
        private val paddingSystemWindowsInsetsStart: Boolean,
        private val paddingSystemWindowsInsetsTop: Boolean,
        private val paddingSystemWindowsInsetsEnd: Boolean,
        private val paddingSystemWindowsInsetsBottom: Boolean,
        private val consumeSystemWindowsInsetsStart: Boolean,
        private val consumeSystemWindowsInsetsTop: Boolean,
        private val consumeSystemWindowsInsetsEnd: Boolean,
        private val consumeSystemWindowsInsetsBottom: Boolean) : OnApplyWindowInsetsListener {

    private var lastInsets: WindowInsetsCompat? = null

    open fun setInitialPadding(left: Int, top: Int, right: Int, bottom: Int) {
        initialPaddingLeft = left
        initialPaddingTop = top
        initialPaddingRight = right
        initialPaddingBottom = bottom

        lastInsets?.let { applyWindowInsets(it) }
    }

    open fun setInitialPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        val isRTL = view.layoutDirection == View.LAYOUT_DIRECTION_RTL
        if (isRTL) {
            setInitialPadding(start, top, end, bottom)
        } else {
            setInitialPadding(start, top, end, bottom)
        }
    }

    private fun applyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        val isRTL = view.layoutDirection == View.LAYOUT_DIRECTION_RTL

        val paddingSystemWindowsInsetsLeft = (!isRTL && paddingSystemWindowsInsetsStart) || (isRTL && paddingSystemWindowsInsetsEnd)
        val paddingSystemWindowsInsetsRight = (!isRTL && paddingSystemWindowsInsetsEnd) || (isRTL && paddingSystemWindowsInsetsStart)

        val paddingLeft = initialPaddingLeft + if (paddingSystemWindowsInsetsLeft) insets.systemWindowInsetLeft else 0
        val paddingTop = initialPaddingTop + if (paddingSystemWindowsInsetsTop) insets.systemWindowInsetTop else 0
        val paddingRight = initialPaddingRight + if (paddingSystemWindowsInsetsRight) insets.systemWindowInsetRight else 0
        val paddingBottom = initialPaddingBottom + if (paddingSystemWindowsInsetsBottom) insets.systemWindowInsetBottom else 0

        val consumeSystemWindowsInsetsLeft = (!isRTL && consumeSystemWindowsInsetsStart) || (isRTL && consumeSystemWindowsInsetsEnd)
        val consumeSystemWindowsInsetsRight = (!isRTL && consumeSystemWindowsInsetsEnd) || (isRTL && consumeSystemWindowsInsetsStart)

        val systemInsetsLeft = if (consumeSystemWindowsInsetsLeft) 0 else insets.systemWindowInsetLeft
        val systemInsetsTop = if (consumeSystemWindowsInsetsTop) 0 else insets.systemWindowInsetTop
        val systemInsetsRight = if (consumeSystemWindowsInsetsRight) 0 else insets.systemWindowInsetRight
        val systemInsetsBottom = if (consumeSystemWindowsInsetsBottom) 0 else insets.systemWindowInsetBottom

        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)

        return WindowInsetsCompat.Builder(insets)
                .setSystemWindowInsets(Insets.of(systemInsetsLeft, systemInsetsTop, systemInsetsRight, systemInsetsBottom))
                .build()
    }

    override fun onApplyWindowInsets(view: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        if (lastInsets == insets) {
            return insets
        }

        lastInsets = insets

        return applyWindowInsets(insets)
    }

    companion object {

        @JvmStatic
        fun attach(view: View, attrs: AttributeSet): WindowInsetsHelper? {
            val a = view.context.obtainStyledAttributes(attrs, R.styleable.WindowInsetsHelper, 0, 0)
            val systemUiVisibility = a.getInt(R.styleable.WindowInsetsHelper_systemUiVisibility, Int.MIN_VALUE)
            val systemWindowsInsetsStart = a.getInt(R.styleable.WindowInsetsHelper_systemWindowsInsetsStart, 0)
            val systemWindowsInsetsTop = a.getInt(R.styleable.WindowInsetsHelper_systemWindowsInsetsTop, 0)
            val systemWindowsInsetsEnd = a.getInt(R.styleable.WindowInsetsHelper_systemWindowsInsetsEnd, 0)
            val systemWindowsInsetsBottom = a.getInt(R.styleable.WindowInsetsHelper_systemWindowsInsetsBottom, 0)
            a.recycle()

            return attach(view, systemUiVisibility,
                    systemWindowsInsetsStart,
                    systemWindowsInsetsTop,
                    systemWindowsInsetsEnd,
                    systemWindowsInsetsBottom)
        }

        @JvmStatic
        fun attach(view: View, systemUiVisibility: Int,
                   systemWindowsInsetsStart: Int,
                   systemWindowsInsetsTop: Int,
                   systemWindowsInsetsEnd: Int,
                   systemWindowsInsetsBottom: Int): WindowInsetsHelper? {

            if (systemUiVisibility > 0) {
                view.systemUiVisibility = (view.systemUiVisibility or systemUiVisibility)
            }

            if (systemWindowsInsetsStart + systemWindowsInsetsTop + systemWindowsInsetsEnd + systemWindowsInsetsBottom == 0) {
                return null
            }

            val initialPaddingLeft: Int = view.paddingLeft
            val initialPaddingTop: Int = view.paddingTop
            val initialPaddingRight: Int = view.paddingRight
            val initialPaddingBottom: Int = view.paddingBottom

            val listener = WindowInsetsHelper(view,
                    initialPaddingLeft,
                    initialPaddingTop,
                    initialPaddingRight,
                    initialPaddingBottom,
                    systemWindowsInsetsStart and 1 == 1,
                    systemWindowsInsetsTop and 1 == 1,
                    systemWindowsInsetsEnd and 1 == 1,
                    systemWindowsInsetsBottom and 1 == 1,
                    systemWindowsInsetsStart and 2 == 2,
                    systemWindowsInsetsTop and 2 == 2,
                    systemWindowsInsetsEnd and 2 == 2,
                    systemWindowsInsetsBottom and 2 == 2)
            ViewCompat.setOnApplyWindowInsetsListener(view, listener)
            return listener
        }

    }
}