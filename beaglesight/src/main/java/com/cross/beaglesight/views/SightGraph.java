package com.cross.beaglesight.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.cross.beaglesightlibs.BowConfig;
import com.cross.beaglesight.R;
import com.cross.beaglesightlibs.PositionPair;
import com.cross.beaglesightlibs.PositionCalculator;
import com.cross.beaglesightlibs.exceptions.InvalidNumberFormatException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO: document your custom view class.
 */
public class SightGraph extends View {

    private Paint linePaint;
    private Paint pointPaint;
    private Paint pointSelectedPaint;
    private Paint graphPaint;
    private Paint graphMinorAxis;
    private Paint backgroundPaint;
    private Paint labelPaint;
    private Paint axisLabelPaint;
    private BowConfig bowConfig;
    private PositionCalculator pc;

    float minDist = 0;
    float maxDist = 100;
    float minPos = 0;
    float maxPos = 100;

    float contentWidthStart;
    float contentWidthEnd;

    float contentHeightStart;
    float contentHeightEnd;

    float selectedDistance;

    PositionPair selectedPairPixel;
    Map<PositionPair, PositionPair> positionPairMap;

    private SightGraphCallback updateCallback;
    private float lineWidth;
    private float touchRadius;

    public SightGraph(Context context) {
        super(context);
        init(null, 0);
    }

    public SightGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SightGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private Paint getPaint(TypedArray a, int colorStyle, int def, float lineWidth)
    {
        int lineColor = def;
        if (!isInEditMode() && a != null) {
            lineColor = a.getColor(
                    colorStyle,
                    def);
        }
        Paint temp = new Paint();
        temp.setColor(lineColor);
        temp.setStrokeWidth(lineWidth);

        return temp;
    }

    private void init(AttributeSet attrs, int defStyle) {
        if (isInEditMode()) {
            BowConfig bowConfig = new BowConfig("TempName", "TempDescription");

            try {
                bowConfig.addPosition(new PositionPair("15", "45"));
                bowConfig.addPosition(new PositionPair("18", "40"));
                bowConfig.addPosition(new PositionPair("20", "40"));
                bowConfig.addPosition(new PositionPair("30", "42"));
                bowConfig.addPosition(new PositionPair("40", "45"));
                bowConfig.addPosition(new PositionPair("50", "49"));
                bowConfig.addPosition(new PositionPair("60", "54"));
            }
            catch (InvalidNumberFormatException nfe)
            {
                // Do nothing, will never happen.
            }

            setBowConfig(bowConfig);
        }

        // Load attributes
        TypedArray a = null;
        if (!isInEditMode()) {
            a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.SightGraph, defStyle, 0);
        }

        lineWidth = 10f;
        if (!isInEditMode() && a != null) {
            lineWidth= a.getDimension(
                    R.styleable.SightGraph_lineWidth,
                    10);
        }

        linePaint = getPaint(a, R.styleable.SightGraph_lineColor, Color.BLACK, lineWidth);
        graphPaint = getPaint(a, R.styleable.SightGraph_graphColor, Color.BLUE, lineWidth);
        graphMinorAxis = getPaint(a, R.styleable.SightGraph_graphColor, Color.BLUE, lineWidth/2);
        pointPaint = getPaint(a, R.styleable.SightGraph_pointColor, Color.RED, lineWidth);
        backgroundPaint = getPaint(a, R.styleable.SightGraph_backgroundColor, Color.GREEN, lineWidth);
        labelPaint = getPaint(a, R.styleable.SightGraph_labelColor, Color.YELLOW, lineWidth);
        axisLabelPaint = getPaint(a, R.styleable.SightGraph_labelColor, Color.YELLOW, lineWidth);

        float textSize = 10f;
        if (!isInEditMode() && a != null) {
            textSize = a.getDimension(
                    R.styleable.SightGraph_labelSize,
                    10);
        }
        labelPaint.setTextSize(textSize);
        axisLabelPaint.setTextSize(textSize/2);

        pointSelectedPaint = new Paint(pointPaint);
        pointSelectedPaint.setColor(manipulateColor(pointPaint.getColor(), 0.8f));

