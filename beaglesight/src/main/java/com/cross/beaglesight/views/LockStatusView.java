package com.cross.beaglesight.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;

import com.cross.beaglesight.R;
import com.cross.beaglesightlibs.LockStatus;

/**
 * Indicator View, shows when the GPS lock is stable.
 */
public class LockStatusView extends View {
    private LockStatus.Status status = LockStatus.Status.WEAK;
    private LockStatus lockStatus = new LockStatus();

    private Paint weakPaint;
    private Paint mediumPaint;
    private Paint strongPaint;

    public LockStatusView(Context context) {
        super(context);
        init(null, 0);
    }

    public LockStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LockStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.LockStatusView, defStyle, 0);
        a.recycle();

        // Set up a default TextPaint object
        weakPaint = new Paint();
        weakPaint.setColor(Color.RED);
        weakPaint.setStrokeWidth(0);
        mediumPaint = new Paint();
        mediumPaint.setColor(Color.YELLOW);
        mediumPaint.setStrokeWidth(0);
        strongPaint = new Paint();
        strongPaint.setColor(Color.GREEN);
        strongPaint.setStrokeWidth(0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the circle
        Paint temp = null;
        switch (status)
        {
            default:
            case WEAK:
                temp = weakPaint;
                break;
            case MEDIUM:
                temp = mediumPaint;
                break;
            case STRONG:
                temp = strongPaint;
                break;
        }
        canvas.drawCircle(paddingLeft + (contentWidth) / 2,
                paddingTop + (contentHeight) / 2,
                (contentWidth) / 2,
                temp);
    }

    public LockStatus.Status updateLocation(Location location) {
        status = this.lockStatus.updateLocation(location);
        return status;
    }

    public void setStatus(LockStatus.Status lockStatus) {
        this.status = lockStatus;
    }
}
