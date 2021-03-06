package teralser.widget;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by Alexey Tereshchenko on 12.07.2017.
 */

public class ProgressTabStrip extends View implements ViewPager.OnPageChangeListener, ValueAnimator.AnimatorUpdateListener {

    private static final String TAG = ProgressTabStrip.class.getSimpleName();

    private static final String INSTANCE_STATE = "INSTANCE_STATE";
    private static final String INSTANCE_SELECTED_CIRCLE_COLOR = "INSTANCE_SELECTED_CIRCLE_COLOR";
    private static final String INSTANCE_SELECTED_CIRCLE_MARGIN = "INSTANCE_SELECTED_CIRCLE_MARGIN";
    private static final String INSTANCE_SELECTED_CIRCLE_RADIUS = "INSTANCE_SELECTED_CIRCLE_RADIUS";
    private static final String INSTANCE_POINT_RADIUS = "INSTANCE_POINT_RADIUS";
    private static final String INSTANCE_POINT_NORMAL_COLOR = "INSTANCE_POINT_NORMAL_COLOR";
    private static final String INSTANCE_POINT_SELECTED_COLOR = "INSTANCE_POINT_SELECTED_COLOR";
    private static final String INSTANCE_POINT_COUNT = "INSTANCE_POINT_COUNT";
    private static final String INSTANCE_STRIP_COLOR = "INSTANCE_STRIP_COLOR";
    private static final String INSTANCE_STRIP_HEIGHT = "INSTANCE_STRIP_HEIGHT";
    private static final String INSTANCE_STRIP_WIDTH = "INSTANCE_STRIP_WIDTH";
    private static final String INSTANCE_SELECTED_POSITION = "INSTANCE_SELECTED_POSITION";
    private static final String INSTANCE_HIGHEST_SELECTED = "INSTANCE_HIGHEST_SELECTED";
    private static final String INSTANCE_HOLD_POSITIONS = "INSTANCE_HOLD_POSITIONS";
    private static final String INSTANCE_ANIMATED = "INSTANCE_ANIMATED";

    private static final String COLOR_GRAY = "#4dffffff";
    private static final String COLOR_WHITE = "#ffffff";

    private static final float SELECTED_CIRCLE_RADIUS = 11.6f; //dps
    private static final float SELECTED_CIRCLE_MARGIN = 2f; //dps
    private static final float POINT_RADIUS = 2.5f; //dps
    private static final float STRIP_HEIGHT = 1f; //dps

    private static final int ANIMATION_TIME = 80; //ms

    private Paint mSelectedCirclePaint;
    private Paint mPointNormalPaint;
    private Paint mPointSelectedPaint;
    private Paint mStripPaint;

    private float[] mCurrentPointCenter = new float[2];
    private float mSliceWidth;
    private float drawWidth;

    private int mSelectedCircleColor;
    private int mSelectedCircleMargin;
    private int mSelectedCircleRadius;
    private int mSelectedCircleRadiusOriginal;
    private int mPointsCount;
    private int mPointNormalColor;
    private int mPointSelectedColor;
    private int mPointRadius;
    private int mStripColor;
    private int mStripHeight;
    private int mSelectedPosition;
    private int mSelectedPositionDelayed;
    private int mHighestSelectedPosition;
    private int mStripWidth;

    private boolean mHoldPassedPositions;
    private boolean mIsAnimated;

    private AnimatorSet mAnimatorSet;

    public ProgressTabStrip(Context context) {
        super(context);
        init(null);
    }

