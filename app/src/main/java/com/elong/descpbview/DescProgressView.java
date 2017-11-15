package com.elong.descpbview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author by feilong on 17/11/13.
 */

public class DescProgressView extends View {
    private static final float SCALE_OF_PROGRESS_HEIGHT = 70.F / 120;
    private static final float SCALE_OF_TOP_AND_BOTTOM_PADDING = 10.F / 120;
    private static final float SCALE_OF_LEFT_AND_RIGHT_PADDING = 20.F / 120;
    private static final float SCALE_OF_TEXT_DESC_CONTAINER = 50.F / 120;
    private static final float SCALE_OF_BIG_CIRCLE_HEIGHT = 22.F / 120;
    private static final float SCALE_OF_SMALL_CIRCLE_HEIGHT = 16.F / 120;
    private static final float SCALE_OF_LINE_HEIGHT = 4.F / 120;
    private static final float DEF_VIEW_HEIGHT = 120.F;

    private int dpViewHeight;
    private int dpViewWidth;
    private int progressContainerHeight;
    private int topAndBottomPadding;
    private int leftAndRightPadding;
    private int textDescContainerHeight;
    private int smallCircleRadio;
    private int bigCircleRadio;
    private int lineHeight;

    private int textNormalColor;
    private int textSelectedColor;
    private int dpvTextSize;
    private int dpvProgressBgColor;
    private int dpvSmallCicleColor;
    private int dpvBigCircleColor;
    private int screenWidth;

    private Paint smallCirclePaint;
    private Paint bigCirclePaint;
    private Paint textDescPaint;
    private Paint linePaint;
    private Paint grayPaint;

    private List<String> descs;
    private List<Point> allDescTextPoints;
    private List<Point> textPoints4Draw;
    private List<Point> bgCirclePoints;
    private int currentSelectPosition;

    private RectF bgLineRect;
    private RectF selectedLineRectF;
    private RectF gradualRectF;
    private RectF grayRectF;

    private boolean hasOnsizeChanged;

    public DescProgressView(Context context) {
        this(context, null);
    }

