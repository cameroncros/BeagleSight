package com.cross.beaglesight.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;

import com.cross.beaglesight.BuildConfig;
import com.cross.beaglesight.R;
import com.cross.beaglesightlibs.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Double.NaN;

public class ARView extends View {
    private List<Target> targets;
    private List<RenderableTarget> renderableTargets = new ArrayList<>();
    private Location currentLocation;

    private double contentWidthStart;
    private double contentWidthEnd;

    private double contentHeightStart;
    private double contentHeightEnd;
    private double phoneRotation = NaN;
    private double phonePitch = NaN;
    private double phoneBearing = NaN;

    public static int X = 0;
    public static int Y = 1;
    public static int Z = 2;

    private static float[] mRotationMatrixFromVector = new float[9];
    private static float[] mRotationMatrix = new float[9];
    private static float[] orientationVals = new float[3];

    private static double FOV = Math.PI / 3;  // Field of view for camera. Guessed, but should be close enough.
    private Paint black;

    public ARView(Context context) {
        super(context);
        init(null, 0);
    }

    public ARView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ARView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        TypedArray a = null;
        if (!isInEditMode()) {
            a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.ARView, defStyle, 0);
        }

        black = new Paint();
        black.setColor(Color.BLACK);
        black.setStrokeWidth(2);
        black.setTextSize(40);

        if (!isInEditMode() && a != null) {
            a.recycle();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
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

        // Precalculate target locations
        precalcTargetLocations();
    }

    /**
     * Sets the Targets to use to draw the view.
     * @param targetList The list of Targets.
     */
    public void setTargets(List<Target> targetList) {
        targets = targetList;
        precalcTargetLocations();
    }

    /**
     * Set the current phone location
     * @param location
     */
    public void setLocation(Location location) {
        currentLocation = location;
        precalcTargetLocations();
    }

    /**
     * Set the gravity values of the phone
     * @param values
     */
    public void updateGravity(float[] values) {
        // Phone orientated vertically, screen facing user.
        // Z+ is towards the back of the phone.
        //          Y-  Z+
        //        #####
        //        #   #
        //     X+ #   # X-
        //        #   #
        //        #####
        //     Z-   Y+
        // This may not be the best way, and certainly will have gimbal lock issues.
        double vectorMagnitude = Math.sqrt(values[X] * values[X] + values[Y] * values[Y] + values[Z] * values[Z]);
        phonePitch = (Math.acos(values[Z] / vectorMagnitude) - Math.PI / 2);
        phoneRotation = Math.atan(values[X] / values[Y]);
        if (values[Y] < 0) {
            phoneRotation += Math.PI;
        }
        if (phoneRotation > Math.PI) {
            phoneRotation -= 2 * Math.PI;
        }
        invalidate();
    }

    /**
     * Update the current rotation of the phone
     * @param values
     */
    public void updateRotation(float[] values) {
        // Convert the rotation-vector to a 4x4 matrix.
        SensorManager.getRotationMatrixFromVector(mRotationMatrixFromVector, values);
        SensorManager.remapCoordinateSystem(mRotationMatrixFromVector,
                SensorManager.AXIS_X, SensorManager.AXIS_Z,
                mRotationMatrix);
        SensorManager.getOrientation(mRotationMatrix, orientationVals);

        phoneBearing = orientationVals[0];
    }

    private void precalcTargetLocations() {
        if (currentLocation == null) {
            return;
        }
        renderableTargets = new ArrayList<>();
        if (targets != null) {
            for (Target target : targets) {
                Location location = target.getTargetLocation().getLocation();
                double bearing = currentLocation.bearingTo(location) * Math.PI / 180;
                double distance = currentLocation.distanceTo(location);
                double elevation = (currentLocation.getAltitude() - location.getAltitude());
                double pitch = Math.atan(elevation / distance);
                renderableTargets.add(new RenderableTarget(target, bearing, pitch, distance));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (BuildConfig.DEBUG) {
            String info = String.format(Locale.ENGLISH, "Pitch: %f°, Rotation: %f°, Bearing: %f°",
                    phonePitch * 180 / Math.PI, phoneRotation * 180 / Math.PI, phoneBearing * 180 / Math.PI);
            canvas.drawText(info, (float) contentWidthStart, (float) (contentHeightStart + (contentHeightEnd - contentHeightStart) / 2), black);

            if (renderableTargets.size() > 0) {
                String info2 = String.format(Locale.ENGLISH, "Pitch: %f°, Bearing: %f°",
                        renderableTargets.get(0).pitch * 180 / Math.PI, renderableTargets.get(0).bearing * 180 / Math.PI);
                canvas.drawText(info2, (float) contentWidthStart, (float) (contentHeightStart + (contentHeightEnd - contentHeightStart) / 3), black);
            } else {
                canvas.drawText("No targets loaded.", (float) contentWidthStart, (float) (contentHeightStart + (contentHeightEnd - contentHeightStart) / 3), black);
            }
        }

        for (RenderableTarget target : renderableTargets) {
            // Calculate pixel locations.
            double bearingDiff = phoneBearing - target.bearing;
            double xPixelDiff = bearingDiff * (contentWidthEnd - contentWidthStart) / FOV;

            double pitchDiff = phonePitch - target.pitch;
            double yPixelDiff = pitchDiff * (contentWidthEnd - contentWidthStart) / FOV;

            // Rotate pixel locations.
            double xPixelRot = (Math.cos(phoneRotation) * xPixelDiff) + (Math.sin(phoneRotation) * yPixelDiff);
            double yPixelRot = (Math.sin(phoneRotation) * xPixelDiff) - (Math.cos(phoneRotation) * yPixelDiff);

            // Translate the pixels locations to the center of the screen.
            double xPixel = -xPixelRot + (contentWidthEnd - contentWidthStart) / 2;
            double yPixel = -yPixelRot + (contentHeightEnd - contentHeightStart) / 2;

            canvas.drawCircle((float) xPixel, (float) yPixel, 10, black);
        }
    }

    class RenderableTarget {
        Target target;
        double bearing;
        double distance;
        double pitch;

        RenderableTarget(Target target, double bearing, double pitch, double distance) {
            this.target = target;
            this.bearing = bearing;
            this.pitch = pitch;
            this.distance = distance;
        }
    }
}