    public ProgressTabStrip(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ProgressTabStrip(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ProgressTabStrip(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs,
                R.styleable.ProgressTabStrip, 0, 0);
        try {
            mSelectedCircleRadiusOriginal = typedArray.getDimensionPixelSize(R.styleable.ProgressTabStrip_selectedCircleRadius,
                    Math.round(dp2px(SELECTED_CIRCLE_RADIUS)));
            mSelectedCircleRadius = mSelectedCircleRadiusOriginal;
            mSelectedCircleMargin = typedArray.getDimensionPixelSize(R.styleable.ProgressTabStrip_selectedCircleMargin,
                    Math.round(dp2px(SELECTED_CIRCLE_MARGIN)));
            mSelectedCircleColor = typedArray.getColor(R.styleable.ProgressTabStrip_selectedCircleColor,
                    Color.parseColor(COLOR_GRAY));
            mPointRadius = typedArray.getDimensionPixelSize(R.styleable.ProgressTabStrip_pointRadius,
                    Math.round(dp2px(POINT_RADIUS)));
            mPointNormalColor = typedArray.getColor(R.styleable.ProgressTabStrip_pointColorNormal,
                    Color.parseColor(COLOR_GRAY));
            mPointSelectedColor = typedArray.getColor(R.styleable.ProgressTabStrip_pointColorSelected,
                    Color.parseColor(COLOR_WHITE));
            mPointsCount = typedArray.getInteger(R.styleable.ProgressTabStrip_pointsCount, 0);
            mStripHeight = typedArray.getDimensionPixelSize(R.styleable.ProgressTabStrip_stripHeight,
                    Math.round(dp2px(STRIP_HEIGHT)));
            mStripWidth = typedArray.getDimensionPixelSize(R.styleable.ProgressTabStrip_stripWidth, 0);
            mStripColor = typedArray.getColor(R.styleable.ProgressTabStrip_stripColor,
                    Color.parseColor(COLOR_GRAY));
            mSelectedPosition = typedArray.getInteger(R.styleable.ProgressTabStrip_selectedPosition, 0);
            mHoldPassedPositions = typedArray.getBoolean(R.styleable.ProgressTabStrip_holdPassedPositions, false);
            mIsAnimated = typedArray.getBoolean(R.styleable.ProgressTabStrip_animated, false);
        } finally {
            typedArray.recycle();
        }

        initPaints();
        initAnimatorSet();

        if (isInEditMode()) {
            if (getPointsCount() == 0) {
                mPointsCount = 6;
            }

            if (getBackground() == null) {
                setBackgroundColor(Color.DKGRAY);
            }
        }
        invalidate();
    }

    private void initPaints() {
        mSelectedCirclePaint = generatePaint(mSelectedCircleColor);
        mPointNormalPaint = generatePaint(mPointNormalColor);
        mPointSelectedPaint = generatePaint(mPointSelectedColor);
        mStripPaint = generatePaint(mStripColor);
    }

    private Paint generatePaint(@ColorInt int color) {
        Paint result = new Paint(Paint.ANTI_ALIAS_FLAG);
        result.setColor(color);
        return result;
    }

    private void initAnimatorSet() {

        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet = null;
        }

        ValueAnimator animator = ValueAnimator.ofInt(mSelectedCircleRadius, 0);
        animator.setDuration(ANIMATION_TIME);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(this);

        ValueAnimator reverseAnimator = ValueAnimator.ofInt(1, mSelectedCircleRadius);
        reverseAnimator.setDuration(ANIMATION_TIME);
        reverseAnimator.setInterpolator(new DecelerateInterpolator());
        reverseAnimator.addUpdateListener(this);

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(animator, reverseAnimator);
    }

    public void setSelectedCircleColor(@ColorInt int selectedCircleColor) {
        mSelectedCircleColor = selectedCircleColor;
        if (mSelectedCirclePaint == null) initPaints();
        mSelectedCirclePaint.setColor(selectedCircleColor);
        invalidate();
    }

    public void setPointNormalColor(@ColorInt int pointNormalColor) {
        mPointNormalColor = pointNormalColor;
        if (mPointNormalPaint == null) initPaints();
        mPointNormalPaint.setColor(mPointNormalColor);
        invalidate();
    }

    public void setPointSelectedColor(@ColorInt int pointSelectedColor) {
        mPointSelectedColor = pointSelectedColor;
        if (mPointSelectedPaint == null) initPaints();
        mPointSelectedPaint.setColor(mPointSelectedColor);
        invalidate();
    }

    public void setStripColor(@ColorInt int stripColor) {
        mStripColor = stripColor;
        if (mStripPaint == null) initPaints();
        mStripPaint.setColor(mStripColor);
        invalidate();
    }

    public void setSelectedCircleMargin(int selectedCircleMargin) {
        mSelectedCircleMargin = selectedCircleMargin;
        invalidate();
    }

    public void setSelectedCircleRadius(int selectedCircleRadius) {
        mSelectedCircleRadius = selectedCircleRadius;
        mSelectedCircleRadiusOriginal = mSelectedCircleRadius;
        initAnimatorSet();
        invalidate();
    }

    public void setPointRadius(int pointRadius) {
        mPointRadius = pointRadius;
        invalidate();
    }

    public void setStripHeight(int stripHeight) {
        mStripHeight = stripHeight;
        invalidate();
    }

    public void setStripWidth(int stripWidth) {
        this.mStripWidth = stripWidth;
        invalidate();
    }

