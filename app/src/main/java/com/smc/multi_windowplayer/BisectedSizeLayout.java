package com.smc.multi_windowplayer;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * @Author songminchao@bytedance.com
 * Please contact if you have any questions
 */
public class BisectedSizeLayout extends ViewGroup {

    int padding = 4;

    public BisectedSizeLayout(@NonNull Context context) {
        super(context);
    }

    public BisectedSizeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAnimate();
    }

    public BisectedSizeLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ObjectAnimatorBinding")
    private void initAnimate() {
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(500);

        PropertyValuesHolder animInX = PropertyValuesHolder.ofFloat("scaleX", 0.3f, 1f);
        PropertyValuesHolder animInY = PropertyValuesHolder.ofFloat("scaleY", 0.3f, 1f);
        ObjectAnimator animatorSetIn = ObjectAnimator.ofPropertyValuesHolder((Object) null, animInX, animInY);
        layoutTransition.setAnimator(LayoutTransition.APPEARING, animatorSetIn);


        PropertyValuesHolder animOutX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0f);
        PropertyValuesHolder animOutY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0f);
        ObjectAnimator animatorSetOut = ObjectAnimator.ofPropertyValuesHolder((Object) null, animOutX, animOutY);
        layoutTransition.setAnimator(LayoutTransition.DISAPPEARING, animatorSetOut);

        setLayoutTransition(layoutTransition);
    }

    @Override
    public void addView(View child) {
        this.addView(child, -1);
    }

    @Override
    public void addView(View child, int index) {
        if (child == null || getChildCount() == 6) {
            return;
        }
        super.addView(child, index);
    }

    private List<View> getVisibleChild() {
        List<View> childList = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() == VISIBLE) {
                childList.add(childView);
            }
        }
        return childList;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        List<View> childList = getVisibleChild();
        //第一行的横坐标起点
        int x1 = 0;

        //第二行的横坐标起点
        int x2 = 0;

        //第二行的纵坐标起点
        int y = 0;

        //子 view 的宽高
        int width = 0, height = 0;

        if (!childList.isEmpty()) {
            View child = childList.get(0);
            width = child.getMeasuredWidth();
            height = child.getMeasuredHeight();

            switch (childList.size()) {
                case 3:
                    x2 = (getWidth() - width) / 2;
                    break;
                case 5:
                    x2 = (getWidth() - padding - 2 * width) / 2;
                    break;
                default:
                    break;
            }
            y = padding + height;
        }

        for (int i = 0; i < childList.size(); i++) {
            View child = childList.get(i);

            //第一行
            if (x1 < getWidth() / 4 * 3) {
                childLayout(child, x1, 0, width, height);
                x1 = x1 + padding + width;
            }

            //第二行
            else {
                childLayout(child, x2, y, width, height);
                x2 = x2 + padding + width;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        List<View> childList = getVisibleChild();
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        //根据可见子组件数量不同计算摆放大小
        int width = 0, height = 0;
        switch (childList.size()) {
            case 1:
                width = widthSize;
                height = heightSize;
                break;
            case 2:
                height = heightSize;
                width = (widthSize - padding) / 2;
                break;
            case 3:
            case 4:
                height = (heightSize - padding) / 2;
                width = (widthSize - padding) / 2;
                break;
            case 5:
            case 6:
                height = (heightSize - padding) / 2;
                width = (widthSize - 2 * padding) / 3;
                break;
            default:
                break;
        }

        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            int widthSpec, heightSpec;
            widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            v.measure(widthSpec, heightSpec);
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    private void childLayout(View child, int x, int y, int width, int height) {
        child.layout(x, y, x + width, y + height);
    }
}
