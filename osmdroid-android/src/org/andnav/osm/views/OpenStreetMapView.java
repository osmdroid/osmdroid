// Created by plusminus on 17:45:56 - 25.09.2008
package org.andnav.osm.views;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.util.BoundingBoxE6;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.overlay.OpenStreetMapTilesOverlay;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay.Snappable;
import org.andnav.osm.views.util.Mercator;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;
import org.andnav.osm.views.util.OpenStreetMapTileProvider;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

public class OpenStreetMapView extends View implements OpenStreetMapViewConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapView.class);
	
	final static OpenStreetMapRendererInfo DEFAULTRENDERER = OpenStreetMapRendererInfo.MAPNIK;
   	final static String BUNDLE_RENDERER = "org.andnav.osm.views.OpenStreetMapView.RENDERER";
	final static String BUNDLE_SCROLL_X = "org.andnav.osm.views.OpenStreetMapView.SCROLL_X";
	final static String BUNDLE_SCROLL_Y = "org.andnav.osm.views.OpenStreetMapView.SCROLL_Y";
	final static String BUNDLE_ZOOM_LEVEL = "org.andnav.osm.views.OpenStreetMapView.ZOOM";

	private static final int MULTI_NONE = 0;
	private static final int MULTI_ACTIVE = 1;
	private static final int MULTI_HANDLED = 2;

	private float mPointerDownDistance; /* distance for a ACTION_POINTER_DOWN MotionEvent */
	private int mMultiMode = MULTI_NONE;  /* if we are in after an ACTION_POINTER_DOWN */

	// get API level 5 MotionEvent constants by reflection
	// TODO can remove this stuff if we upgrade to API level 5
	private static int ACTION_MASK = 255;
	private static int ACTION_POINTER_DOWN = 5;
	private static int ACTION_POINTER_UP = 6;
	private static Method MotionEvent_getX;
	private static Method MotionEvent_getY;
	static {
		final MotionEvent me = MotionEvent.obtain(0, 0, 0, 0f, 0f, 0);
		try {
			ACTION_MASK = MotionEvent.class.getField("ACTION_MASK").getInt(me);
		} catch (final Exception e) {}
		try {
			ACTION_POINTER_DOWN = MotionEvent.class.getField("ACTION_POINTER_DOWN").getInt(me);
		} catch (final Exception e) {}
		try {
			ACTION_POINTER_UP = MotionEvent.class.getField("ACTION_POINTER_UP").getInt(me);
		} catch (final Exception e) {}
		try {
			MotionEvent_getX = MotionEvent.class.getMethod("getX", new Class[] { int.class });
		} catch (final Exception e) {}
		try {
			MotionEvent_getY = MotionEvent.class.getMethod("getY", new Class[] { int.class });
		} catch (final Exception e) {}
	}

	// ===========================================================
	// Fields
	// ===========================================================

	protected int mZoomLevel = 0;								/** Current zoom level for map tiles */
	protected final List<OpenStreetMapViewOverlay> mOverlays = new ArrayList<OpenStreetMapViewOverlay>();

	protected Bitmap mBackBuffer;
	protected Canvas mBackCanvas;
	protected Matrix mTrans = new Matrix();
	protected final Paint mPaint = new Paint();
	private OpenStreetMapViewProjection mProjection;

	private OpenStreetMapView mMiniMap, mMaxiMap;
	private final OpenStreetMapTilesOverlay mMapOverlay;

	private final GestureDetector mGestureDetector = new GestureDetector(new OpenStreetMapViewGestureDetectorListener());
	final Scroller mScroller;							/** Handles map scrolling */
	final Scaler mScaler;
	private OpenStreetMapViewController mController;
	private int mMiniMapOverriddenVisibility = NOT_SET;
	private int mMiniMapZoomDiff = NOT_SET;

	private ZoomButtonsController mZoomController;
	private boolean mEnableZoomController = false;


	// ===========================================================
	// Constructors
	// ===========================================================

	private OpenStreetMapView(final Context context, AttributeSet attrs,
			final OpenStreetMapRendererInfo aRendererInfo,
			final OpenStreetMapTileProvider aTileProvider) {
		super(context, attrs);
		this.mController = new OpenStreetMapViewController(this);
		this.mScroller = new Scroller(context);
		this.mScaler = new Scaler(context, new LinearInterpolator());
		this.mMapOverlay = new OpenStreetMapTilesOverlay(this, aRendererInfo, aTileProvider);
		mOverlays.add(this.mMapOverlay);
		this.mZoomController = new ZoomButtonsController(this);
		this.mZoomController.setOnZoomListener(new OpenStreetMapViewZoomListener());
	}

	/**
	 * XML Constructor (uses default Renderer)
	 */
	public OpenStreetMapView(Context context, AttributeSet attrs) {
		this(context, attrs, DEFAULTRENDERER, null);
	}

	/**
	 * Standard Constructor for {@link OpenStreetMapView}.
	 *
	 * @param context
	 * @param aRendererInfo
	 *            pass a {@link OpenStreetMapRendererInfo} you like.
	 */
	public OpenStreetMapView(final Context context, final OpenStreetMapRendererInfo aRendererInfo) {
		this(context, null, aRendererInfo, null);
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
	public OpenStreetMapView(final Context context,
			final OpenStreetMapRendererInfo aRendererInfo,
			final OpenStreetMapView aMapToShareTheTileProviderWith) {
		this(context, null, aRendererInfo, /* TODO aMapToShareTheTileProviderWith.mTileProvider */ null);
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

		// TODO Synchronize the Views.
//		this.setMapCenter(this.mLatitudeE6, this.mLongitudeE6);
//		this.setZoomLevel(this.getZoomLevel());
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
				throw new IllegalArgumentException("See javadoc of this method !!!");
		}
		this.mMiniMapOverriddenVisibility = aVisiblity;
	}

	protected void setMaxiMap(final OpenStreetMapView aOsmvMaxiMap) {
		this.mMaxiMap = aOsmvMaxiMap;
	}

	public OpenStreetMapViewController getController() {
		return this.mController;
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
		return this.getDrawnBoundingBoxE6().getLongitudeSpanE6() / 1E6;
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
		final int mapTileZoom = mMapOverlay.getRendererInfo().MAPTILE_ZOOM;
		final int world_2 = (1 << mZoomLevel + mapTileZoom - 1);
		final int north = world_2 + getScrollY() - getHeight()/2;
		final int south = world_2 + getScrollY() + getHeight()/2;
		final int west = world_2 + getScrollX() - getWidth()/2;
		final int east = world_2 + getScrollX() + getWidth()/2;

		return Mercator.getBoundingBoxFromCoords(west, north, east, south, mZoomLevel + mapTileZoom);
	}

	/**
	 * This class is only meant to be used during on call of onDraw(). Otherwise
	 * it may produce strange results.
	 *
	 * @return
	 */
	public OpenStreetMapViewProjection getProjection() {
		return mProjection;
	}

	public void setMapCenter(final GeoPoint aCenter) {
		this.setMapCenter(aCenter.getLatitudeE6(), aCenter.getLongitudeE6());
	}
