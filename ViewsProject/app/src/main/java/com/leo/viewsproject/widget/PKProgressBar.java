package com.leo.viewsproject.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.leo.viewsproject.R;

/**
 * Describe : 对战积分对比
 * Created by Leo on 2019/2/20 on 9:10.
 */
public class PKProgressBar extends View {

    private final String TAG = "PKProgressBar";
    private Paint paintBackGround, paintBar, paintOtherBar, paintLight;
    private int backGroundColor = Color.GRAY, barColor = Color.RED;
    private Paint paintLeftText, paintRightText;
    private Drawable drawable = null;
    private OnProgressChangeListener onProgressChangeListener;
    private int halfDrawableWidth = 0, halfDrawableHeight = 0;
    private int barPadding = 0;
    private boolean isRound = true;
    private float progress = 50f, max = 100f;
    private double progressPercentage = 0.5f;
    private float x;
    private int y;
    private int progressWidth = 100;

    private RectF rectFBG = new RectF(), rectFPB = new RectF(), rectFPBO = new RectF();
    private Path barRoundPath = new Path(), barRoundPathOther = new Path();
    private Rect rectLeftText = new Rect(), rectRightText = new Rect();

    //是否使用颜色渐变器
    private boolean isGradient = false;
    //颜色渐变器
    private LinearGradient linearGradient, linearGradientOther;
    private int gradientStartColor = Color.RED, gradientEndColor = Color.YELLOW;
    private int otherGradientStartColor = Color.RED, otherGradientEndColor = Color.YELLOW;
    private int barStartWidth, barEndWidth;     // 进度条最大绘制位置与最小绘制位置
    private float barRadioSize;                 // 圆角大小

    private int textColor = Color.WHITE;
    private int textSize = 12;
    private boolean textIsBold = true;
    private String leftTextStr = "我方得分0", rightTextStr = "0对方得分";

    private Bitmap lightBitmap;
    private int lightDrawableId = 0;

    public PKProgressBar(Context context) {
        super(context);
        init();
    }