        if (!isInEditMode() && a != null) {
            a.recycle();
        }
    }

    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int canvasWidth = getWidth() - paddingLeft - paddingRight;
        int canvasHeight = getHeight() - paddingTop - paddingBottom;

        // Canvas drawable bounds.
        contentWidthStart = paddingLeft;
        contentWidthEnd = canvasWidth + paddingLeft;
        contentHeightStart = paddingTop;
        contentHeightEnd = canvasHeight + paddingTop;

        // Touch radius
        if (canvasWidth > canvasHeight)
        {
            touchRadius = canvasHeight*0.2f * canvasHeight*0.2f ;
        }
        else
        {
            touchRadius = canvasWidth*0.2f * canvasWidth*0.2f;
        }

        // Precalculate dot locations
        precalcDotLocations();
    }

    /**
     * Sets the bowConfig to use to draw the view.
     * @param config The BowConfig to use to draw the view.
     */
    public void setBowConfig(BowConfig config)
    {
        bowConfig = config;
        pc = config.getPositionCalculator();
        minPos = pc.getMinPosition(minDist, maxDist);
        maxPos = pc.getMaxPosition(minDist, maxDist);
        float tenPercent = (maxPos - minPos) * 0.1f;
        minPos -= tenPercent;
        maxPos += tenPercent;

        // Precalculate dot locations
        precalcDotLocations();
    }

    private void precalcDotLocations() {
        positionPairMap = new HashMap<>();
        List<PositionPair> positions = bowConfig.getPositions();

        for (PositionPair pair : positions)
        {
            float position = pair.getPositionFloat();
            float distance = pair.getDistanceFloat();

            float positionPixel = positionToPixel(position);
            float distancePixel = distanceToPixel(distance);

            PositionPair pixelPair = new PositionPair(distancePixel, positionPixel);
            positionPairMap.put(pixelPair, pair);
        }
    }

    float pixelToDistance(float pixel)
    {
        // 20m == contentWidthStart
        // 100m == contentWidthEnd.
        float percent = (pixel - contentWidthStart) / (contentWidthEnd - contentWidthStart);
        return minDist + percent * (maxDist - minDist);
    }

    float distanceToPixel(float distance)
    {
        float percent = (distance - minDist) / (maxDist - minDist);
        return Math.round(contentWidthStart + percent * (contentWidthEnd - contentWidthStart));
    }

    float pixelToPosition(float pixel)
    {
        float percent = (pixel - contentHeightStart) / (contentHeightEnd - contentHeightStart);
        return minPos + percent * (maxPos - minPos);
    }

    float positionToPixel(float position)
    {
        float percent = (position - minPos) / (maxPos - minPos);
        return Math.round(contentHeightStart + percent * (contentHeightEnd - contentHeightStart));
    }

    float calculateYVal(float xVal)
    {
        float distance = pixelToDistance(xVal);
        float position = pc.calcPosition(distance);
        return  positionToPixel(position);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw Background
        canvas.drawRect((long)contentWidthStart, (long)contentHeightStart, (long)contentWidthEnd, (long)contentHeightEnd, backgroundPaint);

        // Draw Axis
        for (float i = 0; i < maxDist; i+=10)
        {
            float xPixel = distanceToPixel(i);
            canvas.drawLine(xPixel, contentHeightStart, xPixel, contentHeightEnd, graphMinorAxis);
            canvas.drawText(Float.toString(i), xPixel+axisLabelPaint.getTextSize(), contentHeightEnd-axisLabelPaint.getTextSize(), axisLabelPaint);
        }

        for (float i = Math.round(minPos/10)* 10; i < maxPos; i+=10)
        {
            float yPixel = positionToPixel(i);
            canvas.drawLine(contentWidthStart, yPixel, contentWidthEnd, yPixel, graphMinorAxis);
            canvas.drawText(Float.toString(i), axisLabelPaint.getTextSize(), yPixel + axisLabelPaint.getTextSize(), axisLabelPaint);
        }

        // Draw the graph.
        float lastYVal = calculateYVal(contentWidthStart);
        for (float i = contentWidthStart; i < contentWidthEnd; i++)
        {
            float yVal = calculateYVal(i);
            canvas.drawLine(i-1, lastYVal, i, yVal, graphPaint);
            lastYVal = yVal;
        }

        // Draw the dots.
        Set<PositionPair> positions = positionPairMap.keySet();
        if (selectedPairPixel != null)
        {
            float positionPixel = selectedPairPixel.getPositionFloat();
            float distancePixel = selectedPairPixel.getDistanceFloat();

            canvas.drawCircle(distancePixel, positionPixel, lineWidth*4, pointSelectedPaint);
        }
        for (PositionPair pair : positions)
        {
            float positionPixel = pair.getPositionFloat();
            float distancePixel = pair.getDistanceFloat();

            canvas.drawCircle(distancePixel, positionPixel, lineWidth*2, pointPaint);
        }

        // Draw the select line.
        if (selectedDistance > 0)
        {
            float xval = distanceToPixel(selectedDistance);
            canvas.drawLine(xval, contentHeightStart, xval, contentHeightEnd, linePaint);

            float position = pc.calcPosition(selectedDistance);
            float yval = positionToPixel(position);
            canvas.drawLine(contentWidthStart, yval, contentWidthEnd, yval, linePaint);

            canvas.drawText(PositionCalculator.getDisplayValue(position, 2),
                    xval + labelPaint.getTextSize() / 2,
                    yval - labelPaint.getTextSize() / 2,
                    labelPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                updateOnTouch(event);
                break;
            case MotionEvent.ACTION_MOVE:
                updateOnTouch(event);
                break;
            case MotionEvent.ACTION_UP:
                performClick();
                setPressed(false);
                this.getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                this.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return true;
    }

    private void updateOnTouch(MotionEvent event) {
        float xPixel = event.getX();
        float yPixel = event.getY();

        selectedDistance = pixelToDistance(xPixel);

        if (updateCallback != null)
        {
            updateCallback.updateDistance(selectedDistance);
        }

        Set<PositionPair> pixelPairs = positionPairMap.keySet();
        float closestDist = Float.MAX_VALUE;
        PositionPair closestPixel = null;
        for (PositionPair pair : pixelPairs)
        {
            float xdist = pair.getDistanceFloat()-xPixel;
            float ydist = pair.getPositionFloat()-yPixel;
            float dist = xdist*xdist + ydist*ydist;
            if (dist < closestDist)
            {
                closestPixel = pair;
                closestDist = dist;
            }
        }

        if (closestDist < touchRadius)
        {
            selectedPairPixel = closestPixel;
            if (updateCallback != null)
            {
                updateCallback.setSelected(positionPairMap.get(selectedPairPixel));
            }
        }
        else
        {
            if (selectedPairPixel != null)
            {
                updateCallback.setSelected(null);
            }
            selectedPairPixel = null;
        }
    }

    public void setSelectedDistance(float selectedDistance)
    {
        this.selectedDistance = selectedDistance;
        invalidate();
    }

    public void setUpdateDistanceCallback(SightGraphCallback updateCallback)
    {
        this.updateCallback = updateCallback;
    }



    @Override
    public boolean performClick() {
        return super.performClick();
    }


    public interface SightGraphCallback
    {
        void updateDistance(float distance);
        void setSelected(PositionPair selectedPair);
    }
}
