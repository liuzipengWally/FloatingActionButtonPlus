package com.lzp.floatingactionbuttonplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by liuzipeng on 15/11/23.
 * 这个ViewGroup的作用是为了包裹FloatingActionButton为其提供一个label标签，
 * 该label标签是support库中的CardView。
 */
public class FabTagLayout extends ViewGroup {
    private String mTagText;
    private TagView mTagView;

    /*这两个常量表示了当前Fab在屏幕中的位置，用来决定标签应该在左还是右，
    TO_RIGHT应该在左,TO_LEFT应该在右，默认为TO_RIGHT*/
    public static final int TO_RIGHT = 0;
    public static final int TO_LEFT = 1;
    private int mOrientation = TO_RIGHT;

    /*这个变量表示是单独使用，还是在FloatingActionButtonPlus中使用，以通过不同的方式获得LayoutParams*/
    private boolean mScene;

    private TagOnClickListener mTagOnClickListener;
    private FabOnClickListener mFabOnClickListener;

    public interface TagOnClickListener {
        void onClick();
    }

    public interface FabOnClickListener {
        void onClick();
    }

    public void setTagOnClickListener(TagOnClickListener onClickListener) {
        mTagOnClickListener = onClickListener;
    }

    public void setFabOnClickListener(FabOnClickListener onClickListener) {
        mFabOnClickListener = onClickListener;
    }

    public FabTagLayout(Context context) {
        this(context, null);
    }

    public FabTagLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FabTagLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttributes(context, attrs);
        settingsView(context);
    }

    private void getAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FabTagLayout);
        mTagText = typedArray.getString(R.styleable.FabTagLayout_tagText);
        typedArray.recycle();
    }

    private void settingsView(Context context) {
        mTagView = new TagView(context);
        mTagView.setTagText(mTagText);
        addView(mTagView);
    }

    /**
     * 设置tag显示的文字
     *
     * @param text 显示的文字
     */
    public void setTagText(String text) {
        mTagView.setTagText(text);
    }

    /**
     * 改变标标签的显示位置
     * 通过 TO_RIGHT or TO_LEFT来判断
     *
     * @param orientation FloatingActionButton所处的方向
     */
    public void setOrientation(int orientation) {
        mOrientation = orientation;
        invalidate();
    }

    /**
     * 设置使用场景，以通过不同方式获取LayoutParams
     *
     * @param scene 表示是在FloatingActionButton中被使用还是单独被使用
     */
    public void setScene(boolean scene) {
        mScene = scene;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int ChildCount = getChildCount();

        for (int i = 0; i < ChildCount; i++) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int groupWidth = getChildAt(0).getMeasuredWidth() + getChildAt(1).getMeasuredWidth() + convertDp(24 + 8 + 8);
        int groupHeight = Math.max(getChildAt(0).getMeasuredHeight(), getChildAt(1).getMeasuredHeight()) + convertDp(12);

        MarginLayoutParams params;

        if (mScene) {
            params = new MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            params = (MarginLayoutParams) getLayoutParams();
        }

        params.width = groupWidth;
        params.height = groupHeight;
        setLayoutParams(params);

        View tagView = getChildAt(0);
        View FabView = (View) getChildAt(1);

        int fabWidth = FabView.getMeasuredWidth();
        int fabHeight = FabView.getMeasuredHeight();
        int tagWidth = tagView.getMeasuredWidth();
        int tagHeight = tagView.getMeasuredHeight();

        int fl = 0, ft = 0, fr = 0, fb = 0, tl = 0, tt = 0, tr = 0, tb = 0;

        switch (mOrientation) {
            case TO_RIGHT:
                /*FAB*/
                fl = tagWidth + convertDp(16);
//                ft = convertDp(4);
                fr = fl + fabWidth;
                fb = ft + fabHeight;

                /*TAG*/
                tl = convertDp(8);
                tt = (fabHeight - tagHeight) / 2;
                tr = tl + tagWidth;
                tb = tt + tagHeight;
                break;
            case TO_LEFT:
                /*FAB*/
                fl = convertDp(24);
//                ft = convertDp(4);
                fr = fl + fabWidth;
                fb = ft + fabHeight;

                /*TAG*/
                tl = convertDp(32) + fabWidth;
                tt = (fabHeight - tagHeight) / 2 ;
                tr = tl + tagWidth;
                tb = tt + tagHeight;
                break;
        }

        FabView.layout(fl, ft, fr, fb);
        tagView.layout(tl, tt, tr, tb);

        bindEvents(tagView, FabView);
    }

    /**
     * 为fab和tag添加点击事件监听
     *
     * @param tagView
     * @param fabView
     */
    private void bindEvents(View tagView, View fabView) {
        tagView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTagOnClickListener != null) {
                    mTagOnClickListener.onClick();
                }
            }
        });

        fabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFabOnClickListener != null) {
                    mFabOnClickListener.onClick();
                }
            }
        });
    }

    /**
     * 将数值转换为DP
     * api<21则返回0
     *
     * @param value
     * @return
     */
    private int convertDp(int value) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
        } else {
            return 0;
        }
    }
}
