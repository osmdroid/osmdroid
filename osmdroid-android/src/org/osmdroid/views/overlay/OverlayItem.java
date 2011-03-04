// Created by plusminus on 00:02:58 - 03.10.2008
package org.osmdroid.views.overlay;

import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Immutable class describing a GeoPoint with a Title and a Description.
 * 
 * @author Nicolas Gramlich
 * @author Theodore Hong
 * @author Fred Eisele
 * 
 */
public class OverlayItem {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final int ITEM_STATE_FOCUSED_MASK = 4;
	public static final int ITEM_STATE_PRESSED_MASK = 1;
	public static final int ITEM_STATE_SELECTED_MASK = 2;

	protected static final Point DEFAULT_MARKER_SIZE = new Point(26, 94);

	public enum HotspotPlace {
		CUSTOM, // indicates the mMarker is set by the user
		CENTER, BOTTOM_CENTER, // default
		TOP_CENTER, RIGHT_CENTER, LEFT_CENTER, UPPER_RIGHT_CORNER, LOWER_RIGHT_CORNER, UPPER_LEFT_CORNER, LOWER_LEFT_CORNER
	}

	// ===========================================================
	// Fields
	// ===========================================================

	public final long mKey;
	public final String mTitle;
	public final String mDescription;
	public final GeoPoint mGeoPoint;
	protected Drawable mMarker;
	protected Point mMarkerHotspotPoint;
	protected HotspotPlace mMarkerHotspotPlace;
	private float mScale;

	// ===========================================================
	// Constructors
	// ===========================================================

	/**
	 * @param aTitle
	 *            this should be <b>singleLine</b> (no <code>'\n'</code> )
	 * @param aDescription
	 *            a <b>multiLine</b> description ( <code>'\n'</code> possible)
	 * @param aGeoPoint
	 */
	public OverlayItem(final String aTitle, final String aDescription, final GeoPoint aGeoPoint) {
		this(-1L, aTitle, aDescription, aGeoPoint, 1.0f);
	}

	public OverlayItem(final String aTitle, final String aDescription, final GeoPoint aGeoPoint,
			final float scale) {
		this(-1L, aTitle, aDescription, aGeoPoint, scale);
	}