    public void setPointsCount(int pointsCount) {
        if (pointsCount < 0) {
            throw new IndexOutOfBoundsException("Points count could not be lower than 0");
        }

        mPointsCount = pointsCount;
        invalidate();
    }

    public void setSelected(int position) {
        if (position > getPointsCount() - 1) {
            throw new IndexOutOfBoundsException("Selected position is bigger that total points count");
        }

        if (mIsAnimated) {
            mSelectedPositionDelayed = position;
            if (mAnimatorSet.isRunning()) mAnimatorSet.cancel();
            mAnimatorSet.start();
        } else {
            if (mHoldPassedPositions) updateHighestSelectedPosition(position);
            mSelectedPosition = position;
            invalidate();
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        int val = (Integer) animation.getAnimatedValue();
        mSelectedCircleRadius = val;
        invalidate();

        if (val == 0) {
            if (mHoldPassedPositions) updateHighestSelectedPosition(mSelectedPositionDelayed);
            mSelectedPosition = mSelectedPositionDelayed;
        }
    }

    public void setHoldPassedPositions(boolean holdPassedPositions) {
        mHoldPassedPositions = holdPassedPositions;
        mHighestSelectedPosition = holdPassedPositions ? mSelectedPosition : 0;
        invalidate();
    }

    public void setAnimated(boolean isAnimated) {
        mIsAnimated = isAnimated;
    }

    private void updateHighestSelectedPosition(int position) {
        if (position > mHighestSelectedPosition)
            mHighestSelectedPosition = position;
    }

    public int getPointsCount() {
        return mPointsCount;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public void setupWithViewPager(ViewPager viewPager) {
        if (viewPager == null || viewPager.getAdapter() == null) {
            throw new NullPointerException("ViewPager ot it's adapter is null");
        }
        mPointsCount = viewPager.getAdapter().getCount();
        mSelectedPosition = viewPager.getCurrentItem();
        viewPager.addOnPageChangeListener(this);
        invalidate();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Log.v(TAG, "onMeasure w: " + MeasureSpec.toString(widthMeasureSpec));
        Log.v(TAG, "onMeasure h: " + MeasureSpec.toString(heightMeasureSpec));

        int desiredWidth = getWidth();
        int desiredHeight = (mSelectedCircleRadiusOriginal * 2) + getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(measureDimension(desiredWidth, widthMeasureSpec),
                measureDimension(desiredHeight, heightMeasureSpec));
    }

    private int measureDimension(int desiredSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        //Must be this size
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = desiredSize;
            //Can't be bigger than...
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        if (result < desiredSize) {
            Log.e(TAG, "The view is too small, the content might get cut");
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        calculateSlice();

        for (int i = 0; i < getPointsCount(); i++) {
            calculateCurrentPointCenter(i);
            drawSelectedCircle(canvas, i);
            drawPoint(canvas, i);
            drawStrip(canvas, i);
        }
    }

    private void calculateSlice() {
        drawWidth = getWidth() - (getPaddingStart() + getPaddingEnd() +
                (mSelectedCircleMargin * 2) + (mSelectedCircleRadiusOriginal * 2));

        if (mStripWidth > 0) {
            if (mStripWidth < mSelectedCircleRadiusOriginal * 2 + mSelectedCircleMargin) {
                mStripWidth = mSelectedCircleRadiusOriginal * 2 + mSelectedCircleMargin;
            }

            float manualWidth = mStripWidth * (getPointsCount() - 1);
            if (manualWidth > drawWidth) {
                mStripWidth = 0;
            } else {
                drawWidth = manualWidth;
            }
        }

        mSliceWidth = drawWidth / (getPointsCount() - 1);
    }

    private float getVerticalCenter() {
        float result = getHeight() / 2;
        if (getPaddingTop() != 0) {
            result += (getPaddingTop() / 2);
        }
        if (getPaddingBottom() != 0) {
            result -= (getPaddingBottom() / 2);
        }
        return result;
    }

    private void calculateCurrentPointCenter(int i) {

        float cx;
        float cy = getVerticalCenter();
        float center = getWidth() / 2;
        boolean isStretched = mStripWidth <= 0;

        if (isRTL()) {
            cx = i == 0 ? (!isStretched ? (center + (drawWidth / 2)) + mPointRadius :
                    (getWidth() - (getPaddingEnd() + mSelectedCircleRadiusOriginal + mSelectedCircleMargin)))
                    : mCurrentPointCenter[0] - mSliceWidth;
        } else {
            cx = i == 0 ? (!isStretched ? (center - (drawWidth / 2)) - mPointRadius :
                    ((getPaddingStart() + mSelectedCircleRadiusOriginal + mSelectedCircleMargin)))
                    : mCurrentPointCenter[0] + mSliceWidth;
        }

        mCurrentPointCenter[0] = cx;
        mCurrentPointCenter[1] = cy;
    }

    private void drawSelectedCircle(Canvas canvas, int position) {
        if (position == mSelectedPosition)
            canvas.drawCircle(mCurrentPointCenter[0], mCurrentPointCenter[1],
                    mSelectedCircleRadius, mSelectedCirclePaint);
    }

    private void drawPoint(Canvas canvas, int i) {
        int position = mHoldPassedPositions ? mHighestSelectedPosition : mSelectedPosition;
        canvas.drawCircle(mCurrentPointCenter[0], mCurrentPointCenter[1],
                mPointRadius, i <= position ? mPointSelectedPaint : mPointNormalPaint);
    }

    private void drawStrip(Canvas canvas, int i) {
        if (i == (isRTL() ? 0 : getPointsCount() - 1)) return;

        float left = mCurrentPointCenter[0] + mSelectedCircleRadiusOriginal + mSelectedCircleMargin;
        float top = getVerticalCenter() - (mStripHeight / 2);
        float right = mCurrentPointCenter[0] + (mSliceWidth -
                (mSelectedCircleRadiusOriginal + mSelectedCircleMargin));
        float bottom = getVerticalCenter() + (mStripHeight / 2);
        canvas.drawRect(left, top, right, bottom, mStripPaint);
    }

    public float dp2px(float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    public boolean isRTL() {
        Configuration config = getResources().getConfiguration();
        return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_SELECTED_CIRCLE_COLOR, mSelectedCircleColor);
        bundle.putInt(INSTANCE_SELECTED_CIRCLE_MARGIN, mSelectedCircleMargin);
        bundle.putInt(INSTANCE_SELECTED_CIRCLE_RADIUS, mSelectedCircleRadiusOriginal);
        bundle.putInt(INSTANCE_POINT_RADIUS, mPointRadius);
        bundle.putInt(INSTANCE_POINT_NORMAL_COLOR, mPointNormalColor);
        bundle.putInt(INSTANCE_POINT_SELECTED_COLOR, mPointSelectedColor);
        bundle.putInt(INSTANCE_POINT_COUNT, mPointsCount);
        bundle.putInt(INSTANCE_STRIP_COLOR, mStripColor);
        bundle.putInt(INSTANCE_STRIP_HEIGHT, mStripHeight);
        bundle.putInt(INSTANCE_STRIP_WIDTH, mStripWidth);
        bundle.putInt(INSTANCE_SELECTED_POSITION, mSelectedPosition);
        bundle.putInt(INSTANCE_HIGHEST_SELECTED, mHighestSelectedPosition);
        bundle.putBoolean(INSTANCE_HOLD_POSITIONS, mHoldPassedPositions);
        bundle.putBoolean(INSTANCE_ANIMATED, mIsAnimated);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mSelectedCircleColor = bundle.getInt(INSTANCE_SELECTED_CIRCLE_COLOR);
            mSelectedCircleMargin = bundle.getInt(INSTANCE_SELECTED_CIRCLE_MARGIN);
            mSelectedCircleRadiusOriginal = bundle.getInt(INSTANCE_SELECTED_CIRCLE_RADIUS);
            mPointRadius = bundle.getInt(INSTANCE_POINT_RADIUS);
            mPointNormalColor = bundle.getInt(INSTANCE_POINT_NORMAL_COLOR);
            mPointSelectedColor = bundle.getInt(INSTANCE_POINT_SELECTED_COLOR);
            mPointsCount = bundle.getInt(INSTANCE_POINT_COUNT);
            mStripColor = bundle.getInt(INSTANCE_STRIP_COLOR);
            mStripHeight = bundle.getInt(INSTANCE_STRIP_HEIGHT);
            mStripWidth = bundle.getInt(INSTANCE_STRIP_WIDTH);
            mSelectedPosition = bundle.getInt(INSTANCE_SELECTED_POSITION);
            mHighestSelectedPosition = bundle.getInt(INSTANCE_HIGHEST_SELECTED);
            mHoldPassedPositions = bundle.getBoolean(INSTANCE_HOLD_POSITIONS);
            mIsAnimated = bundle.getBoolean(INSTANCE_ANIMATED);
            initPaints();
            initAnimatorSet();
            invalidate();
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
