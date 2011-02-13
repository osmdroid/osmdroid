// Created by plusminus on 20:50:06 - 03.10.2008
package org.osmdroid.views.overlay;

import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class ItemizedOverlayWithFocus<T extends OverlayItem> extends ItemizedOverlay<T> {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final int DESCRIPTION_BOX_PADDING = 3;
	public static final int DESCRIPTION_BOX_CORNERWIDTH = 3;

	public static final int DESCRIPTION_LINE_HEIGHT = 12;
	/** Additional to <code>DESCRIPTION_LINE_HEIGHT</code>. */
	public static final int DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT = 2;

	// protected static final Point DEFAULTMARKER_FOCUSED_HOTSPOT = new Point(10, 19);
	protected static final int DEFAULTMARKER_BACKGROUNDCOLOR = Color.rgb(101, 185, 74);

	protected static final int DESCRIPTION_MAXWIDTH = 200;

	// ===========================================================
	// Fields
	// ===========================================================

	protected final Point mMarkerFocusedHotSpot;
	protected final int mMarkerFocusedBackgroundColor;
	protected final Paint mMarkerBackgroundPaint, mDescriptionPaint, mTitlePaint;

	protected Drawable mMarkerFocusedBase;
	protected int mFocusedItemIndex;
	protected boolean mFocusItemsOnTap;
	private final Point mFocusedScreenCoords = new Point();

	private final String UNKNOWN;
	private final float mScale;

	// ===========================================================
	// Constructors
	// ===========================================================

	public ItemizedOverlayWithFocus(final Context ctx, final List<T> aList,
			final OnItemGestureListener<T> aOnItemTapListener) {
		this(ctx, aList, aOnItemTapListener, new DefaultResourceProxyImpl(ctx));
	}

	public ItemizedOverlayWithFocus(final Context ctx, final List<T> aList,
			final OnItemGestureListener<T> aOnItemTapListener, final ResourceProxy pResourceProxy) {
		this(ctx, aList, null, null, null, null, NOT_SET, aOnItemTapListener, pResourceProxy);
	}

	public ItemizedOverlayWithFocus(final Context ctx, final List<T> aList, final Drawable pMarker,
			final Point pMarkerHotspot, final Drawable pMarkerFocusedBase,
			final Point pMarkerFocusedHotSpot, final int pFocusedBackgroundColor,
			final OnItemGestureListener<T> aOnItemTapListener, final ResourceProxy pResourceProxy) {

		super(ctx, aList, pMarker, pMarkerHotspot, aOnItemTapListener, pResourceProxy);

		mScale = ctx.getResources().getDisplayMetrics().density;

		UNKNOWN = mResourceProxy.getString(ResourceProxy.string.unknown);

		this.mMarkerFocusedBase = (pMarkerFocusedBase != null) ? pMarkerFocusedBase
				: mResourceProxy.getDrawable(ResourceProxy.bitmap.marker_default_focused_base);

		this.mMarkerFocusedHotSpot = (pMarkerFocusedHotSpot != null) ? pMarkerFocusedHotSpot
				: new Point(mMarkerFocusedBase.getIntrinsicWidth() / 2,
						mMarkerFocusedBase.getIntrinsicHeight());

		this.mMarkerFocusedBackgroundColor = (pFocusedBackgroundColor != NOT_SET) ? pFocusedBackgroundColor
				: DEFAULTMARKER_BACKGROUNDCOLOR;

		this.mMarkerBackgroundPaint = new Paint(); // Color is set in onDraw(...)

		this.mDescriptionPaint = new Paint();
		this.mDescriptionPaint.setAntiAlias(true);
		this.mTitlePaint = new Paint();
		this.mTitlePaint.setFakeBoldText(true);
		this.mTitlePaint.setAntiAlias(true);
		this.unSetFocusedItem();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public T getFocusedItem() {
		if (this.mFocusedItemIndex == NOT_SET)
			return null;
		return this.mItemList.get(this.mFocusedItemIndex);
	}

	public void setFocusedItem(final int pIndex) {
		this.mFocusedItemIndex = pIndex;
	}

	public void unSetFocusedItem() {
		this.mFocusedItemIndex = NOT_SET;
	}

	public void setFocusedItem(final T pItem) {
		final int indexFound = super.mItemList.indexOf(pItem);
		if (indexFound < 0)
			throw new IllegalArgumentException();

		this.setFocusedItem(indexFound);
	}

	public void setFocusItemsOnTap(final boolean doit) {
		this.mFocusItemsOnTap = doit;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected boolean onSingleTapUpHelper(final int index, final T item, final MapView mapView) {
		if (this.mFocusItemsOnTap) {
			this.mFocusedItemIndex = index;
			mapView.postInvalidate();
		}
		return this.mOnItemGestureListener.onItemSingleTapUp(index, item);
	}

	/**
	 * This is called after onDraw. It is intended to draw the top items, in this case the item of
	 * focus.
	 */
	@Override
	protected void onDrawFinished(final Canvas c, final MapView osmv) {
		if (this.mFocusedItemIndex == NOT_SET)
			return;

		// get focused item's preferred marker & hotspot
		final T focusedItem = super.mItemList.get(this.mFocusedItemIndex);
		Drawable markerFocusedBase = focusedItem.getMarker(OverlayItem.ITEM_STATE_FOCUSED_MASK);
		Point markerFocusedHotspot = focusedItem
				.getMarkerHotspot(OverlayItem.ITEM_STATE_FOCUSED_MASK);
		if (markerFocusedBase == null) {
			markerFocusedBase = this.mMarkerFocusedBase;
		}
		if (markerFocusedHotspot == null) {
			markerFocusedHotspot = this.mMarkerFocusedHotSpot;
		}

		/* Calculate and set the bounds of the marker. */
		final int markerFocusedWidth = (int) (markerFocusedBase.getIntrinsicWidth() * mScale);
		final int markerFocusedHeight = (int) (markerFocusedBase.getIntrinsicHeight() * mScale);
		final int left = this.mFocusedScreenCoords.x - (int) (markerFocusedHotspot.x * mScale);
		final int right = left + markerFocusedWidth;
		final int top = this.mFocusedScreenCoords.y - (int) (markerFocusedHotspot.y * mScale);
		final int bottom = top + markerFocusedHeight;
		markerFocusedBase.setBounds(left, top, right, bottom);

		/* Strings of the OverlayItem, we need. */
		final String itemTitle = (focusedItem.mTitle == null) ? UNKNOWN : focusedItem.mTitle;
		final String itemDescription = (focusedItem.mDescription == null) ? UNKNOWN
				: focusedItem.mDescription;

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
			if (!Character.isLetter(itemDescription.charAt(i)))
				lastwhitespace = i;

			final float charwidth = widths[i];

			if (curLineWidth + charwidth > DESCRIPTION_MAXWIDTH) {
				if (lastStop == lastwhitespace)
					i--;
				else
					i = lastwhitespace;

				sb.append(itemDescription.subSequence(lastStop, i));
				sb.append('\n');

				lastStop = i;
				maxWidth = Math.max(maxWidth, curLineWidth);
				curLineWidth = 0;
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
		final int descBoxLeft = left - descWidth / 2 - DESCRIPTION_BOX_PADDING + markerFocusedWidth
				/ 2;
		final int descBoxRight = descBoxLeft + descWidth + 2 * DESCRIPTION_BOX_PADDING;
		final int descBoxBottom = top;
		final int descBoxTop = descBoxBottom - DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT
				- (lines.length + 1) * DESCRIPTION_LINE_HEIGHT /* +1 because of the title. */
				- 2 * DESCRIPTION_BOX_PADDING;

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
		markerFocusedBase.draw(c);
	}

	/**
	 * Actual drawing of focus item will take place in onDrawFinished.
	 */
	@Override
	protected void onDrawItem(final Canvas c, final int index, final Point screenCoords) {
		if (this.mFocusedItemIndex != NOT_SET && index == this.mFocusedItemIndex) {
			// Because we are reusing the screencoords passed here, we cannot simply store the
			// reference.
			this.mFocusedScreenCoords.set(screenCoords.x, screenCoords.y);
		} else {
			super.onDrawItem(c, index, screenCoords);
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