    public DescProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DescProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
        initData();
        getScreenWidth();
        initPaints();
    }

    private void initData() {
        descs = new ArrayList<>();
        allDescTextPoints = new ArrayList<>();
        textPoints4Draw = new ArrayList<>();
        bgCirclePoints = new ArrayList<>();
        bgLineRect = new RectF();
        selectedLineRectF = new RectF();
        gradualRectF = new RectF();
        grayRectF = new RectF();
    }

    private void initPaints() {
        smallCirclePaint = creatPaint(dpvSmallCicleColor, 0, Paint.Style.FILL, 0);
        bigCirclePaint = creatPaint(dpvBigCircleColor, 0, Paint.Style.FILL, 0);
        textDescPaint = creatPaint(textNormalColor, dpvTextSize, Paint.Style.FILL, 0);
        linePaint = creatPaint(dpvSmallCicleColor, 0, Paint.Style.FILL, 0);
        grayPaint = creatPaint(dpvProgressBgColor, 0, Paint.Style.FILL, 0);
    }

    private void getScreenWidth() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            screenWidth = displayMetrics.widthPixels;
        }
    }

    private void initAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DescProgressView, defStyleAttr, R.style.Def_DescProgressViewStyle);
        int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.DescProgressView_dpv_text_normal_color:
                    textNormalColor = typedArray.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.DescProgressView_dpv_text_seleced_color:
                    textSelectedColor = typedArray.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.DescProgressView_dpv_text_size:
                    dpvTextSize = typedArray.getDimensionPixelSize(attr, 0);
                    break;
                case R.styleable.DescProgressView_dev_progress_bg_color:
                    dpvProgressBgColor = typedArray.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.DescProgressView_dev_progress_small_circle_color:
                    dpvSmallCicleColor = typedArray.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.DescProgressView_dev_progress_big_circle_color:
                    dpvBigCircleColor = typedArray.getColor(attr, Color.BLACK);
                    break;
            }
        }
        typedArray.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        hasOnsizeChanged = true;
        dpViewHeight = h;
        dpViewWidth = w;
        progressContainerHeight = (int) (SCALE_OF_PROGRESS_HEIGHT * dpViewHeight);
        topAndBottomPadding = (int) (SCALE_OF_TOP_AND_BOTTOM_PADDING * dpViewHeight);
        leftAndRightPadding = (int) (SCALE_OF_LEFT_AND_RIGHT_PADDING * dpViewHeight);
        textDescContainerHeight = (int) (SCALE_OF_TEXT_DESC_CONTAINER * dpViewHeight);
        smallCircleRadio = (int) (SCALE_OF_SMALL_CIRCLE_HEIGHT * dpViewHeight / 2);
        bigCircleRadio = (int) (SCALE_OF_BIG_CIRCLE_HEIGHT * dpViewHeight / 2);
        lineHeight = (int) (SCALE_OF_LINE_HEIGHT * dpViewHeight);

        getDescTextWidthAndHeight();
        getDescTextRegonPoint();
        getBgLineRectF();
        getBgCirclePoints();
        getSelectedRectF();
        getColorFullRectF();
        getGrayRectF();
    }

    /**
     * 获取灰色的矩形区域
     */
    private void getGrayRectF() {
        float bTop = bgLineRect.top;
        float bLeft = bgCirclePoints.get(currentSelectPosition - 1).x;
        float bBottom = bgLineRect.bottom;
        float bRight = bgCirclePoints.get(bgCirclePoints.size() - 1).x;
        grayRectF = new RectF(bLeft, bTop, bRight, bBottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int widthSize;
        int heightSize;

        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, screenWidth, getResources().getDisplayMetrics());
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        }

        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_VIEW_HEIGHT, getResources().getDisplayMetrics());
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDescText(canvas);
        drawBgLine(canvas);
        drawSelectedLine(canvas);
        drawGrayRectF(canvas);
        drawSelectedCircles(canvas);
    }

    /**
     * 绘制彩色渐变后剩余灰色的部分
     *
     * @param canvas 灰色的部分
     */
    private void drawGrayRectF(Canvas canvas) {
        grayPaint.setColor(dpvProgressBgColor);
        canvas.drawRect(grayRectF, grayPaint);
    }

    /**
     * 初始化画笔
     *
     * @param paintColor 画笔颜色
     * @param textSize   文字大小
     * @param style      画笔风格
     * @param lineWidth  画笔宽度
     * @return 画笔
     */
    private Paint creatPaint(int paintColor, int textSize, Paint.Style style, int lineWidth) {
        Paint paint = new Paint();
        paint.setColor(paintColor);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(lineWidth);
        paint.setDither(true);
        paint.setTextSize(textSize);
        paint.setStyle(style);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        return paint;
    }

    public void setProgressDescs(List<String> descs, int currentSelectPosition) {
        this.currentSelectPosition = currentSelectPosition;
        this.textPoints4Draw.clear();
        this.bgCirclePoints.clear();
        this.allDescTextPoints.clear();
        if (descs != null && descs.size() > 1) {
            this.descs.clear();
            this.descs.addAll(descs);
            this.allDescTextPoints.clear();
            if (hasOnsizeChanged) {
                linePaint.setShader(null);
                getDescTextWidthAndHeight();
                getDescTextRegonPoint();
                getBgLineRectF();
                getBgCirclePoints();
                getSelectedRectF();
                getColorFullRectF();
                getGrayRectF();
            }
            invalidate();
        }
    }

    /**
     * 获取选中的矩形区域
     */
    private void getColorFullRectF() {
        float bTop = bgLineRect.top;
        float bLeft = bgLineRect.left + (bgCirclePoints.get(currentSelectPosition - 2).x - bgCirclePoints.get(0).x);
        float bBottom = bgLineRect.bottom;
        float bRight = bLeft + (bgCirclePoints.get(currentSelectPosition - 1).x - bgCirclePoints.get(currentSelectPosition - 2).x);
        gradualRectF = new RectF(bLeft, bTop, bRight, bBottom);
    }

    /**
     * 获取渐变选中的矩形区域
     */
    private void getSelectedRectF() {
        float bTop = bgLineRect.top;
        float bLeft = bgLineRect.left;
        float bBottom = bgLineRect.bottom;
        float bRight = bgLineRect.left + (bgCirclePoints.get(currentSelectPosition - 2).x - bgCirclePoints.get(0).x);
        selectedLineRectF = new RectF(bLeft, bTop, bRight, bBottom);
    }

    /**
     * 获取背景圆圈的坐标
     */
    private void getBgCirclePoints() {
        for (int i = 0; i < textPoints4Draw.size(); i++) {
            int cX = textPoints4Draw.get(i).x + allDescTextPoints.get(i).x / 2;
            int cY = progressContainerHeight / 2;
            Point currentPointP = new Point(cX, cY);
            bgCirclePoints.add(currentPointP);
        }
    }

    /**
     * 获取背景灰色进度的区域
     */
    private void getBgLineRectF() {
        int bTop = (int) (progressContainerHeight / 2.F - lineHeight / 2.F);
        int bLeft = allDescTextPoints.get(0).x / 2 + leftAndRightPadding;
        int bBottom = (int) (progressContainerHeight / 2.F + lineHeight / 2.F);
        int bRight = dpViewWidth - allDescTextPoints.get(allDescTextPoints.size() - 1).x / 2 - leftAndRightPadding;

        bgLineRect.top = bTop;
        bgLineRect.left = bLeft;
        bgLineRect.bottom = bBottom;
        bgLineRect.right = bRight;
    }

    /**
     * 获取文字在画布中的位置
     */
    private void getDescTextRegonPoint() {
        for (int i = 0; i < descs.size(); i++) {
            Point textRegonPoint = new Point();
            int sumX = 0;
            for (int j = 0; j < i; j++) {
                Point tempSum = allDescTextPoints.get(j);
                sumX += tempSum.x;
            }
            sumX += i * getTextDescSpace();
            textRegonPoint.x = sumX + leftAndRightPadding;
            textRegonPoint.y = dpViewHeight - topAndBottomPadding - textDescContainerHeight / 2;
            textPoints4Draw.add(textRegonPoint);
        }
    }

    /**
     * 获取文字自身的长度和高度
     */
    private void getDescTextWidthAndHeight() {
        for (int i = 0; i < this.descs.size(); i++) {
            Point currentPoint = getTextWidthAndHeight(this.descs.get(i), textDescPaint);
            allDescTextPoints.add(currentPoint);
        }
    }

    /**
     * 获取文字的宽和高
     *
     * @param textDesc 文字描述
     * @param paint    绘制文字的画笔
     * @return 返回的文字宽高对象(X代表款, Y代表高)
     */
    private Point getTextWidthAndHeight(String textDesc, Paint paint) {
        if (null == textDesc) return null;
        Point point = new Point();
        int textW = (int) paint.measureText(textDesc);
        Paint.FontMetrics fm = paint.getFontMetrics();
        int textH = (int) Math.ceil(fm.descent - fm.top);
        point.set(textW, textH);
        return point;
    }


    /**
     * 获取文字的间距
     *
     * @return 获取文字的间距
     */
    private float getTextDescSpace() {
        float allDescWith = 0;
        for (Point tempDesc : allDescTextPoints) {
            allDescWith += tempDesc.x;
        }
        int textContainerW = (int) (dpViewWidth - leftAndRightPadding * 2 - allDescWith);
        if (descs != null && descs.size() > 1) {
            int spaceCount = descs.size() - 1;
            return textContainerW * 1.F / spaceCount;
        }
        return 0;
    }

    /**
     * 绘制进度的描述文字
     *
     * @param canvas 画布
     */
    private void drawDescText(Canvas canvas) {
        if (descs != null && descs.size() > 1) {
            for (int i = 0; i < descs.size(); i++) {
                if (i == currentSelectPosition - 1) {
                    textDescPaint.setColor(textSelectedColor);
                } else {
                    textDescPaint.setColor(textNormalColor);
                }
                Point currentTextPoint = textPoints4Draw.get(i);
                canvas.drawText(descs.get(i), currentTextPoint.x, currentTextPoint.y, textDescPaint);
            }
        }
    }

    /**
     * 绘制选中的圆圈,注意大圈和小圈
     *
     * @param canvas 画布
     */
    private void drawSelectedCircles(Canvas canvas) {
        for (int i = 0; i < bgCirclePoints.size(); i++) {
            Point bgCircle = bgCirclePoints.get(i);
            int currentPosition = currentSelectPosition - 1;
            if (i < currentPosition) {
                smallCirclePaint.setColor(dpvSmallCicleColor);
                canvas.drawCircle(bgCircle.x, bgCircle.y, smallCircleRadio, smallCirclePaint);
            } else if (i == currentPosition) {
                bigCirclePaint.setColor(dpvBigCircleColor);
                canvas.drawCircle(bgCircle.x, bgCircle.y, bigCircleRadio, bigCirclePaint);
            } else if (i > currentPosition) {
                smallCirclePaint.setColor(dpvProgressBgColor);
                canvas.drawCircle(bgCircle.x, bgCircle.y, smallCircleRadio, smallCirclePaint);
            }

        }
    }

    /**
     * 绘制选中的线(彩色的线和渐变的线条
     *
     * @param canvas 画布
     */
    private void drawSelectedLine(Canvas canvas) {
        linePaint.setColor(dpvSmallCicleColor);
        canvas.drawRect(selectedLineRectF, linePaint);
        linePaint.setColor(dpvBigCircleColor);
        LinearGradient linearGradient = new LinearGradient(gradualRectF.left, gradualRectF.top, gradualRectF.right, gradualRectF.bottom, dpvSmallCicleColor, dpvBigCircleColor, Shader.TileMode.CLAMP);
        linePaint.setShader(linearGradient);
        canvas.drawRect(gradualRectF, linePaint);
    }

    /**
     * 绘制背景线条(颜色一致)
     *
     * @param canvas 画布
     */
    private void drawBgLine(Canvas canvas) {
        linePaint.setColor(dpvProgressBgColor);
        canvas.drawRect(bgLineRect, linePaint);
    }

}
