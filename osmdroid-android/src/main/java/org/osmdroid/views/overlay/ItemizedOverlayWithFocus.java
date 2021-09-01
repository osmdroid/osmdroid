// Created by plusminus on 20:50:06 - 03.10.2008
package org.osmdroid.views.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import org.osmdroid.library.R;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import java.util.List;

/**
 * @param <Item>
 * @deprecated see {@link Marker}
 * it is generally recommended to use the  {@link Marker} class instead of this.
 * While it does work and is usually maintained, the Marker class as a lot more capabilities
 */
@Deprecated
public class ItemizedOverlayWithFocus<Item extends OverlayItem> extends ItemizedIconOverlay<Item> {

    // ===========================================================
    // Constants
    // ===========================================================

    private final int DEFAULTMARKER_BACKGROUNDCOLOR = Color.rgb(101, 185, 74);


    // ===========================================================
    // Fields
    // ===========================================================

    private int DESCRIPTION_BOX_PADDING = 3;
    private int DESCRIPTION_BOX_CORNERWIDTH = 3;

    /**
     * Additional to <code>DESCRIPTION_LINE_HEIGHT</code>.
     */
    private int DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT = 2;

    private int FONT_SIZE_DP = 14;
    private int DESCRIPTION_MAXWIDTH = 600;
    private int DESCRIPTION_LINE_HEIGHT = 30;

    protected int mMarkerFocusedBackgroundColor;
    protected Paint mMarkerBackgroundPaint, mDescriptionPaint, mTitlePaint;


    protected Drawable mMarkerFocusedBase;
    protected int mFocusedItemIndex;
    protected boolean mFocusItemsOnTap;
    private int fontSizePixels;
    private final Point mFocusedScreenCoords = new Point();

    private Context mContext;

    private String UNKNOWN;

    // ===========================================================
    // Constructors
    // ===========================================================

    public ItemizedOverlayWithFocus(final Context pContext, final List<Item> aList,
                                    final OnItemGestureListener<Item> aOnItemTapListener) {
        this(aList, aOnItemTapListener, pContext);
    }

    public ItemizedOverlayWithFocus(final List<Item> aList,
                                    final OnItemGestureListener<Item> aOnItemTapListener, Context pContext) {
        this(aList,
                pContext.getResources().getDrawable(R.drawable.marker_default)
                , null, NOT_SET,
                aOnItemTapListener, pContext);
    }

    public ItemizedOverlayWithFocus(final List<Item> aList, final Drawable pMarker,
                                    final Drawable pMarkerFocused, final int pFocusedBackgroundColor,
                                    final OnItemGestureListener<Item> aOnItemTapListener, Context pContext) {

        super(aList, pMarker, aOnItemTapListener, pContext);
        mContext = pContext;
        if (pMarkerFocused == null) {
            this.mMarkerFocusedBase = boundToHotspot(
                    pContext.getResources().getDrawable(R.drawable.marker_default_focused_base)
                    ,
                    HotspotPlace.BOTTOM_CENTER);
        } else
            this.mMarkerFocusedBase = pMarkerFocused;

        this.mMarkerFocusedBackgroundColor = (pFocusedBackgroundColor != NOT_SET) ? pFocusedBackgroundColor
                : DEFAULTMARKER_BACKGROUNDCOLOR;

        calculateDrawSettings();

        this.unSetFocusedItem();
    }

    private void calculateDrawSettings() {
        //calculate font size based on DP
        fontSizePixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                FONT_SIZE_DP, mContext.getResources().getDisplayMetrics());
        DESCRIPTION_LINE_HEIGHT = fontSizePixels + 5;

