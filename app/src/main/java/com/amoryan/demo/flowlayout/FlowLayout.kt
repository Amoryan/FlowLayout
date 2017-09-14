package com.amoryan.demo.flowlayout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * @author: Domino
 * @className: FlowLayout
 * @description: 流式布局
 * @createTime: 2017/9/14 下午8:12
 */
class FlowLayout : ViewGroup {

    private lateinit var mContext: Context

    private var mViews = HashMap<Int, ArrayList<View>>()

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView(context, attributeSet)
    }

    private fun initView(context: Context, attributeSet: AttributeSet?) {
        mContext = context
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        measureChildBeforeLayout(widthMeasureSpec, heightMeasureSpec)

        val wSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        var wResult = wSpecSize

        when (wSpecMode) {
            MeasureSpec.AT_MOST -> {
                wResult = Math.min(wResult, getChildTotalWidth())
            }
        }

        val hSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        var hResult = hSpecSize

        when (hSpecMode) {
            MeasureSpec.AT_MOST -> {
                hResult = Math.min(hResult, getChildTotalHeight(wResult))
            }
        }

        setMeasuredDimension(wResult, hResult)
    }

    private fun measureChildBeforeLayout(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        for (i in 0..(childCount - 1)) {
            var child = getChildAt(i)
            var lp = child.layoutParams as MarginLayoutParams

            if (View.GONE == child.visibility) continue

            var widthUsed = paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin
            var heightUsed = paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin

            measureChildWithMargins(child, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed)
        }
    }

    private fun getChildTotalWidth(): Int {
        var totalWidth = paddingLeft + paddingRight
        for (i in 0..(childCount - 1)) {
            val child = getChildAt(i)
            var lp = child.layoutParams as MarginLayoutParams

            if (View.GONE == child.visibility) continue

            totalWidth += child.measuredWidth + lp.leftMargin + lp.rightMargin
        }
        return totalWidth
    }

    private fun getChildTotalHeight(maxWidth: Int): Int {

        var totalHeight = paddingTop + paddingBottom
        var colIndex = 0
        var rowWidth = paddingLeft + paddingRight
        var rowHeight = 0

        for (i in 0..(childCount - 1)) {
            val child = getChildAt(i)
            val lp = child.layoutParams as MarginLayoutParams

            if (View.GONE == child.visibility) continue

            val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
            val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

            if (rowWidth + childWidth > maxWidth) {
                if (colIndex == 0) {
                    totalHeight += childHeight
                    rowWidth = paddingLeft + paddingRight
                    rowHeight = 0
                } else {
                    totalHeight += rowHeight
                    rowWidth = paddingLeft + paddingRight + childWidth
                    rowHeight = childHeight
                    colIndex = 1
                }
            } else {
                colIndex++
                rowWidth += childWidth
                rowHeight = Math.max(rowHeight, childHeight)
            }
        }
        totalHeight += rowHeight
        return totalHeight
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mViews.clear()/*多次调用这个方法的情况*/

        layoutChild()

        var rowTop = paddingTop

        for (i in 0..(mViews.size - 1)) {
            val lineViews = mViews[i]
            var rowLeft = paddingLeft

            if (lineViews == null) continue

            for (view in lineViews) {
                val lp = view.layoutParams as MarginLayoutParams
                val left = rowLeft + lp.leftMargin
                val top = rowTop + lp.topMargin
                view.layout(left, top, left + view.measuredWidth, top + view.measuredHeight)
                rowLeft += view.measuredWidth + lp.leftMargin + lp.rightMargin
            }
            rowTop += getRowMaxHeight(i)
        }
    }

    private fun layoutChild() {
        var rowIndex = 0
        var lineViews = ArrayList<View>()
        var colIndex = 0
        var rowWidth = paddingLeft + paddingRight

        for (i in 0..(childCount - 1)) {
            val child = getChildAt(i)
            val lp = child.layoutParams as MarginLayoutParams

            if (View.GONE == child.visibility) continue

            val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin

            if (rowWidth + childWidth > measuredWidth) {
                if (colIndex == 0) {
                    lineViews.add(child)
                    mViews.put(rowIndex, lineViews)
                    lineViews = ArrayList<View>()
                    rowWidth = paddingLeft + paddingRight
                } else {
                    mViews.put(rowIndex, lineViews)
                    lineViews = ArrayList<View>()
                    lineViews.add(child)
                    rowWidth = paddingLeft + paddingRight + childWidth
                    colIndex = 1
                }
                rowIndex++
            } else {
                colIndex++
                rowWidth += childWidth
                lineViews.add(child)
            }
        }
        mViews.put(rowIndex, lineViews)
    }

    private fun getRowMaxHeight(rowIndex: Int): Int {
        val views = mViews[rowIndex]
        var maxHeight = 0
        if (views != null) {
            for (view in views) {
                val lp = view.layoutParams as MarginLayoutParams
                maxHeight = Math.max(maxHeight, view.measuredHeight + lp.topMargin + lp.bottomMargin)
            }
        }
        return maxHeight
    }

    override fun generateLayoutParams(attrs: AttributeSet?) = MarginLayoutParams(mContext, attrs)
}