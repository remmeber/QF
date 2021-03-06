package com.rhg.qf.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.rhg.qf.R;
import com.rhg.qf.utils.SizeUtil;

/**
 * desc:水平进度条
 * author：remember
 * time：2016/6/21 15:02
 * email：1013773046@qq.com
 */
public class LineProgress extends View {
    public static final float DEFAULT_TEXT_SIZE = 12;
    public static final int DEFAULT_COLOR = Color.GRAY;
    public static final int COVERED_COLOR = Color.GREEN;
    public static final int STATE_NONE = -1;
    public static final int STATE_LEFT = 0;
    public static final int STATE_CENTER = 1;
    public static final int STATE_RIGHT = 2;
    float textStartY;
    int finalWidth = 0;
    int finalHeight = 0;
    float totalLength;
    float leftX;
    float centerX;
    float rightX;
    private int state = STATE_NONE;
    private float strokeWidth = SizeUtil.dip2px(18);
    private float mTextSize = DEFAULT_TEXT_SIZE;
    private int mTextDefaultColor;/*默认字体颜色*/
    private int mTextCoveredColor;/*渲染字体颜色*/
    private int mProgressBackground;/*默认颜色*/
    private int mProgressColor;/*渲染颜色*/
    private int marginBetweenTextAndBar;/*字体与进度条的距离*/
    private String leftText = "已接单";
    private String centerText = "配送中";
    private String rightText = "已送达";
    private Paint mDefaultTextPaint;
    private Paint mCoveredTextPaint;
    private Paint mDefaultProgressPaint;
    private Paint mCoveredProgressPaint;
    boolean isAnim;
    float progress;
    float startY;


    public LineProgress(Context context) {
        this(context, null);
    }

    public LineProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LineProgress, defStyleAttr, 0);
        mTextSize = typedArray.getDimension(R.styleable.LineProgress_lineProgressTextSize, mTextSize);
        mTextDefaultColor = typedArray.getColor(R.styleable.LineProgress_lineProgressTextDefaultColor, DEFAULT_COLOR);
        mTextCoveredColor = typedArray.getColor(R.styleable.LineProgress_lineProgressTextColor, COVERED_COLOR);
        mProgressBackground = typedArray.getColor(R.styleable.LineProgress_lineProgressBackground, DEFAULT_COLOR);
        mProgressColor = typedArray.getColor(R.styleable.LineProgress_lineProgressColor, COVERED_COLOR);

        strokeWidth = SizeUtil.dip2px(typedArray.getDimension(R.styleable.LineProgress_lineProgressHeight, 18));
        marginBetweenTextAndBar = SizeUtil.dip2px(typedArray.getDimension(
                R.styleable.LineProgress_marginBetweenTextAndBar, 6));
        typedArray.recycle();
        mDefaultTextPaint = getTextPaint(mTextDefaultColor);
        mCoveredTextPaint = getTextPaint(mTextCoveredColor);
        mDefaultProgressPaint = getProgressPaint(mProgressBackground);
        mCoveredProgressPaint = getProgressPaint(mProgressColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureHeight(int heightMeasureSpec) {
        int result = 0;
        int textHeight = (int) Math.ceil(mDefaultTextPaint.descent() - mDefaultTextPaint.ascent() +
                getPaddingTop() + getPaddingBottom());
        if (!"".equals(leftText) || !"".equals(centerText) || !"".equals(rightText)) {
            result += textHeight;
        }
        result += marginBetweenTextAndBar;
        result += strokeWidth;
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /*获得测量后的view的宽和高*/
        finalWidth = w;
        finalHeight = h;
        startY = finalHeight - strokeWidth / 2;
        progress = strokeWidth/2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isAnim) {
            Paint.FontMetrics fm = mDefaultTextPaint.getFontMetrics();
            textStartY = getPaddingTop() + fm.descent - fm.ascent;
            totalLength = finalWidth - strokeWidth;
            leftX = totalLength / 6 - mDefaultTextPaint.measureText(leftText) / 2 + strokeWidth / 2;
            centerX = leftX + totalLength / 3;
            rightX = centerX + totalLength / 3;
            canvas.drawLine(strokeWidth / 2, startY, finalWidth - strokeWidth / 2, startY, mDefaultProgressPaint);
        }
        drawText(canvas);
        canvas.drawLine(strokeWidth / 2, startY, progress, startY, mCoveredProgressPaint);
    }

    private void drawText(Canvas canvas) {
        switch (state) {
            case STATE_LEFT:
                canvas.drawText(leftText, leftX, textStartY, mCoveredTextPaint);
                canvas.drawText(centerText, centerX, textStartY, mDefaultTextPaint);
                canvas.drawText(rightText, rightX, textStartY, mDefaultTextPaint);
                break;
            case STATE_CENTER:
                canvas.drawText(leftText, leftX, textStartY, mCoveredTextPaint);
                canvas.drawText(centerText, centerX, textStartY, mCoveredTextPaint);
                canvas.drawText(rightText, rightX, textStartY, mDefaultTextPaint);
                break;
            case STATE_RIGHT:
                canvas.drawText(leftText, leftX, textStartY, mCoveredTextPaint);
                canvas.drawText(centerText, centerX, textStartY, mCoveredTextPaint);
                canvas.drawText(rightText, rightX, textStartY, mCoveredTextPaint);
                break;
            default:
                canvas.drawText(leftText, leftX, textStartY, mDefaultTextPaint);
                canvas.drawText(centerText, centerX, textStartY, mDefaultTextPaint);
                canvas.drawText(rightText, rightX, textStartY, mDefaultTextPaint);
                break;
        }
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        if (state != LineProgress.STATE_NONE)
            startAnim();
    }

    private void startAnim() {
        final ValueAnimator va = ValueAnimator.ofFloat(strokeWidth / 2, getMax());
        va.setRepeatCount(0);
        va.setDuration(1000);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress = (float) animation.getAnimatedValue();
                if (animation.getAnimatedFraction() == 1.0f) {
                    isAnim = false;
                    va.cancel();
                    return;
                }
                invalidate();
            }
        });
        va.start();
    }

    private float getMax() {
        if (state == STATE_LEFT)
            return totalLength / 3 + strokeWidth / 2;
        else if (state == STATE_CENTER)
            return totalLength * 2 / 3 + strokeWidth / 2;
        else if (state == STATE_RIGHT)
            return totalLength + strokeWidth / 2;
        return strokeWidth / 2;
    }

    public void setContent(String leftText, String centerText, String rightText) {
        this.leftText = leftText;
        this.centerText = centerText;
        this.rightText = rightText;
        requestLayout();
    }

    private Paint getTextPaint(int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(mTextSize);
        paint.setColor(color);
        return paint;
    }

    /*获取进度条的画笔*/
    private Paint getProgressPaint(int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(color);
        return paint;
    }

   /* public void animateIndicator(float progress) {
        Interpolator interpolator = new AnticipateOvershootInterpolator(1.8f);
        ObjectAnimator animation = ObjectAnimator.ofFloat(this, "progress", progress);
        animation.setDuration(1000);
        animation.setInterpolator(interpolator);
        animation.start();
    }*/
}
