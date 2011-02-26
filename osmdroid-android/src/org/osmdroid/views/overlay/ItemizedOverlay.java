// Created by plusminus on 23:18:23 - 02.10.2008
package org.osmdroid.views.overlay;

import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Draws a list of {@link OverlayItem} as markers to a map. The item with the lowest index is drawn
 * as last and therefore the 'topmost' marker. It also gets checked for onTap first. This class is
 * generic, because you then you get your custom item-class passed back in onTap().
 *
 * @author Nicolas Gramlich
 * @author Theodore Hong
 * @author Fred Eisele
 *
 * @param <T>
 */
public class ItemizedOverlay<T extends OverlayItem> extends Overlay {

	// ===========================================================
	// Constants
	// ===========================================================
	static final boolean DEBUG_GRAPHICS = false;

	// ===========================================================
	// Fields
	// ===========================================================

	protected OnItemGestureListener<T> mOnItemGestureListener;
	protected GestureDetector mGestureDetector;
	protected final List<T> mItemList;
	protected final OverlayItem mDefaultItem;
	private final float mScale;
	private int mDrawnItemsLimit = Integer.MAX_VALUE;

	// ===========================================================
	// Constructors
	// ===========================================================

	public ItemizedOverlay(final Context ctx, final List<T> aList,
			final OnItemGestureListener<T> aOnItemGestureListener) {
		this(ctx, aList, aOnItemGestureListener, new DefaultResourceProxyImpl(ctx));
	}

	public ItemizedOverlay(final Context ctx, final List<T> aList,
			final OnItemGestureListener<T> aOnItemGestureListener,
			final ResourceProxy pResourceProxy) {
		this(ctx, aList, null, null, null, aOnItemGestureListener, pResourceProxy);
	}

	public ItemizedOverlay(final Context ctx, final List<T> aList, final Drawable pMarker,
			final Point pMarkerHotspot, final OnItemGestureListener<T> aOnItemGestureListener,
			final ResourceProxy pResourceProxy) {
		this(ctx, aList, pMarker, pMarkerHotspot, null, aOnItemGestureListener, pResourceProxy);
	}

