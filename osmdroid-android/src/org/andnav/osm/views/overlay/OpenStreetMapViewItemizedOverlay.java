// Created by plusminus on 23:18:23 - 02.10.2008
package org.andnav.osm.views.overlay;

import java.awt.Rectangle;
import java.util.List;

import org.andnav.osm.DefaultResourceProxyImpl;
import org.andnav.osm.ResourceProxy;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

/**
 * Draws a list of {@link OpenStreetMapViewOverlayItem} as markers to a map.
 * The item with the lowest index is drawn as last and therefore the 'topmost' marker. 
 * It also gets checked for onTap first.
 * This class is generic, because you then you get your custom item-class passed back in onTap().
 * @author Nicolas Gramlich
 * @author Theodore Hong
 * @author Fred Eisele
 *
 * @param <T>
 */
public class OpenStreetMapViewItemizedOverlay<T extends OpenStreetMapViewOverlayItem> extends OpenStreetMapViewOverlay {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected OnItemTapListener<T> mOnItemTapListener;
	protected final List<T> mItemList;
	protected final OpenStreetMapViewOverlayItem mDefaultItem;

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
        this(ctx, aList, null, null, null, aOnItemTapListener, pResourceProxy);
	}

	public OpenStreetMapViewItemizedOverlay(
			final Context ctx,
			final List<T> aList,
			final Drawable pMarker,
			final Point pMarkerHotspot,
			final OnItemTapListener<T> aOnItemTapListener,
			final ResourceProxy pResourceProxy) {
		this(ctx, aList, null, null, null, aOnItemTapListener, pResourceProxy);
	}
	public OpenStreetMapViewItemizedOverlay(
			final Context ctx,
			final List<T> aList,
			final Drawable pMarker,
			final Point pMarkerHotspot,
			final OpenStreetMapViewOverlayItem.HotspotPlace pHotSpotPlace,
			final OnItemTapListener<T> aOnItemTapListener,
			final ResourceProxy pResourceProxy) {

		super(pResourceProxy);

		assert(ctx != null);
		assert(aList != null);

		this.mDefaultItem = OpenStreetMapViewOverlayItem.getDefaultItem(pMarker, pMarkerHotspot, pHotSpotPlace, pResourceProxy);

        this.mOnItemTapListener = aOnItemTapListener;

		// Add one sample item.
		this.mItemList = aList;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces (and supporting methods)
	// ===========================================================

	/**
	 * Called to draw any items after all other items.
	 */
	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {
		return;
	}

	private static final Paint bullseyePaint = new Paint();
	static {
		bullseyePaint.setStyle(Paint.Style.STROKE);
		bullseyePaint.setColor(Color.RED);
	}

	@Override
	public void onDraw(final Canvas canvas, final OpenStreetMapView mapView) {
		final OpenStreetMapViewProjection pj = mapView.getProjection();
		final Point curScreenCoords = new Point();
		/* Draw in backward cycle, so the items with the least index are on the front. */
		for(int i = this.mItemList.size() - 1; i >= 0; i--){
			T item = this.mItemList.get(i);
			pj.toMapPixels(item.mGeoPoint, curScreenCoords);

			onDrawItem(canvas, i, curScreenCoords);
		}
        // indicate the place touched with a bullseye
		if (touchPoint != null) canvas.drawCircle(touchPoint.x, touchPoint.y, 20, bullseyePaint);
	}

	@Override
	public boolean onSingleTapUp(final MotionEvent event, final OpenStreetMapView mapView) {
		return ( activateSelectedItems(event,mapView, new ActiveItem() {
			@Override
			public boolean run(int aIndex) {
				runTap(aIndex);
				return false;
			}
		})) 
            ? true
		    : super.onSingleTapUp(event, mapView);
	}
	
	@Override
	public boolean onLongPress(MotionEvent event, OpenStreetMapView mapView) {
		return ( activateSelectedItems(event,mapView, new ActiveItem() {
			@Override
			public boolean run(int aIndex) {
				runLongPress(aIndex);
				return false;
			}
		})) 
			? true
		    : super.onLongPress(event, mapView);
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
	 * @param canvas what the item is drawn upon
	 * @param index which item is to be drawn
	 * @param curScreenCoords
	 */
	protected void onDrawItem(final Canvas canvas, final int index, final Point curScreenCoords) {
		// get this item's preferred marker & hotspot
		final T item = this.mItemList.get(index);
		
		final Drawable marker = (item.getMarker(0) == null) 
                ? this.mDefaultItem.getMarker(0) 
		        : item.getMarker(0);
                
        final Rect rect = new Rect();
        getItemBoundingRetangle(item, rect, curScreenCoords);
		// draw it
		marker.setBounds(rect);
		marker.draw(canvas);
		// the following lines place objects on the screen indicating the item boundary.
		if (touchPoint != null) canvas.drawRect(rect, boudaryPaint);
	}

	protected boolean runTap(int pIndex) {
		if(this.mOnItemTapListener == null) return false;
		return this.mOnItemTapListener.onItemTap(pIndex, this.mItemList.get(pIndex));
	}
	
	protected boolean runLongPress(int pIndex) {
		if(this.mOnItemTapListener == null) return false;
		return this.mOnItemTapListener.onItemTap(pIndex, this.mItemList.get(pIndex));
	}

	/**
	 * When a content sensitive action is performed the content item needs to be identified.
	 * This method does that and then performs the assigned task on that item.
	 * 
	 * @param event
	 * @param mapView
	 * @param task
	 * @return true if event is handled false otherwise
	 */
	private boolean activateSelectedItems(final MotionEvent event, final OpenStreetMapView mapView, final ActiveItem task) {
		final OpenStreetMapViewProjection pj = mapView.getProjection();
		final int eventX = (int) event.getX();
		final int eventY = (int) event.getY();

		/* These objects are created to avoid construct new ones every cycle. */
		final Point touchScreenCoords = new Point();
		pj.fromMapPixels(eventX, eventY, touchScreenCoords);

		touchPoint = touchScreenCoords;
		
		final Rect markerScreenBounds = new Rect();
		final Point curScreenCoords = new Point();
		for(int i = 0; i < this.mItemList.size(); ++i) {
			final T item = this.mItemList.get(i);
			pj.toMapPixels(item.mGeoPoint, curScreenCoords);
			
			getItemBoundingRetangle(item, markerScreenBounds, curScreenCoords);
		
			if (! markerScreenBounds.contains(touchScreenCoords.x, touchScreenCoords.y)) 
				continue;
			if (task.run(i)) return true;
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
		final Drawable marker = (item.getMarker(0) == null) 
		              ? this.mDefaultItem.getMarker(0) 
		    		  : item.getMarker(0);
		final Point markerHotspot = (item.getMarkerHotspot(0) == null) 
		              ? this.mDefaultItem.getMarkerHotspot(0) 
		              : item.getMarkerHotspot(0);
		
		// calculate bounding rectangle
		final int markerWidth = marker.getIntrinsicWidth();
		final int markerHeight = marker.getIntrinsicHeight();
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
	public static interface OnItemTapListener<T>{
		public boolean onItemTap(final int aIndex, final T aItem);
		public boolean onItemLongPress(final int aIndex, final T aItem);
	}
	
	public static interface ActiveItem {
		public boolean run(final int aIndex);
	}
}
