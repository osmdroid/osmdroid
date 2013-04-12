// Created by plusminus on 23:18:23 - 02.10.2008
package org.osmdroid.views.overlay;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;
import org.osmdroid.views.safecanvas.ISafeCanvas;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

/**
 * Draws a list of {@link OverlayItem} as markers to a map. The item with the lowest index is drawn
 * as last and therefore the 'topmost' marker. It also gets checked for onTap first. This class is
 * generic, because you then you get your custom item-class passed back in onTap().
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 * @author Theodore Hong
 * @author Fred Eisele
 *
 * @param <Item>
 */
public abstract class ItemizedOverlay<Item extends OverlayItem> extends SafeDrawOverlay implements
		Overlay.Snappable {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final Drawable mDefaultMarker;
	private final ArrayList<Item> mInternalItemList;
	private final Rect mRect = new Rect();
	private final Point mCurScreenCoords = new Point();
	protected boolean mDrawFocusedItem = true;
	private Item mFocusedItem;
	private boolean mPendingFocusChangedEvent = false;
	private OnFocusChangeListener mOnFocusChangeListener;

	// ===========================================================
	// Abstract methods
	// ===========================================================

	/**
	 * Method by which subclasses create the actual Items. This will only be called from populate()
	 * we'll cache them for later use.
	 */
	protected abstract Item createItem(int i);

	/**
	 * The number of items in this overlay.
	 */
	public abstract int size();

	// ===========================================================
	// Constructors
	// ===========================================================

	public ItemizedOverlay(final Drawable pDefaultMarker, final ResourceProxy pResourceProxy) {

		super(pResourceProxy);

		if (pDefaultMarker == null) {
			throw new IllegalArgumentException("You must pass a default marker to ItemizedOverlay.");
		}

		this.mDefaultMarker = pDefaultMarker;

		mInternalItemList = new ArrayList<Item>();
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces (and supporting methods)
	// ===========================================================

	/**
	 * Draw a marker on each of our items. populate() must have been called first.<br/>
	 * <br/>
	 * The marker will be drawn twice for each Item in the Overlay--once in the shadow phase, skewed
	 * and darkened, then again in the non-shadow phase. The bottom-center of the marker will be
	 * aligned with the geographical coordinates of the Item.<br/>
	 * <br/>
	 * The order of drawing may be changed by overriding the getIndexToDraw(int) method. An item may
	 * provide an alternate marker via its OverlayItem.getMarker(int) method. If that method returns
	 * null, the default marker is used.<br/>
	 * <br/>
	 * The focused item is always drawn last, which puts it visually on top of the other items.<br/>
	 *
	 * @param canvas
	 *            the Canvas upon which to draw. Note that this may already have a transformation
	 *            applied, so be sure to leave it the way you found it
	 * @param mapView
	 *            the MapView that requested the draw. Use MapView.getProjection() to convert
	 *            between on-screen pixels and latitude/longitude pairs
	 * @param shadow
	 *            if true, draw the shadow layer. If false, draw the overlay contents.
	 */
	@Override
	protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow) {

		if (shadow) {
			return;
		}
		
		if (!mPendingFocusChangedEvent && mOnFocusChangeListener != null)
			mOnFocusChangeListener.onFocusChanged(this, mFocusedItem);
		mPendingFocusChangedEvent = false;

		final Projection pj = mapView.getProjection();
		final int size = this.mInternalItemList.size() - 1;

		/* Draw in backward cycle, so the items with the least index are on the front. */
		for (int i = size; i >= 0; i--) {
			final Item item = getItem(i);
			pj.toMapPixels(item.mGeoPoint, mCurScreenCoords);

			onDrawItem(canvas.getSafeCanvas(), item, mCurScreenCoords);
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Utility method to perform all processing on a new ItemizedOverlay. Subclasses provide Items
	 * through the createItem(int) method. The subclass should call this as soon as it has data,
	 * before anything else gets called.
	 */
	protected final void populate() {
		final int size = size();
		mInternalItemList.clear();
		mInternalItemList.ensureCapacity(size);
		for (int a = 0; a < size; a++) {
			mInternalItemList.add(createItem(a));
		}
	}

	/**
	 * Returns the Item at the given index.
	 *
	 * @param position
	 *            the position of the item to return
	 * @return the Item of the given index.
	 */
	public final Item getItem(final int position) {
		return mInternalItemList.get(position);
	}

	/**
	 * Draws an item located at the provided screen coordinates to the canvas.
	 *
	 * @param canvas
	 *            what the item is drawn upon
	 * @param item
	 *            the item to be drawn
	 * @param curScreenCoords
	 *            the screen coordinates of the item
	 */
	protected void onDrawItem(final Canvas canvas, final Item item, final Point curScreenCoords) {
		final int state = (mDrawFocusedItem && (mFocusedItem == item) ? OverlayItem.ITEM_STATE_FOCUSED_MASK
				: 0);
		final Drawable marker = (item.getMarker(state) == null) ? getDefaultMarker(state) : item
				.getMarker(state);
		final HotspotPlace hotspot = item.getMarkerHotspot();

		boundToHotspot(marker, hotspot);

		// draw it
		Overlay.drawAt(canvas, marker, curScreenCoords.x, curScreenCoords.y, false);
	}

	private Drawable getDefaultMarker(final int state) {
		OverlayItem.setState(mDefaultMarker, state);
		return mDefaultMarker;
	}

	/**
	 * See if a given hit point is within the bounds of an item's marker. Override to modify the way
	 * an item is hit tested. The hit point is relative to the marker's bounds. The default
	 * implementation just checks to see if the hit point is within the touchable bounds of the
	 * marker.
	 *
	 * @param item
	 *            the item to hit test
	 * @param marker
	 *            the item's marker
	 * @param hitX
	 *            x coordinate of point to check
	 * @param hitY
	 *            y coordinate of point to check
	 * @return true if the hit point is within the marker
	 */
	protected boolean hitTest(final Item item, final android.graphics.drawable.Drawable marker, final int hitX,
			final int hitY) {
		return marker.getBounds().contains(hitX, hitY);
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
		final Projection pj = mapView.getProjection();
		final Rect screenRect = pj.getIntrinsicScreenRect();
		final int size = this.size() - 1;

		/* Draw in backward cycle, so the items with the least index are on the front. */
		for (int i = 0; i < size; i++) {
			final Item item = getItem(i);
			pj.toMapPixels(item.mGeoPoint, mCurScreenCoords);

			final int state = (mDrawFocusedItem && (mFocusedItem == item) ? OverlayItem.ITEM_STATE_FOCUSED_MASK
					: 0);
			final Drawable marker = (item.getMarker(state) == null) ? getDefaultMarker(state)
					: item.getMarker(state);
			boundToHotspot(marker, item.getMarkerHotspot());
			if (hitTest(item, marker, mCurScreenCoords.x - screenRect.left - (int) e.getX(),
					mCurScreenCoords.y - screenRect.top - (int) e.getY())) {
				// We have a hit, do we get a response from onTap?
				if (onTap(i)) {
					// We got a response so consume the event
					return true;
				}
			}
		}

		return super.onSingleTapConfirmed(e, mapView);
	}

	/**
	 * Override this method to handle a "tap" on an item. This could be from a touchscreen tap on an
	 * onscreen Item, or from a trackball click on a centered, selected Item. By default, does
	 * nothing and returns false.
	 * 
	 * @return true if you handled the tap, false if you want the event that generated it to pass to
	 *         other overlays.
	 */
	protected boolean onTap(int index) {
		return false;
	}

	/**
	 * Set whether or not to draw the focused item. The default is to draw it, but some clients may
	 * prefer to draw the focused item themselves.
	 */
	public void setDrawFocusedItem(final boolean drawFocusedItem) {
		mDrawFocusedItem = drawFocusedItem;
	}

	/**
	 * If the given Item is found in the overlay, force it to be the current focus-bearer. Any
	 * registered {@link ItemizedOverlay#OnFocusChangeListener} will be notified. This does not move
	 * the map, so if the Item isn't already centered, the user may get confused. If the Item is not
	 * found, this is a no-op. You can also pass null to remove focus.
	 */
	public void setFocus(final Item item) {
		mPendingFocusChangedEvent = item != mFocusedItem;
		mFocusedItem = item;
	}

	/**
	 *
	 * @return the currently-focused item, or null if no item is currently focused.
	 */
	public Item getFocus() {
		return mFocusedItem;
	}

	/**
	 * Adjusts a drawable's bounds so that (0,0) is a pixel in the location described by the hotspot
	 * parameter. Useful for "pin"-like graphics. For convenience, returns the same drawable that
	 * was passed in.
	 *
	 * @param marker
	 *            the drawable to adjust
	 * @param hotspot
	 *            the hotspot for the drawable
	 * @return the same drawable that was passed in.
	 */
	protected synchronized Drawable boundToHotspot(final Drawable marker, HotspotPlace hotspot) {
		final int markerWidth = marker.getIntrinsicWidth();
		final int markerHeight = marker.getIntrinsicHeight();

		mRect.set(0, 0, 0 + markerWidth, 0 + markerHeight);

		if (hotspot == null) {
			hotspot = HotspotPlace.BOTTOM_CENTER;
		}

		switch (hotspot) {
		default:
		case NONE:
			break;
		case CENTER:
			mRect.offset(-markerWidth / 2, -markerHeight / 2);
			break;
		case BOTTOM_CENTER:
			mRect.offset(-markerWidth / 2, -markerHeight);
			break;
		case TOP_CENTER:
			mRect.offset(-markerWidth / 2, 0);
			break;
		case RIGHT_CENTER:
			mRect.offset(-markerWidth, -markerHeight / 2);
			break;
		case LEFT_CENTER:
			mRect.offset(0, -markerHeight / 2);
			break;
		case UPPER_RIGHT_CORNER:
			mRect.offset(-markerWidth, 0);
			break;
		case LOWER_RIGHT_CORNER:
			mRect.offset(-markerWidth, -markerHeight);
			break;
		case UPPER_LEFT_CORNER:
			mRect.offset(0, 0);
			break;
		case LOWER_LEFT_CORNER:
			mRect.offset(0, -markerHeight);
			break;
		}
		marker.setBounds(mRect);
		return marker;
	}

	public void setOnFocusChangeListener(OnFocusChangeListener l) {
		mOnFocusChangeListener = l;
	}

	public static interface OnFocusChangeListener {
		void onFocusChanged(ItemizedOverlay<?> overlay, OverlayItem newFocus);
	}
}