	public ItemizedOverlay(final Context ctx, final List<T> aList, final Drawable pMarker,
			final Point pMarkerHotspot, final OverlayItem.HotspotPlace pHotSpotPlace,
			final OnItemGestureListener<T> aOnItemGestureListener,
			final ResourceProxy pResourceProxy) {

		super(pResourceProxy);

		assert (ctx != null);
		assert (aList != null);

		mScale = ctx.getResources().getDisplayMetrics().density;

		this.mDefaultItem = OverlayItem.getDefaultItem(pMarker, pMarkerHotspot, pHotSpotPlace,
				pResourceProxy);

		this.mOnItemGestureListener = aOnItemGestureListener;

		// Add one sample item.
		this.mItemList = aList;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public int getDrawnItemsLimit() {
		return this.mDrawnItemsLimit;
	}

	public void setDrawnItemsLimit(final int aLimit) {
		this.mDrawnItemsLimit = aLimit;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces (and supporting methods)
	// ===========================================================

	private static final Paint bullseyePaint = new Paint();
	static {
		bullseyePaint.setStyle(Paint.Style.STROKE);
		bullseyePaint.setColor(Color.RED);
	}

	@Override
	public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {

		if (shadow) {
			return;
		}

		final Projection pj = mapView.getProjection();
		final Point curScreenCoords = new Point();
		int limit = this.mItemList.size() - 1;
		if (limit > this.mDrawnItemsLimit) {
			limit = this.mDrawnItemsLimit;
		}

		/* Draw in backward cycle, so the items with the least index are on the front. */
		for (int i = limit; i >= 0; i--) {
			final T item = this.mItemList.get(i);
			pj.toMapPixels(item.mGeoPoint, curScreenCoords);

			onDrawItem(canvas, i, curScreenCoords);
		}
		// indicate the place touched with a bullseye
		if (DEBUG_GRAPHICS) {
			if (touchPoint != null) {
				canvas.drawCircle(touchPoint.x, touchPoint.y, 20, bullseyePaint);
			}
		}
	}

	/**
	 * Each of these methods performs a item sensitive check. If the item is located its
	 * corresponding method is called. The result of the call is returned.
	 *
	 * Helper methods are provided so that child classes may more easily override behavior without
	 * resorting to overriding the ItemGestureListener methods.
	 */
	@Override
	public boolean onSingleTapUp(final MotionEvent event, final MapView mapView) {
		return (activateSelectedItems(event, mapView, new ActiveItem() {
			@Override
			public boolean run(final int index) {
				final ItemizedOverlay<T> that = ItemizedOverlay.this;
				if (that.mOnItemGestureListener == null) {
					return false;
				}
				return onSingleTapUpHelper(index, that.mItemList.get(index), mapView);
			}
		})) ? true : super.onSingleTapUp(event, mapView);
	}

	protected boolean onSingleTapUpHelper(final int index, final T item, final MapView mapView) {
		return this.mOnItemGestureListener.onItemSingleTapUp(index, item);
	}

	@Override
	public boolean onLongPress(final MotionEvent event, final MapView mapView) {
		return (activateSelectedItems(event, mapView, new ActiveItem() {
			@Override
			public boolean run(final int index) {
				final ItemizedOverlay<T> that = ItemizedOverlay.this;
				if (that.mOnItemGestureListener == null) {
					return false;
				}
				return onLongPressHelper(index, that.mItemList.get(index));
			}
		})) ? true : super.onLongPress(event, mapView);
	}

	protected boolean onLongPressHelper(final int index, final T item) {
		return this.mOnItemGestureListener.onItemLongPress(index, item);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private static final Paint boudaryPaint = new Paint();
	static {
		boudaryPaint.setStyle(Paint.Style.STROKE);
		boudaryPaint.setColor(Color.BLUE);
	}

	/**
	 *
	 * @param canvas
	 *            what the item is drawn upon
	 * @param index
	 *            which item is to be drawn
	 * @param curScreenCoords
	 */
	protected void onDrawItem(final Canvas canvas, final int index, final Point curScreenCoords) {
		// get this item's preferred marker & hotspot
		final T item = this.mItemList.get(index);

		final Drawable marker = (item.getMarker(0) == null) ? this.mDefaultItem.getMarker(0) : item
				.getMarker(0);

		final Rect rect = new Rect();
		getItemBoundingRetangle(item, rect, curScreenCoords);
		// draw it
		marker.setBounds(rect);
		marker.draw(canvas);
		// the following lines place objects on the screen indicating the item boundary.
		if (DEBUG_GRAPHICS) {
			if (touchPoint != null) {
				canvas.drawRect(rect, boudaryPaint);
			}
		}
	}

	/**
	 * When a content sensitive action is performed the content item needs to be identified. This
	 * method does that and then performs the assigned task on that item.
	 *
	 * @param event
	 * @param mapView
	 * @param task
	 * @return true if event is handled false otherwise
	 */
	private boolean activateSelectedItems(final MotionEvent event, final MapView mapView,
			final ActiveItem task) {
		final Projection pj = mapView.getProjection();
		final int eventX = (int) event.getX();
		final int eventY = (int) event.getY();

		/* These objects are created to avoid construct new ones every cycle. */
		final Point touchScreenCoords = new Point();
		pj.fromMapPixels(eventX, eventY, touchScreenCoords);

		touchPoint = touchScreenCoords;

		final Rect markerScreenBounds = new Rect();
		final Point curScreenCoords = new Point();
		for (int i = 0; i < this.mItemList.size(); ++i) {
			final T item = this.mItemList.get(i);
			pj.toMapPixels(item.mGeoPoint, curScreenCoords);

			getItemBoundingRetangle(item, markerScreenBounds, curScreenCoords);

			if (!markerScreenBounds.contains(touchScreenCoords.x, touchScreenCoords.y)) {
				continue;
			}
			if (task.run(i)) {
				return true;
			}
		}
		return false;
	}

	private Point touchPoint = null;

	/**
	 * Finds the bounding rectangle for the object in current projection.
	 *
	 * @param item
	 * @param rect
	 * @return
	 */
	private Rect getItemBoundingRetangle(final T item, final Rect rect, final Point ctr) {
		final Drawable marker = (item.getMarker(0) == null) ? this.mDefaultItem.getMarker(0) : item
				.getMarker(0);
		final Point markerHotspot = (item.getMarkerHotspot(0) == null) ? this.mDefaultItem
				.getMarkerHotspot(0) : item.getMarkerHotspot(0);

		// Scale the markerHotspot
		markerHotspot.set((int) (markerHotspot.x * mScale), (int) (markerHotspot.y * mScale));

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

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	/**
	 * When the item is touched one of these methods may be invoked depending on the type of touch.
	 *
	 * Each of them returns true if the event was completely handled.
	 */
	public static interface OnItemGestureListener<T> {
		public boolean onItemSingleTapUp(final int index, final T item);

		public boolean onItemLongPress(final int index, final T item);
	}

	public static interface ActiveItem {
		public boolean run(final int aIndex);
	}

}
