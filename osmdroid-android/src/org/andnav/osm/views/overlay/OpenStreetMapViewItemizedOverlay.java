// Created by plusminus on 23:18:23 - 02.10.2008
package org.andnav.osm.views.overlay;

import java.util.List;

import org.andnav.osm.DefaultResourceProxyImpl;
import org.andnav.osm.ResourceProxy;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

/**
 * Draws a list of {@link OpenStreetMapViewOverlayItem} as markers to a map.
 * The item with the lowest index is drawn as last and therefore the 'topmost' marker. It also gets checked for onTap first.
 * This class is generic, because you then you get your custom item-class passed back in onTap().
 * @author Nicolas Gramlich
 *
 * @param <T>
 */
public class OpenStreetMapViewItemizedOverlay<T extends OpenStreetMapViewOverlayItem> extends OpenStreetMapViewOverlay {
	
	// ===========================================================
	// Constants
	// ===========================================================

	protected static final Point DEFAULTMARKER_HOTSPOT = new Point(13, 47);

	// ===========================================================
	// Fields
	// ===========================================================

	protected OnItemTapListener<T> mOnItemTapListener;
	protected final List<T> mItemList;
	protected final Point mMarkerHotSpot;
	protected final Drawable mMarker;
	protected final int mMarkerWidth, mMarkerHeight;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapViewItemizedOverlay(
			final Context ctx, 
			final List<T> aList, 
			final OnItemTapListener<T> aOnItemTapListener) {
        this(ctx, aList, aOnItemTapListener, new DefaultResourceProxyImpl(ctx));
	}

	public OpenStreetMapViewItemizedOverlay(
			final Context ctx, 
			final List<T> aList, 
			final OnItemTapListener<T> aOnItemTapListener, 
			final ResourceProxy pResourceProxy) {
        this(ctx, aList, null, null, aOnItemTapListener, pResourceProxy);
	}

	public OpenStreetMapViewItemizedOverlay(
			final Context ctx, 
			final List<T> aList, 
			final Drawable pMarker, 
			final Point pMarkerHotspot, 
			final OnItemTapListener<T> aOnItemTapListener, 
			final ResourceProxy pResourceProxy) {
		
		super(pResourceProxy);
		
		assert(ctx != null);
		assert(aList != null);

		this.mMarker = (pMarker != null) ? pMarker : mResourceProxy.getDrawable(ResourceProxy.drawable.marker_default);

		this.mMarkerHotSpot = (pMarkerHotspot != null) ? pMarkerHotspot : DEFAULTMARKER_HOTSPOT;

        this.mOnItemTapListener = aOnItemTapListener;

		this.mMarkerWidth = this.mMarker.getIntrinsicWidth();
		this.mMarkerHeight = this.mMarker.getIntrinsicHeight();

		// Add one sample item.
		this.mItemList = aList;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {
		return;
	}

	@Override
	public void onDraw(final Canvas c, final OpenStreetMapView mapView) {
		final OpenStreetMapViewProjection pj = mapView.getProjection();

		final Point curScreenCoords = new Point();

		/* Draw in backward cycle, so the items with the least index are on the front. */
		for(int i = this.mItemList.size() - 1; i >= 0; i--){
			T item = this.mItemList.get(i);
			pj.toMapPixels(item.mGeoPoint, curScreenCoords);

			onDrawItem(c, i, curScreenCoords);
		}
	}

	protected void onDrawItem(final Canvas c, final int index, final Point curScreenCoords) {
		final int left = curScreenCoords.x - this.mMarkerHotSpot.x;
		final int right = left + this.mMarkerWidth;
		final int top = curScreenCoords.y - this.mMarkerHotSpot.y;
		final int bottom = top + this.mMarkerHeight;

		this.mMarker.setBounds(left, top, right, bottom);

		this.mMarker.draw(c);
	}

	@Override
	public boolean onSingleTapUp(final MotionEvent event, final OpenStreetMapView mapView) {
		final OpenStreetMapViewProjection pj = mapView.getProjection();
		final int eventX = (int) event.getX();
		final int eventY = (int) event.getY();
		final int markerWidth = this.mMarker.getIntrinsicWidth();
		final int markerHeight = this.mMarker.getIntrinsicHeight();

		/* These objects are created to avoid construct new ones every cycle. */
		final Rect curMarkerBounds = new Rect();
		final Point curScreenCoords = new Point();
		final Point curScreenCoords2 = new Point();

		for(int i = 0; i < this.mItemList.size(); i++){
			final T item = this.mItemList.get(i);

			pj.toMapPixels(item.mGeoPoint, curScreenCoords);

			final int left = (curScreenCoords.x - this.mMarkerHotSpot.x);
			final int right = left + markerWidth;
			final int top = (curScreenCoords.y - this.mMarkerHotSpot.y);
			final int bottom = top + markerHeight;
			curMarkerBounds.set(left, top, right, bottom);

			pj.fromMapPixels(eventX, eventY, curScreenCoords2);
			if (curMarkerBounds.contains(curScreenCoords2.x, curScreenCoords2.y)) {
				if (onTap(i)) {
					return true;
				}
			}
		}
		return super.onSingleTapUp(event, mapView);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	protected boolean onTap(int pIndex) {
		if(this.mOnItemTapListener != null)
			return this.mOnItemTapListener.onItemTap(pIndex, this.mItemList.get(pIndex));
		else
			return false;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	public static interface OnItemTapListener<T>{
		public boolean onItemTap(final int aIndex, final T aItem);
	}
}