        //calculate max width based on screen width.
        DESCRIPTION_MAXWIDTH = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.8);
        UNKNOWN = mContext.getResources().getString(R.string.unknown);

        this.mMarkerBackgroundPaint = new Paint(); // Color is set in onDraw(...)

        this.mDescriptionPaint = new Paint();
        this.mDescriptionPaint.setAntiAlias(true);
        this.mDescriptionPaint.setTextSize(fontSizePixels);
        this.mTitlePaint = new Paint();
        this.mTitlePaint.setTextSize(fontSizePixels);
        this.mTitlePaint.setFakeBoldText(true);
        this.mTitlePaint.setAntiAlias(true);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================


    /**
     * default is 3 pixels
     *
     * @param value
     */
    public void setDescriptionBoxPadding(int value) {
        DESCRIPTION_BOX_PADDING = value;
    }

    /**
     * default 3
     *
     * @param value
     */
    public void setDescriptionBoxCornerWidth(int value) {
        DESCRIPTION_BOX_CORNERWIDTH = value;
    }

    /**
     * default is 2
     *
     * @param value
     */
    public void setDescriptionTitleExtraLineHeight(int value) {
        DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT = value;
    }

    /**
     * default is a green like color
     *
     * @param value
     */
    public void setMarkerBackgroundColor(int value) {
        mMarkerFocusedBackgroundColor = value;
    }

    public void setMarkerTitleForegroundColor(int value) {
        mTitlePaint.setColor(value);
    }

    public void setMarkerDescriptionForegroundColor(int value) {
        mDescriptionPaint.setColor(value);
    }

    /**
     * default is 14
     *
     * @param value
     */
    public void setFontSize(int value) {
        FONT_SIZE_DP = value;
        calculateDrawSettings();
    }

    /**
     * in pixels, default is 600
     *
     * @param value
     */
    public void setDescriptionMaxWidth(int value) {
        DESCRIPTION_MAXWIDTH = value;
        calculateDrawSettings();
    }

    /**
     * default is 30
     *
     * @param value
     */
    public void setDescriptionLineHeight(int value) {
        DESCRIPTION_LINE_HEIGHT = value;
        calculateDrawSettings();
    }

    public Item getFocusedItem() {
        if (this.mFocusedItemIndex == NOT_SET) {
            return null;
        }
        return this.mItemList.get(this.mFocusedItemIndex);
    }

    public void setFocusedItem(final int pIndex) {
        this.mFocusedItemIndex = pIndex;
    }

    public void unSetFocusedItem() {
        this.mFocusedItemIndex = NOT_SET;
    }

    public void setFocusedItem(final Item pItem) {
        final int indexFound = super.mItemList.indexOf(pItem);
        if (indexFound < 0) {
            throw new IllegalArgumentException();
        }

        this.setFocusedItem(indexFound);
    }

    public void setFocusItemsOnTap(final boolean doit) {
        this.mFocusItemsOnTap = doit;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    protected boolean onSingleTapUpHelper(final int index, final Item item, final MapView mapView) {
        if (this.mFocusItemsOnTap) {
            this.mFocusedItemIndex = index;
            mapView.postInvalidate();
        }
        return this.mOnItemGestureListener.onItemSingleTapUp(index, item);
    }

    private final Rect mRect = new Rect();

    @Override
    public void draw(final Canvas c, final Projection pProjection) {

        super.draw(c, pProjection);

        if (this.mFocusedItemIndex == NOT_SET) {
            return;
        }

        // this happens during shutdown
        if (super.mItemList == null)
            return;
        // get focused item's preferred marker & hotspot
        final Item focusedItem = super.mItemList.get(this.mFocusedItemIndex);
        Drawable markerFocusedBase = focusedItem.getMarker(OverlayItem.ITEM_STATE_FOCUSED_MASK);
        if (markerFocusedBase == null) {
            markerFocusedBase = this.mMarkerFocusedBase;
        }

        /* Calculate and set the bounds of the marker. */
        pProjection.toPixels(focusedItem.getPoint(), mFocusedScreenCoords);

        markerFocusedBase.copyBounds(mRect);
        mRect.offset(mFocusedScreenCoords.x, mFocusedScreenCoords.y);

        /* Strings of the OverlayItem, we need. */
        final String itemTitle = (focusedItem.getTitle() == null) ? UNKNOWN : focusedItem
                .getTitle();
        final String itemDescription = (focusedItem.getSnippet() == null) ? UNKNOWN : focusedItem
                .getSnippet();

        /*
         * Store the width needed for each char in the description to a float array. This is pretty
         * efficient.
         */
        final float[] widths = new float[itemDescription.length()];
        this.mDescriptionPaint.getTextWidths(itemDescription, widths);

        final StringBuilder sb = new StringBuilder();
        int maxWidth = 0;
        int curLineWidth = 0;
        int lastStop = 0;
        int i;
        int lastwhitespace = 0;
        /*
         * Loop through the charwidth array and harshly insert a linebreak, when the width gets
         * bigger than DESCRIPTION_MAXWIDTH.
         */
        for (i = 0; i < widths.length; i++) {
            if (!Character.isLetter(itemDescription.charAt(i))) {
                lastwhitespace = i;
            }

            final float charwidth = widths[i];

            if (itemDescription.charAt(i) == '\n') {
                sb.append(itemDescription.subSequence(lastStop, i + 1));
                lastStop = i + 1;
                maxWidth = Math.max(maxWidth, curLineWidth);
                curLineWidth = 0;
                lastwhitespace = lastStop;
                continue;
            } else if (curLineWidth + charwidth > DESCRIPTION_MAXWIDTH) {
                boolean noSpace = lastStop == lastwhitespace;
                if (!noSpace) {
                    i = lastwhitespace;
                }

                sb.append(itemDescription.subSequence(lastStop, i));
                sb.append('\n');

                lastStop = i;
                maxWidth = Math.max(maxWidth, curLineWidth);
                curLineWidth = 0;
                lastwhitespace = lastStop;
                if (noSpace) {
                    i--;
                    continue;
                }
            }

            curLineWidth += charwidth;
        }
        /* Add the last line to the rest to the buffer. */
        if (i != lastStop) {
            final String rest = itemDescription.substring(lastStop, i);
            maxWidth = Math.max(maxWidth, (int) this.mDescriptionPaint.measureText(rest));
            sb.append(rest);
        }
        final String[] lines = sb.toString().split("\n");

        /*
         * The title also needs to be taken into consideration for the width calculation.
         */
        final int titleWidth = (int) this.mDescriptionPaint.measureText(itemTitle);

        maxWidth = Math.max(maxWidth, titleWidth);
        final int descWidth = Math.min(maxWidth, DESCRIPTION_MAXWIDTH);

        /* Calculate the bounds of the Description box that needs to be drawn. */
        final int descBoxLeft = mRect.left - descWidth / 2 - DESCRIPTION_BOX_PADDING
                + mRect.width() / 2;
        final int descBoxRight = descBoxLeft + descWidth + 2 * DESCRIPTION_BOX_PADDING;
        final int descBoxBottom = mRect.top;
        final int descBoxTop = descBoxBottom - DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT
                - (lines.length + 1) * DESCRIPTION_LINE_HEIGHT /* +1 because of the title. */
                - 2 * DESCRIPTION_BOX_PADDING;

        if (pProjection.getOrientation() != 0) {
            c.save();
            c.rotate(-pProjection.getOrientation(), mFocusedScreenCoords.x, mFocusedScreenCoords.y);
        }

        /* Twice draw a RoundRect, once in black with 1px as a small border. */
        this.mMarkerBackgroundPaint.setColor(Color.BLACK);
        c.drawRoundRect(new RectF(descBoxLeft - 1, descBoxTop - 1, descBoxRight + 1,
                        descBoxBottom + 1), DESCRIPTION_BOX_CORNERWIDTH, DESCRIPTION_BOX_CORNERWIDTH,
                this.mDescriptionPaint);
        this.mMarkerBackgroundPaint.setColor(this.mMarkerFocusedBackgroundColor);
        c.drawRoundRect(new RectF(descBoxLeft, descBoxTop, descBoxRight, descBoxBottom),
                DESCRIPTION_BOX_CORNERWIDTH, DESCRIPTION_BOX_CORNERWIDTH,
                this.mMarkerBackgroundPaint);

        final int descLeft = descBoxLeft + DESCRIPTION_BOX_PADDING;
        int descTextLineBottom = descBoxBottom - DESCRIPTION_BOX_PADDING;

        /* Draw all the lines of the description. */
        for (int j = lines.length - 1; j >= 0; j--) {
            c.drawText(lines[j].trim(), descLeft, descTextLineBottom, this.mDescriptionPaint);
            descTextLineBottom -= DESCRIPTION_LINE_HEIGHT;
        }
        /* Draw the title. */
        c.drawText(itemTitle, descLeft, descTextLineBottom - DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT,
                this.mTitlePaint);
        c.drawLine(descBoxLeft, descTextLineBottom, descBoxRight, descTextLineBottom,
                mDescriptionPaint);

        /*
         * Finally draw the marker base. This is done in the end to make it look better.
         */
        markerFocusedBase.setBounds(mRect);
        markerFocusedBase.draw(c);
        mRect.offset(-mFocusedScreenCoords.x, -mFocusedScreenCoords.y);
        markerFocusedBase.setBounds(mRect);

        if (pProjection.getOrientation() != 0) {
            c.restore();
        }
    }

    @Override
    public void onDetach(MapView mapView) {
        super.onDetach(mapView);
        this.mContext = null;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
