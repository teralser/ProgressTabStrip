package teralser.widget;

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

/**
 * Created by Alexey Tereshchenko on 12.07.2017.
 */

public class ProgressTabStrip extends View implements ViewPager.OnPageChangeListener {

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
    private static final String INSTANCE_SELECTED_POSITION = "INSTANCE_SELECTED_POSITION";

    private static final String COLOR_GRAY = "#4dffffff";
    private static final String COLOR_WHITE = "#ffffff";

    private static final float SELECTED_CIRCLE_RADIUS = 11.6f; //dps
    private static final float SELECTED_CIRCLE_MARGIN = 2f; //dps
    private static final float POINT_RADIUS = 2.5f; //dps
    private static final float STRIP_HEIGHT = 0.5f; //dps

    private Paint mSelectedCirclePaint;
    private Paint mPointNormalPaint;
    private Paint mPointSelectedPaint;
    private Paint mStripPaint;

    private float[] mCurrentPointCenter = new float[2];
    private float mSliceWidth;

    private int mSelectedCircleColor;
    private int mSelectedCircleMargin;
    private int mSelectedCircleRadius;
    private int mPointsCount;
    private int mPointNormalColor;
    private int mPointSelectedColor;
    private int mPointRadius;
    private int mStripColor;
    private int mStripHeight;
    private int mSelectedPosition;

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
            mSelectedCircleRadius = typedArray.getDimensionPixelSize(R.styleable.ProgressTabStrip_selectedCircleRadius,
                    Math.round(dp2px(SELECTED_CIRCLE_RADIUS)));
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
            mStripColor = typedArray.getColor(R.styleable.ProgressTabStrip_stripColor,
                    Color.parseColor(COLOR_GRAY));
            mSelectedPosition = typedArray.getInteger(R.styleable.ProgressTabStrip_selectedPosition, 0);
        } finally {
            typedArray.recycle();
        }

        initPaints();

        if (isInEditMode()) {
            if (getPointsCount() == 0) {
                mPointsCount = 6;
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

    public void setPointsCount(int pointsCount) {
        mPointsCount = pointsCount;
        if (pointsCount < 0) {
            throw new IndexOutOfBoundsException("Points count could not be lower than 0");
        }
        invalidate();
    }

    public void setSelected(int position) {
        mSelectedPosition = position;
        if (position > getPointsCount() - 1) {
            throw new IndexOutOfBoundsException("Selected position is bigger that total points count");
        }
        invalidate();
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

        int desiredWidth = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
        int desiredHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(measureDimension(desiredWidth, widthMeasureSpec),
                measureDimension(desiredHeight, heightMeasureSpec));
    }

    private int measureDimension(int desiredSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = desiredSize;
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
        float totalWidth = getWidth() - (getPaddingStart() + getPaddingEnd() +
                (mSelectedCircleMargin * 2) + (mSelectedCircleRadius * 2));
        mSliceWidth = totalWidth / (getPointsCount() - 1);
    }

    private void calculateCurrentPointCenter(int i) {

        float cx;
        float cy = getHeight() / 2;

        if (isRTL()) {
            cx = i == 0 ? getWidth() - (getPaddingEnd() + mSelectedCircleRadius + mSelectedCircleMargin) :
                    i == (getPointsCount() - 1) ? (getPaddingStart() + mSelectedCircleRadius + mSelectedCircleMargin) :
                            mCurrentPointCenter[0] - mSliceWidth;
        } else {
            cx = i == 0 ? (getPaddingStart() + mSelectedCircleRadius + mSelectedCircleMargin) :
                    i == (getPointsCount() - 1) ?
                            getWidth() - (getPaddingEnd() + mSelectedCircleRadius + mSelectedCircleMargin) :
                            mCurrentPointCenter[0] + mSliceWidth;
        }

        mCurrentPointCenter[0] = cx;
        mCurrentPointCenter[1] = cy;
    }

    private void drawSelectedCircle(Canvas canvas, int i) {
        if (i == mSelectedPosition)
            canvas.drawCircle(mCurrentPointCenter[0], mCurrentPointCenter[1],
                    mSelectedCircleRadius, mSelectedCirclePaint);
    }

    private void drawPoint(Canvas canvas, int i) {
        canvas.drawCircle(mCurrentPointCenter[0], mCurrentPointCenter[1],
                mPointRadius, i <= mSelectedPosition ? mPointSelectedPaint : mPointNormalPaint);
    }

    private void drawStrip(Canvas canvas, int i) {
        if (i == (isRTL() ? 0 : getPointsCount() - 1)) return;

        float left = mCurrentPointCenter[0] + mSelectedCircleRadius + mSelectedCircleMargin;
        float top = getHeight() / 2 - (mStripHeight / 2);
        float right = mCurrentPointCenter[0] + (mSliceWidth -
                (mSelectedCircleRadius + mSelectedCircleMargin));
        float bottom = getHeight() / 2 + (mStripHeight / 2);
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
        bundle.putInt(INSTANCE_SELECTED_CIRCLE_RADIUS, mSelectedCircleRadius);
        bundle.putInt(INSTANCE_POINT_RADIUS, mPointRadius);
        bundle.putInt(INSTANCE_POINT_NORMAL_COLOR, mPointNormalColor);
        bundle.putInt(INSTANCE_POINT_SELECTED_COLOR, mPointSelectedColor);
        bundle.putInt(INSTANCE_POINT_COUNT, mPointsCount);
        bundle.putInt(INSTANCE_STRIP_COLOR, mStripColor);
        bundle.putInt(INSTANCE_STRIP_HEIGHT, mStripHeight);
        bundle.putInt(INSTANCE_SELECTED_POSITION, mSelectedPosition);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mSelectedCircleColor = bundle.getInt(INSTANCE_SELECTED_CIRCLE_COLOR);
            mSelectedCircleMargin = bundle.getInt(INSTANCE_SELECTED_CIRCLE_MARGIN);
            mSelectedCircleRadius = bundle.getInt(INSTANCE_SELECTED_CIRCLE_RADIUS);
            mPointRadius = bundle.getInt(INSTANCE_POINT_RADIUS);
            mPointNormalColor = bundle.getInt(INSTANCE_POINT_NORMAL_COLOR);
            mPointSelectedColor = bundle.getInt(INSTANCE_POINT_SELECTED_COLOR);
            mPointsCount = bundle.getInt(INSTANCE_POINT_COUNT);
            mStripColor = bundle.getInt(INSTANCE_STRIP_COLOR);
            mStripHeight = bundle.getInt(INSTANCE_STRIP_HEIGHT);
            mSelectedPosition = bundle.getInt(INSTANCE_SELECTED_POSITION);
            initPaints();
            invalidate();
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
