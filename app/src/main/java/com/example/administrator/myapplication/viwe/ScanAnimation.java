package com.example.administrator.myapplication.viwe;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.example.administrator.myapplication.FeViewUtils;
import com.example.administrator.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by liwei on 2016/8/4.
 */
public class ScanAnimation {

    private final int[] circles = new int[]{
            R.id.circle_1, R.id.circle_2, R.id.circle_3, R.id.circle_4,
            R.id.circle_5, R.id.circle_6, R.id.circle_7, R.id.circle_8,
            R.id.circle_9, R.id.circle_10, R.id.circle_11, R.id.circle_12,
    };

    private RelativeLayout mContentView;
    private List<CircleLayout> mCircleLayouts;
    private View mScanLine;

    private int mScreenWidth;
    private int mFinalNumber;
    private int mIndex;
    private int mStep;
    private Random mRandom;

    public ScanAnimation(Activity activity) {
        mScreenWidth = getScreenWidth(activity) - FeViewUtils.dpToPx(37);//padding + line width

        mCircleLayouts = new ArrayList<>();

        final LayoutInflater layoutInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mContentView = (RelativeLayout) layoutInflater.inflate(R.layout.scan_anim, null);

        for (int id : circles) {
            mCircleLayouts.add((CircleLayout) mContentView.findViewById(id));
        }
        mFinalNumber = mCircleLayouts.size(); //小圆球的个数
        mIndex = 1;    //当前活动的圆球索引
        mStep = mScreenWidth / mFinalNumber; // 屏幕按圆球个数平分
        mRandom = new Random();

        mScanLine = mContentView.findViewById(R.id.scan_line);   //活动的线条
    }

    public RelativeLayout getContentView() {
        return mContentView;
    }

    private int getScreenWidth(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public void startAnimation() {
        setupScanLineAnimation(mScanLine);
    }

    public void cancelAnimation() {
        Animation animation;
        if (mScanLine != null) {
            animation = mScanLine.getAnimation();
            if (animation != null) {
                animation.cancel();
            }
        }

        if (mCircleLayouts != null) {
            for (CircleLayout layout : mCircleLayouts) {
                animation = layout.getAnimation();
                if (animation != null) {
                    animation.cancel();
                }
            }
        }
    }

    public void resumeAnimation() {
        Animation animation;
        if (mScanLine != null) {
            animation = mScanLine.getAnimation();
            if (animation != null) {
                animation.reset();
            }
        }
    }

    private void setupScanLineAnimation(View view) {
        int duration = 3000;
        final ObjectAnimator toEndAnimator = ObjectAnimator.ofFloat(view, "translationX", 0.0f, mScreenWidth);
        toEndAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        toEndAnimator.setDuration(duration);

        final ObjectAnimator toStartAnimator = ObjectAnimator.ofFloat(view, "translationX", mScreenWidth, 0.0f);
        toStartAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        toStartAnimator.setDuration(duration);

        toEndAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hideCircleLayout();
                mIndex = mFinalNumber;
                toStartAnimator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        toEndAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if ((mStep * mIndex) < value) {

                    if (mIndex > 0 && mIndex <= mFinalNumber) {
                        CircleLayout circleLayout = mCircleLayouts.get(mIndex - 1);
                        Animation circleAnimation = circleLayout.getAnimation();
                        if (circleAnimation == null || !circleAnimation.hasStarted()) {
                            setupCircleShowAnimation(circleLayout, mIndex++, 0);
                        }
                    }

                }

            }
        });

        toStartAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hideCircleLayout();
                mIndex = 1;
                toEndAnimator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        toStartAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                if ((mStep * mIndex) > value) {

                    if (mIndex > 0 && mIndex <= mFinalNumber) {
                        CircleLayout circleLayout = mCircleLayouts.get(mIndex - 1);
                        Animation circleAnimation = circleLayout.getAnimation();
                        if (circleAnimation == null || !circleAnimation.hasStarted()) {
                            setupCircleShowAnimation(circleLayout, mIndex--, 0);
                        }
                    }
                }
            }
        });

        toEndAnimator.start();
    }

    private void setupCircleShowAnimation(final View view, final int index, long delay) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);

        float multiple = mRandom.nextFloat();
        multiple += 1f;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, multiple);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, multiple);

        final AnimatorSet animSet = new AnimatorSet();
        animSet.setStartDelay(delay > 0 ? delay : 0);
        animSet.setDuration(300);
        animSet.play(alpha).with(scaleX).with(scaleY);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSet.start();
    }

    private void hideCircleLayout() {
        for (CircleLayout layout : mCircleLayouts) {
            setupCircleHideAnimation(layout);
        }
    }

    private void setupCircleHideAnimation(final View view) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);

        alpha.setDuration(400);
        alpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        alpha.start();
    }

}
