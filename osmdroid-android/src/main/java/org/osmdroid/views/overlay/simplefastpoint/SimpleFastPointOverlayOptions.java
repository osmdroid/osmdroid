package org.osmdroid.views.overlay.simplefastpoint;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * Options for SimpleFastPointOverlay.
 * Created by Miguel Porto on 25-10-2016.
 */

public class SimpleFastPointOverlayOptions {
    public enum RenderingAlgorithm {NO_OPTIMIZATION, MEDIUM_OPTIMIZATION, MAXIMUM_OPTIMIZATION}
    public enum Shape {CIRCLE, SQUARE}
    protected Paint mPointStyle;
    protected Paint mSelectedPointStyle;
    protected Paint mTextStyle;
    protected float mCircleRadius = 5;
    protected float mSelectedCircleRadius = 13;
    protected boolean mClickable = true;
    protected int mCellSize = 10;   // the size of the grid cells in pixels.
    protected RenderingAlgorithm mAlgorithm = RenderingAlgorithm.MAXIMUM_OPTIMIZATION;
    protected Shape mSymbol = Shape.CIRCLE;
    protected int mMaxNumLabels = 200;

    public SimpleFastPointOverlayOptions() {
        mPointStyle = new Paint();
        mPointStyle.setStyle(Paint.Style.FILL);
        mPointStyle.setColor(Color.parseColor("#ff7700"));

        mSelectedPointStyle = new Paint();
        mSelectedPointStyle.setStrokeWidth(5);
        mSelectedPointStyle.setStyle(Paint.Style.STROKE);
        mSelectedPointStyle.setColor(Color.parseColor("#ffff00"));

        mTextStyle = new Paint();
        mTextStyle.setStyle(Paint.Style.FILL);
        mTextStyle.setColor(Color.parseColor("#ffff00"));
        mTextStyle.setTextAlign(Paint.Align.CENTER);
        mTextStyle.setTextSize(24);
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
     * Sets the rendering algorithm. There are three options:
     * NO_OPTIMIZATION: Slowest option. Draw all points on each draw event.
     * MEDIUM_OPTIMIZATION: Faster. Recalculates the grid index on each draw event.
     *          Not recommended for >10k points. Better UX, but may be choppier.
     * MAXIMUM_OPTIMIZATION: Fastest. Only recalculates the grid on touch up and animation end
     *          , hence much faster display on move. Recommended for >10k points.
     * @param algorithm A {@link RenderingAlgorithm}.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setAlgorithm(RenderingAlgorithm algorithm) {
        mAlgorithm = algorithm;
        return this;
    }

    /**
     * Sets the symbol shape for this layer.
     * @param symbol The symbol.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setSymbol(Shape symbol) {
        mSymbol = symbol;
        return this;
    }

    /**
     * Sets the style for the labels.
     * @param textStyle The style.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setTextStyle(Paint textStyle) {
        mTextStyle = textStyle;
        return this;
    }

    /**
     * Sets the maximum threshold of the visible number of labels after which no labels will be
     * drawn.
     * @param maxNumLabels
     * @return
     */
    public SimpleFastPointOverlayOptions setMaxNumLabels(int maxNumLabels) {
        mMaxNumLabels = maxNumLabels;
        return this;
    }
}