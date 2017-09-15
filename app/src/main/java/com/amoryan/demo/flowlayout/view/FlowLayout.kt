package com.amoryan.demo.flowlayout.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.amoryan.demo.flowlayout.R

/**
 * @author: Domino
 * @className: FlowLayout
 * @description: 流式布局
 * @createTime: 2017/9/14 下午8:12
 */
class FlowLayout : ViewGroup {

    private lateinit var mContext: Context

    private var mHorizontalSpace: Float = 0f
    private var mVerticalSpace: Float = 0f

    private var mViews = ArrayList<ArrayList<View>>()

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView(context, attributeSet)
    }

    private fun initView(context: Context, attributeSet: AttributeSet?) {
        mContext = context

        var attrs = mContext.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout)
        mHorizontalSpace = attrs.getDimension(R.styleable.FlowLayout_horizontalSpace, 0f)
        mVerticalSpace = attrs.getDimension(R.styleable.FlowLayout_verticalSpace, 0f)
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
        /*horizontal方向的间距为控件个数-1*/
        totalWidth += mHorizontalSpace.toInt() * (childCount - 1)
        return totalWidth
    }

    private fun getChildTotalHeight(maxWidth: Int): Int {

        var totalHeight = paddingTop + paddingBottom
        var colIndex = 0
        var rowIndex = 0
        var rowWidth = paddingLeft + paddingRight
        var rowHeight = 0

        for (i in 0..(childCount - 1)) {
            val child = getChildAt(i)
            val lp = child.layoutParams as MarginLayoutParams

            if (View.GONE == child.visibility) continue

            val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
            val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

            if (colIndex == 0) {
                if (rowWidth + childWidth > maxWidth) {
                    totalHeight += childHeight
                    if (rowIndex != 0) {
                        totalHeight += mVerticalSpace.toInt()
                    }
                    rowWidth = paddingLeft + paddingRight
                    rowHeight = 0
                    rowIndex++
                } else {
                    colIndex++
                    rowWidth += childWidth
                    if (rowIndex == 0) {
                        rowHeight = Math.max(rowHeight, childHeight)
                    } else {
                        rowHeight = Math.max(rowHeight, childHeight + mVerticalSpace.toInt())
                    }
                }
            } else {
                if (rowWidth + childWidth + mHorizontalSpace.toInt() > maxWidth) {
                    totalHeight += rowHeight
                    rowWidth = paddingLeft + paddingRight + childWidth
                    rowHeight = childHeight + mVerticalSpace.toInt()
                    colIndex = 1
                    rowIndex++
                } else {
                    colIndex++
                    rowWidth += childWidth + mHorizontalSpace.toInt()
                    if (rowIndex == 0) {
                        rowHeight = Math.max(rowHeight, childHeight)
                    } else {
                        rowHeight = Math.max(rowHeight, childHeight + mVerticalSpace.toInt())
                    }
                }
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

            for (i in 0..(lineViews.size - 1)) {
                val view = lineViews[i]
                val lp = view.layoutParams as MarginLayoutParams

                var left = rowLeft + lp.leftMargin
                val top = rowTop + lp.topMargin
                var space = view.measuredWidth + lp.leftMargin + lp.rightMargin
                if (i != 0) {/*不是第一个控件就加上间距*/
                    left += mHorizontalSpace.toInt()
                    space += mHorizontalSpace.toInt()
                }
                view.layout(left, top, left + view.measuredWidth, top + view.measuredHeight)
                rowLeft += space
            }
            rowTop += getRowMaxHeight(i)
        }
    }

    private fun layoutChild() {
        var lineViews = ArrayList<View>()
        var colIndex = 0
        var rowWidth = paddingLeft + paddingRight

        for (i in 0..(childCount - 1)) {
            val child = getChildAt(i)
            val lp = child.layoutParams as MarginLayoutParams

            if (View.GONE == child.visibility) continue

            val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin

            if (colIndex == 0) {/*这一行的第一个控件是不用添加间距的*/
                if (rowWidth + childWidth > measuredWidth) {
                    lineViews.add(child)
                    mViews.add(lineViews)
                    lineViews = ArrayList<View>()
                    rowWidth = paddingLeft + paddingRight
                } else {
                    colIndex++
                    rowWidth += childWidth
                    lineViews.add(child)
                }
            } else {/*一行的非第一个控件需要添加间距*/
                if (rowWidth + childWidth + mHorizontalSpace > measuredWidth) {
                    mViews.add(lineViews)
                    lineViews = ArrayList<View>()
                    lineViews.add(child)
                    rowWidth = paddingLeft + paddingRight + childWidth
                    colIndex = 1
                } else {
                    colIndex++
                    rowWidth += childWidth + mHorizontalSpace.toInt()
                    lineViews.add(child)
                }
            }
        }
        mViews.add(lineViews)
    }

    private fun getRowMaxHeight(rowIndex: Int): Int {
        val views = mViews[rowIndex]
        var maxHeight = 0
        for (view in views) {
            val lp = view.layoutParams as MarginLayoutParams
            maxHeight = Math.max(maxHeight, view.measuredHeight + lp.topMargin + lp.bottomMargin)
        }
        if (rowIndex < mViews.size) {
            maxHeight += mVerticalSpace.toInt()
        }
        return maxHeight
    }

    override fun generateLayoutParams(attrs: AttributeSet?) = MarginLayoutParams(mContext, attrs)
}