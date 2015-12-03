package com.lzp.floatingactionbuttonplus;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by liuzipeng on 15/11/26.
 *
 * 这是一个CardView，中间放了一个textView。此View作为FloatingActionButtonPlus的label标签
 */
public class TagView extends CardView {
    private TextView mTextView;

    public TagView(Context context) {
        super(context);

        mTextView = new TextView(context);
        mTextView.setSingleLine(true);
    }

    public TagView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置字体尺寸
     * @param size
     */
    protected void setTextSize(float size) {
        mTextView.setTextSize(size);
    }

    /**
     * 设置字体颜色
     * @param color
     */
    protected void setTextColor(int color) {
        mTextView.setTextColor(color);
    }

    /**
     * 设置字体
     * @param text
     */
    protected void setTagText(String text) {
        mTextView.setText(text);
        positionView();
    }

    private void positionView() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        int l = convertDp(8), r = convertDp(8), t = convertDp(4), b = convertDp(4);
        layoutParams.setMargins(l, t, r, b);
        addView(mTextView, layoutParams);
    }

    /**
     * 将数值转换为DP
     *
     * @param value
     * @return
     */
    private int convertDp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