//
//	public void setMapCenter(final double aLatitude, final double aLongitude) {
//		this.setMapCenter((int) (aLatitude * 1E6), (int) (aLongitude * 1E6));
//	}
//
	public void setMapCenter(final int aLatitudeE6, final int aLongitudeE6) {
		this.setMapCenter(aLatitudeE6, aLongitudeE6, true);
	}

	protected void setMapCenter(final int aLatitudeE6, final int aLongitudeE6,
			final boolean doPassFurther) {
		if (doPassFurther && this.mMiniMap != null)
			this.mMiniMap.setMapCenter(aLatitudeE6, aLongitudeE6, false);
		else if (this.mMaxiMap != null)
			this.mMaxiMap.setMapCenter(aLatitudeE6, aLongitudeE6, false);

		final int[] coords = Mercator.projectGeoPoint(aLatitudeE6, aLongitudeE6, getPixelZoomLevel(), null);
		final int worldSize_2 = getWorldSizePx()/2;
		if (getAnimation() == null || getAnimation().hasEnded()) {
			mScroller.startScroll(getScrollX(), getScrollY(),
					coords[MAPTILE_LONGITUDE_INDEX] - worldSize_2 - getScrollX(),
					coords[MAPTILE_LATITUDE_INDEX] - worldSize_2 - getScrollY(), 500);
			postInvalidate();
		}
	}

	public OpenStreetMapRendererInfo getRenderer() {
		return this.mMapOverlay.getRendererInfo();
	}

	public void setRenderer(final OpenStreetMapRendererInfo aRenderer) {
		this.mMapOverlay.setRendererInfo(aRenderer);
		this.checkZoomButtons();
		postInvalidate();
	}

	/**
	 * @param aZoomLevel
	 *            between 0 (equator) and 18/19(closest), depending on the
	 *            Renderer chosen.
	 */
	protected int setZoomLevel(final int aZoomLevel) {
		final int minZoomLevel = this.mMapOverlay.getRendererInfo().ZOOM_MINLEVEL;
		final int maxZoomLevel = this.mMapOverlay.getRendererInfo().ZOOM_MAXLEVEL;
		final int newZoomLevel = Math.max(minZoomLevel, Math.min(maxZoomLevel, aZoomLevel));
		final int curZoomLevel = this.mZoomLevel;

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

		this.mZoomLevel = newZoomLevel;
		this.checkZoomButtons();

		if(newZoomLevel > curZoomLevel)
			scrollTo(getScrollX()<<(newZoomLevel-curZoomLevel), getScrollY()<<(newZoomLevel-curZoomLevel));
		else if(newZoomLevel < curZoomLevel)
			scrollTo(getScrollX()>>(curZoomLevel-newZoomLevel), getScrollY()>>(curZoomLevel-newZoomLevel));

		// TODO snap for all snappables
		Point snapPoint = new Point();
		mProjection = new OpenStreetMapViewProjection();
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays) {
			if (osmvo instanceof Snappable &&
					((Snappable)osmvo).onSnapToItem(getScrollX(), getScrollY(), snapPoint, this)) {
				scrollTo(snapPoint.x, snapPoint.y);
			}
		}
		return this.mZoomLevel;
	}

	/**
	 * Get the current ZoomLevel for the map tiles.
	 * @return the current ZoomLevel between 0 (equator) and 18/19(closest),
	 *         depending on the Renderer chosen.
	 */
	public int getZoomLevel() {
		return this.mZoomLevel;
	}

	/*
	 * Returns the maximum zoom level for the point currently at the center.
	 * @return The maximum zoom level for the map's current center.
	 */
	public int getMaxZoomLevel() {
		return getRenderer().ZOOM_MAXLEVEL;
	}

	public GeoPoint getMapCenter() {
		return new GeoPoint(getMapCenterLatitudeE6(), getMapCenterLongitudeE6());
	}

	public int getMapCenterLatitudeE6() {
		return (int)(Mercator.tile2lat(getScrollY() + getWorldSizePx()/2, getPixelZoomLevel()) * 1E6);
	}

	public int getMapCenterLongitudeE6() {
		return (int)(Mercator.tile2lon(getScrollX() + getWorldSizePx()/2, getPixelZoomLevel()) * 1E6);
	}

	public void onSaveInstanceState(android.os.Bundle state) {
    	state.putInt(BUNDLE_RENDERER, getRenderer().ordinal());
    	state.putInt(BUNDLE_SCROLL_X, getScrollX());
    	state.putInt(BUNDLE_SCROLL_Y, getScrollY());
    	state.putInt(BUNDLE_ZOOM_LEVEL, getZoomLevel());
	}

	public void onRestoreInstanceState(android.os.Bundle state) {
		setRenderer(OpenStreetMapRendererInfo.values()[state.getInt(BUNDLE_RENDERER, 0)]);
    	setZoomLevel(state.getInt(BUNDLE_ZOOM_LEVEL, 1));
    	scrollTo(state.getInt(BUNDLE_SCROLL_X, 0), state.getInt(BUNDLE_SCROLL_Y, 0));
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
			if (osmvo.onSingleTapUp(e, this)) {
				postInvalidate();
				return true;
			}

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

		scrollBy((int)(event.getX() * 25), (int)(event.getY() * 25));

		return super.onTrackballEvent(event);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {

	    logger.debug(DEBUGTAG, "onTouchEvent(" + event + ")");

		/*
		 * handle multi touch events:
		 * 1. mask out the action with the ACTION_MASK
		 * 2. measure the spreading
		 * 3. on ACTION_POINTER_DOWN remember the spreading in the
		 *    pointerDownDistance and set multiDown mode to ACTIVE
		 * 4. on first move changing the spreading by a
		 *    factor of 2 or a factor of 0.5 increase or
		 *    decrease the zoom level
		 *    switch off multiDown mode and set it to HANDLED
		 * 5. on ACTION_POINTER_UP also switch off the multiDown mode
		 * 6. in any of these cases: claim the event handled and
		 *    return true
		 */
		final int action = event.getAction() & ACTION_MASK;

		if (action == ACTION_POINTER_DOWN) {
			mPointerDownDistance = spreading(event);
			mMultiMode = MULTI_ACTIVE;
			return true;
		} else if (action == ACTION_POINTER_UP) {
			mMultiMode = MULTI_NONE;
			return true;
		} else if (mMultiMode != MULTI_NONE) {
			if (mMultiMode == MULTI_ACTIVE && action == MotionEvent.ACTION_MOVE) {
				final float pointerUpDistance = spreading(event);

				if (pointerUpDistance > 2 * mPointerDownDistance) {
					mMultiMode = MULTI_HANDLED;
					setZoomLevel(mZoomLevel + 1);
				} else if (pointerUpDistance < 0.5 * mPointerDownDistance) {
					setZoomLevel(mZoomLevel - 1);
					mMultiMode = MULTI_HANDLED;
				}
			}
			return true;
		}

		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			if (osmvo.onTouchEvent(event, this))
				return true;

		if (this.mGestureDetector.onTouchEvent(event))
			return true;

		return super.onTouchEvent(event);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
//			int oldX = getScrollX();
//			int oldY = getScrollY();
//			int x = mScroller.getCurrX();
//			int y = mScroller.getCurrY();
//			if (x != oldX || y != oldY)
			if (mScroller.isFinished())
				mController.onScrollingFinished();
			else
				scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();	// Keep on drawing until the animation has finished.
		}
	}

	private float spreading(final MotionEvent event) {
		// TODO can do this directly if we upgrade to API level 5
		try {
			final float x0 = Float.valueOf(MotionEvent_getX.invoke(event, 0).toString());
			final float x1 = Float.valueOf(MotionEvent_getX.invoke(event, 1).toString());
			final float y0 = Float.valueOf(MotionEvent_getY.invoke(event, 0).toString());
			final float y1 = Float.valueOf(MotionEvent_getY.invoke(event, 1).toString());
			final float x = x0 - x1;
			final float y = y0 - y1;
			return FloatMath.sqrt(x * x + y * y);
		} catch(final Exception e) {
			return 1;
		}
	}

	private void computeScale() {
		if (mScaler.computeScale()) {
			if (mScaler.isFinished())
				mController.onScalingFinished();
			else
				postInvalidate();
		}
	}

	@Override
	public void scrollTo(int x, int y) {
		final int worldSize = getWorldSizePx();
		x %= worldSize;
		y %= worldSize;
		super.scrollTo(x, y);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if(this.mBackBuffer != null) {
			this.mBackBuffer.recycle();
			this.mBackBuffer = null;
		}
		this.mBackBuffer = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		this.mBackCanvas = new Canvas(this.mBackBuffer);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public void onDraw(final Canvas c) {
		final long startMs = System.currentTimeMillis();

		mProjection = new OpenStreetMapViewProjection();

		c.translate(getWidth()/2, getHeight()/2);
		if (!mScaler.isFinished()) {
			Matrix m = c.getMatrix();
			m.preScale(mScaler.mCurrScale, mScaler.mCurrScale, getScrollX(), getScrollY());
			c.setMatrix(m);
		}

		/* Draw background */
		c.drawColor(Color.LTGRAY);
//		This is to slow:
//		final Rect r = c.getClipBounds();
//		mPaint.setColor(Color.GRAY);
//		mPaint.setPathEffect(new DashPathEffect(new float[] {1, 1}, 0));
//		for (int x = r.left; x < r.right; x += 20)
//			c.drawLine(x, r.top, x, r.bottom, mPaint);
//		for (int y = r.top; y < r.bottom; y += 20)
//			c.drawLine(r.left, y, r.right, y, mPaint);

		/* Draw all Overlays. */
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			osmvo.onManagedDraw(c, this);

		if (this.mMaxiMap != null) { // If this is a MiniMap
			this.mPaint.setColor(Color.RED);
			this.mPaint.setStyle(Style.STROKE);
			final int viewWidth = this.getWidth();
			final int viewHeight = this.getHeight();
			c.drawRect(0, 0, viewWidth, viewHeight, this.mPaint);
		}

		final long endMs = System.currentTimeMillis();
		if (DEBUGMODE)
			logger.debug(DEBUGTAG, "Rendering overall: " + (endMs - startMs) + "ms");
		computeScale();
	}

	@Override
	protected void onDetachedFromWindow() {
		this.mZoomController.setVisible(false);
		this.mMapOverlay.detach();
		super.onDetachedFromWindow();
	}

	// ===========================================================
	// Package Methods
	// ===========================================================

	/**
	 * Get the world size in pixels.
	 */
	int getWorldSizePx() {
		return (1 << getPixelZoomLevel());
	}

	/**
	 * Get the equivalent zoom level on pixel scale
	 */
	int getPixelZoomLevel() {
		return this.mZoomLevel + this.mMapOverlay.getRendererInfo().MAPTILE_ZOOM;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void checkZoomButtons() {
		final int maxZoomLevel = this.mMapOverlay.getRendererInfo().ZOOM_MAXLEVEL;
		this.mZoomController.setZoomInEnabled(mZoomLevel < maxZoomLevel);
		this.mZoomController.setZoomOutEnabled(mZoomLevel > 0);
	}

	private int[] getCenterMapTileCoords() {
		final int mapTileZoom = this.mMapOverlay.getRendererInfo().MAPTILE_ZOOM;
		final int worldTiles_2 = 1 << (mZoomLevel-1);
		// convert to tile coordinate and make positive
		return new int[] {  (getScrollY() >> mapTileZoom) + worldTiles_2,
							(getScrollX() >> mapTileZoom) + worldTiles_2 };
	}

	/**
	 * @param centerMapTileCoords
	 * @param tileSizePx
	 * @param reuse
	 *            just pass null if you do not have a Point to be 'recycled'.
	 */
	private Point getUpperLeftCornerOfCenterMapTileInScreen(final int[] centerMapTileCoords,
			final int tileSizePx, final Point reuse) {
		final Point out = (reuse != null) ? reuse : new Point();

		final int worldTiles_2 = 1 << (mZoomLevel-1);
		final int centerMapTileScreenLeft = (centerMapTileCoords[MAPTILE_LONGITUDE_INDEX] - worldTiles_2) * tileSizePx - tileSizePx/2;
		final int centerMapTileScreenTop = (centerMapTileCoords[MAPTILE_LATITUDE_INDEX] - worldTiles_2) * tileSizePx - tileSizePx/2;

		out.set(centerMapTileScreenLeft, centerMapTileScreenTop);
		return out;
	}

	public void setBuiltInZoomControls(boolean on) {
		this.mEnableZoomController = on;
		this.checkZoomButtons();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	/**
	 * This class may return valid results until the underlying
	 * {@link OpenStreetMapView} gets modified in any way (i.e. new center).
	 *
	 * @author Nicolas Gramlich
	 * @author Manuel Stahl
	 */
	public class OpenStreetMapViewProjection {

		private final int viewWidth_2 = getWidth() / 2;
		private final int viewHeight_2 = getHeight() / 2;
		private final int worldSize_2 = getWorldSizePx()/2;
		private final int offsetX = - worldSize_2;
		private final int offsetY = - worldSize_2;

		private final BoundingBoxE6 bb;
		private final int zoomLevel;
		private final int tileSizePx;
		private final int[] centerMapTileCoords;
		private final Point upperLeftCornerOfCenterMapTile;

		private final int[] reuseInt2 = new int[2];

		public OpenStreetMapViewProjection() {

			/*
			 * Do some calculations and drag attributes to local variables to
			 * save some performance.
			 */
			zoomLevel = OpenStreetMapView.this.mZoomLevel; // TODO Draw to
															// attributes and so
															// make it only
															// 'valid' for a
															// short time.
			tileSizePx = getRenderer().MAPTILE_SIZEPX;

			/*
			 * Get the center MapTile which is above this.mLatitudeE6 and
			 * this.mLongitudeE6 .
			 */
			centerMapTileCoords = getCenterMapTileCoords();
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
			return bb.getGeoPointOfRelativePositionWithLinearInterpolation(x / viewWidth_2, y
					/ viewHeight_2);
		}

		public Point fromMapPixels(int x, int y, Point reuse) {
			final Point out = (reuse != null) ? reuse : new Point();
			out.set(x - viewWidth_2, y - viewHeight_2);
			out.offset(getScrollX(), getScrollY());
			return out;
		}

		private static final int EQUATORCIRCUMFENCE = 40075004;

		public float metersToEquatorPixels(final float aMeters) {
			return aMeters / EQUATORCIRCUMFENCE * getWorldSizePx();
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
		public Point toMapPixels(final GeoPoint in, final Point reuse) {
			final Point out = (reuse != null) ? reuse : new Point();

			final int[] coords = Mercator.projectGeoPoint(in.getLatitudeE6(), in.getLongitudeE6(), getPixelZoomLevel(), null);
			out.set(coords[MAPTILE_LONGITUDE_INDEX], coords[MAPTILE_LATITUDE_INDEX]);
			out.offset(offsetX, offsetY);
			return out;
		}

		/**
		 * Performs only the first computationally heavy part of the projection, needToCall toMapPixelsTranslated to get final position.
		 * @param latituteE6
		 * 			 the latitute of the point
		 * @param longitudeE6
		 * 			 the longitude of the point
		 * @param reuse
		 *            just pass null if you do not have a Point to be
		 *            'recycled'.
		 * @return intermediate value to be stored and passed to toMapPixelsTranslated on paint.
		 */
		public Point toMapPixelsProjected(final int latituteE6, final int longitudeE6, final Point reuse) {
			final Point out = (reuse != null) ? reuse : new Point();

			//26 is the biggest zoomlevel we can project
			final int[] coords = Mercator.projectGeoPoint(latituteE6, longitudeE6, 28, this.reuseInt2);
			out.set(coords[MAPTILE_LONGITUDE_INDEX], coords[MAPTILE_LATITUDE_INDEX]);
			return out;
		}

		/**
		 * Performs the second computationally light part of the projection.
		 * @param in
		 * 			 the Point calculated by the toMapPixelsProjected
		 * @param reuse
		 *            just pass null if you do not have a Point to be
		 *            'recycled'.
		 * @return the Point containing the approximated ScreenCoordinates of
		 *         the initial GeoPoint passed to the toMapPixelsProjected.
		 */
		public Point toMapPixelsTranslated(final Point in, final Point reuse) {
			final Point out = (reuse != null) ? reuse : new Point();

			//26 is the biggest zoomlevel we can project
			int zoomDifference = 28 - getPixelZoomLevel();
			out.set((in.x >> zoomDifference) + offsetX , (in.y >> zoomDifference) + offsetY );
			return out;
		}


		/**
		 * Translates a rectangle from screen coordinates to intermediate coordinates.
		 * @param in the rectangle in screen coordinates
		 * @return a rectangle in intermediate coords.
		 */
		public Rect fromPixelsToProjected(final Rect in)
		{
			Rect result = new Rect();

			//26 is the biggest zoomlevel we can project
			int zoomDifference = 28 - getPixelZoomLevel();

			int x0 = (in.left - offsetX) << zoomDifference;
			int x1 = (in.right - offsetX) << zoomDifference;
			int y0 = (in.bottom - offsetX) << zoomDifference;
			int y1 = (in.top - offsetX) << zoomDifference;

			result.set(Math.min(x0,x1), Math.min(y0,y1), Math.max(x0,x1), Math.max(y0,y1));
			return result;
		}

		public Point toPixels(final int[] tileCoords, final Point reuse) {
			return toPixels(tileCoords[MAPTILE_LONGITUDE_INDEX], tileCoords[MAPTILE_LATITUDE_INDEX], reuse);
		}

		public Point toPixels(int tileX, int tileY, final Point reuse) {
			final Point out = (reuse != null) ? reuse : new Point();

			out.set(tileX * tileSizePx, tileY * tileSizePx);
			out.offset(offsetX, offsetY);

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
				final int[] underGeopointTileCoords = Mercator.projectGeoPoint(gp
						.getLatitudeE6(), gp.getLongitudeE6(), zoomLevel, null);

				/*
				 * Calculate the Latitude/Longitude on the left-upper
				 * ScreenCoords of the MapTile.
				 */
				final BoundingBoxE6 bb = Mercator.getBoundingBoxFromMapTile(underGeopointTileCoords,
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
					out.moveTo(x, y);
//				out.moveTo(x + OpenStreetMapView.this.mTouchMapOffsetX, y
//						+ OpenStreetMapView.this.mTouchMapOffsetY);
				else
					out.lineTo(x, y);
				first = false;
			}

			return out;
		}
	}

	private class OpenStreetMapViewGestureDetectorListener implements OnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			mZoomController.setVisible(mEnableZoomController);
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			final int worldSize = getWorldSizePx();
			mScroller.fling(getScrollX(), getScrollY(), (int)-velocityX, (int)-velocityY, -worldSize, worldSize, -worldSize, worldSize);
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			OpenStreetMapView.this.onLongPress(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			scrollBy((int)distanceX, (int)distanceY);
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return OpenStreetMapView.this.onSingleTapUp(e);
		}

	}

	private class OpenStreetMapViewZoomListener implements OnZoomListener {
    	@Override
    	public void onZoom(boolean zoomIn) {
    		if(zoomIn)
				getController().zoomIn();
    		else
				getController().zoomOut();
    	}
    	@Override
    	public void onVisibilityChanged(boolean visible) {}
    }

	class Scaler {

	    private float mStartScale;
	    private float mFinalScale;
	    private float mCurrScale;

	    private long mStartTime;
	    private int mDuration;
	    private float mDurationReciprocal;
	    private float mDeltaScale;
	    private boolean mFinished;
	    private Interpolator mInterpolator;

	    /**
	     * Create a Scaler with the specified interpolator.
	     */
	    public Scaler(Context context, Interpolator interpolator) {
	        mFinished = true;
	        mInterpolator = interpolator;
	    }

	    /**
	     *
	     * Returns whether the scaler has finished scaling.
	     *
	     * @return True if the scaler has finished scaling, false otherwise.
	     */
	    public final boolean isFinished() {
	        return mFinished;
	    }

	    /**
	     * Force the finished field to a particular value.
	     *
	     * @param finished The new finished value.
	     */
	    public final void forceFinished(boolean finished) {
	        mFinished = finished;
	    }

	    /**
	     * Returns how long the scale event will take, in milliseconds.
	     *
	     * @return The duration of the scale in milliseconds.
	     */
	    public final int getDuration() {
	        return mDuration;
	    }

	    /**
	     * Returns the current scale factor.
	     *
	     * @return The new scale factor.
	     */
	    public final float getCurrScale() {
	        return mCurrScale;
	    }

	    /**
	     * Returns the start scale factor.
	     *
	     * @return The start scale factor.
	     */
	    public final float getStartScale() {
	        return mStartScale;
	    }

	    /**
	     * Returns where the scale will end.
	     *
	     * @return The final scale factor.
	     */
	    public final float getFinalScale() {
	        return mFinalScale;
	    }

	    /**
	     * Sets the final scale for this scaler.
	     *
	     * @param newScale The new scale factor.
	     */
	    public void setFinalScale(float newScale) {
			mFinalScale = newScale;
			mDeltaScale = mFinalScale - mStartScale;
	        mFinished = false;
	    }


		/**
	     * Call this when you want to know the new scale.  If it returns true,
	     * the animation is not yet finished.
	     */
	    public boolean computeScale() {
	        if (mFinished) {
	        	mCurrScale = 1.0f;
	            return false;
	        }

	        int timePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);

	        if (timePassed < mDuration) {
                float x = (float)timePassed * mDurationReciprocal;

                x = mInterpolator.getInterpolation(x);

                mCurrScale = mStartScale + x * mDeltaScale;
                if (mCurrScale == mFinalScale)
                    mFinished = true;

	        } else {
	            mCurrScale = mFinalScale;
	            mFinished = true;
	        }
	        return true;
	    }

	    /**
	     * Start scaling by providing the starting scale and the final scale.
	     *
	     * @param startX Starting horizontal scroll offset in pixels. Positive
	     *        numbers will scroll the content to the left.
	     * @param startY Starting vertical scroll offset in pixels. Positive numbers
	     *        will scroll the content up.
	     * @param dx Horizontal distance to travel. Positive numbers will scroll the
	     *        content to the left.
	     * @param dy Vertical distance to travel. Positive numbers will scroll the
	     *        content up.
	     * @param duration Duration of the scroll in milliseconds.
	     */
	    public void startScale(float startScale, float finalScale, int duration) {
	        mFinished = false;
	        mDuration = duration;
	        mStartTime = AnimationUtils.currentAnimationTimeMillis();
	        mStartScale = startScale;
	        mFinalScale = finalScale;
	        mDeltaScale = finalScale - startScale;
	        mDurationReciprocal = 1.0f / (float) mDuration;
	    }

	    /**
	     * Extend the scale animation. This allows a running animation to scale
	     * further and longer, when used with {@link #setFinalScale(float)}.
	     *
	     * @param extend Additional time to scale in milliseconds.
	     * @see #setFinalScale(float)
	     */
	    public void extendDuration(int extend) {
	        int passed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
	        mDuration = passed + extend;
	        mDurationReciprocal = 1.0f / (float)mDuration;
	        mFinished = false;
	    }

	}

}