    public PKProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PKProgressBar_pk, 0, 0);
        backGroundColor = typedArray.getColor(R.styleable.PKProgressBar_pk_backGroundColor, Color.GRAY);
        barColor = typedArray.getColor(R.styleable.PKProgressBar_pk_barColor, Color.RED);
        drawable = typedArray.getDrawable(R.styleable.PKProgressBar_pk_drawable);
        halfDrawableWidth = typedArray.getDimensionPixelSize(R.styleable.PKProgressBar_pk_halfDrawableWidth, 35);
        halfDrawableHeight = typedArray.getDimensionPixelSize(R.styleable.PKProgressBar_pk_halfDrawableHeight, 35);
        barPadding = typedArray.getDimensionPixelSize(R.styleable.PKProgressBar_pk_barPadding, 5);
        isRound = typedArray.getBoolean(R.styleable.PKProgressBar_pk_isRound, true);
        max = typedArray.getInt(R.styleable.PKProgressBar_pk_max, 100);
        lightDrawableId = typedArray.getResourceId(R.styleable.PKProgressBar_pk_lightDrawable, 0);
        textColor = typedArray.getColor(R.styleable.PKProgressBar_pk_textColor, Color.BLACK);
        textSize = typedArray.getDimensionPixelSize(R.styleable.PKProgressBar_pk_textSize, 13);
        textIsBold = typedArray.getBoolean(R.styleable.PKProgressBar_pk_textIsBold, false);
        isGradient = typedArray.getBoolean(R.styleable.PKProgressBar_pk_isGradient, false);
        gradientStartColor = typedArray.getColor(R.styleable.PKProgressBar_pk_gradientStartColor, Color.RED);
        gradientEndColor = typedArray.getColor(R.styleable.PKProgressBar_pk_gradientEndColor, Color.YELLOW);
        otherGradientStartColor = typedArray.getColor(R.styleable.PKProgressBar_pk_otherGradientStartColor, Color.RED);
        otherGradientEndColor = typedArray.getColor(R.styleable.PKProgressBar_pk_otherGradientEndColor, Color.YELLOW);

        typedArray.recycle();
        init();
    }

    public PKProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        // 底部边框背景
        paintBackGround = new Paint();
        paintBackGround.setColor(backGroundColor);
        paintBackGround.setAntiAlias(true);
        paintLight = new Paint();
        paintLight.setAntiAlias(true);

        // 左半部分进度条
        paintBar = new Paint();
        paintBar.setColor(barColor);
        paintBar.setAntiAlias(true);
        // 右半部分进度条
        paintOtherBar = new Paint();
        paintOtherBar.setColor(barColor);
        paintOtherBar.setAntiAlias(true);

        // 左半部分文字
        paintLeftText = new Paint();
        paintLeftText.setStyle(Paint.Style.FILL);
        paintLeftText.setColor(textColor);
        paintLeftText.setTextSize(textSize);
        paintLeftText.setFakeBoldText(textIsBold);   // 加粗
        paintLeftText.setAntiAlias(true);
        paintLeftText.setTextSkewX(-0.25f);   // 斜体
        // 右半部分文字
        paintRightText = new Paint();
        paintRightText.setStyle(Paint.Style.FILL);
        paintRightText.setColor(textColor);
        paintRightText.setTextSize(textSize);
        paintRightText.setFakeBoldText(textIsBold);
        paintRightText.setAntiAlias(true);
        paintRightText.setTextSkewX(-0.25f);

        if (lightDrawableId != 0) {
            lightBitmap = BitmapFactory.decodeResource(getResources(), lightDrawableId);
        }

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);

                //是否需要渐变器
                if (isGradient) {
                    linearGradient = new LinearGradient(0, y / 2f,
                            progressWidth, y / 2f, gradientStartColor, gradientEndColor, Shader.TileMode.CLAMP);
                    linearGradientOther = new LinearGradient(0, y / 2f,
                            progressWidth, y / 2f, otherGradientStartColor, otherGradientEndColor, Shader.TileMode.CLAMP);
                    paintBar.setShader(linearGradient);
                    paintOtherBar.setShader(linearGradientOther);
                }
                return false;
            }
        });
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 光标水平位置
        x = (float) ((progressWidth - 3 * barPadding) * progressPercentage + barPadding);
        y = getHeight();
        // 圆角大小，为实际进度条高度一半
        if (isRound) {
            barRadioSize = (y - barPadding * 2) / 2f;
        }

        // 绘制底部边框
        drawBackground(canvas);
        // 绘制左半部分进度条
        drawLeftBar(canvas);
        // 绘制右半部分进度条
        drawRightBar(canvas);
        // 绘制横向半透明光柱
        drawLight(canvas);
        // 绘制比例文字
        drawRateText(canvas);
        // 绘制光标
        drawPicture(canvas);
    }

    private void drawLight(Canvas canvas) {
        if (lightBitmap == null) {
            return;
        }

        canvas.save();

        // 将画布坐标系移动到画布中央
        canvas.translate(0, barPadding);

        Rect src = new Rect(barStartWidth, 0, barEndWidth, (y - barPadding) / 2);
        canvas.drawBitmap(lightBitmap, src, src, paintLight);
        canvas.restore();
    }

    private void drawBackground(Canvas canvas) {

        canvas.save();
        rectFBG.set(0, 0, progressWidth, y);
        if (isRound) {
            canvas.drawRoundRect(rectFBG, y / 2f, y / 2f, paintBackGround);
        } else {
            canvas.drawRect(rectFBG, paintBackGround);
        }
        canvas.restore();
    }

    private void drawLeftBar(Canvas canvas) {

        float right = getBoundaryPosition();

        float[] radios = new float[]{
                barRadioSize, barRadioSize, 0, 0,
                0, 0, barRadioSize, barRadioSize};

        if (progressPercentage == 1) {
            radios[2] = barRadioSize;
            radios[3] = barRadioSize;
            radios[4] = barRadioSize;
            radios[5] = barRadioSize;
        }

        canvas.save();
        rectFPB.set(barPadding, barPadding, right, y - barPadding);
        barRoundPath.reset();
        barRoundPath.addRoundRect(rectFPB, radios, Path.Direction.CCW);
        barRoundPath.close();
        if (isRound) {
            canvas.drawPath(barRoundPath, paintBar);
        } else {
            canvas.drawRect(rectFPB, paintBar);
        }
        canvas.restore();
    }

    private void drawRightBar(Canvas canvas) {

        float left = getBoundaryPosition();

        float[] radios = new float[]{0, 0,
                barRadioSize, barRadioSize,
                barRadioSize, barRadioSize, 0, 0};

        if (progressPercentage == 0) {
            radios[0] = barRadioSize;
            radios[1] = barRadioSize;
            radios[6] = barRadioSize;
            radios[7] = barRadioSize;
        }

        canvas.save();
        rectFPBO.set(left, barPadding, barEndWidth, y - barPadding);
        barRoundPathOther.reset();
        barRoundPathOther.addRoundRect(rectFPBO, radios, Path.Direction.CCW);
        barRoundPathOther.close();
        if (isRound) {
            canvas.drawPath(barRoundPathOther, paintOtherBar);
        } else {
            canvas.drawRect(rectFPBO, paintOtherBar);
        }
        canvas.restore();
    }

    private void drawRateText(Canvas canvas) {

        paintLeftText.getTextBounds(leftTextStr, 0, leftTextStr.length(), rectLeftText);
        paintRightText.getTextBounds(rightTextStr, 0, rightTextStr.length(), rectRightText);

        int des1W = rectRightText.width();
        int desH = rectLeftText.height();

        canvas.drawText(leftTextStr,
                y / 2f,
                y / 2f + desH / 3.0f,
                paintLeftText);

        canvas.drawText(rightTextStr,
                progressWidth - y / 2f - des1W - barPadding,
                y / 2f + desH / 3.0f,
                paintRightText);
    }

    /**
     * 获取极限位置进度条的绘制位置
     * 1. 最左边
     * 2. 最左边到圆角区段位置
     * 3. 中间正常位置
     * 4. 最右边圆角区段位置
     * 5. 最右边
     */
    private float getBoundaryPosition() {
        // 默认为计算的比例位置
        float boundaryPos = x;

        if (progressPercentage == 0 || x == barStartWidth) {
            // 光标位于最左边
            boundaryPos = barPadding;
        } else if (progressPercentage == 1 || x == barEndWidth) {
            // 光标位于最右边
            boundaryPos = barEndWidth;
        } else if (((x - barStartWidth) < barRadioSize || (x - barStartWidth) == barRadioSize)
                && x > barStartWidth) {
            // 光标位于进度条左侧弧形区域
            boundaryPos = Math.max(x, barRadioSize + barStartWidth);
        } else if ((x > barEndWidth - barRadioSize || x == barEndWidth - barRadioSize)
                && x < barEndWidth) {
            // 光标位于进度条右侧弧形区域
            boundaryPos = Math.min(x, barEndWidth - barRadioSize);
        }

        return boundaryPos;
    }

    private void drawPicture(Canvas canvas) {
        if (drawable == null) {
            return;
        }

        drawable.setBounds((int) getBoundaryPosition() - halfDrawableWidth, 0, (int) (getBoundaryPosition() + halfDrawableWidth), y);
        drawable.draw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            width = halfDrawableWidth * 2;
        }

        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            height = halfDrawableHeight * 2;
        }

        // 记录进度条总长度
        progressWidth = width;
        // 记录进度条起始位置
        barStartWidth = barPadding;
        // 记录进度条结束位置
        barEndWidth = progressWidth - barPadding;
        setMeasuredDimension(width, height);
    }

    interface OnProgressChangeListener {
        void onOnProgressChange(int progress);

        void onOnProgressFinish();
    }

    /************************public method***********************/

    public synchronized void setAnimProgress(long leftValue, long rightValue) {
        leftTextStr = "我方得分" + leftValue;
        rightTextStr = rightValue + "对方得分";

        if (leftValue + rightValue == 0) {
            setAnimProgress(50f);
        } else {
            setAnimProgress((100f * leftValue) / (leftValue + rightValue));
        }
    }

    /**
     * 传入百分比值
     *
     * @param progressValue 百分比值
     */
    public void setAnimProgress(final float progressValue) {
        if (progressValue < 0 || progressValue > 100) {
            return;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(progress, progressValue);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setProgress(Float.parseFloat(valueAnimator.getAnimatedValue().toString()));
            }
        });
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration((long) (Math.abs(progress - progressValue) * 20));
        animator.start();
    }

    public void setProgress(float progressValue) {
        if (progress <= max) {
            this.progress = progressValue;
        } else if (progress < 0) {
            this.progress = 0;
        } else {
            this.progress = max;
        }

        progressPercentage = progress / (max * 1.0f);
        doProgressRefresh();
        invalidate();
    }

    private synchronized void doProgressRefresh() {
        if (onProgressChangeListener != null) {
            onProgressChangeListener.onOnProgressChange((int) progress);
            if (progress >= max) {
                onProgressChangeListener.onOnProgressFinish();
            }
        }
    }

    //设置颜色渐变器
    public void setLinearGradient(LinearGradient linearGradient) {
        this.linearGradient = linearGradient;
    }

    //设置进度监听器
    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        this.onProgressChangeListener = onProgressChangeListener;
    }
}
