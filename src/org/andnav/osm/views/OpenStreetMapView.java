// Created by plusminus on 17:45:56 - 25.09.2008
package org.andnav.osm.views;

import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.util.BoundingBoxE6;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.MyMath;
import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.andnav.osm.views.controller.OpenStreetMapViewController;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;
import org.andnav.osm.views.util.OpenStreetMapTileDownloader;
import org.andnav.osm.views.util.OpenStreetMapTileFilesystemProvider;
import org.andnav.osm.views.util.OpenStreetMapTileProvider;
import org.andnav.osm.views.util.Util;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;

public class OpenStreetMapView extends View implements OpenStreetMapConstants,
		OpenStreetMapViewConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	final OpenStreetMapRendererInfo DEFAULTRENDERER = OpenStreetMapRendererInfo.MAPNIK;

	// ===========================================================
	// Fields
	// ===========================================================

	protected int mLatitudeE6 = 0, mLongitudeE6 = 0;
	protected int mZoomLevel = 0;

	protected OpenStreetMapRendererInfo mRendererInfo;
	protected final OpenStreetMapTileProvider mTileProvider;

	protected final GestureDetector mGestureDetector = new GestureDetector(new OpenStreetMapViewGestureDetectorListener());

	protected final List<OpenStreetMapViewOverlay> mOverlays = new ArrayList<OpenStreetMapViewOverlay>();

	protected final Paint mPaint = new Paint();
	private int mTouchDownX;
	private int mTouchDownY;
	private int mTouchMapOffsetX;
	private int mTouchMapOffsetY;

	private OpenStreetMapView mMiniMap, mMaxiMap;

	private OpenStreetMapViewController mController;
	private int mMiniMapOverriddenVisibility = NOT_SET;
	private int mMiniMapZoomDiff = NOT_SET;


	// ===========================================================
	// Constructors
	// ===========================================================

	/**
	 * XML Constructor (uses default Renderer)
	 */
	public OpenStreetMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mRendererInfo = DEFAULTRENDERER;
		this.mTileProvider = new OpenStreetMapTileProvider(context, new SimpleInvalidationHandler());
	}

	/**
	 * Standard Constructor for {@link OpenStreetMapView}.
	 * 
	 * @param context
	 * @param aRendererInfo
	 *            pass a {@link OpenStreetMapRendererInfo} you like.
	 */
	public OpenStreetMapView(final Context context, final OpenStreetMapRendererInfo aRendererInfo) {
		super(context);
		this.mRendererInfo = aRendererInfo;
		this.mTileProvider = new OpenStreetMapTileProvider(context, new SimpleInvalidationHandler());
	}

	/**
	 * 
	 * @param context
	 * @param aRendererInfo
	 *            pass a {@link OpenStreetMapRendererInfo} you like.
	 * @param osmv
	 *            another {@link OpenStreetMapView}, to share the TileProvider
	 *            with.<br/>
	 *            May significantly improve the render speed, when using the
	 *            same {@link OpenStreetMapRendererInfo}.
	 */
	public OpenStreetMapView(final Context context, final OpenStreetMapRendererInfo aRendererInfo,
			final OpenStreetMapView aMapToShareTheTileProviderWith) {
		super(context);
		this.mRendererInfo = aRendererInfo;
		this.mTileProvider = aMapToShareTheTileProviderWith.mTileProvider;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	/**
	 * This MapView takes control of the {@link OpenStreetMapView} passed as
	 * parameter.<br />
	 * I.e. it zoomes it to x levels less than itself and centers it the same
	 * coords.<br />
	 * Its pretty usefull when the MiniMap uses the same TileProvider.
	 * 
	 * @see OpenStreetMapView.OpenStreetMapView(
	 * @param aOsmvMinimap
	 * @param aZoomDiff
	 *            3 is a good Value. Pass {@link OpenStreetMapViewConstants}
	 *            .NOT_SET to disable autozooming of the minimap.
	 */
	public void setMiniMap(final OpenStreetMapView aOsmvMinimap, final int aZoomDiff) {
		this.mMiniMapZoomDiff = aZoomDiff;
		this.mMiniMap = aOsmvMinimap;
		aOsmvMinimap.setMaxiMap(this);

		// Synchronize the Views.
		this.setMapCenter(this.mLatitudeE6, this.mLongitudeE6);
		this.setZoomLevel(this.getZoomLevel());
	}

	public boolean hasMiniMap() {
		return this.mMiniMap != null;
	}

	/**
	 * @return {@link View}.GONE or {@link View}.VISIBLE or {@link View}
	 *         .INVISIBLE or {@link OpenStreetMapViewConstants}.NOT_SET
	 * */
	public int getOverrideMiniMapVisiblity() {
		return this.mMiniMapOverriddenVisibility;
	}

	/**
	 * Use this method if you want to make the MiniMap visible i.e.: always or
	 * never. Use {@link View}.GONE , {@link View}.VISIBLE, {@link View}
	 * .INVISIBLE. Use {@link OpenStreetMapViewConstants}.NOT_SET to reset this
	 * feature.
	 * 
	 * @param aVisiblity
	 */
	public void setOverrideMiniMapVisiblity(final int aVisiblity) {
		switch (aVisiblity) {
			case View.GONE:
			case View.VISIBLE:
			case View.INVISIBLE:
				if (this.mMiniMap != null)
					this.mMiniMap.setVisibility(aVisiblity);
			case NOT_SET:
				this.setZoomLevel(this.mZoomLevel);
				break;
			default:
				throw new IllegalArgumentException("See javadoch of this method !!!");
		}
		this.mMiniMapOverriddenVisibility = aVisiblity;
	}

	protected void setMaxiMap(final OpenStreetMapView aOsmvMaxiMap) {
		this.mMaxiMap = aOsmvMaxiMap;
	}

	public OpenStreetMapViewController getController() {
		if (this.mController != null)
			return this.mController;
		else
			return this.mController = new OpenStreetMapViewController(this);
	}

	/**
	 * You can add/remove/reorder your Overlays using the List of
	 * {@link OpenStreetMapViewOverlay}. The first (index 0) Overlay gets drawn
	 * first, the one with the highest as the last one.
	 */
	public List<OpenStreetMapViewOverlay> getOverlays() {
		return this.mOverlays;
	}

	public double getLatitudeSpan() {
		return this.getDrawnBoundingBoxE6().getLongitudeSpanE6() / 1E6;
	}

	public int getLatitudeSpanE6() {
		return this.getDrawnBoundingBoxE6().getLatitudeSpanE6();
	}

	public double getLongitudeSpan() {
		return this.getDrawnBoundingBoxE6().getLatitudeSpanE6() / 1E6;
	}

	public int getLongitudeSpanE6() {
		return this.getDrawnBoundingBoxE6().getLatitudeSpanE6();
	}

	public BoundingBoxE6 getDrawnBoundingBoxE6() {
		return getBoundingBox(this.getWidth(), this.getHeight());
	}
	
	public BoundingBoxE6 getVisibleBoundingBoxE6() {
//		final ViewParent parent = this.getParent();
//		if(parent instanceof RotateView){
//			final RotateView par = (RotateView)parent;
//			return getBoundingBox(par.getMeasuredWidth(), par.getMeasuredHeight());
//		}else{
			return getBoundingBox(this.getWidth(), this.getHeight());
//		}
	}
	
	private BoundingBoxE6 getBoundingBox(final int pViewWidth, final int pViewHeight){	
		/* Get the center MapTile which is above this.mLatitudeE6 and this.mLongitudeE6 .*/
		final int[] centerMapTileCoords = Util.getMapTileFromCoordinates(this.mLatitudeE6, this.mLongitudeE6, this.mZoomLevel, null);
		
		final BoundingBoxE6 tmp = Util.getBoundingBoxFromMapTile(centerMapTileCoords, this.mZoomLevel);
		
		final int mLatitudeSpan_2 = (int)(1.0f * tmp.getLatitudeSpanE6() * pViewHeight / this.mRendererInfo.MAPTILE_SIZEPX) / 2; 
		final int mLongitudeSpan_2 = (int)(1.0f * tmp.getLongitudeSpanE6() * pViewWidth / this.mRendererInfo.MAPTILE_SIZEPX) / 2;
		
		final int north = this.mLatitudeE6 + mLatitudeSpan_2;
		final int south = this.mLatitudeE6 - mLatitudeSpan_2;
		final int west = this.mLongitudeE6 - mLongitudeSpan_2;
		final int east = this.mLongitudeE6 + mLongitudeSpan_2;
		
		return new BoundingBoxE6(north, east, south, west);
	}

	/**
	 * This class is only meant to be used during on call of onDraw(). Otherwise
	 * it may produce strange results.
	 * 
	 * @return
	 */
	public OpenStreetMapViewProjection getProjection() {
		return new OpenStreetMapViewProjection();
	}

	public void setMapCenter(final GeoPoint aCenter) {
		this.setMapCenter(aCenter.getLatitudeE6(), aCenter.getLongitudeE6());
	}

	public void setMapCenter(final double aLatitude, final double aLongitude) {
		this.setMapCenter((int) (aLatitude * 1E6), (int) (aLongitude * 1E6));
	}

	public void setMapCenter(final int aLatitudeE6, final int aLongitudeE6) {
		this.setMapCenter(aLatitudeE6, aLongitudeE6, true);
	}

	protected void setMapCenter(final int aLatitudeE6, final int aLongitudeE6,
			final boolean doPassFurther) {
		this.mLatitudeE6 = aLatitudeE6;
		this.mLongitudeE6 = aLongitudeE6;

		if (doPassFurther && this.mMiniMap != null)
			this.mMiniMap.setMapCenter(aLatitudeE6, aLongitudeE6, false);
		else if (this.mMaxiMap != null)
			this.mMaxiMap.setMapCenter(aLatitudeE6, aLongitudeE6, false);

		this.postInvalidate();
	}

	public OpenStreetMapRendererInfo getRenderer() {
		return this.mRendererInfo;
	}
	
	public void setRenderer(final OpenStreetMapRendererInfo aRenderer) {
		this.mRendererInfo = aRenderer;
		this.setZoomLevel(this.mZoomLevel); // Invalidates the map and zooms to
											// the maximum level of the
											// renderer.
	}

	/**
	 * @param aZoomLevel
	 *            between 0 (equator) and 18/19(closest), depending on the
	 *            Renderer chosen.
	 */
	public void setZoomLevel(final int aZoomLevel) {
		this.mZoomLevel = Math.max(0, Math.min(this.mRendererInfo.ZOOM_MAXLEVEL, aZoomLevel));

		if (this.mMiniMap != null) {
			if (this.mZoomLevel < this.mMiniMapZoomDiff) {
				if (this.mMiniMapOverriddenVisibility == NOT_SET)
					this.mMiniMap.setVisibility(View.INVISIBLE);
			} else {
				if (this.mMiniMapOverriddenVisibility == NOT_SET
						&& this.mMiniMap.getVisibility() != View.VISIBLE) {
					this.mMiniMap.setVisibility(View.VISIBLE);
				}
				if (this.mMiniMapZoomDiff != NOT_SET)
					this.mMiniMap.setZoomLevel(this.mZoomLevel - this.mMiniMapZoomDiff);
			}
		}
		this.postInvalidate();
	}

	/**
	 * Zooms in if possible.
	 */
	public void zoomIn() {

//		final String nextBelowMaptileUrlString = this.mRendererInfo.getTileURLString(Util
//				.getMapTileFromCoordinates(this.mLatitudeE6, this.mLongitudeE6, this.mZoomLevel + 1,
//						null), this.mZoomLevel + 1);
//		this.mTileProvider.preCacheTile(nextBelowMaptileUrlString);

		this.setZoomLevel(this.mZoomLevel + 1);
	}

	/**
	 * Zooms out if possible.
	 */
	public void zoomOut() {
		this.setZoomLevel(this.mZoomLevel - 1);
	}

	/**
	 * @return the current ZoomLevel between 0 (equator) and 18/19(closest),
	 *         depending on the Renderer chosen.
	 */
	public int getZoomLevel() {
		return this.mZoomLevel;
	}

	public GeoPoint getMapCenter() {
		return new GeoPoint(this.mLatitudeE6, this.mLongitudeE6);
	}

	public int getMapCenterLatitudeE6() {
		return this.mLatitudeE6;
	}

	public int getMapCenterLongitudeE6() {
		return this.mLongitudeE6;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	public void onLongPress(MotionEvent e) {
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			if (osmvo.onLongPress(e, this))
				return;
	}

	public boolean onSingleTapUp(MotionEvent e) {
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			if (osmvo.onSingleTapUp(e, this))
				return true;

		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			if (osmvo.onKeyDown(keyCode, event, this))
				return true;

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			if (osmvo.onKeyUp(keyCode, event, this))
				return true;

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			if (osmvo.onTrackballEvent(event, this))
				return true;

		return super.onTrackballEvent(event);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			if (osmvo.onTouchEvent(event, this))
				return true;

		this.mGestureDetector.onTouchEvent(event);

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				this.mTouchDownX = (int) event.getX();
				this.mTouchDownY = (int) event.getY();
				invalidate();
				return true;
			case MotionEvent.ACTION_MOVE:
				this.mTouchMapOffsetX = (int) event.getX() - this.mTouchDownX;
				this.mTouchMapOffsetY = (int) event.getY() - this.mTouchDownY;
				invalidate();
				return true;
			case MotionEvent.ACTION_UP:
				final int viewWidth_2 = this.getWidth() / 2;
				final int viewHeight_2 = this.getHeight() / 2;
				final GeoPoint newCenter = this.getProjection().fromPixels(viewWidth_2,
						viewHeight_2);
				this.mTouchMapOffsetX = 0;
				this.mTouchMapOffsetY = 0;
				this.setMapCenter(newCenter); // Calls invalidate
		}

		return super.onTouchEvent(event);
	}

	@Override
	public void onDraw(final Canvas c) {
		final long startMs = System.currentTimeMillis();

		/*
		 * Do some calculations and drag attributes to local variables to save
		 * some performance.
		 */
		final int zoomLevel = this.mZoomLevel;
		final int viewWidth = this.getWidth();
		final int viewHeight = this.getHeight();
		final int tileSizePx = this.mRendererInfo.MAPTILE_SIZEPX;

		/*
		 * Get the center MapTile which is above this.mLatitudeE6 and
		 * this.mLongitudeE6 .
		 */
		final int[] centerMapTileCoords = Util.getMapTileFromCoordinates(this.mLatitudeE6,
				this.mLongitudeE6, zoomLevel, null);

		/*
		 * Calculate the Latitude/Longitude on the left-upper ScreenCoords of
		 * the center MapTile. So in the end we can determine which MapTiles we
		 * additionally need next to the centerMapTile.
		 */
		final Point upperLeftCornerOfCenterMapTile = getUpperLeftCornerOfCenterMapTileInScreen(
				centerMapTileCoords, tileSizePx, null);

		final int centerMapTileScreenLeft = upperLeftCornerOfCenterMapTile.x;
		final int centerMapTileScreenTop = upperLeftCornerOfCenterMapTile.y;

		final int centerMapTileScreenRight = centerMapTileScreenLeft + tileSizePx;
		final int centerMapTileScreenBottom = centerMapTileScreenTop + tileSizePx;

		/*
		 * Calculate the amount of tiles needed for each side around the center
		 * one.
		 */
		final int additionalTilesNeededToLeftOfCenter = (int) Math
				.ceil((float) centerMapTileScreenLeft / tileSizePx); // i.e.
																		// "30 / 256"
																		// = 1;
		final int additionalTilesNeededToRightOfCenter = (int) Math
				.ceil((float) (viewWidth - centerMapTileScreenRight) / tileSizePx);
		final int additionalTilesNeededToTopOfCenter = (int) Math
				.ceil((float) centerMapTileScreenTop / tileSizePx); // i.e.
																	// "30 / 256"
																	// = 1;
		final int additionalTilesNeededToBottomOfCenter = (int) Math
				.ceil((float) (viewHeight - centerMapTileScreenBottom) / tileSizePx);

		final int mapTileUpperBound = (int) Math.pow(2, zoomLevel);
		final int[] mapTileCoords = new int[] { centerMapTileCoords[MAPTILE_LATITUDE_INDEX],
				centerMapTileCoords[MAPTILE_LONGITUDE_INDEX] };

		/* Draw all the MapTiles (from the upper left to the lower right). */
		for (int y = -additionalTilesNeededToTopOfCenter; y <= additionalTilesNeededToBottomOfCenter; y++) {
			for (int x = -additionalTilesNeededToLeftOfCenter; x <= additionalTilesNeededToRightOfCenter; x++) {
				/*
				 * Add/substract the difference of the tile-position to the one
				 * of the center.
				 */
				mapTileCoords[MAPTILE_LATITUDE_INDEX] = MyMath.mod(
						centerMapTileCoords[MAPTILE_LATITUDE_INDEX] + y, mapTileUpperBound);
				mapTileCoords[MAPTILE_LONGITUDE_INDEX] = MyMath.mod(
						centerMapTileCoords[MAPTILE_LONGITUDE_INDEX] + x, mapTileUpperBound);
				/* Construct a URLString, which represents the MapTile. */
				final String tileURLString = this.mRendererInfo.getTileURLString(mapTileCoords,
						zoomLevel);

				/* Draw the MapTile 'i tileSizePx' above of the centerMapTile */
				final Bitmap currentMapTile = this.mTileProvider.getMapTile(tileURLString);
				final int tileLeft = this.mTouchMapOffsetX + centerMapTileScreenLeft
						+ (x * tileSizePx);
				final int tileTop = this.mTouchMapOffsetY + centerMapTileScreenTop
						+ (y * tileSizePx);
				c.drawBitmap(currentMapTile, tileLeft, tileTop, this.mPaint);

				if (DEBUGMODE) {
					c.drawLine(tileLeft, tileTop, tileLeft + tileSizePx, tileTop, this.mPaint);
					c.drawLine(tileLeft, tileTop, tileLeft, tileTop + tileSizePx, this.mPaint);
				}
			}
		}

		/* Draw all Overlays. */
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			osmvo.onManagedDraw(c, this);

		this.mPaint.setStyle(Style.STROKE);
		if (this.mMaxiMap != null) // If this is a MiniMap
			c.drawRect(0, 0, viewWidth - 1, viewHeight - 1, this.mPaint);

		final long endMs = System.currentTimeMillis();
		Log.i(DEBUGTAG, "Rendering overall: " + (endMs - startMs) + "ms");
	}

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * @param centerMapTileCoords
	 * @param tileSizePx
	 * @param reuse
	 *            just pass null if you do not have a Point to be 'recycled'.
	 */
	private Point getUpperLeftCornerOfCenterMapTileInScreen(final int[] centerMapTileCoords,
			final int tileSizePx, final Point reuse) {
		final Point out = (reuse != null) ? reuse : new Point();

		final int viewWidth = this.getWidth();
		final int viewWidth_2 = viewWidth / 2;
		final int viewHeight = this.getHeight();
		final int viewHeight_2 = viewHeight / 2;

		/*
		 * Calculate the Latitude/Longitude on the left-upper ScreenCoords of
		 * the center MapTile. So in the end we can determine which MapTiles we
		 * additionally need next to the centerMapTile.
		 */
		final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(centerMapTileCoords,
				this.mZoomLevel);
		final float[] relativePositionInCenterMapTile = bb
				.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
						this.mLatitudeE6, this.mLongitudeE6, null);

		final int centerMapTileScreenLeft = viewWidth_2
				- (int) (0.5f + (relativePositionInCenterMapTile[MAPTILE_LONGITUDE_INDEX] * tileSizePx));
		final int centerMapTileScreenTop = viewHeight_2
				- (int) (0.5f + (relativePositionInCenterMapTile[MAPTILE_LATITUDE_INDEX] * tileSizePx));

		out.set(centerMapTileScreenLeft, centerMapTileScreenTop);
		return out;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	/**
	 * This class may return valid results until the underlying
	 * {@link OpenStreetMapView} gets modified in any way (i.e. new center).
	 * 
	 * @author Nicolas Gramlich
	 */
	public class OpenStreetMapViewProjection {

		final int viewWidth;
		final int viewHeight;
		final BoundingBoxE6 bb;
		final int zoomLevel;
		final int tileSizePx;
		final int[] centerMapTileCoords;
		final Point upperLeftCornerOfCenterMapTile;

		public OpenStreetMapViewProjection() {
			viewWidth = OpenStreetMapView.this.getWidth();
			viewHeight = OpenStreetMapView.this.getHeight();

			/*
			 * Do some calculations and drag attributes to local variables to
			 * save some performance.
			 */
			zoomLevel = OpenStreetMapView.this.mZoomLevel; // TODO Draw to
															// attributes and so
															// make it only
															// 'valid' for a
															// short time.
			tileSizePx = OpenStreetMapView.this.mRendererInfo.MAPTILE_SIZEPX;

			/*
			 * Get the center MapTile which is above this.mLatitudeE6 and
			 * this.mLongitudeE6 .
			 */
			centerMapTileCoords = Util.getMapTileFromCoordinates(
					OpenStreetMapView.this.mLatitudeE6, OpenStreetMapView.this.mLongitudeE6,
					zoomLevel, null);
			upperLeftCornerOfCenterMapTile = getUpperLeftCornerOfCenterMapTileInScreen(
					centerMapTileCoords, tileSizePx, null);

			bb = OpenStreetMapView.this.getDrawnBoundingBoxE6();
		}

		/**
		 * Converts x/y ScreenCoordinates to the underlying GeoPoint.
		 * 
		 * @param x
		 * @param y
		 * @return GeoPoint under x/y.
		 */
		public GeoPoint fromPixels(float x, float y) {
			/* Subtract the offset caused by touch. */
			x -= OpenStreetMapView.this.mTouchMapOffsetX;
			y -= OpenStreetMapView.this.mTouchMapOffsetY;

			return bb.getGeoPointOfRelativePositionWithLinearInterpolation(x / viewWidth, y
					/ viewHeight);
		}

		private static final int EQUATORCIRCUMFENCE = 40075004;

		public float metersToEquatorPixels(final float aMeters) {
			return aMeters / EQUATORCIRCUMFENCE
					* OpenStreetMapView.this.mRendererInfo.MAPTILE_SIZEPX;
		}

		/**
		 * Converts a GeoPoint to its ScreenCoordinates. <br/>
		 * <br/>
		 * <b>CAUTION</b> ! Conversion currently has a large error on
		 * <code>zoomLevels <= 7</code>.<br/>
		 * The Error on ZoomLevels higher than 7, the error is below
		 * <code>1px</code>.<br/>
		 * TODO: Add a linear interpolation to minimize this error.
		 * 
		 * <PRE>
		 * Zoom 	Error(m) 	Error(px)
		 * 11 	6m 	1/12px
		 * 10 	24m 	1/6px
		 * 8 	384m 	1/2px
		 * 6 	6144m 	3px
		 * 4 	98304m 	10px
		 * </PRE>
		 * 
		 * @param in
		 *            the GeoPoint you want the onScreenCoordinates of.
		 * @param reuse
		 *            just pass null if you do not have a Point to be
		 *            'recycled'.
		 * @return the Point containing the approximated ScreenCoordinates of
		 *         the GeoPoint passed.
		 */
		public Point toPixels(final GeoPoint in, final Point reuse) {
			return toPixels(in, reuse, true);
		}

		protected Point toPixels(final GeoPoint in, final Point reuse, final boolean doGudermann) {

			final Point out = (reuse != null) ? reuse : new Point();

			final int[] underGeopointTileCoords = Util.getMapTileFromCoordinates(
					in.getLatitudeE6(), in.getLongitudeE6(), zoomLevel, null);

			/*
			 * Calculate the Latitude/Longitude on the left-upper ScreenCoords
			 * of the MapTile.
			 */
			final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(underGeopointTileCoords,
					zoomLevel);

			final float[] relativePositionInCenterMapTile;
			if (doGudermann && zoomLevel < 7)
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
								in.getLatitudeE6(), in.getLongitudeE6(), null);
			else
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(in
								.getLatitudeE6(), in.getLongitudeE6(), null);

			final int tileDiffX = centerMapTileCoords[MAPTILE_LONGITUDE_INDEX]
					- underGeopointTileCoords[MAPTILE_LONGITUDE_INDEX];
			final int tileDiffY = centerMapTileCoords[MAPTILE_LATITUDE_INDEX]
					- underGeopointTileCoords[MAPTILE_LATITUDE_INDEX];
			final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x
					- (tileSizePx * tileDiffX);
			final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y
					- (tileSizePx * tileDiffY);

			final int x = underGeopointTileScreenLeft
					+ (int) (relativePositionInCenterMapTile[MAPTILE_LONGITUDE_INDEX] * tileSizePx);
			final int y = underGeopointTileScreenTop
					+ (int) (relativePositionInCenterMapTile[MAPTILE_LATITUDE_INDEX] * tileSizePx);

			/* Add up the offset caused by touch. */
			out.set(x + OpenStreetMapView.this.mTouchMapOffsetX, y
					+ OpenStreetMapView.this.mTouchMapOffsetY);
			return out;
		}

		public Path toPixels(final List<? extends GeoPoint> in, final Path reuse) {
			return toPixels(in, reuse, true);
		}

		protected Path toPixels(final List<? extends GeoPoint> in, final Path reuse, final boolean doGudermann)
				throws IllegalArgumentException {
			if (in.size() < 2)
				throw new IllegalArgumentException("List of GeoPoints needs to be at least 2.");

			final Path out = (reuse != null) ? reuse : new Path();
			out.incReserve(in.size());

			boolean first = true;
			for (GeoPoint gp : in) {
				final int[] underGeopointTileCoords = Util.getMapTileFromCoordinates(gp
						.getLatitudeE6(), gp.getLongitudeE6(), zoomLevel, null);

				/*
				 * Calculate the Latitude/Longitude on the left-upper
				 * ScreenCoords of the MapTile.
				 */
				final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(underGeopointTileCoords,
						zoomLevel);

				final float[] relativePositionInCenterMapTile;
				if (doGudermann && zoomLevel < 7)
					relativePositionInCenterMapTile = bb
							.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
									gp.getLatitudeE6(), gp.getLongitudeE6(), null);
				else
					relativePositionInCenterMapTile = bb
							.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(gp
									.getLatitudeE6(), gp.getLongitudeE6(), null);

				final int tileDiffX = centerMapTileCoords[MAPTILE_LONGITUDE_INDEX]
						- underGeopointTileCoords[MAPTILE_LONGITUDE_INDEX];
				final int tileDiffY = centerMapTileCoords[MAPTILE_LATITUDE_INDEX]
						- underGeopointTileCoords[MAPTILE_LATITUDE_INDEX];
				final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x
						- (tileSizePx * tileDiffX);
				final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y
						- (tileSizePx * tileDiffY);

				final int x = underGeopointTileScreenLeft
						+ (int) (relativePositionInCenterMapTile[MAPTILE_LONGITUDE_INDEX] * tileSizePx);
				final int y = underGeopointTileScreenTop
						+ (int) (relativePositionInCenterMapTile[MAPTILE_LATITUDE_INDEX] * tileSizePx);

				/* Add up the offset caused by touch. */
				if (first)
					out.moveTo(x + OpenStreetMapView.this.mTouchMapOffsetX, y
							+ OpenStreetMapView.this.mTouchMapOffsetY);
				else
					out.lineTo(x + OpenStreetMapView.this.mTouchMapOffsetX, y
							+ OpenStreetMapView.this.mTouchMapOffsetY);
				first = false;
			}

			return out;
		}
	}

	private class SimpleInvalidationHandler extends Handler {

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case OpenStreetMapTileDownloader.MAPTILEDOWNLOADER_SUCCESS_ID:
				case OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID:
					OpenStreetMapView.this.invalidate();
					break;
			}
		}
	}

	private class OpenStreetMapViewGestureDetectorListener implements OnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			// TODO Could be used for smoothly 'scroll-out' the map on a fast
			// motion.
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			OpenStreetMapView.this.onLongPress(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return OpenStreetMapView.this.onSingleTapUp(e);
		}

	}
}
