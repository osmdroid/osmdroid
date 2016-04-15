package org.osmdroid.views.overlay.mylocation;

import java.util.LinkedList;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.library.R;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.IOverlayMenuProvider;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Overlay.Snappable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

/**
 * 
 * @author Marc Kurtz
 * @author Manuel Stahl
 * 
 */
public class MyLocationNewOverlay extends Overlay implements IMyLocationConsumer,
		IOverlayMenuProvider, Snappable {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final Paint mPaint = new Paint();
	protected final Paint mCirclePaint = new Paint();

	protected Bitmap mPersonBitmap;
	protected Bitmap mDirectionArrowBitmap;

	protected final MapView mMapView;

	private final IMapController mMapController;
	public IMyLocationProvider mMyLocationProvider;

	private final LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
	private final Point mMapCoordsProjected = new Point();
	private final Point mMapCoordsTranslated = new Point();
	private final Handler mHandler;
	private final Object mHandlerToken = new Object();

	private Location mLocation;
	private final GeoPoint mGeoPoint = new GeoPoint(0, 0); // for reuse
	private boolean mIsLocationEnabled = false;
	protected boolean mIsFollowing = false; // follow location updates
	protected boolean mDrawAccuracyEnabled = true;

	/** Coordinates the feet of the person are located scaled for display density. */
	protected final PointF mPersonHotspot;

	protected float mDirectionArrowCenterX;
	protected float mDirectionArrowCenterY;

	public static final int MENU_MY_LOCATION = getSafeMenuId();

	private boolean mOptionsMenuEnabled = true;

	// to avoid allocations during onDraw
	private final float[] mMatrixValues = new float[9];
	private final Matrix mMatrix = new Matrix();
	private final Rect mMyLocationRect = new Rect();
	private final Rect mMyLocationPreviousRect = new Rect();

	// ===========================================================
	// Constructors
	// ===========================================================

	public MyLocationNewOverlay(MapView mapView) {
		this(new GpsMyLocationProvider(mapView.getContext()), mapView);
	}

	public MyLocationNewOverlay(IMyLocationProvider myLocationProvider, MapView mapView) {
		super(mapView.getContext());

		mMapView = mapView;
		mMapController = mapView.getController();
		mCirclePaint.setARGB(0, 100, 100, 255);
		mCirclePaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);


		setDirectionArrow(((BitmapDrawable)mapView.getContext().getResources().getDrawable(R.drawable.person)).getBitmap(),
				((BitmapDrawable)mapView.getContext().getResources().getDrawable(R.drawable.direction_arrow)).getBitmap());

		// Calculate position of person icon's feet, scaled to screen density
		mPersonHotspot = new PointF(24.0f * mScale + 0.5f, 39.0f * mScale + 0.5f);

		mHandler = new Handler(Looper.getMainLooper());
		setMyLocationProvider(myLocationProvider);
	}

	/**
	 * fix for https://github.com/osmdroid/osmdroid/issues/249
	 * @param personBitmap
	 * @param directionArrowBitmap
     */
	public void setDirectionArrow(final Bitmap personBitmap, final Bitmap directionArrowBitmap){
		this.mPersonBitmap = personBitmap;
		this.mDirectionArrowBitmap=directionArrowBitmap;


		mDirectionArrowCenterX = mDirectionArrowBitmap.getWidth() / 2.0f - 0.5f;
		mDirectionArrowCenterY = mDirectionArrowBitmap.getHeight() / 2.0f - 0.5f;

	}

	@Override
	public void onDetach(MapView mapView) {
		this.disableMyLocation();
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

	public void setPersonHotspot(float x, float y) {
		mPersonHotspot.set(x, y);
	}

	protected void drawMyLocation(final Canvas canvas, final MapView mapView, final Location lastFix) {
		final Projection pj = mapView.getProjection();
		pj.toPixelsFromProjected(mMapCoordsProjected, mMapCoordsTranslated);

		if (mDrawAccuracyEnabled) {
			final float radius = lastFix.getAccuracy()
					/ (float) TileSystem.GroundResolution(lastFix.getLatitude(),
							mapView.getZoomLevel());

			mCirclePaint.setAlpha(50);
			mCirclePaint.setStyle(Style.FILL);
			canvas.drawCircle(mMapCoordsTranslated.x, mMapCoordsTranslated.y, radius, mCirclePaint);

			mCirclePaint.setAlpha(150);
			mCirclePaint.setStyle(Style.STROKE);
			canvas.drawCircle(mMapCoordsTranslated.x, mMapCoordsTranslated.y, radius, mCirclePaint);
		}

		canvas.getMatrix(mMatrix);
		mMatrix.getValues(mMatrixValues);

		if (DEBUGMODE) {
			final float tx = (-mMatrixValues[Matrix.MTRANS_X] + 20)
					/ mMatrixValues[Matrix.MSCALE_X];
			final float ty = (-mMatrixValues[Matrix.MTRANS_Y] + 90)
					/ mMatrixValues[Matrix.MSCALE_Y];
			canvas.drawText("Lat: " + lastFix.getLatitude(), tx, ty + 5, mPaint);
			canvas.drawText("Lon: " + lastFix.getLongitude(), tx, ty + 20, mPaint);
			canvas.drawText("Alt: " + lastFix.getAltitude(), tx, ty + 35, mPaint);
			canvas.drawText("Acc: " + lastFix.getAccuracy(), tx, ty + 50, mPaint);
		}

		// Calculate real scale including accounting for rotation
		float scaleX = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_X]
				* mMatrixValues[Matrix.MSCALE_X] + mMatrixValues[Matrix.MSKEW_Y]
				* mMatrixValues[Matrix.MSKEW_Y]);
		float scaleY = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_Y]
				* mMatrixValues[Matrix.MSCALE_Y] + mMatrixValues[Matrix.MSKEW_X]
				* mMatrixValues[Matrix.MSKEW_X]);
		if (lastFix.hasBearing()) {
			canvas.save();
			// Rotate the icon
			canvas.rotate(lastFix.getBearing(), mMapCoordsTranslated.x, mMapCoordsTranslated.y);
			// Counteract any scaling that may be happening so the icon stays the same size
			canvas.scale(1 / scaleX, 1 / scaleY, mMapCoordsTranslated.x, mMapCoordsTranslated.y);
			// Draw the bitmap
			canvas.drawBitmap(mDirectionArrowBitmap, mMapCoordsTranslated.x
					- mDirectionArrowCenterX, mMapCoordsTranslated.y - mDirectionArrowCenterY,
					mPaint);
			canvas.restore();
		} else {
			canvas.save();
			// Unrotate the icon if the maps are rotated so the little man stays upright
			canvas.rotate(-mMapView.getMapOrientation(), mMapCoordsTranslated.x,
					mMapCoordsTranslated.y);
			// Counteract any scaling that may be happening so the icon stays the same size
			canvas.scale(1 / scaleX, 1 / scaleY, mMapCoordsTranslated.x, mMapCoordsTranslated.y);
			// Draw the bitmap
			canvas.drawBitmap(mPersonBitmap, mMapCoordsTranslated.x - mPersonHotspot.x,
					mMapCoordsTranslated.y - mPersonHotspot.y, mPaint);
			canvas.restore();
		}
	}

	protected Rect getMyLocationDrawingBounds(int zoomLevel, Location lastFix, Rect reuse) {
		if (reuse == null)
			reuse = new Rect();

		final Projection pj = mMapView.getProjection();
		pj.toPixelsFromProjected(mMapCoordsProjected, mMapCoordsTranslated);

		// Start with the bitmap bounds
		if (lastFix.hasBearing()) {
			// Get a square bounding box around the object, and expand by the length of the diagonal
			// so as to allow for extra space for rotating
			int widestEdge = (int) Math.ceil(Math.max(mDirectionArrowBitmap.getWidth(),
					mDirectionArrowBitmap.getHeight()) * Math.sqrt(2));
			reuse.set(mMapCoordsTranslated.x, mMapCoordsTranslated.y, mMapCoordsTranslated.x
					+ widestEdge, mMapCoordsTranslated.y + widestEdge);
			reuse.offset(-widestEdge / 2, -widestEdge / 2);
		} else {
			reuse.set(mMapCoordsTranslated.x, mMapCoordsTranslated.y, mMapCoordsTranslated.x
					+ mPersonBitmap.getWidth(), mMapCoordsTranslated.y + mPersonBitmap.getHeight());
			reuse.offset((int) (-mPersonHotspot.x + 0.5f), (int) (-mPersonHotspot.y + 0.5f));
		}

		// Add in the accuracy circle if enabled
		if (mDrawAccuracyEnabled) {
			final int radius = (int) Math.ceil(lastFix.getAccuracy()
					/ (float) TileSystem.GroundResolution(lastFix.getLatitude(), zoomLevel));
			reuse.union(mMapCoordsTranslated.x - radius, mMapCoordsTranslated.y - radius,
					mMapCoordsTranslated.x + radius, mMapCoordsTranslated.y + radius);
			final int strokeWidth = (int) Math.ceil(mCirclePaint.getStrokeWidth() == 0 ? 1
					: mCirclePaint.getStrokeWidth());
			reuse.inset(-strokeWidth, -strokeWidth);
		}

		return reuse;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void draw(Canvas c, MapView mapView, boolean shadow) {
		if (shadow)
			return;

		if (mLocation != null && isMyLocationEnabled()) {
			drawMyLocation(c, mapView, mLocation);
		}
	}

	@Override
	public boolean onSnapToItem(final int x, final int y, final Point snapPoint,
			final IMapView mapView) {
		if (this.mLocation != null) {
			Projection pj = mMapView.getProjection();
			pj.toPixelsFromProjected(mMapCoordsProjected, mMapCoordsTranslated);
			snapPoint.x = mMapCoordsTranslated.x;
			snapPoint.y = mMapCoordsTranslated.y;
			final double xDiff = x - mMapCoordsTranslated.x;
			final double yDiff = y - mMapCoordsTranslated.y;
			boolean snap = xDiff * xDiff + yDiff * yDiff < 64;
			if (DEBUGMODE) {
                    Log.d(IMapView.LOGTAG, "snap=" + snap);
			}
			return snap;
		} else {
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			this.disableFollowLocation();
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

		if (location != null) {
			// These location updates can come in from different threads
			mHandler.postAtTime(new Runnable() {
				@Override
				public void run() {
					setLocation(location);

					for (final Runnable runnable : mRunOnFirstFix) {
						new Thread(runnable).start();
					}
					mRunOnFirstFix.clear();
				}
			}, mHandlerToken, 0);
		}
	}

	protected void setLocation(Location location) {
		// If we had a previous location, let's get those bounds
		Location oldLocation = mLocation;
		if (oldLocation != null) {
			this.getMyLocationDrawingBounds(mMapView.getZoomLevel(), oldLocation,
					mMyLocationPreviousRect);
		}

		mLocation = location;

		// Cache location point
		mMapView.getProjection().toProjectedPixels((int) (mLocation.getLatitude() * 1E6),
				(int) (mLocation.getLongitude() * 1E6), mMapCoordsProjected);

		if (mIsFollowing) {
			mGeoPoint.setLatitudeE6((int) (mLocation.getLatitude() * 1E6));
			mGeoPoint.setLongitudeE6((int) (mLocation.getLongitude() * 1E6));
			mMapController.animateTo(mGeoPoint);
		} else {
			// Get new drawing bounds
			this.getMyLocationDrawingBounds(mMapView.getZoomLevel(), mLocation, mMyLocationRect);

			// If we had a previous location, merge in those bounds too
			if (oldLocation != null) {
				mMyLocationRect.union(mMyLocationPreviousRect);
			}

			final int left = mMyLocationRect.left;
			final int top = mMyLocationRect.top;
			final int right = mMyLocationRect.right;
			final int bottom = mMyLocationRect.bottom;

			// Invalidate the bounds
			mMapView.invalidateMapCoordinates(left, top, right, bottom);
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
			new Thread(runnable).start();
			return true;
		} else {
			mRunOnFirstFix.addLast(runnable);
			return false;
		}
	}
     
     /**
      * enabls you to change the my location 'person' icon at runtime. note that the
      * hotspot is not updated with this method. see 
      * {@link #setPersonHotspot}
      * @param icon 
      */
     public void setPersonIcon(Bitmap icon){
          mPersonBitmap = icon;
     }
}
