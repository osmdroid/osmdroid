package org.osmdroid.views;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.ZoomButtonsController;

import androidx.annotation.ColorInt;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import org.metalev.multitouch.controller.MultiTouchController;
import org.metalev.multitouch.controller.MultiTouchController.MultiTouchObjectCanvas;
import org.metalev.multitouch.controller.MultiTouchController.PointInfo;
import org.metalev.multitouch.controller.MultiTouchController.PositionAndScale;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.IStyledTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.GeometryMath;
import org.osmdroid.util.ReusablePool;
import org.osmdroid.util.TileSystem;
import org.osmdroid.util.TileSystemWebMercator;
import org.osmdroid.views.overlay.DefaultOverlayManager;
import org.osmdroid.views.overlay.IViewBoundingBoxChangedListener;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the primary view for osmdroid. <br><br>
 * As of version 6.0.0, please respect the android view lifecycle by calling
 * {@link MapView#onPause()} and {@link MapView#onResume()} respectively
 *
 * @author plusminus on 17:45:56 - 25.09.2008
 * @author and many other contributors
 * @since the begining
 */
public class MapView extends ViewGroup implements IMapView, MultiTouchObjectCanvas<Object>, LifecycleOwner {

    /**
     * Current zoom level for map tiles.
     */
    private double mZoomLevel = 0;

    private OverlayManager mOverlayManager;

    protected Projection mProjection;

    private TilesOverlay mMapOverlay;

    private final GestureDetector mGestureDetector;

    /**
     * Handles map scrolling
     */
    private final Scroller mScroller;
    protected boolean mIsFlinging;
    /**
     * Set to true when the `Projection` actually adjusted the scroll values
     * Consequence: on this side effect, we must stop the flinging
     */
    private boolean mImpossibleFlinging;

    protected final AtomicBoolean mIsAnimating = new AtomicBoolean(false);

    protected Double mMinimumZoomLevel;
    protected Double mMaximumZoomLevel;

    private final MapController mController;

    private final CustomZoomButtonsController mZoomController;

    private MultiTouchController<Object> mMultiTouchController;

    /**
     * Initial pinch gesture pixel (typically, the middle of both fingers)
     */
    private final PointF mMultiTouchScaleInitPoint = new PointF();
    /**
     * Initial pinch gesture geo point, computed from {@link MapView#mMultiTouchScaleInitPoint}
     * and the current Projection
     */
    private final GeoPoint mMultiTouchScaleGeoPoint = new GeoPoint(0., 0);
    /**
     * Current pinch gesture pixel (again, the middle of both fingers)
     * We must ensure that this pixel is the projection of {@link MapView#mMultiTouchScaleGeoPoint}
     */
    private final PointF mMultiTouchScaleCurrentPoint = new PointF();
    private boolean mHasMultiTouchScaleCurrentPoint = false;

    // For rotation
    private float mapOrientation = 0;
    private final Rect mInvalidateRect = new Rect();

    private boolean mScrollableAreaLimitLatitude;
    private double mScrollableAreaLimitNorth;
    private double mScrollableAreaLimitSouth;
    private boolean mScrollableAreaLimitLongitude;
    private double mScrollableAreaLimitWest;
    private double mScrollableAreaLimitEast;
    private int mScrollableAreaLimitExtraPixelWidth;
    private int mScrollableAreaLimitExtraPixelHeight;

    private MapTileProviderBase mTileProvider;
    private Handler mTileRequestCompleteHandler;
    private boolean mTilesScaledToDpi = false;
    private float mTilesScaleFactor = 1f;

    final Point mRotateScalePoint = new Point();

    /* a point that will be reused to lay out added views */
    private final Point mLayoutPoint = new Point();

    // Keep a set of listeners for when the maps have a layout
    private final LinkedList<OnFirstLayoutListener> mOnFirstLayoutListeners = new LinkedList<MapView.OnFirstLayoutListener>();

    /* becomes true once onLayout has been called for the first time i.e. map is ready to go. */
    private boolean mLayoutOccurred = false;

    private boolean horizontalMapRepetitionEnabled = true;
    private boolean verticalMapRepetitionEnabled = true;

    private double mCenterLat;
    private double mCenterLon;
    private long mMapScrollX;
    private long mMapScrollY;
    protected final List<MapListener> mListeners = new ArrayList<>();

    private double mStartAnimationZoom;

    private boolean mZoomRounding;

    /**
     * @since 6.0.3
     */
    private final MapViewRepository mRepository = new MapViewRepository(this);

    private final ReusablePool<ScrollEvent> mScrollEventsCache = new ReusablePool<>(new ReusablePool.IReusablePoolItemCallback<ScrollEvent>() {
        @Override
        public ScrollEvent newInstance() { return ScrollEvent.newInstanceForReusablePool(); }
    }, 2);

    private final LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);

    public interface OnFirstLayoutListener {
        /**
         * this generally means that the map is ready to go
         */
        @UiThread @MainThread
        void onFirstLayout(View v, int left, int top, int right, int bottom);
    }

    private static TileSystem mTileSystem = new TileSystemWebMercator();

    /**
     * @since 6.1.0
     */
    private final Rect mRescaleScreenRect = new Rect(); // optimization

    /**
     * @since 6.1.1
     * The map center used to be projected into the screen center.
     * Now we have a possible offset from the screen center; default offset is [0, 0].
     */
    private int mMapCenterOffsetX;
    private int mMapCenterOffsetY;

    private final IViewBoundingBoxChangedListener mViewBoundingBoxChangedListener = new IViewBoundingBoxChangedListener() {
        @UiThread @MainThread
        @Override
        public void onViewBoundingBoxChanged(@NonNull final Rect fromBounds, final int fromZoom, @NonNull final Rect toBounds, final int toZoom) {
            for (final MapListener cMapListener : mListeners)
                cMapListener.onViewBoundingBoxChanged(fromBounds, fromZoom, toBounds, toZoom);
        }
    };

    private final Point mSnapPoint = new Point();

    private final GeoPoint mReusableForZoomToBoundingBox = new GeoPoint(0d, 0d);
    private final ZoomEvent mReusableZoomEvent = ZoomEvent.newInstanceForReusablePool();

    // ===========================================================
    // Constructors
    // ===========================================================

    public MapView(final Context context,
                   MapTileProviderBase tileProvider,
                   final Handler tileRequestCompleteHandler, final AttributeSet attrs) {
        this(context, tileProvider, tileRequestCompleteHandler, attrs, Configuration.getInstance().isMapViewHardwareAccelerated());

    }

    public MapView(final Context context,
                   MapTileProviderBase tileProvider,
                   final Handler tileRequestCompleteHandler, final AttributeSet attrs, boolean hardwareAccelerated) {
        super(context, attrs);

        // Hacky workaround: If no storage location was set manually, we need to try to be
        // the first to give DefaultConfigurationProvider a chance to detect the best storage
        // location WITH a context. Otherwise there will be no valid cache directory on >API29!
        Configuration.getInstance().getOsmdroidTileCache(context);

        if (isInEditMode()) {    //fix for edit mode in the IDE
            mTileRequestCompleteHandler = null;
            mController = null;
            mZoomController = null;
            mScroller = null;
            mGestureDetector = null;
            return;
        }
        if (!hardwareAccelerated && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        if (context instanceof LifecycleOwner) {
            ((LifecycleOwner)context).getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override public void onCreate(@NonNull final LifecycleOwner owner) { mLifecycleRegistry.setCurrentState(owner.getLifecycle().getCurrentState()); }
                @Override public void onStart(@NonNull final LifecycleOwner owner) { mLifecycleRegistry.setCurrentState(owner.getLifecycle().getCurrentState()); }
                @Override public void onResume(@NonNull final LifecycleOwner owner) {
                    mLifecycleRegistry.setCurrentState(owner.getLifecycle().getCurrentState());
                    MapView.this.onResume();
                }
                @Override
                public void onPause(@NonNull final LifecycleOwner owner) {
                    MapView.this.onPause();
                    mLifecycleRegistry.setCurrentState(owner.getLifecycle().getCurrentState());
                }
                @Override
                public void onStop(@NonNull final LifecycleOwner owner) { mLifecycleRegistry.setCurrentState(owner.getLifecycle().getCurrentState()); }
                @Override
                public void onDestroy(@NonNull final LifecycleOwner owner) {
                    mTileProvider.detach();
                    if (mZoomController != null) {
                        mZoomController.onDetach();
                    }

                    //https://github.com/osmdroid/osmdroid/issues/390
                    if (mTileRequestCompleteHandler instanceof SimpleInvalidationHandler) {
                        ((SimpleInvalidationHandler) mTileRequestCompleteHandler).destroy();
                    }
                    mTileRequestCompleteHandler = null;
                    if (mProjection != null)
                        mProjection.detach();
                    mProjection = null;
                    mRepository.onDetach();
                    mListeners.clear();

                    mLifecycleRegistry.setCurrentState(owner.getLifecycle().getCurrentState());
                    MapView.this.onDestroy();
                }
            });
        } else if ((context instanceof android.app.Activity) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
            ((android.app.Activity)context).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override public void onActivityCreated(@NonNull final Activity activity, @Nullable final Bundle savedInstanceState) { mLifecycleRegistry.setCurrentState(Lifecycle.State.CREATED); }
                @Override public void onActivityStarted(@NonNull final Activity activity) { mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED); }
                @Override public void onActivityResumed(@NonNull final Activity activity) { mLifecycleRegistry.setCurrentState(Lifecycle.State.RESUMED); }
                @Override public void onActivityPaused(@NonNull final Activity activity) { mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED); }
                @Override public void onActivityStopped(@NonNull final Activity activity) { mLifecycleRegistry.setCurrentState(Lifecycle.State.CREATED); }
                @Override public void onActivitySaveInstanceState(@NonNull final Activity activity, @NonNull final Bundle outState) { /*nothing*/ }
                @Override public void onActivityDestroyed(@NonNull final Activity activity) { mLifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED); }
            });
        } else {
            throw new IllegalStateException("MapView's context needs to implement some kind of Lifecycle");
        }

        this.mController = new MapController(this);
        this.mScroller = new Scroller(context);

        if (tileProvider == null) {
            final ITileSource tileSource = getTileSourceFromAttributes(attrs);
            tileProvider = new MapTileProviderBasic(context.getApplicationContext(), tileSource);
        }

        mTileRequestCompleteHandler = tileRequestCompleteHandler == null
                ? new SimpleInvalidationHandler(this)
                : tileRequestCompleteHandler;
        mTileProvider = tileProvider;
        mTileProvider.getTileRequestCompleteHandlers().add(mTileRequestCompleteHandler);
        updateTileSizeForDensity(mTileProvider.getTileSource());

        this.mMapOverlay = new TilesOverlay(mTileProvider, context, horizontalMapRepetitionEnabled, verticalMapRepetitionEnabled);
        mOverlayManager = new DefaultOverlayManager(mMapOverlay);
        mOverlayManager.setMapViewLifecycle(this);
        mOverlayManager.setViewBoundingBoxChangedListener(mViewBoundingBoxChangedListener);

        mZoomController = new CustomZoomButtonsController(this);
        mZoomController.setOnZoomListener(new MapViewZoomListener());
        checkZoomButtons();

        mGestureDetector = new GestureDetector(context, new MapViewGestureDetectorListener());
        mGestureDetector.setOnDoubleTapListener(new MapViewDoubleClickListener());

		/*
		fix for map in recycler views
		see https://github.com/osmdroid/osmdroid/issues/588
		https://github.com/osmdroid/osmdroid/issues/568
		 */
        if (Configuration.getInstance().isMapViewRecyclerFriendly())
            if (Build.VERSION.SDK_INT >= 16)
                this.setHasTransientState(true);

        mZoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
    }

    /**
     * Constructor used by XML layout resource (uses default tile source).
     */
    public MapView(final Context context, final AttributeSet attrs) {
        this(context, null, null, attrs);
    }

    public MapView(final Context context) {
        this(context, null, null, null);
    }


    public MapView(final Context context,
                   final MapTileProviderBase aTileProvider) {
        this(context, aTileProvider, null);
    }

    public MapView(final Context context,
                   final MapTileProviderBase aTileProvider,
                   final Handler tileRequestCompleteHandler) {
        this(context, aTileProvider, tileRequestCompleteHandler, null);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    @NonNull
    @Override
    public Lifecycle getLifecycle() { return mLifecycleRegistry; }

    @Override
    public IMapController getController() {
        return this.mController;
    }

    /** You can add/remove/reorder your Overlays using the List of {@link Overlay}. The first (index * 0) Overlay gets drawn first, the one with the highest as the last one. */
    public List<Overlay> getOverlays() {
        return this.getOverlayManager().overlays();
    }

    public OverlayManager getOverlayManager() {
        return mOverlayManager;
    }

    public void setOverlayManager(@NonNull final OverlayManager overlayManager) {
        if (Objects.equals(overlayManager, mOverlayManager)) return;
        mOverlayManager.onDestroyInternal();
        mOverlayManager = overlayManager;
        mOverlayManager.setMapViewLifecycle(this);
        mOverlayManager.setViewBoundingBoxChangedListener(mViewBoundingBoxChangedListener);
    }

    public MapTileProviderBase getTileProvider() {
        return mTileProvider;
    }

    public Scroller getScroller() {
        return mScroller;
    }

    public Handler getTileRequestCompleteHandler() {
        return mTileRequestCompleteHandler;
    }

    public void setTileRequestCompleteHandler(@Nullable final Handler handler) {
        final Collection<Handler> cHandlers = mTileProvider.getTileRequestCompleteHandlers();
        cHandlers.clear();
        cHandlers.add(mTileRequestCompleteHandler = handler);
    }

    @Override
    public double getLatitudeSpanDouble() {
        return this.getBoundingBox().getLatitudeSpan();
    }

    @Override
    public double getLongitudeSpanDouble() {
        return this.getBoundingBox().getLongitudeSpan();
    }

    public BoundingBox getBoundingBox() {
        return getProjection().getBoundingBox();
    }


    /**
     * Gets the current bounds of the screen in <I>screen coordinates</I>.
     */
    public Rect getScreenRect(@Nullable final Rect reuse) {
        final Rect out = getIntrinsicScreenRect(reuse);
        if (this.getMapOrientation() != 0 && this.getMapOrientation() != 180) {
            GeometryMath.getBoundingBoxForRotatatedRectangle(out, out.centerX(), out.centerY(),
                    this.getMapOrientation(), out);
        }
        return out;
    }

    public Rect getIntrinsicScreenRect(@Nullable final Rect reuse) {
        final Rect out = reuse == null ? new Rect() : reuse;
        out.set(0, 0, getWidth(), getHeight());
        return out;
    }

    /**
     * Get a projection for converting between screen-pixel coordinates and latitude/longitude
     * coordinates. You should not hold on to this object for more than one draw, since the
     * projection of the map could change.
     *
     * @return The Projection of the map in its current state. You should not hold on to this object
     * for more than one draw, since the projection of the map could change.
     */
    @Override
    public Projection getProjection() {
        if (mProjection == null) {
            Projection localCopy = new Projection(this);
            mProjection = localCopy;
            localCopy.adjustOffsets(mMultiTouchScaleGeoPoint, (mHasMultiTouchScaleCurrentPoint ? mMultiTouchScaleCurrentPoint : null));
            if (mScrollableAreaLimitLatitude) {
                localCopy.adjustOffsets(
                        mScrollableAreaLimitNorth, mScrollableAreaLimitSouth, true,
                        mScrollableAreaLimitExtraPixelHeight);
            }
            if (mScrollableAreaLimitLongitude) {
                localCopy.adjustOffsets(
                        mScrollableAreaLimitWest, mScrollableAreaLimitEast, false,
                        mScrollableAreaLimitExtraPixelWidth);
            }
            mImpossibleFlinging = localCopy.setMapScroll(this);
        }
        return mProjection;
    }

    /**
     * Use {@link #resetProjection()} instead
     */
    @Deprecated
    protected void setProjection(@NonNull final Projection p) {
        mProjection = p;
    }

    private void resetProjection() {
        mProjection = null;
    }

    /**
     * @deprecated use {@link IMapController#animateTo(IGeoPoint)} or {@link IMapController#setCenter(IGeoPoint)} instead
     */
    @Deprecated
    void setMapCenter(@NonNull final IGeoPoint aCenter) {
        getController().animateTo(aCenter);
    }

    /**
     * @deprecated use {@link #setMapCenter(IGeoPoint)}
     */
    @Deprecated
    void setMapCenter(final int aLatitudeE6, final int aLongitudeE6) {
        setMapCenter(new GeoPoint(aLatitudeE6, aLongitudeE6));
    }

    @Deprecated
    void setMapCenter(final double aLatitude, final double aLongitude) {
        setMapCenter(new GeoPoint(aLatitude, aLongitude));
    }

    public boolean isTilesScaledToDpi() {
        return mTilesScaledToDpi;
    }

    /**
     * if true, tiles are scaled to the current DPI of the display. This effectively makes it easier to read labels, how it may appear pixelated depending on the map source.<br>
     * if false, tiles are rendered in their real size
     */
    public void setTilesScaledToDpi(boolean tilesScaledToDpi) {
        mTilesScaledToDpi = tilesScaledToDpi;
        updateTileSizeForDensity(getTileProvider().getTileSource());
    }

    public float getTilesScaleFactor() {
        return mTilesScaleFactor;
    }

    /**
     * Setting an additional scale factor both for ScaledToDpi and standard size
     * > 1.0 enlarges map display, < 1.0 shrinks map display
     *
     * @since 6.1.0
     */
    public void setTilesScaleFactor(float pTilesScaleFactor) {
        mTilesScaleFactor = pTilesScaleFactor;
        updateTileSizeForDensity(getTileProvider().getTileSource());
    }

    public void resetTilesScaleFactor() {
        mTilesScaleFactor = 1f;
        updateTileSizeForDensity(getTileProvider().getTileSource());
    }

    private void updateTileSizeForDensity(@NonNull final ITileSource aTileSource) {
        int tile_size = aTileSource.getTileSizePixels();
        float density = getResources().getDisplayMetrics().density * 256 / tile_size;
        int size = (int) (tile_size * (isTilesScaledToDpi() ? density * mTilesScaleFactor : mTilesScaleFactor));
        if (Configuration.getInstance().isDebugMapView())
            Log.d(IMapView.LOGTAG, "Scaling tiles to " + size);
        TileSystem.setTileSize(size);
    }

    public void setTileSource(@NonNull final ITileSource aTileSource) {
        mTileProvider.setTileSource(aTileSource);
        updateTileSizeForDensity(aTileSource);
        this.checkZoomButtons();
        this.setZoomLevel(mZoomLevel); // revalidate zoom level
        postInvalidate();
    }

    /**
     * @param aZoomLevel the zoom level bound by the tile source
     *                   Used to be an int - is a double since 6.0
     */
    double setZoomLevel(final double aZoomLevel) {
        final double newZoomLevel = Math.max(getMinZoomLevel(), Math.min(getMaxZoomLevel(), aZoomLevel));
        final double curZoomLevel = this.mZoomLevel;

        if (newZoomLevel != curZoomLevel) {
            if (mScroller != null)    //fix for edit mode in the IDE
                mScroller.forceFinished(true);
            mIsFlinging = false;
        }

        // Get our current center point
        final IGeoPoint centerGeoPoint = getProjection().getCurrentCenter();

        this.mZoomLevel = newZoomLevel;

        setExpectedCenter(centerGeoPoint);
        this.checkZoomButtons();

        if (isLayoutOccurred()) {
            getController().setCenter(centerGeoPoint);

            // snap for all snappables
            final Projection pj = getProjection();
            if (this.getOverlayManager().onSnapToItem((int) mMultiTouchScaleInitPoint.x,
                    (int) mMultiTouchScaleInitPoint.y, mSnapPoint, this)) {
                IGeoPoint geoPoint = pj.fromPixels(mSnapPoint.x, mSnapPoint.y, null, false);
                getController().animateTo(geoPoint);
            }

            mTileProvider.rescaleCache(pj, newZoomLevel, curZoomLevel, getScreenRect(mRescaleScreenRect));
            pauseFling = true;    // issue 269, pause fling during zoom changes
        }

        // do callback on listener
        if (newZoomLevel != curZoomLevel) {
            mReusableZoomEvent.set(this, newZoomLevel);
            for (MapListener mapListener : mListeners)
                mapListener.onZoom(mReusableZoomEvent);
        }

        requestLayout(); // Allows any views fixed to a Location in the MapView to adjust
        invalidate();
        return this.mZoomLevel;
    }

    /**
     * Zoom the map to enclose the specified bounding box, as closely as possible. Must be called
     * after display layout is complete, or screen dimensions are not known, and will always zoom to
     * center of zoom level 0.<br>
     * Suggestion: Check getIntrinsicScreenRect(null).getHeight() &gt; 0
     */
    public void zoomToBoundingBox(@NonNull final BoundingBox boundingBox, final boolean animated) {
        zoomToBoundingBox(boundingBox, animated, 0);
    }

    /**
     * @param pBoundingBox        Bounding box we want to zoom to; may be a single {@link GeoPoint}
     * @param pAnimated           Animation or immediate action?
     * @param pBorderSizeInPixels Border size around the bounding box
     * @param pMaximumZoom        Maximum zoom we want from bounding box computation
     * @param pAnimationSpeed     Animation duration, in milliseconds
     * @since 6.0.3
     */
    public double zoomToBoundingBox(@NonNull final BoundingBox pBoundingBox, final boolean pAnimated,
                                    final int pBorderSizeInPixels, final double pMaximumZoom,
                                    @Nullable final Long pAnimationSpeed) {
        return zoomToBoundingBox(pBoundingBox, pAnimated, pBorderSizeInPixels, pBorderSizeInPixels, pMaximumZoom, pAnimationSpeed);
    }

    /**
     * @param pBoundingBox                  Bounding box we want to zoom to; may be a single {@link GeoPoint}
     * @param pAnimated                     Animation or immediate action?
     * @param pHorizontalBorderSizeInPixels Border size around the bounding box (horizontally)
     * @param pVerticalBorderSizeInPixels   Border size around the bounding box (vertically)
     * @param pMaximumZoom                  Maximum zoom we want from bounding box computation
     * @param pAnimationSpeed               Animation duration, in milliseconds
     * @since 6.1.18
     */
    public double zoomToBoundingBox(@NonNull final BoundingBox pBoundingBox, final boolean pAnimated,
                                    final int pHorizontalBorderSizeInPixels, final int pVerticalBorderSizeInPixels, final double pMaximumZoom,
                                    @Nullable final Long pAnimationSpeed) {
        double nextZoom = mTileSystem.getBoundingBoxZoom(pBoundingBox, getWidth() - 2 * pHorizontalBorderSizeInPixels, getHeight() - 2 * pVerticalBorderSizeInPixels);
        if (nextZoom == Double.MIN_VALUE // e.g. single point bounding box
                || nextZoom > pMaximumZoom) { // e.g. tiny bounding box
            nextZoom = pMaximumZoom;
        }
        nextZoom = Math.min(getMaxZoomLevel(), Math.max(nextZoom, getMinZoomLevel()));
        double centerLat = pBoundingBox.getCenterLatWithDateLine();
        double centerLon = pBoundingBox.getCenterLonWithDateLine();

        // fine-tuning the latitude, cf. https://github.com/osmdroid/osmdroid/issues/1239
        final Projection projection = new Projection(
                nextZoom, getWidth(), getHeight(),
                centerLat, centerLon,
                getMapOrientation(),
                isHorizontalMapRepetitionEnabled(), isVerticalMapRepetitionEnabled(),
                getMapCenterOffsetX(), getMapCenterOffsetY());
        final Point point = new Point();
        final double longitude = pBoundingBox.getCenterLongitude();
        projection.toPixels(new GeoPoint(pBoundingBox.getActualNorth(), longitude), point);
        final int north = point.y;
        projection.toPixels(new GeoPoint(pBoundingBox.getActualSouth(), longitude), point);
        final int south = point.y;
        final int offset = ((getHeight() - south) - north) / 2;
        if (offset != 0) {
            projection.adjustOffsets(0, offset);
            mReusableForZoomToBoundingBox.setCoords(centerLat, centerLon);
            projection.fromPixels(getWidth() / 2, getHeight() / 2, mReusableForZoomToBoundingBox);
            centerLat = mReusableForZoomToBoundingBox.getLatitude();
            centerLon = mReusableForZoomToBoundingBox.getLongitude();
        }

        if (pAnimated) {
            getController().animateTo(centerLat, centerLon, nextZoom, pAnimationSpeed);
        } else { // it's best to set the zoom first, so that the center is accurate
            getController().setZoom(nextZoom);
            getController().setCenter(centerLat, centerLon);
        }
        return nextZoom;
    }

    /**
     * @since 6.0.0
     */
    public void zoomToBoundingBox(@NonNull final BoundingBox pBoundingBox, final boolean pAnimated, final int pBorderSizeInPixels) {
        zoomToBoundingBox(pBoundingBox, pAnimated, pBorderSizeInPixels, getMaxZoomLevel(), null);
    }

    /**
     * Get the current ZoomLevel for the map tiles.
     *
     * @return the current ZoomLevel between 0 (equator) and 18/19(closest), depending on the tile
     * source chosen.
     */
    @Deprecated
    @Override
    public int getZoomLevel() {
        return (int) getZoomLevelDouble();
    }

    /**
     * @since 6.0
     */
    @Override
    public double getZoomLevelDouble() {
        return mZoomLevel;
    }

    /**
     * Get the current ZoomLevel for the map tiles.
     *
     * @param aPending if true and we're animating then return the zoom level that we're animating
     *                 towards, otherwise return the current zoom level
     *                 Used to be an int - is a double since 6.0
     * @return the zoom level
     */
    @Deprecated
    public double getZoomLevel(final boolean aPending) {
        return getZoomLevelDouble();
    }

    /**
     * Get the minimum allowed zoom level for the maps.
     */
    public double getMinZoomLevel() {
        return mMinimumZoomLevel == null ? mMapOverlay.getMinimumZoomLevel() : mMinimumZoomLevel;
    }

    /**
     * Get the maximum allowed zoom level for the maps.
     */
    @Override
    public double getMaxZoomLevel() {
        return mMaximumZoomLevel == null ? mMapOverlay.getMaximumZoomLevel() : mMaximumZoomLevel;
    }

    /**
     * Set the minimum allowed zoom level, or pass null to use the minimum zoom level from the tile
     * provider.
     */
    public void setMinZoomLevel(@Nullable final Double zoomLevel) {
        mMinimumZoomLevel = zoomLevel;
    }

    /**
     * Set the maximum allowed zoom level, or pass null to use the maximum zoom level from the tile
     * provider.
     */
    public void setMaxZoomLevel(@Nullable final Double zoomLevel) {
        mMaximumZoomLevel = zoomLevel;
    }

    public boolean canZoomIn() {
        return mZoomLevel < getMaxZoomLevel();
    }

    public boolean canZoomOut() {
        return mZoomLevel > getMinZoomLevel();
    }

    /**
     * Zoom in by one zoom level.
     * Use {@link MapController#zoomIn()}} instead
     */
    @Deprecated
    boolean zoomIn() {
        return getController().zoomIn();
    }

    @Deprecated
    boolean zoomInFixing(@NonNull final IGeoPoint point) {
        Point coords = getProjection().toPixels(point, null);
        return getController().zoomInFixing(coords.x, coords.y);
    }

    @Deprecated
    boolean zoomInFixing(final int xPixel, final int yPixel) {
        return getController().zoomInFixing(xPixel, yPixel);
    }

    /**
     * Zoom out by one zoom level.
     * Use {@link MapController#zoomOut()} instead
     */
    @Deprecated
    boolean zoomOut() {
        return getController().zoomOut();
    }

    @Deprecated
    boolean zoomOutFixing(@NonNull final IGeoPoint point) {
        Point coords = getProjection().toPixels(point, null);
        return zoomOutFixing(coords.x, coords.y);
    }

    @Deprecated
    boolean zoomOutFixing(final int xPixel, final int yPixel) {
        return getController().zoomOutFixing(xPixel, yPixel);
    }

    /**
     * Returns the current center-point position of the map, as a GeoPoint (latitude and longitude).
     * <br><br>
     * Gives you the actual current map center, as the Projection computes it from the middle of
     * the screen. Most of the time it's supposed to be approximately the same value (because
     * of computing rounding side effects), but in some cases (current zoom gesture or scroll
     * limits) the values may differ (typically, when {@link Projection#adjustOffsets} had to fine-tune
     * the map center).
     *
     * @return A GeoPoint of the map's center-point.
     * @see #getExpectedCenter()
     */
    @Override
    public IGeoPoint getMapCenter() {
        return getMapCenter(null);
    }

    /**
     * @since 6.0.3
     */
    public IGeoPoint getMapCenter(@Nullable final GeoPoint pReuse) {
        return getProjection().fromPixels(getWidth() / 2, getHeight() / 2, pReuse, false);
    }

    /**
     * rotates the map to the desired heading
     *
     * @param degrees
     */
    public void setMapOrientation(float degrees) {
        setMapOrientation(degrees, true);
    }

    /**
     * There are some cases when we don't need explicit redraw
     *
     * @since 6.0.0
     */
    public void setMapOrientation(final float degrees, final boolean forceRedraw) {
        mapOrientation = degrees % 360.0f;
        if (forceRedraw) {
            requestLayout(); // Allows any views fixed to a Location in the MapView to adjust
            invalidate();
        }
    }

    public float getMapOrientation() {
        return mapOrientation;
    }

    /**
     * @since 6.0.0
     */
    @Deprecated
    public float getMapScale() {
        return 1;
    }

    /**
     * Whether to use the network connection if it's available.
     */
    public boolean useDataConnection() {
        return mMapOverlay.useDataConnection();
    }

    /**
     * Set whether to use the network connection if it's available.
     *
     * @param aMode if true use the network connection if it's available. if false don't use the
     *              network connection even if it's available.
     */
    public void setUseDataConnection(final boolean aMode) {
        mMapOverlay.setUseDataConnection(aMode);
    }

    /**
     * Set the map to limit it's scrollable view to the specified BoundingBox. Note this does not
     * limit zooming so it will be possible for the user to zoom out to an area that is larger than the
     * limited area.
     *
     * @param boundingBox A lat/long bounding box to limit scrolling to, or null to remove any scrolling
     *                    limitations
     */
    public void setScrollableAreaLimitDouble(@Nullable final BoundingBox boundingBox) {
        if (boundingBox == null) {
            resetScrollableAreaLimitLatitude();
            resetScrollableAreaLimitLongitude();
        } else {
            setScrollableAreaLimitLatitude(boundingBox.getActualNorth(), boundingBox.getActualSouth(), 0);
            setScrollableAreaLimitLongitude(boundingBox.getLonWest(), boundingBox.getLonEast(), 0);
        }
    }

    /**
     * @since 6.0.0
     */
    public void resetScrollableAreaLimitLatitude() {
        mScrollableAreaLimitLatitude = false;
    }

    /**
     * @since 6.0.0
     */
    public void resetScrollableAreaLimitLongitude() {
        mScrollableAreaLimitLongitude = false;
    }

    /**
     * sets the scroll limit
     * Example:
     * To block vertical scroll of the view outside north/south poles:
     * mapView.setScrollableAreaLimitLatitude(MapView.getTileSystem().getMaxLatitude(),
     * MapView.getTileSystem().getMinLatitude(),
     * 0);
     * Warning:
     * Don't use latitude values outside the [MapView.getTileSystem().getMinLatitude(),
     * MapView.getTileSystem().getMaxLatitude()] range, this would cause an ANR.
     *
     * @param pNorth            decimal degrees latitude
     * @param pSouth            decimal degrees latitude
     * @param pExtraPixelHeight in pixels, enables scrolling this many pixels past the bounds
     * @since 6.0.0
     */
    public void setScrollableAreaLimitLatitude(final double pNorth, final double pSouth, final int pExtraPixelHeight) {
        mScrollableAreaLimitLatitude = true;
        mScrollableAreaLimitNorth = pNorth;
        mScrollableAreaLimitSouth = pSouth;
        mScrollableAreaLimitExtraPixelHeight = pExtraPixelHeight;
    }

    /**
     * sets the scroll limit
     *
     * @param pWest            decimal degrees longitude
     * @param pEast            decimal degrees longitude
     * @param pExtraPixelWidth in pixels, enables scrolling this many pixels past the bounds
     * @since 6.0.0
     */
    public void setScrollableAreaLimitLongitude(final double pWest, final double pEast, final int pExtraPixelWidth) {
        mScrollableAreaLimitLongitude = true;
        mScrollableAreaLimitWest = pWest;
        mScrollableAreaLimitEast = pEast;
        mScrollableAreaLimitExtraPixelWidth = pExtraPixelWidth;
    }

    /**
     * @since 6.0.0
     */
    public boolean isScrollableAreaLimitLatitude() {
        return mScrollableAreaLimitLatitude;
    }

    /**
     * @since 6.0.0
     */
    public boolean isScrollableAreaLimitLongitude() {
        return mScrollableAreaLimitLongitude;
    }


    public void invalidateMapCoordinates(@NonNull final Rect dirty) {
        invalidateMapCoordinates(dirty.left, dirty.top, dirty.right, dirty.bottom, false);
    }

    public void invalidateMapCoordinates(int left, int top, int right, int bottom) {
        invalidateMapCoordinates(left, top, right, bottom, false);
    }

    public void postInvalidateMapCoordinates(int left, int top, int right, int bottom) {
        invalidateMapCoordinates(left, top, right, bottom, true);
    }

    private void invalidateMapCoordinates(int left, int top, int right, int bottom, boolean post) {
        mInvalidateRect.set(left, top, right, bottom);

        final int centerX = getWidth() / 2;
        final int centerY = getHeight() / 2;

        if (this.getMapOrientation() != 0)
            GeometryMath.getBoundingBoxForRotatatedRectangle(mInvalidateRect, centerX, centerY,
                    this.getMapOrientation() + 180, mInvalidateRect);

        if (post)
            super.postInvalidate(mInvalidateRect.left, mInvalidateRect.top, mInvalidateRect.right,
                    mInvalidateRect.bottom);
        else
            super.invalidate(mInvalidateRect);
    }

    /**
     * Returns a set of layout parameters with a width of
     * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}, a height of
     * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT} at the {@link GeoPoint} (0, 0) align
     * with {@link MapView.LayoutParams#BOTTOM_CENTER}.
     */
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, null, null, MapView.LayoutParams.BOTTOM_CENTER, 0, 0);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(final AttributeSet attrs) {
        return new MapView.LayoutParams(getContext(), attrs);
    }

    // Override to allow type-checking of LayoutParams.
    @Override
    protected boolean checkLayoutParams(final ViewGroup.LayoutParams p) {
        return p instanceof MapView.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(final ViewGroup.LayoutParams p) {
        return new MapView.LayoutParams(p);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // Get the children to measure themselves so we know their size in onLayout()
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @UiThread @MainThread
    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        myOnLayout(changed, l, t, r, b);
    }

    /**
     * Code was moved from {@link #onLayout(boolean, int, int, int, int)}
     * in order to avoid Android Studio warnings on direct calls
     *
     * @since 6.0.0
     */
    @UiThread @MainThread
    protected void myOnLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        resetProjection();
        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                final MapView.LayoutParams lp = (MapView.LayoutParams) child.getLayoutParams();
                final int childHeight = child.getMeasuredHeight();
                final int childWidth = child.getMeasuredWidth();
                getProjection().toPixels(lp.geoPointLat, lp.geoPointLon, mLayoutPoint);
                // Apply rotation of mLayoutPoint around the center of the map
                if (getMapOrientation() != 0) {
                    Point p = getProjection().rotateAndScalePoint(mLayoutPoint.x, mLayoutPoint.y,
                            null);
                    mLayoutPoint.x = p.x;
                    mLayoutPoint.y = p.y;
                }
                final long x = mLayoutPoint.x;
                final long y = mLayoutPoint.y;
                long childLeft = x;
                long childTop = y;
                switch (lp.alignment) {
                    case MapView.LayoutParams.TOP_LEFT:
                        childLeft = getPaddingLeft() + x;
                        childTop = getPaddingTop() + y;
                        break;
                    case MapView.LayoutParams.TOP_CENTER:
                        childLeft = getPaddingLeft() + x - childWidth / 2;
                        childTop = getPaddingTop() + y;
                        break;
                    case MapView.LayoutParams.TOP_RIGHT:
                        childLeft = getPaddingLeft() + x - childWidth;
                        childTop = getPaddingTop() + y;
                        break;
                    case MapView.LayoutParams.CENTER_LEFT:
                        childLeft = getPaddingLeft() + x;
                        childTop = getPaddingTop() + y - childHeight / 2;
                        break;
                    case MapView.LayoutParams.CENTER:
                        childLeft = getPaddingLeft() + x - childWidth / 2;
                        childTop = getPaddingTop() + y - childHeight / 2;
                        break;
                    case MapView.LayoutParams.CENTER_RIGHT:
                        childLeft = getPaddingLeft() + x - childWidth;
                        childTop = getPaddingTop() + y - childHeight / 2;
                        break;
                    case MapView.LayoutParams.BOTTOM_LEFT:
                        childLeft = getPaddingLeft() + x;
                        childTop = getPaddingTop() + y - childHeight;
                        break;
                    case MapView.LayoutParams.BOTTOM_CENTER:
                        childLeft = getPaddingLeft() + x - childWidth / 2;
                        childTop = getPaddingTop() + y - childHeight;
                        break;
                    case MapView.LayoutParams.BOTTOM_RIGHT:
                        childLeft = getPaddingLeft() + x - childWidth;
                        childTop = getPaddingTop() + y - childHeight;
                        break;
                }
                childLeft += lp.offsetX;
                childTop += lp.offsetY;
                child.layout(
                        TileSystem.truncateToInt(childLeft), TileSystem.truncateToInt(childTop),
                        TileSystem.truncateToInt(childLeft + childWidth), TileSystem.truncateToInt(childTop + childHeight));
            }
        }
        if (!isLayoutOccurred()) {
            mLayoutOccurred = true;
            for (OnFirstLayoutListener listener : mOnFirstLayoutListeners)
                listener.onFirstLayout(this, l, t, r, b);
            mOnFirstLayoutListeners.clear();
        }
        resetProjection();
    }

    /** Enables you to add a listener for when the map is ready to go */
    public void addOnFirstLayoutListener(@NonNull final OnFirstLayoutListener listener) {
        // Don't add if we already have a layout
        if (!isLayoutOccurred() && !mOnFirstLayoutListeners.contains(listener))
            mOnFirstLayoutListeners.add(listener);
    }

    public void removeOnFirstLayoutListener(@NonNull final OnFirstLayoutListener listener) {
        mOnFirstLayoutListeners.remove(listener);
    }

    public boolean isLayoutOccurred() {
        return mLayoutOccurred;
    }

    protected void onResume() { /*nothing*/ }

    protected void onPause() { /*nothing*/ }

    protected void onDestroy() { /*nothing*/ }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        final boolean result = this.getOverlayManager().onKeyDown(keyCode, event, this);

        return result || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        final boolean result = this.getOverlayManager().onKeyUp(keyCode, event, this);

        return result || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onTrackballEvent(final MotionEvent event) {

        if (this.getOverlayManager().onTrackballEvent(event, this)) {
            return true;
        }

        scrollBy((int) (event.getX() * 25), (int) (event.getY() * 25));

        return super.onTrackballEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {

        if (Configuration.getInstance().isDebugMapView()) {
            Log.d(IMapView.LOGTAG, "dispatchTouchEvent(" + event + ")");
        }

        if (mZoomController.isTouched(event)) {
            mZoomController.activate();
            return true;
        }

        // Get rotated event for some touch listeners.
        MotionEvent rotatedEvent = rotateTouchEvent(event);

        try {
            if (super.dispatchTouchEvent(event)) {
                if (Configuration.getInstance().isDebugMapView()) {
                    Log.d(IMapView.LOGTAG, "super handled onTouchEvent");
                }
                return true;
            }

            if (this.getOverlayManager().onTouchEvent(rotatedEvent, this)) {
                return true;
            }

            boolean handled = false;
            if (mMultiTouchController != null && mMultiTouchController.onTouchEvent(event)) {
                if (Configuration.getInstance().isDebugMapView()) {
                    Log.d(IMapView.LOGTAG, "mMultiTouchController handled onTouchEvent");
                }
                handled = true;
            }

            if (mGestureDetector.onTouchEvent(rotatedEvent)) {
                if (Configuration.getInstance().isDebugMapView()) {
                    Log.d(IMapView.LOGTAG, "mGestureDetector handled onTouchEvent");
                }
                handled = true;
            }

            if (handled)
                return true;
        } finally {
            if (rotatedEvent != event)
                rotatedEvent.recycle();
        }

        if (Configuration.getInstance().isDebugMapView()) {
            Log.d(IMapView.LOGTAG, "no-one handled onTouchEvent");
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    private MotionEvent rotateTouchEvent(MotionEvent ev) {
        if (this.getMapOrientation() == 0)
            return ev;

        MotionEvent rotatedEvent = MotionEvent.obtain(ev);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            getProjection().unrotateAndScalePoint((int) ev.getX(), (int) ev.getY(),
                    mRotateScalePoint);
            rotatedEvent.setLocation(mRotateScalePoint.x, mRotateScalePoint.y);
        } else {
            // This method is preferred since it will rotate historical touch events too
            rotatedEvent.transform(getProjection().getInvertedScaleRotateCanvasMatrix());
        }
        return rotatedEvent;
    }

    @Override
    public void computeScroll() {
        if (mScroller == null) { //fix for edit mode in the IDE
            return;
        }
        if (!mIsFlinging) {
            return;
        }
        if (!mScroller.computeScrollOffset()) {
            return;
        }
        if (mScroller.isFinished()) {
            // we deliberately ignore the very last scrollTo, which sometimes provokes map hiccups
            mIsFlinging = false;
        } else {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        setMapScroll(x, y);
        resetProjection();
        invalidate();

        // Force a layout, so that children are correctly positioned according to map orientation
        if (getMapOrientation() != 0f)
            myOnLayout(true, getLeft(), getTop(), getRight(), getBottom());

        // do callback on listener
        raiseOnScrollEvent(x, y);
    }

    private void raiseOnScrollEvent(final int x, final int y) {
        final ScrollEvent event = mScrollEventsCache.getFreeItemFromPoll();
        ScrollEvent.set(event, this, x, y);
        for (MapListener mapListener : mListeners) {
            mapListener.onScroll(event);
        }
        mScrollEventsCache.returnItemToPool(event);
    }

    /**
     * @since 6.0.0
     */
    @Override
    public void scrollBy(int x, int y) {
        scrollTo((int) (getMapScrollX() + x), (int) (getMapScrollY() + y));
    }

    @Override
    public void setBackgroundColor(@ColorInt final int pColor) {
        mMapOverlay.setLoadingBackgroundColor(pColor);
        invalidate();
    }

    @UiThread @MainThread
    @Override
    protected void dispatchDraw(final Canvas c) {
        final long startMs = System.currentTimeMillis();

        // Reset the projection
        resetProjection();

        // Apply the scale and rotate operations
        getProjection().save(c, true, false);

        /* Draw background */
        // c.drawColor(mBackgroundColor);
        try {
            /* Draw all Overlays. */
            this.getOverlayManager().onDraw(c, this);
            // Restore the canvas matrix
            getProjection().restore(c, false);
            if (mZoomController != null) {
                mZoomController.draw(c);
            }
            super.dispatchDraw(c);
        } catch (Exception ex) {
            //for edit mode
            Log.e(IMapView.LOGTAG, "error dispatchDraw, probably in edit mode", ex);
        }
        if (Configuration.getInstance().isDebugMapView()) {
            final long endMs = System.currentTimeMillis();
            Log.d(IMapView.LOGTAG, "Rendering overall: " + (endMs - startMs) + "ms");
        }
    }

    // ===========================================================
    // Animation
    // ===========================================================

    /**
     * Determines if maps are animating a zoom operation. Useful for overlays to avoid recalculating
     * during an animation sequence.
     *
     * @return boolean indicating whether view is animating.
     */
    public boolean isAnimating() {
        return mIsAnimating.get();
    }

    // ===========================================================
    // Implementation of MultiTouchObjectCanvas
    // ===========================================================

    @Override
    public Object getDraggableObjectAtPoint(@NonNull final PointInfo pt) {
        if (this.isAnimating()) {
            // Zoom animations use the mMultiTouchScale variables to perform their animations so we
            // don't want to step on that.
            return null;
        } else {
            setMultiTouchScaleInitPoint(pt.getX(), pt.getY());
            return this;
        }
    }

    @Override
    public void getPositionAndScale(@NonNull final Object obj, @NonNull final PositionAndScale objPosAndScaleOut) {
        startAnimation();
        objPosAndScaleOut.set(mMultiTouchScaleInitPoint.x, mMultiTouchScaleInitPoint.y, true, 1, false, 0, 0, false, 0);
    }

    @Override
    public void selectObject(@NonNull final Object obj, @NonNull final PointInfo pt) {
        if (mZoomRounding) {
            mZoomLevel = Math.round(mZoomLevel);
            invalidate();
        }
        resetMultiTouchScale();
    }

    @Override
    public boolean setPositionAndScale(@NonNull final Object obj, @NonNull final PositionAndScale aNewObjPosAndScale,
                                       @Nullable final PointInfo aTouchPoint) {
        setMultiTouchScaleCurrentPoint(aNewObjPosAndScale.getXOff(), aNewObjPosAndScale.getYOff());
        setMultiTouchScale(aNewObjPosAndScale.getScale());
        requestLayout(); // Allows any views fixed to a Location in the MapView to adjust
        invalidate();
        return true;
    }

    /**
     * @since 6.0.0
     */
    public void resetMultiTouchScale() {
        mHasMultiTouchScaleCurrentPoint = false;
    }

    /**
     * @since 6.0.0
     */
    protected void setMultiTouchScaleInitPoint(final float pX, final float pY) {
        mMultiTouchScaleInitPoint.set(pX, pY);
        final Point unRotatedPixel = getProjection().unrotateAndScalePoint((int) pX, (int) pY, null);
        getProjection().fromPixels(unRotatedPixel.x, unRotatedPixel.y, mMultiTouchScaleGeoPoint);
        setMultiTouchScaleCurrentPoint(pX, pY);
    }

    /**
     * @since 6.0.0
     */
    protected void setMultiTouchScaleCurrentPoint(final float pX, final float pY) {
        mMultiTouchScaleCurrentPoint.set(pX, pY);
        mHasMultiTouchScaleCurrentPoint = true;
    }

    /**
     * @since 6.0.0
     */
    protected void setMultiTouchScale(final float pMultiTouchScale) {
        setZoomLevel(Math.log(pMultiTouchScale) / Math.log(2) + mStartAnimationZoom);
    }

    /**
     * @since 6.0.0
     */
    protected void startAnimation() {
        mStartAnimationZoom = getZoomLevelDouble();
    }

    /*
     * Set the MapListener for this view
     * @deprecated use addMapListener instead
     */
    @Deprecated
    public void setMapListener(@NonNull final MapListener ml) {
        this.mListeners.add(ml);
    }

    /**
     * Just like the old setMapListener, except it supports more than one
     *
     * @since 6.0.0
     */
    public void addMapListener(@NonNull final MapListener mapListener) {
        if (!mListeners.contains(mapListener)) this.mListeners.add(mapListener);
    }

    /**
     * Removes a map listener
     *
     * @since 6.0.0
     */
    public void removeMapListener(@NonNull final MapListener mapListener) {
        this.mListeners.remove(mapListener);
    }


    // ===========================================================
    // Methods
    // ===========================================================

    private void checkZoomButtons() {
        this.mZoomController.setZoomInEnabled(canZoomIn());
        this.mZoomController.setZoomOutEnabled(canZoomOut());
    }

    /**
     * @deprecated Use {@link #getZoomController().setVisibility()} instead
     */
    @Deprecated
    public void setBuiltInZoomControls(final boolean on) {
        mZoomController.setVisibility(
                on ? CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT
                        : CustomZoomButtonsController.Visibility.NEVER);
    }

    public void setMultiTouchControls(final boolean on) {
        mMultiTouchController = on ? new MultiTouchController<Object>(this, false) : null;
    }

    /**
     * @since 6.0.0
     */
    public boolean isHorizontalMapRepetitionEnabled() {
        return horizontalMapRepetitionEnabled;
    }

    /**
     * If horizontalMapRepetition is enabled the map repeats in left/right direction and scrolling wraps around the
     * edges. If disabled the map is only shown once for the horizontal direction. Default is true.
     *
     * @since 6.0.0
     */
    public void setHorizontalMapRepetitionEnabled(boolean horizontalMapRepetitionEnabled) {
        this.horizontalMapRepetitionEnabled = horizontalMapRepetitionEnabled;
        mMapOverlay.setHorizontalWrapEnabled(horizontalMapRepetitionEnabled);
        resetProjection();
        this.invalidate();
    }

    /**
     * @since 6.0.0
     */
    public boolean isVerticalMapRepetitionEnabled() {
        return verticalMapRepetitionEnabled;
    }

    /**
     * If verticalMapRepetition is enabled the map repeats in top/bottom direction and scrolling wraps around the
     * edges. If disabled the map is only shown once for the vertical direction. Default is true.
     *
     * @since 6.0.0
     */
    public void setVerticalMapRepetitionEnabled(boolean verticalMapRepetitionEnabled) {
        this.verticalMapRepetitionEnabled = verticalMapRepetitionEnabled;
        mMapOverlay.setVerticalWrapEnabled(verticalMapRepetitionEnabled);
        resetProjection();
        this.invalidate();
    }

    private ITileSource getTileSourceFromAttributes(final AttributeSet aAttributeSet) {

        ITileSource tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;

        if (aAttributeSet != null) {
            final String tileSourceAttr = aAttributeSet.getAttributeValue(null, "tilesource");
            if (tileSourceAttr != null) {
                try {
                    final ITileSource r = TileSourceFactory.getTileSource(tileSourceAttr);
                    Log.i(IMapView.LOGTAG, "Using tile source specified in layout attributes: " + r);
                    tileSource = r;
                } catch (final IllegalArgumentException e) {
                    Log.w(IMapView.LOGTAG, "Invalid tile source specified in layout attributes: " + tileSource);
                }
            }
        }

        if (aAttributeSet != null && tileSource instanceof IStyledTileSource) {
            final String style = aAttributeSet.getAttributeValue(null, "style");
            if (style == null) {
                Log.i(IMapView.LOGTAG, "Using default style: 1");
            } else {
                Log.i(IMapView.LOGTAG, "Using style specified in layout attributes: " + style);
                ((IStyledTileSource<?>) tileSource).setStyle(style);
            }
        }

        Log.i(IMapView.LOGTAG, "Using tile source: " + tileSource.name());
        return tileSource;
    }


    private boolean enableFling = true;
    private boolean pauseFling = false;    // issue 269, boolean used for disabling fling during zoom changes

    public void setFlingEnabled(final boolean b) {
        enableFling = b;
    }

    public boolean isFlingEnabled() {
        return enableFling;
    }
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private class MapViewGestureDetectorListener implements OnGestureListener {

        @Override
        public boolean onDown(final MotionEvent e) {

            // Stop scrolling if we are in the middle of a fling!
            if (mIsFlinging) {
                if (mScroller != null)    //fix for edit mode in the IDE
                    mScroller.abortAnimation();
                mIsFlinging = false;
            }

            if (MapView.this.getOverlayManager().onDown(e, MapView.this)) {
                return true;
            }

            if (mZoomController != null) {
                mZoomController.activate();
            }
            return true;
        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2,
                               final float velocityX, final float velocityY) {

            if (!enableFling || pauseFling) {
                // issue 269, if fling occurs during zoom changes, pauseFling is equals to true, so fling is canceled. But need to reactivate fling for next time.
                pauseFling = false;
                return false;
            }


            if (MapView.this.getOverlayManager()
                    .onFling(e1, e2, velocityX, velocityY, MapView.this)) {
                return true;
            }

            if (mImpossibleFlinging) {
                mImpossibleFlinging = false;
                return false;
            }

            mIsFlinging = true;
            if (mScroller != null) {  //fix for edit mode in the IDE
                mScroller.fling((int) getMapScrollX(), (int) getMapScrollY(), -(int) velocityX, -(int) velocityY,
                        Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            }
            return true;
        }

        @Override
        public void onLongPress(final MotionEvent e) {
            if (mMultiTouchController != null && mMultiTouchController.isPinching()) {
                return;
            }
            MapView.this.getOverlayManager().onLongPress(e, MapView.this);
        }

        @Override
        public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX,
                                final float distanceY) {
            if (MapView.this.getOverlayManager().onScroll(e1, e2, distanceX, distanceY,
                    MapView.this)) {
                return true;
            }

            scrollBy((int) distanceX, (int) distanceY);
            return true;
        }

        @Override
        public void onShowPress(final MotionEvent e) {
            MapView.this.getOverlayManager().onShowPress(e, MapView.this);
        }

        @Override
        public boolean onSingleTapUp(final MotionEvent e) {
            if (MapView.this.getOverlayManager().onSingleTapUp(e, MapView.this)) {
                return true;
            }

            return false;
        }

    }

    private class MapViewDoubleClickListener implements GestureDetector.OnDoubleTapListener {
        @Override
        public boolean onDoubleTap(final MotionEvent e) {
            if (MapView.this.getOverlayManager().onDoubleTap(e, MapView.this)) {
                return true;
            }

            getProjection().rotateAndScalePoint((int) e.getX(), (int) e.getY(), mRotateScalePoint);
            return getController().zoomInFixing(mRotateScalePoint.x, mRotateScalePoint.y);
        }

        @Override
        public boolean onDoubleTapEvent(final MotionEvent e) {
            if (MapView.this.getOverlayManager().onDoubleTapEvent(e, MapView.this)) {
                return true;
            }

            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(final MotionEvent e) {
            if (MapView.this.getOverlayManager().onSingleTapConfirmed(e, MapView.this)) {
                return true;
            }

            return false;
        }
    }

    private class MapViewZoomListener implements CustomZoomButtonsController.OnZoomListener, ZoomButtonsController.OnZoomListener {
        @Override
        public void onZoom(final boolean zoomIn) {
            if (zoomIn) {
                getController().zoomIn();
            } else {
                getController().zoomOut();
            }
        }

        @Override
        public void onVisibilityChanged(final boolean visible) {
        }
    }

    // ===========================================================
    // Public Classes
    // ===========================================================

    /**
     * Per-child layout information associated with OpenStreetMapView.
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {

        /**
         * Special value for the alignment requested by a View. TOP_LEFT means that the location
         * will at the top left the View.
         */
        public static final int TOP_LEFT = 1;
        /**
         * Special value for the alignment requested by a View. TOP_RIGHT means that the location
         * will be centered at the top of the View.
         */
        public static final int TOP_CENTER = 2;
        /**
         * Special value for the alignment requested by a View. TOP_RIGHT means that the location
         * will at the top right the View.
         */
        public static final int TOP_RIGHT = 3;
        /**
         * Special value for the alignment requested by a View. CENTER_LEFT means that the location
         * will at the center left the View.
         */
        public static final int CENTER_LEFT = 4;
        /**
         * Special value for the alignment requested by a View. CENTER means that the location will
         * be centered at the center of the View.
         */
        public static final int CENTER = 5;
        /**
         * Special value for the alignment requested by a View. CENTER_RIGHT means that the location
         * will at the center right the View.
         */
        public static final int CENTER_RIGHT = 6;
        /**
         * Special value for the alignment requested by a View. BOTTOM_LEFT means that the location
         * will be at the bottom left of the View.
         */
        public static final int BOTTOM_LEFT = 7;
        /**
         * Special value for the alignment requested by a View. BOTTOM_CENTER means that the
         * location will be centered at the bottom of the view.
         */
        public static final int BOTTOM_CENTER = 8;
        /**
         * Special value for the alignment requested by a View. BOTTOM_RIGHT means that the location
         * will be at the bottom right of the View.
         */
        public static final int BOTTOM_RIGHT = 9;
        /**
         * The location of the child within the map view.
         */
        public Double geoPointLat;
        public Double geoPointLon;

        /**
         * The alignment the alignment of the view compared to the location.
         */
        public int alignment;

        public int offsetX;
        public int offsetY;

        /**
         * Creates a new set of layout parameters with the specified width, height and location.
         *
         * @param width     the width, either {@link #FILL_PARENT}, {@link #WRAP_CONTENT} or a fixed size
         *                  in pixels
         * @param height    the height, either {@link #FILL_PARENT}, {@link #WRAP_CONTENT} or a fixed size
         *                  in pixels
         * @param geoPoint  the location of the child within the map view
         * @param alignment the alignment of the view compared to the location {@link #BOTTOM_CENTER},
         *                  {@link #BOTTOM_LEFT}, {@link #BOTTOM_RIGHT} {@link #TOP_CENTER},
         *                  {@link #TOP_LEFT}, {@link #TOP_RIGHT}
         * @param offsetX   the additional X offset from the alignment location to draw the child within
         *                  the map view
         * @param offsetY   the additional Y offset from the alignment location to draw the child within
         *                  the map view
         */
        @Deprecated
        public LayoutParams(final int width, final int height, @NonNull final IGeoPoint geoPoint,
                            final int alignment, final int offsetX, final int offsetY) {
            this(width, height, geoPoint.getLatitude(), geoPoint.getLongitude(), alignment, offsetX, offsetY);
        }
        /**
         * Creates a new set of layout parameters with the specified width, height and location.
         *
         * @param width     the width, either {@link #FILL_PARENT}, {@link #WRAP_CONTENT} or a fixed size
         *                  in pixels
         * @param height    the height, either {@link #FILL_PARENT}, {@link #WRAP_CONTENT} or a fixed size
         *                  in pixels
         * @param geoPointLat  the location of the child within the map view
         * @param geoPointLon  the location of the child within the map view
         * @param alignment the alignment of the view compared to the location {@link #BOTTOM_CENTER},
         *                  {@link #BOTTOM_LEFT}, {@link #BOTTOM_RIGHT} {@link #TOP_CENTER},
         *                  {@link #TOP_LEFT}, {@link #TOP_RIGHT}
         * @param offsetX   the additional X offset from the alignment location to draw the child within
         *                  the map view
         * @param offsetY   the additional Y offset from the alignment location to draw the child within
         *                  the map view
         */
        public LayoutParams(final int width, final int height, @Nullable final Double geoPointLat, @Nullable final Double geoPointLon,
                            final int alignment, final int offsetX, final int offsetY) {
            super(width, height);
            if ((geoPointLat != null) && (geoPointLon != null)) {
                this.geoPointLat = geoPointLat;
                this.geoPointLon = geoPointLon;
            } else {
                this.geoPointLat = 0.0d;
                this.geoPointLon = 0.0d;
            }
            this.alignment = alignment;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        /**
         * Since we cannot use XML files in this project this constructor is useless. Creates a new
         * set of layout parameters. The values are extracted from the supplied attributes set and
         * context.
         *
         * @param c     the application environment
         * @param attrs the set of attributes fom which to extract the layout parameters values
         */
        public LayoutParams(@NonNull final Context c, final AttributeSet attrs) {
            super(c, attrs);
            this.geoPointLat = 0.0d;
            this.geoPointLon = 0.0d;
            this.alignment = BOTTOM_CENTER;
        }

        public LayoutParams(@NonNull final ViewGroup.LayoutParams source) {
            super(source);
        }
    }


    /**
     * enables you to programmatically set the tile provider (zip, assets, sqlite, etc)
     *
     * @param base
     * @see MapTileProviderBasic
     * @since 4.4
     */
    public void setTileProvider(@NonNull final MapTileProviderBase base) {
        mTileProvider.detach();
        mTileProvider.clearTileCache();
        mTileProvider = base;
        final Collection<Handler> cHandlers = mTileProvider.getTileRequestCompleteHandlers();
        if (!cHandlers.contains(mTileRequestCompleteHandler)) cHandlers.add(mTileRequestCompleteHandler);
        updateTileSizeForDensity(mTileProvider.getTileSource());

        mMapOverlay = new TilesOverlay(mTileProvider, this.getContext(), horizontalMapRepetitionEnabled, verticalMapRepetitionEnabled);

        mOverlayManager.setTilesOverlay(mMapOverlay);
        invalidate();
    }

    /**
     * Sets the initial center point of the map. This can be set before the map view is 'ready'
     * meaning that it can be set and honored with the onFirstLayoutListener
     *
     * @since 6.0.0
     */
    @Deprecated
    public void setInitCenter(@NonNull final IGeoPoint geoPoint) {
        setExpectedCenter(geoPoint);
    }

    public long getMapScrollX() {
        return mMapScrollX;
    }

    public long getMapScrollY() {
        return mMapScrollY;
    }

    void setMapScroll(final long pMapScrollX, final long pMapScrollY) {
        mMapScrollX = pMapScrollX;
        mMapScrollY = pMapScrollY;
        requestLayout(); // Allows any views fixed to a Location in the MapView to adjust
    }

    /**
     * Should never be used except by the constructor of Projection.
     * Most of the time you'll want to call {@link #getMapCenter()}.
     * <p>
     * This method gives to the Projection the desired map center, typically set by
     * MapView.setExpectedCenter when you want to center a map on a particular point.
     * <a href="https://github.com/osmdroid/osmdroid/issues/868">see issue 868</a>
     *
     * @see #getMapCenter()
     * @since 6.0.0
     */
    @Deprecated
    GeoPoint getExpectedCenter() {
        return new GeoPoint(mCenterLat, mCenterLon);
    }

    /**
     * Should never be used except by the constructor of Projection.
     * Most of the time you'll want to call {@link #getMapCenter()}.
     * <p>
     * This method gives to the Projection the desired map center, typically set by
     * MapView.setExpectedCenter when you want to center a map on a particular point.
     * <a href="https://github.com/osmdroid/osmdroid/issues/868">see issue 868</a>
     *
     * @see #getMapCenter()
     * @since 6.0.0
     */
    double getExpectedCenterLat() {
        return mCenterLat;
    }
    /**
     * Should never be used except by the constructor of Projection.
     * Most of the time you'll want to call {@link #getMapCenter()}.
     * <p>
     * This method gives to the Projection the desired map center, typically set by
     * MapView.setExpectedCenter when you want to center a map on a particular point.
     * <a href="https://github.com/osmdroid/osmdroid/issues/868">see issue 868</a>
     *
     * @see #getMapCenter()
     * @since 6.0.0
     */
    double getExpectedCenterLon() {
        return mCenterLon;
    }

    /**
     * Deferred setting of the expected next map center computed by the Projection's constructor,
     * with no guarantee it will be 100% respected.
     * <a href="https://github.com/osmdroid/osmdroid/issues/868">see issue 868</a>
     *
     * @since 6.0.3
     */
    public void setExpectedCenter(@NonNull final IGeoPoint pGeoPoint, final long pOffsetX, final long pOffsetY) {
        final GeoPoint before = getProjection().getCurrentCenter();
        mCenterLat = pGeoPoint.getLatitude();
        mCenterLon = pGeoPoint.getLongitude();
        setMapScroll(-pOffsetX, -pOffsetY);
        resetProjection();
        final GeoPoint after = getProjection().getCurrentCenter();
        if (!after.equals(before)) {
            raiseOnScrollEvent(0, 0);
        }
        invalidate();
    }

    /**
     * Deferred setting of the expected next map center computed by the Projection's constructor,
     * with no guarantee it will be 100% respected.
     * <a href="https://github.com/osmdroid/osmdroid/issues/868">see issue 868</a>
     *
     * @since 6.1.18
     */
    public void setExpectedCenter(final double pGeoPointLat, final double pGeoPointLon, final long pOffsetX, final long pOffsetY) {
        final Projection cProjection = getProjection();
        final GeoPoint before = cProjection.getCurrentCenter();
        mCenterLat = pGeoPointLat;
        mCenterLon = pGeoPointLon;
        setMapScroll(-pOffsetX, -pOffsetY);
        resetProjection();
        final GeoPoint after = cProjection.getCurrentCenter();
        if (!after.equals(before)) {
            raiseOnScrollEvent(0, 0);
        }
        invalidate();
    }

    /**
     * @since 6.0.0
     */
    public void setExpectedCenter(@NonNull final IGeoPoint pGeoPoint) {
        setExpectedCenter(pGeoPoint.getLatitude(), pGeoPoint.getLongitude(), 0, 0);
    }

    /**
     * @since 6.1.18
     */
    public void setExpectedCenter(final double pGeoPointLat, final double pGeoPointLon) {
        setExpectedCenter(pGeoPointLat, pGeoPointLon, 0, 0);
    }

    /**
     * @since 6.0.2
     */
    public void setZoomRounding(final boolean pZoomRounding) {
        mZoomRounding = pZoomRounding;
    }

    /**
     * @since 6.0.2
     */
    public static TileSystem getTileSystem() {
        return mTileSystem;
    }

    /**
     * @since 6.0.2
     */
    public static void setTileSystem(final TileSystem pTileSystem) {
        mTileSystem = pTileSystem;
    }

    /**
     * @since 6.0.3
     */
    public MapViewRepository getRepository() {
        return mRepository;
    }

    /**
     * @since 6.1.0
     */
    public CustomZoomButtonsController getZoomController() {
        return mZoomController;
    }

    /**
     * @since 6.1.0
     */
    public TilesOverlay getMapOverlay() {
        return mMapOverlay;
    }

    /**
     * @since 6.1.1
     */
    public int getMapCenterOffsetX() {
        return mMapCenterOffsetX;
    }

    /**
     * @since 6.1.1
     */
    public int getMapCenterOffsetY() {
        return mMapCenterOffsetY;
    }

    /**
     * @since 6.1.1
     */
    public void setMapCenterOffset(final int pMapCenterOffsetX, final int pMapCenterOffsetY) {
        mMapCenterOffsetX = pMapCenterOffsetX;
        mMapCenterOffsetY = pMapCenterOffsetY;
    }
}

