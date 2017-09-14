package com.amoryan.demo.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Domino
 * @className: FlowLayout
 * @description: 流式布局
 * @createTime: 2017/9/14 下午2:32
 */
public final class FlowLayout_JAVA extends ViewGroup {

    private Map<Integer, List<View>> mViews;

    public FlowLayout_JAVA(Context context) {
        super(context);
        initView();
    }

    public FlowLayout_JAVA(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mViews = new HashMap<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        measureChildBeforeLayout(widthMeasureSpec, heightMeasureSpec);

        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int wResult = wSpecSize;

        switch (wSpecMode) {
            case MeasureSpec.AT_MOST:
                wResult = Math.min(wResult, getChildTotalWidth());
                break;
        }

        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int hResult = hSpecSize;

        switch (hSpecMode) {
            case MeasureSpec.AT_MOST:
                hResult = Math.min(hResult, getChildTotalHeight(wResult));
                break;
        }

        setMeasuredDimension(wResult, hResult);
    }

    private void measureChildBeforeLayout(int widthMeasureSpec, int heightMeasureSpec) {
        /*逐个测量子控件*/
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            if (View.GONE == child.getVisibility()) continue;

            int widthUsed = getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin;
            int heightUsed = getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin;

            measureChildWithMargins(child, widthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
        }
    }

    /*获取总的宽度*/
    private int getChildTotalWidth() {
        int totalWidth = getPaddingLeft() + getPaddingRight();/*首先加上内边距*/
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            if (View.GONE == child.getVisibility()) continue;
            totalWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
        }
        return totalWidth;
    }

    /*获取总的高度*/
    private int getChildTotalHeight(int maxWidth) {
        int totalHeight = getPaddingTop() + getPaddingBottom();/*首先加上内边距*/
        int colIndex = 0;
        int rowWidth = getPaddingLeft() + getPaddingRight();
        int rowHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            if (View.GONE == child.getVisibility()) continue;

            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (rowWidth + childWidth > maxWidth) {
                /*这里分两种情况，这一行的第一个控件，或者这一行的非第一个控件*/
                if (colIndex == 0) {/*这一行的第一个，这个控件放在这一行，并且进行换行*/
                    totalHeight += childHeight;
                    rowWidth = getPaddingLeft() + getPaddingRight();
                    rowHeight = 0;
                } else {/*不是这一行的第一个控件*/
                    totalHeight += rowHeight;
                    rowWidth = getPaddingLeft() + getPaddingRight() + childWidth;
                    rowHeight = childHeight;
                    colIndex = 1;
                }
            } else {
                colIndex++;
                rowWidth += childWidth;
                rowHeight = Math.max(rowHeight, childHeight);
            }
        }
        totalHeight += rowHeight;/*最后一行*/
        return totalHeight;
    }

    @Override
    protected void onLayout(boolean b, int left, int top, int right, int bottom) {
        mViews.clear();
        layoutChild();
        /*确定子控件的位置*/
        int rowTop = getPaddingTop();
        for (int i = 0; i < mViews.size(); i++) {
            List<View> lineViews = mViews.get(i);
            int rowLeft = getPaddingLeft();
            for (View view : lineViews) {
                MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
                int l = rowLeft + lp.leftMargin;
                int t = rowTop + lp.topMargin;
                view.layout(l, t, l + view.getMeasuredWidth(), t + view.getMeasuredHeight());
                rowLeft += view.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            }
            rowTop += getRowMaxHeight(i);
        }
    }

    /*布局子控件*/
    private void layoutChild() {
        int rowIndex = 0;
        List<View> lineViews = new ArrayList<>();
        int colIndex = 0;
        int rowWidth = getPaddingLeft() + getPaddingRight();

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            if (View.GONE == child.getVisibility()) continue;

            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;

            if (rowWidth + childWidth > getMeasuredWidth()) {
                if (colIndex == 0) {
                    lineViews.add(child);
                    mViews.put(rowIndex, lineViews);
                    lineViews = new ArrayList<>();
                    rowWidth = getPaddingLeft() + getPaddingRight();
                } else {
                    mViews.put(rowIndex, lineViews);
                    lineViews = new ArrayList<>();
                    lineViews.add(child);
                    rowWidth = getPaddingLeft() + getPaddingRight() + childWidth;
                    colIndex = 1;
                }
                rowIndex++;
            } else {
                colIndex++;
                rowWidth += childWidth;
                lineViews.add(child);
            }
        }
        mViews.put(rowIndex, lineViews);
    }

    private int getRowMaxHeight(int rowIndex) {
        List<View> views = mViews.get(rowIndex);
        int maxHeight = 0;
        for (View view : views) {
            MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
            maxHeight = Math.max(maxHeight, view.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
        }
        return maxHeight;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }
}
