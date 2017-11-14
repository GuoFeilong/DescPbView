## 目标:自定义一个带文字带进度的控件 ##

##效果图:
![这里写图片描述](http://img.blog.csdn.net/20171114184712595?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvZ2l2ZW1lYWNvbmRvbQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

##不啰嗦先看东西:

![这里写图片描述](http://img.blog.csdn.net/20171114184758399?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvZ2l2ZW1lYWNvbmRvbQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

#步骤分析

 1. 提取自定义属性

```
	//提供对外暴露的属性,如有不够自己扩展
    <declare-styleable name="DescProgressView">
        <attr name="dpv_text_normal_color" format="color" />
        <attr name="dpv_text_seleced_color" format="color" />
        <attr name="dpv_text_size" format="dimension" />
        <attr name="dev_progress_bg_color" format="color" />
        <attr name="dev_progress_small_circle_color" format="color" />
        <attr name="dev_progress_big_circle_color" format="color" />
    </declare-styleable>
```


 2. 解析自定义属性
	 

```
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

```

 3. 测量UI图的比例(包含图标大小比例,位置比例)
	 

```
	// 这里大家可以根据自己的习惯来,我习惯用view的尺寸当做参照,来约束界面的view,各有利弊,也可以暴露出属性设置具体的dp值,根据比例的话,调整好比例后,所有的绘制内容会统一约束
	private static final float SCALE_OF_PROGRESS_HEIGHT = 70.F / 120;
    private static final float SCALE_OF_TOP_AND_BOTTOM_PADDING = 10.F / 120;
    private static final float SCALE_OF_LEFT_AND_RIGHT_PADDING = 20.F / 120;
    private static final float SCALE_OF_TEXT_DESC_CONTAINER = 50.F / 120;
    private static final float SCALE_OF_BIG_CIRCLE_HEIGHT = 22.F / 120;
    private static final float SCALE_OF_SMALL_CIRCLE_HEIGHT = 16.F / 120;
    private static final float SCALE_OF_LINE_HEIGHT = 4.F / 120;
    private static final float DEF_VIEW_HEIGHT = 120.F;
```

 4. 提取绘制的各个元素的位置属性坐标等
	 

```
这个view的唯一要提前确定的就是文字的位置,文字的位置确定需要知道所有文字的长度,左右间距,计算出中间的白色间隔
代码如下
```

```
 /**
     * 获取文字在画布中的位置
     */
    private void getDescTextRegonPoint() {
        for (int i = 0; i < descs.size(); i++) {
            Point textRegonPoint = new Point();
            int sumX = 0;
            //非常重要:计算各个文字在view中的具体坐标,体会下这个二级for循环,子循环是确定每个描述文本的位置
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
```

```
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
```

 5. 绘制
	 

```
我们在view测量确定了尺寸完毕之后,直接绘制即可

```

```
   @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    // 确定各个比例的大小
        super.onSizeChanged(w, h, oldw, oldh);
        dpViewHeight = h;
        dpViewWidth = w;
        progressContainerHeight = (int) (SCALE_OF_PROGRESS_HEIGHT * dpViewHeight);
        topAndBottomPadding = (int) (SCALE_OF_TOP_AND_BOTTOM_PADDING * dpViewHeight);
        leftAndRightPadding = (int) (SCALE_OF_LEFT_AND_RIGHT_PADDING * dpViewHeight);
        textDescContainerHeight = (int) (SCALE_OF_TEXT_DESC_CONTAINER * dpViewHeight);
        smallCircleRadio = (int) (SCALE_OF_SMALL_CIRCLE_HEIGHT * dpViewHeight / 2);
        bigCircleRadio = (int) (SCALE_OF_BIG_CIRCLE_HEIGHT * dpViewHeight / 2);
        lineHeight = (int) (SCALE_OF_LINE_HEIGHT * dpViewHeight);

		// 获取各个部分所需要的约束坐标
        getDescTextWidthAndHeight();
        getDescTextRegonPoint();
        getBgLineRectF();
        getBgCirclePoints();
        getSelectedRectF();
        getColorFullRectF();
        getGrayRectF();
    }
```

```
	 @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDescText(canvas);
        drawBgLine(canvas);
        drawSelectedLine(canvas);
        drawGrayRectF(canvas);
        drawSelectedCircles(canvas);
    }
//绘制部分的代码就是canvas 的API的使用,没有什么技术含量.
//最后暴露给外面设置数据的接口

public void setProgressDescs(List<String> descs, int currentSelectPosition) {
        this.currentSelectPosition = currentSelectPosition;
        if (descs != null && descs.size() > 1) {
            this.descs.clear();
            this.descs.addAll(descs);
            this.allDescTextPoints.clear();
            invalidate();
        }
    }
```
#源代码下载地址[https://github.com/GuoFeilong/DescPbView来个star就更好了谢谢!](https://github.com/GuoFeilong/DescPbView)