	public OverlayItem(final long aKey, final String aTitle, final String aDescription,
			final GeoPoint aGeoPoint, final float scale) {
		this.mTitle = aTitle;
		this.mDescription = aDescription;
		this.mGeoPoint = aGeoPoint;
		this.mKey = aKey;
		this.mScale = scale;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public long getKey() {
		return mKey;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getSnippet() {
		return mDescription;
	}

	public GeoPoint getPoint() {
		return mGeoPoint;
	}

	/*
	 * (copied from Google API docs) Returns the marker that should be used when drawing this item
	 * on the map. A null value means that the default marker should be drawn. Different markers can
	 * be returned for different states. The different markers can have different bounds. The
	 * default behavior is to call {@link setState(android.graphics.drawable.Drawable, int)} on the
	 * overlay item's marker, if it exists, and then return it.
	 * 
	 * @param stateBitset The current state.
	 * 
	 * @return The marker for the current state, or null if the default marker for the overlay
	 * should be used.
	 */
	public Drawable getMarker(final int stateBitset) {
		// marker not specified
		if (mMarker == null) {
			return null;
		}

		// set marker state appropriately
		setState(mMarker, stateBitset);
		mMarkerHotspotPoint = null;
		return mMarker;
	}

	public void setMarker(final Drawable marker) {
		this.mMarker = marker;
		mMarkerHotspotPoint = null;
	}

	/*
	 * public Point getMarkerHotspotPoint(final int stateBitset) { return (mMarkerHotspotPoint ==
	 * null) ? null : mMarkerHotspotPoint; }
	 */

	public void setMarkerHotspotPoint(final Point mMarkerHotspot) {
		this.mMarkerHotspotPoint = mMarkerHotspot;
		this.mMarkerHotspotPlace = HotspotPlace.CUSTOM;
	}

	public HotspotPlace getMarkerHotspotPlace(final int stateBitset) {
		return mMarkerHotspotPlace;
	}

	public Point setMarkerHotspotPlace(final HotspotPlace place) {
		mMarkerHotspotPlace = place;
		mMarkerHotspotPoint = null;
		return this.mMarkerHotspotPoint;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	/*
	 * (copied from the Google API docs) Sets the state of a drawable to match a given state bitset.
	 * This is done by converting the state bitset bits into a state set of R.attr.state_pressed,
	 * R.attr.state_selected and R.attr.state_focused attributes, and then calling {@link
	 * Drawable.setState(int[])}.
	 */
	public static void setState(final Drawable drawable, final int stateBitset) {
		final int[] stateArray = new int[] { -stateBitset & ITEM_STATE_PRESSED_MASK,
				stateBitset & ITEM_STATE_SELECTED_MASK, stateBitset & ITEM_STATE_FOCUSED_MASK, };
		drawable.setState(stateArray);
	}

	/**
	 * 
	 * @param canvas
	 *            what the item is drawn upon
	 * @param index
	 *            which item is to be drawn
	 * @param curScreenCoords
	 */
	protected void onDrawItem(final Canvas canvas, final Point curScreenCoords) {
		final Rect rect = new Rect();
		if (mMarker != null) {
			getItemBoundingRectangle(rect, curScreenCoords);
			// draw it
			mMarker.setBounds(rect);
			mMarker.draw(canvas);
		}
	}

	/**
	 * Finds the bounding rectangle for the object in current projection.
	 * 
	 * @param item
	 * @param rect
	 * @return
	 */
	public Rect getItemBoundingRectangle(final Rect rect, final Point ctr) {

		final Drawable marker = mMarker;
		final Point markerHotspot = getMarkerHotspot();

		// Scale the markerHotspot
		// markerHotspot.set((int) (markerHotspot.x * mScale), (int) (markerHotspot.y * mScale));

		// calculate bounding rectangle
		final int markerWidth = (int) (marker.getIntrinsicWidth() * mScale);
		final int markerHeight = (int) (marker.getIntrinsicHeight() * mScale);
		final int left = ctr.x - markerHotspot.x;
		final int right = left + markerWidth;
		final int top = ctr.y - markerHotspot.y;
		final int bottom = top + markerHeight;

		rect.set(left, top, right, bottom);
		return rect;
	}

	/**
	 * This is a factory method. The interaction between the hot-spot-place and the hot-spot is
	 * somewhat ambiguous. Generally either one or the other will be specified and the other will be
	 * set appropriately. The ambiguity arises in the case where they are both specified.
	 * 
	 * @param pMarker
	 * @param pHotspot
	 * @param pHotspotPlace
	 * @param pResourceProxy
	 * @return a map item with all unspecified values set to reasonable defaults.
	 */
	public static OverlayItem getDefaultItem(final Drawable pMarker, final Point pHotspot,
			final HotspotPlace pHotspotPlace, final ResourceProxy pResourceProxy) {
		final OverlayItem that = new OverlayItem("<default>", "used when no marker is specified",
				new GeoPoint(0.0, 0.0));
		that.mMarker = (pMarker != null) ? pMarker : pResourceProxy
				.getDrawable(ResourceProxy.bitmap.marker_default);

		if (pHotspot == null) {
			if (pHotspotPlace == null) {
				// that.deriveHotspot();
				return that;
			} else {
				that.mMarkerHotspotPlace = pHotspotPlace;
				// that.deriveHotspot();
				return that;
			}
		} else {
			if (pHotspotPlace == null) {
				that.mMarkerHotspotPoint = pHotspot;
				that.mMarkerHotspotPlace = HotspotPlace.CUSTOM;
				return that;
			} else {
				that.mMarkerHotspotPlace = pHotspotPlace;
				return that;
			}
		}
	}

	public Drawable getDrawable() {
		return this.mMarker;
	}

	public int getWidth() {
		return this.mMarker.getIntrinsicWidth();
	}

	public int getHeight() {
		return this.mMarker.getIntrinsicHeight();
	}

	/**
	 * Select one of several standard positions for the hot spot.
	 */
	protected Point getMarkerHotspot() {
		if (this.mMarkerHotspotPoint == null) {
			Drawable marker = mMarker;
			final Point markerSize = (marker == null) ? DEFAULT_MARKER_SIZE : new Point(
					this.getWidth(), this.getHeight());

			switch (this.mMarkerHotspotPlace) {
			case CUSTOM:
				// This shouldn't happen - but we'll just default to 0,0
				this.mMarkerHotspotPoint = new Point(0, 0);
				break;
			case CENTER:
				this.mMarkerHotspotPoint = new Point(markerSize.x / 2, markerSize.y / 2);
				break;
			case BOTTOM_CENTER:
				this.mMarkerHotspotPoint = new Point(markerSize.x / 2, markerSize.y);
				break;
			case TOP_CENTER:
				this.mMarkerHotspotPoint = new Point(markerSize.x / 2, 0);
				break;
			case RIGHT_CENTER:
				this.mMarkerHotspotPoint = new Point(markerSize.x, markerSize.y / 2);
				break;
			case LEFT_CENTER:
				this.mMarkerHotspotPoint = new Point(0, markerSize.y / 2);
				break;
			case UPPER_RIGHT_CORNER:
				this.mMarkerHotspotPoint = new Point(markerSize.x, 0);
				break;
			case LOWER_RIGHT_CORNER:
				this.mMarkerHotspotPoint = new Point(markerSize.x, markerSize.y);
				break;
			case UPPER_LEFT_CORNER:
				this.mMarkerHotspotPoint = new Point(0, 0);
				break;
			case LOWER_LEFT_CORNER:
				this.mMarkerHotspotPoint = new Point(0, markerSize.y);
				break;
			}
			mMarkerHotspotPoint.set((int) (mMarkerHotspotPoint.x * mScale),
					(int) (mMarkerHotspotPoint.y * mScale));
		}
		return this.mMarkerHotspotPoint;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
