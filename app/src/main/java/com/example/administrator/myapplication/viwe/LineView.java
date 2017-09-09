package com.example.administrator.myapplication.viwe;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.example.administrator.myapplication.FeViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 大灯泡 on 2016/2/29.
 */
public class LineView extends View {

    private static final int FORWARD_PATH = 1;

    private static final long ANIMATION_DURATION = 1000;

    private Context mContext;
    private Paint mPaint;
    private Path mPath;

    private List<PointF> mPoints;
    private int mWidth = 800;
    private int mHeight;
    private int mCount = 0;
    private List<Float> mKList;
    private float mCurK;

    private float t;
    private float mYScale = 1.0f;
    private float mCurControlPointY;
    private ValueAnimator mAnimator;
    private boolean mAnimationFirst = true;

    public LineView(Context context) {
        this(context, null);
    }

    public LineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mPath = new Path();
        mPoints = new ArrayList<>();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(mContext.getResources().getColor(android.R.color.holo_blue_light));

        initPoint();
    }

    private void initPoint() {
        mKList = new ArrayList<>();
        mPoints = new ArrayList<>();

        mPoints.add(createPoint(0.1f, 0.1f));
        mPoints.add(createPoint(0.2f, 0.2f));
    }

    public void addPoint(float percent, float speed, int width) {
        if (addPointImpl(percent, speed, width)) {
            startAnimation(ANIMATION_DURATION);
        }
    }

    public void finishLine(float percent, float speed, int width) {
        if (addPointImpl(percent, speed, width)) {
            mAnimationFirst = false;
            t = 1;
            setCurK();
            invalidate();
        }
    }

    private boolean addPointImpl(float percent, float speed, int width) {
        mWidth = width;
        if (percent / 100 * mWidth <= mPoints.get(1).x) {
            return false;
        }

        List<PointF> clonePoints = new ArrayList<>();
        clonePoints.add(mPoints.get(1));
        clonePoints.add(createPoint(percent, speed));
        mPoints = clonePoints;

        return true;
    }

    private PointF createPoint(float percent, float speed) {
        PointF point = new PointF(percent / 100 * mWidth, getViewY(speed));
        mCount++;
        return point;
    }

    private PointF createHistoryPoint(float percent, float speed) {
        return new PointF(percent, getViewY(speed));
    }

    private float getViewY(float y) {
        return (float) (Math.sin(y) + 180);
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /*
        宽度设置
         */
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            mWidth = specSize;
        } else {
            int desireByImg = getPaddingLeft() + getPaddingRight() + 600;

            if (specMode == MeasureSpec.AT_MOST) {
                mWidth = desireByImg;
            }
        }

        /*
        高度设置
         */
        specMode = MeasureSpec.getMode(heightMeasureSpec);
        specSize = MeasureSpec.getSize(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            mHeight = specSize;
        } else {
            int desireByImg = getPaddingLeft() + getPaddingRight() + 600;

            if (specMode == MeasureSpec.AT_MOST) {
                mHeight = desireByImg;
            }
        }
        setMeasuredDimension(mWidth,mHeight);

//        Logger.d("LineView onMeasure Width = " + mWidth + " mHeight = " + mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mAnimationFirst) {
            mAnimationFirst = false;
            setCurK();
            if (mPoints.size() > 0) {
                    mPath.moveTo(mPoints.get(0).x, mPoints.get(0).y);
            }
        } else {
            float[] controlPoint = getSecondOrderControlPoint(mPoints.get(0), mPoints.get(1), mCurK);
//                    Log.e("abc", "==controlPointY==" + controlPoint[1] + "==mHeight==" + mHeight);
            float[] point = getSecondOrderNextPoint(controlPoint, mPoints.get(0), mPoints.get(1), t);

            calcScale(controlPoint[1]);
            mPath.lineTo(point[0], point[1]);
        }

        if (mYScale != 1.0f) {
//                    Log.e("abc", "==mYScale==" + mYScale);
            canvas.scale(1.0f, mYScale, 0, FeViewUtils.dpToPx(128) / 2);
        }

        canvas.drawPath(mPath, mPaint);
    }

    private float[] getFirstOrderNextPoint(PointF startPoint, PointF endPoint, float t) {
        float tmp = (1 - t);
        float x = startPoint.x * tmp + t * endPoint.x;
        float y = startPoint.y * tmp + t * endPoint.y;

        return new float[]{x, y};
    }

    private float[] getSecondOrderNextPoint(float[] controlPoint, PointF startPoint, PointF endPoint, float t) {
        float x = startPoint.x * (1 - t) * (1 - t) + 2 * t * (1 - t) * controlPoint[0] + t * t * endPoint.x;
        float y = startPoint.y * (1 - t) * (1 - t) + 2 * t * (1 - t) * controlPoint[1] + t * t * endPoint.y;

        return new float[]{x, y};
    }

    private float[] getSecondOrderControlPoint(PointF startPoint, PointF endPoint, float k) {
        float x = (endPoint.x + startPoint.x) / 2;
        float y = k * ((endPoint.x - startPoint.x) / 2) + startPoint.y;

        return new float[]{x, y};
    }

    private void setCurK() {
        if (mCount < 3 || mKList.size() < 1) {
            mCurK = 3;
        } else {
            mCurK = calcK(mPoints.get(0), mPoints.get(1), mKList.get(mKList.size() - 1));
        }

        mKList.add(mCurK);
    }

    private float calcK(PointF startPoint, PointF endPoint, float lastK) {
        float k = lastK * (endPoint.x - startPoint.x) + (2 * startPoint.y) - (2 * endPoint.y);
        float tmp = startPoint.x - endPoint.x;
        if (tmp == 0) {
            tmp = 1;
        }

        return k / tmp;
    }

    private void calcScale(float y) {
        if (y != mCurControlPointY) {
            mCurControlPointY = y;
            float yScale = 1.0f;

            if (y > mHeight) {
                yScale = ((float) mHeight) / y;
            }

            if (y < 0) {
                yScale = ((float) mHeight) / (-y + mHeight);
            }

            mYScale = yScale > mYScale ? mYScale : yScale;
        }
    }

    public void startAnimation(long duration) {
        mAnimationFirst = true;

        ValueAnimator.setFrameDelay(100);
        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(duration);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                t = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
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

        mAnimator.start();
    }

}