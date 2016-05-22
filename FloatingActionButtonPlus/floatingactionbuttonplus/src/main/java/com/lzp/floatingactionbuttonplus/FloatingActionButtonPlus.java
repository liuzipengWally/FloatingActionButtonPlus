package com.lzp.floatingactionbuttonplus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by liuzipeng on 15/11/27.
 * FloatingActionButtonPlus是一个基于Google官方support library中的FloatingActionButton与CardView的一个
 * 二次封装，目的为实现类似Google Inbox中带有多个子Fab的效果。由于是使用了官方的FloatingActionButton，因为阴影绘制
 * 方式的不同，在5.0一下系统和5.0以上系统的显示效果有一定差别。后续会做改进。
 * Api版本最低兼容到Api14（minSdkVersion 14）
 * 提供了四个position，分别为POS_LEFT_TOP，POS_LEFT_BOTTOM，POS_RIGHT_TOP，POS_RIGHT_BOTTOM。可在xml中使用
 * {@code position}去设置。也可在java代码中通过setPosition方法去设置{@link #setPosition(int)}
 * 本次添加了3种Animation，可直接在XML中使用{@code animationMode}去设置，也可在java代码中使用
 * setAnimation方法设置{@link #setAnimation(int)}
 * 分别为ANIM_FADE，ANIM_SCALE，ANIM_BOUNCE。后续会添加更多。
 * 动画的duration可在xml中通过{@code animationDuration}去设置，也可在java代码中通过{@link #setAnimationDuration(int)}去设置
 * 主Fab的Icon可在Xml中通过{@code switchFabIcon}来设置,java代码中通过{@link #setContentIcon(Drawable)}设置
 * 主Fab的颜色可在XML中通过{@code switchFabColor}来设置，java代码中通过{@link #setSwitchFabColor(ColorStateList)}设置
 */
public class FloatingActionButtonPlus extends ViewGroup {
    public static final int POS_LEFT_TOP = 0;
    public static final int POS_LEFT_BOTTOM = 1;
    public static final int POS_RIGHT_TOP = 2;
    public static final int POS_RIGHT_BOTTOM = 3;

    public static final int ANIM_FADE = 0;
    public static final int ANIM_SCALE = 1;
    public static final int ANIM_BOUNCE = 2;
    public static final int ANIM_ZHIHU = 3;

    private float mSwitchFabRotateVal = 45F;
    private int mAnimationDuration;

    private int mPosition;
    private int mAnimation;
    private int mBackgroundColor;
    private ColorStateList mFabColor;
    private Drawable mIcon;

    private int mWidth;
    private int mHeight;

    private FloatingActionButton mSwitchFab;
    private View mBackView;

    private boolean mStatus;
    private boolean mSwitchFabStatus = true;
    private boolean mFirstEnter = true;

    private OnItemClickListener mOnItemClickListener;
    private OnSwitchFabClickListener mOnSwitchFabClickListener;

    public interface OnSwitchFabClickListener {
        void onClick();
    }

    /**
     * 设置主Fab的点击时间，该点击事件只在展开前会响应
     *
     * @param onSwitchFabClickListener 主Fab的点击事件接口
     */
    public void setOnSwitchFabClickListener(OnSwitchFabClickListener onSwitchFabClickListener) {
        mOnSwitchFabClickListener = onSwitchFabClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(FabTagLayout tagView, int position);
    }

    /**
     * 设置每一item的点击事件，每一个item中的Fab的Tag均会会响应这同一个事件
     *
     * @param itemClickListener 每一个item的点击事件接口
     */
    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        mOnItemClickListener = itemClickListener;
    }

    public FloatingActionButtonPlus(Context context) {
        this(context, null);
    }

    public FloatingActionButtonPlus(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionButtonPlus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttributes(context, attrs);
        settingsView(context);
    }

    private void getAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButtonPlus);
        mPosition = typedArray.getInt(R.styleable.FloatingActionButtonPlus_position, POS_RIGHT_BOTTOM);
        mAnimation = typedArray.getInt(R.styleable.FloatingActionButtonPlus_animationMode, ANIM_SCALE);
        mBackgroundColor = typedArray.getColor(R.styleable.FloatingActionButtonPlus_mBackgroundColor, 0xf2ffffff);
        mFabColor = typedArray.getColorStateList(R.styleable.FloatingActionButtonPlus_switchFabColor);
        mIcon = typedArray.getDrawable(R.styleable.FloatingActionButtonPlus_switchFabIcon);
        mAnimationDuration = typedArray.getInt(R.styleable.FloatingActionButtonPlus_animationDuration, 150);

        typedArray.recycle();
    }

    private void settingsView(Context context) {
        View backView = new View(context);
        backView.setBackgroundColor(mBackgroundColor);
        backView.setAlpha(0);
        addView(backView);

        mSwitchFab = new FloatingActionButton(context);
        mSwitchFab.setBackgroundTintList(mFabColor);
        mSwitchFab.setImageDrawable(mIcon);
        addView(mSwitchFab);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mStatus;
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
        if (mFirstEnter) {
            MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
            layoutParams.width = getMeasuredWidth();
            layoutParams.height = getMeasuredHeight() - 1;
            setLayoutParams(layoutParams);

            mFirstEnter = false;
        }

        Log.d("FloatingActionButtonPlu", getMeasuredWidth() + " " + getMeasuredHeight());

        if (changed) {
            layoutSwitchFab();
            layoutBackView();

            int childCount = getChildCount();
            for (int i = 0, j = childCount - 2; i < j; i++) {
                FabTagLayout childView = (FabTagLayout) getChildAt(i + 2);
                mWidth = childView.getLayoutParams().width;
                mHeight = childView.getLayoutParams().height;
                childView.setScene(true);
                childView.setVisibility(INVISIBLE);

                int childWidth = childView.getMeasuredWidth();
                int childHeight = childView.getMeasuredHeight();

                int mFabHeight;
                int supportMargin;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    mFabHeight = mSwitchFab.getMeasuredHeight() + dp2px(20);
                    supportMargin = 0;
                } else {
                    mFabHeight = mSwitchFab.getMeasuredHeight();
                    supportMargin = dp2px(8);
                }

                int fl = 0 + supportMargin, ft = 0;

                switch (mPosition) {
                    case POS_LEFT_BOTTOM:
                        childView.setOrientation(FabTagLayout.TO_LEFT);
                        ft = getMeasuredHeight() - (mFabHeight + childHeight * (i + 1));
                        break;
                    case POS_LEFT_TOP:
                        childView.setOrientation(FabTagLayout.TO_LEFT);
                        ft = mFabHeight + childHeight * i;
                        break;
                    case POS_RIGHT_TOP:
                        ft = mFabHeight + childHeight * i;
                        fl = getMeasuredWidth() - childWidth - supportMargin;
                        break;
                    case POS_RIGHT_BOTTOM:
                        ft = getMeasuredHeight() - (mFabHeight + childHeight * (i + 1));
                        fl = getMeasuredWidth() - childWidth - supportMargin;
                        break;
                }

                childView.layout(fl, ft, fl + childWidth, ft + childHeight);
                bindChildEvents(childView, i);
                prepareAnim(childView, i);
            }
        }
    }

    /**
     * 布局完了之后要准备动画，设置好每个View在第一次动画开始前的初始值
     *
     * @param childView
     * @param i
     */
    private void prepareAnim(FabTagLayout childView, int i) {
        switch (mAnimation) {
            case ANIM_BOUNCE:
                childView.setTranslationY(50);
                break;
            case ANIM_SCALE:
                childView.setScaleX(0f);
                childView.setScaleY(0f);
                break;
        }
    }

    private void bindChildEvents(final FabTagLayout childView, final int position) {
        childView.setFabOnClickListener(new FabTagLayout.FabOnClickListener() {
            @Override
            public void onClick() {
                rotateSwitchFab();
                showBackground();
                changeStatus();
                closeItems();

                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(childView, position);
                }
            }
        });

        childView.setTagOnClickListener(new FabTagLayout.TagOnClickListener() {
            @Override
            public void onClick() {
                rotateSwitchFab();
                showBackground();
                changeStatus();
                closeItems();

                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(childView, position);
                }
            }
        });
    }

    private void layoutSwitchFab() {
        int l;
        int t;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            l = dp2px(16);
            t = dp2px(16);
        } else {
            l = 0;
            t = 0;
        }

        int width = mSwitchFab.getMeasuredWidth();
        int height = mSwitchFab.getMeasuredHeight();

        switch (mPosition) {
            case POS_LEFT_BOTTOM:
                t = getMeasuredHeight() - height - t;
                break;
            case POS_RIGHT_TOP:
                l = getMeasuredWidth() - width - l;
                break;
            case POS_RIGHT_BOTTOM:
                l = getMeasuredWidth() - width - l;
                t = getMeasuredHeight() - height - t;
                break;
        }

        mSwitchFab.layout(l, t, l + width, t + height);

        bindSwitchFabEvent();
    }

    private void layoutBackView() {
        mBackView = getChildAt(0);
        mBackView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    private void bindSwitchFabEvent() {
        mSwitchFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateSwitchFab();
                showBackground();
                changeStatus();

                if (mStatus) {
                    openItems();
                    if (mOnSwitchFabClickListener != null) {
                        mOnSwitchFabClickListener.onClick();
                    }
                } else {
                    closeItems();
                }
            }
        });
    }

    private void openItems() {
        switch (mAnimation) {
            case ANIM_BOUNCE:
                bounce();
                break;
            case ANIM_FADE:
                fade();
                break;
            case ANIM_SCALE:
                scale();
                break;
        }
    }


    private void scale() {
        for (int i = 2; i < getChildCount(); i++) {
            View view = getChildAt(i);
            view.setVisibility(VISIBLE);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0F, 1F);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0F, 1F);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0F, 1F);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY, alpha);
            animatorSet.setDuration(mAnimationDuration);
            animatorSet.setStartDelay(i * 40);
            animatorSet.setInterpolator(new OvershootInterpolator());
            animatorSet.start();
        }
    }

    private void fade() {
        for (int i = 2; i < getChildCount(); i++) {
            View view = getChildAt(i);
            view.setVisibility(VISIBLE);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0F, 1F);
            alpha.setDuration(mAnimationDuration);
            alpha.setStartDelay(i * 40);
            alpha.setInterpolator(new OvershootInterpolator());
            alpha.start();
        }
    }

    private void bounce() {
        for (int i = 2; i < getChildCount(); i++) {
            View view = getChildAt(i);
            view.setVisibility(VISIBLE);
            ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY", 50F, 0F);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0F, 1F);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(translationY, alpha);
            animatorSet.setDuration(mAnimationDuration);
            animatorSet.setInterpolator(new BounceInterpolator());
            animatorSet.start();
        }
    }

    private void closeItems() {
        for (int i = 2; i < getChildCount(); i++) {
            ObjectAnimator alpha = ObjectAnimator.ofFloat(getChildAt(i), "alpha", 1F, 0F);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alpha);
            animatorSet.setDuration(mAnimationDuration);
            animatorSet.start();

            hideChild(animatorSet, getChildAt(i));
        }
    }

    private void showBackground() {
        ObjectAnimator backAlpha = mStatus ? ObjectAnimator.ofFloat(mBackView, "alpha", 0.9F, 0F) :
                ObjectAnimator.ofFloat(mBackView, "alpha", 0F, 0.9F);
        backAlpha.setDuration(150);
        backAlpha.start();
    }

    private void changeStatus() {
        mStatus = mStatus ? false : true;
    }

    private void rotateSwitchFab() {
        ObjectAnimator animator = mStatus ? ObjectAnimator.ofFloat(mSwitchFab, "rotation", mSwitchFabRotateVal, 0F) :
                ObjectAnimator.ofFloat(mSwitchFab, "rotation", 0F, mSwitchFabRotateVal);
        animator.setDuration(150);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private void hideChild(AnimatorSet animatorSet, final View childView) {
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                childView.setVisibility(INVISIBLE);
            }
        });
    }

    /**
     * 设置动画持续时间
     *
     * @param duration 动画持续时间，毫秒值
     */
    public void setAnimationDuration(int duration) {
        mAnimationDuration = duration;
    }

    /**
     * 设置主Fab在被点击的时候旋转的角度
     *
     * @param val 主Fab旋转时的度数
     */
    public void setRotateValues(float val) {
        mSwitchFabRotateVal = val;
    }

    /**
     * 设置主Fab的背景颜色
     *
     * @param color 颜色 是一个ColorStateList对象
     */
    public void setSwitchFabColor(ColorStateList color) {
        mSwitchFab.setBackgroundTintList(color);
    }

    /**
     * 设置主Fab的Icon图片
     *
     * @param icon 主Fab的Icon图片，Drawable对象
     */
    public void setContentIcon(Drawable icon) {
        mSwitchFab.setImageDrawable(icon);
    }

    /**
     * 设置item展开的动画（Animation），可选值有
     * FloatingActionButtonPlus.ANIM_BOUNCE
     * FloatingActionButtonPlus.ANIM_FADE
     * FloatingActionButtonPlus.ANIM_SCALE
     * FloatingActionButtonPlus.ANIM_ZHIHU
     *
     * @param animationMode 动画模式
     */
    public void setAnimation(int animationMode) {
        mAnimation = animationMode;
    }

    /**
     * 设置在屏幕中的位置，可选值有
     * FloatingActionButtonPlus.POS_LEFT_TOP
     * FloatingActionButtonPlus.POS_LEFT_BOTTOM
     * FloatingActionButtonPlus.POS_RIGHT_BOTTOM
     * FloatingActionButtonPlus.POS_RIGHT_TOP
     *
     * @param position Fab所处的位置
     */
    public void setPosition(int position) {
        mPosition = position;
    }

    /**
     * 隐藏FloatingActionButtonPlus
     */
    public void hideFab() {
        if (mSwitchFabStatus) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(mSwitchFab, "scaleX", 1F, 0F);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(mSwitchFab, "scaleY", 1F, 0F);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(mSwitchFab, "alpha", 1F, 0F);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY, alpha);
            animatorSet.setDuration(300);
            animatorSet.setInterpolator(new OvershootInterpolator());
            animatorSet.start();

            hideChild(animatorSet, mSwitchFab);
            mSwitchFabStatus = false;
        }
    }

    /**
     * 显示FloatingActionButtonPlus
     */
    public void showFab() {
        if (!mSwitchFabStatus) {
            mSwitchFab.setVisibility(VISIBLE);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(mSwitchFab, "scaleX", 0F, 1F);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(mSwitchFab, "scaleY", 0F, 1F);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(mSwitchFab, "alpha", 0F, 1F);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY, alpha);
            animatorSet.setDuration(300);
            animatorSet.setInterpolator(new OvershootInterpolator());
            animatorSet.start();
            mSwitchFabStatus = true;
        }
    }

    /**
     * 返回当前主Fab的显示状态
     *
     * @return 显示的时候返回true，隐藏的时候返回false
     */
    public boolean getSwitchFabDisplayState() {
        return mSwitchFabStatus;
    }

    /**
     * 将数值转换为DP
     *
     * @param value
     * @return
     */
    private int dp2px(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
