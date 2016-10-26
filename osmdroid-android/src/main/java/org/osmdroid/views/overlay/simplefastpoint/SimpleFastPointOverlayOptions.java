package org.osmdroid.views.overlay.simplefastpoint;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * Options for SimpleFastPointOverlay.
 * Created by Miguel Porto on 25-10-2016.
 */

public class SimpleFastPointOverlayOptions {
    public enum RenderingAlgorithm {FAST, ULTRAFAST}
    protected Paint mPointStyle;
    protected Paint mSelectedPointStyle;
    protected float mCircleRadius = 5;
    protected float mSelectedCircleRadius = 13;
    protected boolean mClickable = true;
    protected int mCellSize = 10;   // the size of the grid cells in pixels. could be adjusted according to the nr of data points...
    protected RenderingAlgorithm mAlgorithm = RenderingAlgorithm.ULTRAFAST;

    public SimpleFastPointOverlayOptions() {
        mPointStyle = new Paint();
        mPointStyle.setStyle(Paint.Style.FILL);
        mPointStyle.setColor(Color.parseColor("#ff7700"));

        mSelectedPointStyle = new Paint();
        mSelectedPointStyle.setStrokeWidth(5);
        mSelectedPointStyle.setStyle(Paint.Style.STROKE);
        mSelectedPointStyle.setColor(Color.parseColor("#ffff00"));
    }

    /**
     * Creates a new {@link SimpleFastPointOverlayOptions} object with default options.
     * @return {@link SimpleFastPointOverlayOptions}
     */
    public static SimpleFastPointOverlayOptions getDefaultStyle() {
        return new SimpleFastPointOverlayOptions();
    }

    /**
     * Sets the style for the point overlay, which is applied to all circles.
     * @param style A Paint object.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setPointStyle(Paint style) {
        mPointStyle = style;
        return this;
    }

    /**
     * Sets the style for the selected point.
     * @param style A Paint object.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setSelectedPointStyle(Paint style) {
        mSelectedPointStyle = style;
        return this;
    }

    /**
     * Sets the radius of the circles to be drawn.
     * @param radius Radius.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setRadius(float radius) {
        mCircleRadius = radius;
        return this;
    }

    /**
     * Sets the radius of the selected point's circle.
     * @param radius Radius.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setSelectedRadius(float radius) {
        mSelectedCircleRadius = radius;
        return this;
    }

    /**
     * Sets whether this overlay is clickable or not. A clickable overlay will automatically select
     * the nearest point.
     * @param clickable True or false.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setIsClickable(boolean clickable) {
        mClickable = clickable;
        return this;
    }

    /**
     * Sets the grid cell size used for indexing, in pixels. Larger cells result in faster rendering
     * speed, but worse fidelity. Default is 10 pixels, for large datasets (>10k points), use 15.
     * @param cellSize The cell size in pixels.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setCellSize(int cellSize) {
        mCellSize = cellSize;
        return this;
    }

    /**
     * Sets the rendering algorithm. There are two options:
     * FAST: not recommended for >10k points. Recalculates the grid index on each draw event.
     *       Better UX, but may be choppier.
     * ULTRAFAST: recommended for >10k points. Only recalculates the grid on touch up, hence much
     *       faster display on move.
     * @param algorithm A {@link RenderingAlgorithm}.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setAlgorithm(RenderingAlgorithm algorithm) {
        mAlgorithm = algorithm;
        return this;
    }

}
