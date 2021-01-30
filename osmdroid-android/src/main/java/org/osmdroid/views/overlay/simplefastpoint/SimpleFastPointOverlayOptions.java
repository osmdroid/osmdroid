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

    public enum LabelPolicy {ZOOM_THRESHOLD, DENSITY_THRESHOLD}

    protected Paint mPointStyle;
    protected Paint mSelectedPointStyle;
    protected Paint mTextStyle;
    protected float mCircleRadius = 5;
    protected float mSelectedCircleRadius = 13;
    protected boolean mClickable = true;
    protected int mCellSize = 10;   // the size of the grid cells in pixels.
    protected RenderingAlgorithm mAlgorithm = RenderingAlgorithm.MAXIMUM_OPTIMIZATION;
    protected Shape mSymbol = Shape.SQUARE;     // default is square, cause circle is a slow renderer
    protected LabelPolicy mLabelPolicy = LabelPolicy.ZOOM_THRESHOLD;
    protected int mMaxNShownLabels = 250;
    protected int mMinZoomShowLabels = 11;

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
     *
     * @return {@link SimpleFastPointOverlayOptions}
     */
    public static SimpleFastPointOverlayOptions getDefaultStyle() {
        return new SimpleFastPointOverlayOptions();
    }

    /**
     * Sets the style for the point overlay, which is applied to all circles.
     * If the layer is individually styled, the individual style overrides this.
     *
     * @param style A Paint object.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setPointStyle(Paint style) {
        mPointStyle = style;
        return this;
    }

    /**
     * Sets the style for the selected point.
     *
     * @param style A Paint object.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setSelectedPointStyle(Paint style) {
        mSelectedPointStyle = style;
        return this;
    }

    /**
     * Sets the radius of the circles to be drawn.
     *
     * @param radius Radius.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setRadius(float radius) {
        mCircleRadius = radius;
        return this;
    }

    /**
     * Sets the radius of the selected point's circle.
     *
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
     *
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
     *
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
     * Not recommended for >10k points. Better UX, but may be choppier.
     * MAXIMUM_OPTIMIZATION: Fastest. Only recalculates the grid on touch up and animation end
     * , hence much faster display on move. Recommended for >10k points.
     *
     * @param algorithm A {@link RenderingAlgorithm}.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setAlgorithm(RenderingAlgorithm algorithm) {
        mAlgorithm = algorithm;
        return this;
    }

    /**
     * Sets the symbol shape for this layer. Hint: circle shape is less performant, avoid for large N.
     *
     * @param symbol The symbol, currently CIRCLE or SQUARE.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setSymbol(Shape symbol) {
        mSymbol = symbol;
        return this;
    }

    /**
     * Sets the style for the labels.
     * If the layer is individually styled, the individual style overrides this.
     *
     * @param textStyle The style.
     * @return The updated {@link SimpleFastPointOverlayOptions}
     */
    public SimpleFastPointOverlayOptions setTextStyle(Paint textStyle) {
        mTextStyle = textStyle;
        return this;
    }

    /**
     * Sets the minimum zoom level at which the labels should be drawn. This option is
     * <b>ignored</b> if LabelPolicy is DENSITY_THRESHOLD.
     *
     * @param minZoomShowLabels The zoom level.
     * @return
     */
    public SimpleFastPointOverlayOptions setMinZoomShowLabels(int minZoomShowLabels) {
        mMinZoomShowLabels = minZoomShowLabels;
        return this;
    }

    /**
     * Sets the threshold (nr. of visible points) after which labels will not be drawn. <b>This
     * option only works when LabelPolicy is DENSITY_THRESHOLD and the algorithm is
     * MAXIMUM_OPTIMIZATION</b>.
     *
     * @param maxNShownLabels The maximum number of visible points
     * @return
     */
    public SimpleFastPointOverlayOptions setMaxNShownLabels(int maxNShownLabels) {
        mMaxNShownLabels = maxNShownLabels;
        return this;
    }

    /**
     * Sets the policy for displaying point labels. Can be:<br/>
     * ZOOM_THRESHOLD: Labels are not displayed is current map zoom level is lower than
     * <code>MinZoomShowLabels</code>
     * DENSITY_THRESHOLD: Labels are not displayed when the number of visible points is larger
     * than <code>MaxNShownLabels</code>. <b>This only works for MAXIMUM_OPTIMIZATION</b><br/>
     *
     * @param labelPolicy One of <code>ZOOM_THRESHOLD</code> or <code>DENSITY_THRESHOLD</code>
     * @return
     */
    public SimpleFastPointOverlayOptions setLabelPolicy(LabelPolicy labelPolicy) {
        mLabelPolicy = labelPolicy;
        return this;
    }

    /* getters for the protected options, see setters for description */

    public Paint getPointStyle() {
        return mPointStyle;
    }

    public Paint getSelectedPointStyle() {
        return mSelectedPointStyle;
    }

    public Paint getTextStyle() {
        return mTextStyle;
    }

    public float getCircleRadius() {
        return mCircleRadius;
    }

    public float getSelectedCircleRadius() {
        return mSelectedCircleRadius;
    }

    public boolean isClickable() {
        return mClickable;
    }

    public int getCellSize() {
        return mCellSize;
    }

    public RenderingAlgorithm getAlgorithm() {
        return mAlgorithm;
    }

    public Shape getSymbol() {
        return mSymbol;
    }

    public LabelPolicy getLabelPolicy() {
        return mLabelPolicy;
    }

    public int getMaxNShownLabels() {
        return mMaxNShownLabels;
    }

    public int getMinZoomShowLabels() {
        return mMinZoomShowLabels;
    }
}
