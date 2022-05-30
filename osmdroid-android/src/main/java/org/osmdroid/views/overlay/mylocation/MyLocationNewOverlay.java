package org.osmdroid.views.overlay.mylocation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.library.R;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.IOverlayMenuProvider;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Overlay.Snappable;

import java.util.LinkedList;

/**
 * @author Marc Kurtz
 * @author Manuel Stahl
 */
public class MyLocationNewOverlay extends Overlay implements IMyLocationConsumer,
        IOverlayMenuProvider, Snappable {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    protected Paint mPaint = new Paint();
    protected Paint mCirclePaint = new Paint();

	protected Bitmap mPersonBitmap;
	protected Bitmap mDirectionArrowBitmap;

    protected MapView mMapView;

    private IMapController mMapController;
    public IMyLocationProvider mMyLocationProvider;

    private final LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
    private final Point mDrawPixel = new Point();
    private final Point mSnapPixel = new Point();
    private Handler mHandler;
    private Object mHandlerToken = new Object();

    /**
     * if true, when the user pans the map, follow my location will automatically disable
     * if false, when the user pans the map, the map will continue to follow current location
     */
    protected boolean enableAutoStop = true;
    private Location mLocation;
    private final GeoPoint mGeoPoint = new GeoPoint(0, 0); // for reuse
    private boolean mIsLocationEnabled = false;
    protected boolean mIsFollowing = false; // follow location updates
    protected boolean mDrawAccuracyEnabled = true;

    /**
     * Coordinates the feet of the person are located scaled for display density.
     */
    protected final PointF mPersonHotspot;

    protected float mDirectionArrowCenterX;
    protected float mDirectionArrowCenterY;

    public static final int MENU_MY_LOCATION = getSafeMenuId();

    private boolean mOptionsMenuEnabled = true;

    private boolean wasEnabledOnPause = false;
    // ===========================================================
    // Constructors
    // ===========================================================

    public MyLocationNewOverlay(MapView mapView) {
        this(new GpsMyLocationProvider(mapView.getContext()), mapView);
    }

	public MyLocationNewOverlay(IMyLocationProvider myLocationProvider, MapView mapView) {
		super();

        mMapView = mapView;
        mMapController = mapView.getController();
        mCirclePaint.setARGB(0, 100, 100, 255);
        mCirclePaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);


		setPersonIcon(((BitmapDrawable)mapView.getContext().getResources().getDrawable(R.drawable.person)).getBitmap());
		setDirectionIcon(((BitmapDrawable)mapView.getContext().getResources().getDrawable(R.drawable.round_navigation_white_48)).getBitmap());

		// Calculate position of person icon's feet, scaled to screen density
		mPersonHotspot = new PointF();
		setPersonAnchor(.5f, .8125f); // anchor for the default icon
		setDirectionAnchor(.5f, .5f); // anchor for the default icon

        mHandler = new Handler(Looper.getMainLooper());
        setMyLocationProvider(myLocationProvider);
    }

	/**
	 * fix for https://github.com/osmdroid/osmdroid/issues/249
	 * @deprecated Use {@link #setPersonIcon(Bitmap)}, {@link #setDirectionIcon(Bitmap)},
	 * {@link #setPersonAnchor(float, float)} and {@link #setDirectionAnchor(float, float)} instead
     */
	@Deprecated
	public void setDirectionArrow(final Bitmap personBitmap, final Bitmap directionArrowBitmap){
		setPersonIcon(personBitmap);
		setDirectionIcon(directionArrowBitmap);
		setDirectionAnchor(.5f, .5f);
	}

	/**
	 * @since 6.2.0
	 */
	public void setDirectionIcon(final Bitmap pDirectionArrowBitmap){
		mDirectionArrowBitmap = pDirectionArrowBitmap;
	}

	@Override
	public void onResume(){
		super.onResume();
		if (wasEnabledOnPause)
			this.enableFollowLocation();
		this.enableMyLocation();
	}
	@Override
	public void onPause(){
		wasEnabledOnPause=mIsFollowing;
		this.disableMyLocation();
		super.onPause();
	}

	@Override
	public void onDetach(MapView mapView) {
		this.disableMyLocation();
		/*if (mPersonBitmap != null) {
			mPersonBitmap.recycle();
		}
		if (mDirectionArrowBitmap != null) {
			mDirectionArrowBitmap.recycle();
		}*/
		this.mMapView = null;
		this.mMapController = null;
		mHandler = null;
		mCirclePaint = null;
		//mPersonBitmap = null;
		//mDirectionArrowBitmap = null;
		mHandlerToken = null;
		mLocation = null;
		mMapController = null;
		if (mMyLocationProvider!=null)
			mMyLocationProvider.destroy();

		mMyLocationProvider = null;
		super.onDetach(mapView);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	/**
	 * If enabled, an accuracy circle will be drawn around your current position.
	 * 
	 * @param drawAccuracyEnabled
	 *            whether the accuracy circle will be enabled
	 */
	public void setDrawAccuracyEnabled(final boolean drawAccuracyEnabled) {
		mDrawAccuracyEnabled = drawAccuracyEnabled;
	}

	/**
	 * If enabled, an accuracy circle will be drawn around your current position.
	 * 
	 * @return true if enabled, false otherwise
	 */
	public boolean isDrawAccuracyEnabled() {
		return mDrawAccuracyEnabled;
	}

	public IMyLocationProvider getMyLocationProvider() {
		return mMyLocationProvider;
	}

	protected void setMyLocationProvider(IMyLocationProvider myLocationProvider) {
		if (myLocationProvider == null)
			throw new RuntimeException(
					"You must pass an IMyLocationProvider to setMyLocationProvider()");

		if (isMyLocationEnabled())
			stopLocationProvider();

		mMyLocationProvider = myLocationProvider;
	}

	/**
	 * @deprecated Use {@link #setPersonAnchor(float, float)} instead
	 */
	@Deprecated
	public void setPersonHotspot(float x, float y) {
		mPersonHotspot.set(x, y);
	}

	protected void drawMyLocation(final Canvas canvas, final Projection pj, final Location lastFix) {
		pj.toPixels(mGeoPoint, mDrawPixel);

		if (mDrawAccuracyEnabled) {
			final float radius = lastFix.getAccuracy()
					/ (float) TileSystem.GroundResolution(lastFix.getLatitude(),
							pj.getZoomLevel());

			mCirclePaint.setAlpha(50);
			mCirclePaint.setStyle(Style.FILL);
			canvas.drawCircle(mDrawPixel.x, mDrawPixel.y, radius, mCirclePaint);

			mCirclePaint.setAlpha(150);
			mCirclePaint.setStyle(Style.STROKE);
			canvas.drawCircle(mDrawPixel.x, mDrawPixel.y, radius, mCirclePaint);
		}

		if (lastFix.hasBearing()) {
			canvas.save();
			// Rotate the icon if we have a GPS fix, take into account if the map is already rotated
			float mapRotation;
			mapRotation=lastFix.getBearing();
			if (mapRotation >=360.0f)
				mapRotation=mapRotation-360f;
			canvas.rotate(mapRotation, mDrawPixel.x, mDrawPixel.y);
			// Draw the bitmap
			canvas.drawBitmap(mDirectionArrowBitmap, mDrawPixel.x
					- mDirectionArrowCenterX, mDrawPixel.y - mDirectionArrowCenterY,
					mPaint);
			canvas.restore();
		} else {
			canvas.save();
			// Unrotate the icon if the maps are rotated so the little man stays upright
			canvas.rotate(-mMapView.getMapOrientation(), mDrawPixel.x,
					mDrawPixel.y);
			// Draw the bitmap
			canvas.drawBitmap(mPersonBitmap, mDrawPixel.x - mPersonHotspot.x,
					mDrawPixel.y - mPersonHotspot.y, mPaint);
			canvas.restore();
		}
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void draw(Canvas c, Projection pProjection) {
		if (mLocation != null && isMyLocationEnabled()) {
			drawMyLocation(c, pProjection, mLocation);
		}
	}

	@Override
	public boolean onSnapToItem(final int x, final int y, final Point snapPoint,
			final IMapView mapView) {
		if (this.mLocation != null) {
			Projection pj = mMapView.getProjection();
			pj.toPixels(mGeoPoint, mSnapPixel);
			snapPoint.x = mSnapPixel.x;
			snapPoint.y = mSnapPixel.y;
			final double xDiff = x - mSnapPixel.x;
			final double yDiff = y - mSnapPixel.y;
			boolean snap = xDiff * xDiff + yDiff * yDiff < 64;
			if (Configuration.getInstance().isDebugMode()) {
                    Log.d(IMapView.LOGTAG, "snap=" + snap);
			}
			return snap;
		} else {
			return false;
		}
	}

	public void setEnableAutoStop(boolean value){
		this.enableAutoStop=value;
	}
	public boolean getEnableAutoStop(){
		return this.enableAutoStop;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
		final boolean isSingleFingerDrag = (event.getAction() == MotionEvent.ACTION_MOVE)
				&& (event.getPointerCount() == 1);

		if (event.getAction() == MotionEvent.ACTION_DOWN && enableAutoStop) {
			this.disableFollowLocation();
		} else if (isSingleFingerDrag && isFollowLocationEnabled()) {
			return true;  // prevent the pan
		}

		return super.onTouchEvent(event, mapView);
	}

	// ===========================================================
	// Menu handling methods
	// ===========================================================

	@Override
	public void setOptionsMenuEnabled(final boolean pOptionsMenuEnabled) {
		this.mOptionsMenuEnabled = pOptionsMenuEnabled;
	}

	@Override
	public boolean isOptionsMenuEnabled() {
		return this.mOptionsMenuEnabled;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
			final MapView pMapView) {
		pMenu.add(0, MENU_MY_LOCATION + pMenuIdOffset, Menu.NONE,
				pMapView.getContext().getResources().getString(R.string.my_location)
				)
				.setIcon(
						pMapView.getContext().getResources().getDrawable(R.drawable.ic_menu_mylocation)
						)
				.setCheckable(true);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
			final MapView pMapView) {
		pMenu.findItem(MENU_MY_LOCATION + pMenuIdOffset).setChecked(this.isMyLocationEnabled());
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem pItem, final int pMenuIdOffset,
			final MapView pMapView) {
		final int menuId = pItem.getItemId() - pMenuIdOffset;
		if (menuId == MENU_MY_LOCATION) {
			if (this.isMyLocationEnabled()) {
				this.disableFollowLocation();
				this.disableMyLocation();
			} else {
				this.enableFollowLocation();
				this.enableMyLocation();
			}
			return true;
		} else {
			return false;
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Return a GeoPoint of the last known location, or null if not known.
	 */
	public GeoPoint getMyLocation() {
		if (mLocation == null) {
			return null;
		} else {
			return new GeoPoint(mLocation);
		}
	}

	public Location getLastFix() {
		return mLocation;
	}

	/**
	 * Enables "follow" functionality. The map will center on your current location and
	 * automatically scroll as you move. Scrolling the map in the UI will disable.
	 */
	public void enableFollowLocation() {
		mIsFollowing = true;

		// set initial location when enabled
		if (isMyLocationEnabled()) {
			Location location = mMyLocationProvider.getLastKnownLocation();
			if (location != null) {
				setLocation(location);
			}
		}

		// Update the screen to see changes take effect
		if (mMapView != null) {
			mMapView.postInvalidate();
		}
	}

	/**
	 * Disables "follow" functionality.
	 */
	public void disableFollowLocation() {
		if (mMapController!=null)
			mMapController.stopAnimation(false);
		mIsFollowing = false;
	}

	/**
	 * If enabled, the map will center on your current location and automatically scroll as you
	 * move. Scrolling the map in the UI will disable.
	 * 
	 * @return true if enabled, false otherwise
	 */
	public boolean isFollowLocationEnabled() {
		return mIsFollowing;
	}

	@Override
	public void onLocationChanged(final Location location, IMyLocationProvider source) {

		if (location != null && mHandler!=null) {
			// These location updates can come in from different threads
			mHandler.postAtTime(new Runnable() {
				@Override
				public void run() {
					setLocation(location);

					for (final Runnable runnable : mRunOnFirstFix) {
						Thread t = new Thread(runnable);
						t.setName(this.getClass().getName() + "#onLocationChanged");
						t.start();
					}
					mRunOnFirstFix.clear();
				}
			}, mHandlerToken, 0);
		}
	}

	protected void setLocation(Location location) {
		mLocation = location;
		mGeoPoint.setCoords(mLocation.getLatitude(), mLocation.getLongitude());
		if (mIsFollowing) {
			mMapController.animateTo(mGeoPoint);
		} else if ( mMapView != null ) {
			mMapView.postInvalidate();
		}
	}

	public boolean enableMyLocation(IMyLocationProvider myLocationProvider) {
		// Set the location provider. This will call stopLocationProvider().
		setMyLocationProvider(myLocationProvider);

		boolean success = mMyLocationProvider.startLocationProvider(this);
		mIsLocationEnabled = success;

		// set initial location when enabled
		if (success) {
			Location location = mMyLocationProvider.getLastKnownLocation();
			if (location != null) {
				setLocation(location);
			}
		}

		// Update the screen to see changes take effect
		if (mMapView != null) {
			mMapView.postInvalidate();
		}

		return success;
	}

	/**
	 * Enable receiving location updates from the provided IMyLocationProvider and show your
	 * location on the maps. You will likely want to call enableMyLocation() from your Activity's
	 * Activity.onResume() method, to enable the features of this overlay. Remember to call the
	 * corresponding disableMyLocation() in your Activity's Activity.onPause() method to turn off
	 * updates when in the background.
	 */
	public boolean enableMyLocation() {
		return enableMyLocation(mMyLocationProvider);
	}

	/**
	 * Disable location updates
	 */
	public void disableMyLocation() {
		mIsLocationEnabled = false;

		stopLocationProvider();

		// Update the screen to see changes take effect
		if (mMapView != null) {
			mMapView.postInvalidate();
		}
	}

	protected void stopLocationProvider() {
		if (mMyLocationProvider != null) {
			mMyLocationProvider.stopLocationProvider();
		}
		if (mHandler!=null && mHandlerToken!=null)
			mHandler.removeCallbacksAndMessages(mHandlerToken);
	}

	/**
	 * If enabled, the map is receiving location updates and drawing your location on the map.
	 * 
	 * @return true if enabled, false otherwise
	 */
	public boolean isMyLocationEnabled() {
		return mIsLocationEnabled;
	}

	/**
	 * Queues a runnable to be executed as soon as we have a location fix. If we already have a fix,
	 * we'll execute the runnable immediately and return true. If not, we'll hang on to the runnable
	 * and return false; as soon as we get a location fix, we'll run it in in a new thread.
	 */
	public boolean runOnFirstFix(final Runnable runnable) {
		if (mMyLocationProvider != null && mLocation != null) {
			Thread t = new Thread(runnable);
			t.setName(this.getClass().getName() + "#runOnFirstFix");
			t.start();
			return true;
		} else {
			mRunOnFirstFix.addLast(runnable);
			return false;
		}
	}
     
     /**
      * enables you to change the my location 'person' icon at runtime. note that the
      * hotspot is not updated with this method. see {@link #setPersonAnchor(float, float)}
      */
     public void setPersonIcon(Bitmap icon){
          mPersonBitmap = icon;
     }

	/**
	 * Anchors for the person icon
	 * Expected values between 0 and 1, 0 being top/left, .5 center and 1 bottom/right
	 * @since 6.2.0
	 */
	public void setPersonAnchor(final float pHorizontal, final float pVertical){
		mPersonHotspot.set(mPersonBitmap.getWidth() * pHorizontal, mPersonBitmap.getHeight() * pVertical);
	}

	/**
	 * Anchors for the direction icon
	 * Expected values between 0 and 1, 0 being top/left, .5 center and 1 bottom/right
	 * @since 6.2.0
	 */
	public void setDirectionAnchor(final float pHorizontal, final float pVertical){
		mDirectionArrowCenterX = mDirectionArrowBitmap.getWidth() * pHorizontal;
		mDirectionArrowCenterY = mDirectionArrowBitmap.getHeight() * pVertical;
	}
}
