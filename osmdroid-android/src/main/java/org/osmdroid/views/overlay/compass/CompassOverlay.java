// Created by plusminus on 22:01:11 - 29.09.2008
package org.osmdroid.views.overlay.compass;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.IOverlayMenuProvider;
import org.osmdroid.views.overlay.SafeDrawOverlay;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import org.osmdroid.views.safecanvas.ISafeCanvas.UnsafeCanvasHandler;
import org.osmdroid.views.safecanvas.SafePaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.FloatMath;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.WindowManager;

/**
 * 
 * @author Marc Kurtz
 * @author Manuel Stahl
 * 
 */
public class CompassOverlay extends SafeDrawOverlay implements IOverlayMenuProvider, IOrientationConsumer
{
    protected final MapView mMapView;
    private final Display mDisplay;

    public IOrientationProvider mOrientationProvider;

    protected final SafePaint mPaint = new SafePaint();
	protected Bitmap mCompassFrameBitmap;
	protected Bitmap mCompassRoseBitmap;
    private final Matrix mCompassMatrix = new Matrix();
	private final Matrix mCanvasIdentityMatrix = new Matrix();
    private boolean mIsCompassEnabled;

    /**
     * The bearing, in degrees east of north, or NaN if none has been set.
     */
    private float mAzimuth = Float.NaN;

    private float mCompassCenterX = 35.0f;
    private float mCompassCenterY = 35.0f;
    private final float mCompassRadius = 20.0f;

    protected final float mCompassFrameCenterX;
    protected final float mCompassFrameCenterY;
    protected final float mCompassRoseCenterX;
    protected final float mCompassRoseCenterY;

    public static final int MENU_COMPASS = getSafeMenuId();

    private boolean mOptionsMenuEnabled = true;

    // ===========================================================
    // Constructors
    // ===========================================================

	public CompassOverlay(Context context, MapView mapView) {
		this(context, new InternalCompassOrientationProvider(context), mapView);
	}

	public CompassOverlay(Context context, IOrientationProvider orientationProvider, MapView mapView)
    {
        this(context, orientationProvider, mapView, new DefaultResourceProxyImpl(context));
    }

	public CompassOverlay(Context context, IOrientationProvider orientationProvider,
			MapView mapView, ResourceProxy pResourceProxy)
    {
        super(pResourceProxy);

        mMapView = mapView;
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();

        createCompassFramePicture();
        createCompassRosePicture();

		mCompassFrameCenterX = mCompassFrameBitmap.getWidth() / 2 - 0.5f;
		mCompassFrameCenterY = mCompassFrameBitmap.getHeight() / 2 - 0.5f;
		mCompassRoseCenterX = mCompassRoseBitmap.getWidth() / 2 - 0.5f;
		mCompassRoseCenterY = mCompassRoseBitmap.getHeight() / 2 - 0.5f;

		setOrientationProvider(orientationProvider);
    }

    @Override
    public void onDetach(MapView mapView) {
        this.disableCompass();
        super.onDetach(mapView);
    }

    private void invalidateCompass()
    {
        Rect screenRect = mMapView.getProjection().getScreenRect();
        final int frameLeft = screenRect.left + (mMapView.getWidth() / 2)
                + (int) FloatMath.ceil((mCompassCenterX - mCompassFrameCenterX) * mScale);
        final int frameTop = screenRect.top + (mMapView.getHeight() / 2)
                + (int) FloatMath.ceil((mCompassCenterY - mCompassFrameCenterY) * mScale);
        final int frameRight = screenRect.left + (mMapView.getWidth() / 2)
                + (int) FloatMath.ceil((mCompassCenterX + mCompassFrameCenterX) * mScale);
        final int frameBottom = screenRect.top + (mMapView.getHeight() / 2)
                + (int) FloatMath.ceil((mCompassCenterY + mCompassFrameCenterY) * mScale);

        // Expand by 2 to cover stroke width
        mMapView.postInvalidate(frameLeft - 2, frameTop - 2, frameRight + 2, frameBottom + 2);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void setCompassCenter(final float x, final float y)
    {
        mCompassCenterX = x;
        mCompassCenterY = y;
    }

    public IOrientationProvider getOrientationProvider()
    {
        return mOrientationProvider;
    }

    protected void setOrientationProvider(IOrientationProvider orientationProvider)
    {
		if (orientationProvider == null)
			throw new RuntimeException(
					"You must pass an IOrientationProvider to setOrientationProvider()");

        if (mOrientationProvider != null)
            mOrientationProvider.stopOrientationProvider();

        mOrientationProvider = orientationProvider;
    }

	protected void drawCompass(final ISafeCanvas canvas, final float bearing, final Rect screenRect) {
		final float centerX = mCompassCenterX * mScale;
		final float centerY = mCompassCenterY * mScale;

		canvas.getMatrix(mCanvasIdentityMatrix);
		mCanvasIdentityMatrix.invert(mCanvasIdentityMatrix);

		mCompassMatrix.setTranslate(-mCompassFrameCenterX, -mCompassFrameCenterY);
		mCompassMatrix.postTranslate(centerX, centerY);

		canvas.getUnsafeCanvas(new UnsafeCanvasHandler() {
			@Override
			public void onUnsafeCanvas(Canvas canvas) {
				canvas.save();
				mMapView.invertCanvas(canvas);
				canvas.concat(mCompassMatrix);
				canvas.drawBitmap(mCompassFrameBitmap, 0, 0, null);
				canvas.restore();
			}
		});

		mCompassMatrix.setRotate(-bearing, mCompassRoseCenterX, mCompassRoseCenterY);
		mCompassMatrix.postTranslate(-mCompassRoseCenterX, -mCompassRoseCenterY);
		mCompassMatrix.postTranslate(centerX, centerY);

		canvas.getUnsafeCanvas(new UnsafeCanvasHandler() {
			@Override
			public void onUnsafeCanvas(Canvas canvas) {
				canvas.save();
				mMapView.invertCanvas(canvas);
				canvas.concat(mCompassMatrix);
				canvas.drawBitmap(mCompassRoseBitmap, 0, 0, null);
				canvas.restore();
			}
		});
	}

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow)
    {
        if (shadow) {
            return;
        }

        if (isCompassEnabled() && !Float.isNaN(mAzimuth)) {
            drawCompass(canvas, mAzimuth + getDisplayOrientation(), mapView.getProjection().getScreenRect());
        }
    }

    // ===========================================================
    // Menu handling methods
    // ===========================================================

    @Override
    public void setOptionsMenuEnabled(final boolean pOptionsMenuEnabled)
    {
        this.mOptionsMenuEnabled = pOptionsMenuEnabled;
    }

    @Override
    public boolean isOptionsMenuEnabled()
    {
        return this.mOptionsMenuEnabled;
    }

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
			final MapView pMapView)
	{
		pMenu.add(0, MENU_COMPASS + pMenuIdOffset, Menu.NONE,
				mResourceProxy.getString(ResourceProxy.string.compass))
				.setIcon(mResourceProxy.getDrawable(ResourceProxy.bitmap.ic_menu_compass))
				.setCheckable(true);

		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(final Menu pMenu, final int pMenuIdOffset, final MapView pMapView)
    {
		pMenu.findItem(MENU_COMPASS + pMenuIdOffset).setChecked(this.isCompassEnabled());
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem pItem, final int pMenuIdOffset, final MapView pMapView)
    {
        final int menuId = pItem.getItemId() - pMenuIdOffset;
        if (menuId == MENU_COMPASS) {
            if (this.isCompassEnabled()) {
                this.disableCompass();
            } else {
				this.enableCompass();
            }
            return true;
        } else {
            return false;
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    @Override
    public void onOrientationChanged(float orientation, IOrientationProvider source)
    {
        mAzimuth = orientation;
        this.invalidateCompass();
    }

	public boolean enableCompass(IOrientationProvider orientationProvider)
	{
		this.setOrientationProvider(orientationProvider);
		mIsCompassEnabled = false;
		return enableCompass();
	}

    /**
     * Enable receiving orientation updates from the provided IOrientationProvider and show a compass on the
     * map. You will likely want to call enableCompass() from your Activity's Activity.onResume() method, to
     * enable the features of this overlay. Remember to call the corresponding disableCompass() in your
     * Activity's Activity.onPause() method to turn off updates when in the background.
     */
    public boolean enableCompass()
    {
        boolean result = true;

		if (mIsCompassEnabled)
			mOrientationProvider.stopOrientationProvider();

        result = mOrientationProvider.startOrientationProvider(this);
        mIsCompassEnabled = result;

        // Update the screen to see changes take effect
        if (mMapView != null) {
            this.invalidateCompass();
        }

        return result;
    }

    /**
     * Disable orientation updates
     */
    public void disableCompass()
    {
        mIsCompassEnabled = false;

        if (mOrientationProvider != null) {
            mOrientationProvider.stopOrientationProvider();
        }

        // Reset values
        mAzimuth = Float.NaN;

        // Update the screen to see changes take effect
        if (mMapView != null) {
            this.invalidateCompass();
        }
    }

    /**
     * If enabled, the map is receiving orientation updates and drawing your location on the map.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isCompassEnabled()
    {
        return mIsCompassEnabled;
    }

    public float getOrientation()
    {
        return mAzimuth;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private Point calculatePointOnCircle(final float centerX, final float centerY, final float radius,
            final float degrees)
    {
        // for trigonometry, 0 is pointing east, so subtract 90
        // compass degrees are the wrong way round
        final double dblRadians = Math.toRadians(-degrees + 90);

        final int intX = (int) (radius * Math.cos(dblRadians));
        final int intY = (int) (radius * Math.sin(dblRadians));

        return new Point((int) centerX + intX, (int) centerY - intY);
    }

    private void drawTriangle(final Canvas canvas, final float x, final float y, final float radius,
            final float degrees, final Paint paint)
    {
        canvas.save();
        final Point point = this.calculatePointOnCircle(x, y, radius, degrees);
        canvas.rotate(degrees, point.x, point.y);
        final Path p = new Path();
        p.moveTo(point.x - 2 * mScale, point.y);
        p.lineTo(point.x + 2 * mScale, point.y);
        p.lineTo(point.x, point.y - 5 * mScale);
        p.close();
        canvas.drawPath(p, paint);
        canvas.restore();
    }

    private int getDisplayOrientation()
    {
        switch (mDisplay.getOrientation()) {
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                return 0;
        }
    }

    private void createCompassFramePicture()
    {
        // The inside of the compass is white and transparent
        final Paint innerPaint = new Paint();
        innerPaint.setColor(Color.WHITE);
        innerPaint.setAntiAlias(true);
        innerPaint.setStyle(Style.FILL);
        innerPaint.setAlpha(200);

        // The outer part (circle and little triangles) is gray and transparent
        final Paint outerPaint = new Paint();
        outerPaint.setColor(Color.GRAY);
        outerPaint.setAntiAlias(true);
        outerPaint.setStyle(Style.STROKE);
        outerPaint.setStrokeWidth(2.0f);
        outerPaint.setAlpha(200);

		final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2 * mScale);
        final int center = picBorderWidthAndHeight / 2;

		mCompassFrameBitmap = Bitmap.createBitmap(picBorderWidthAndHeight, picBorderWidthAndHeight,
				Config.ARGB_8888);
		final Canvas canvas = new Canvas(mCompassFrameBitmap);

        // draw compass inner circle and border
        canvas.drawCircle(center, center, mCompassRadius * mScale, innerPaint);
        canvas.drawCircle(center, center, mCompassRadius * mScale, outerPaint);

        // Draw little triangles north, south, west and east (don't move)
        // to make those move use "-bearing + 0" etc. (Note: that would mean to draw the triangles
        // in the onDraw() method)
        drawTriangle(canvas, center, center, mCompassRadius * mScale, 0, outerPaint);
        drawTriangle(canvas, center, center, mCompassRadius * mScale, 90, outerPaint);
        drawTriangle(canvas, center, center, mCompassRadius * mScale, 180, outerPaint);
        drawTriangle(canvas, center, center, mCompassRadius * mScale, 270, outerPaint);
    }

    private void createCompassRosePicture()
    {
        // Paint design of north triangle (it's common to paint north in red color)
        final Paint northPaint = new Paint();
        northPaint.setColor(0xFFA00000);
        northPaint.setAntiAlias(true);
        northPaint.setStyle(Style.FILL);
        northPaint.setAlpha(220);

        // Paint design of south triangle (black)
        final Paint southPaint = new Paint();
        southPaint.setColor(Color.BLACK);
        southPaint.setAntiAlias(true);
        southPaint.setStyle(Style.FILL);
        southPaint.setAlpha(220);

        // Create a little white dot in the middle of the compass rose
        final Paint centerPaint = new Paint();
        centerPaint.setColor(Color.WHITE);
        centerPaint.setAntiAlias(true);
        centerPaint.setStyle(Style.FILL);
        centerPaint.setAlpha(220);

		final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2 * mScale);
        final int center = picBorderWidthAndHeight / 2;

		mCompassRoseBitmap = Bitmap.createBitmap(picBorderWidthAndHeight, picBorderWidthAndHeight,
				Config.ARGB_8888);
		final Canvas canvas = new Canvas(mCompassRoseBitmap);

        // Blue triangle pointing north
        final Path pathNorth = new Path();
        pathNorth.moveTo(center, center - (mCompassRadius - 3) * mScale);
        pathNorth.lineTo(center + 4 * mScale, center);
        pathNorth.lineTo(center - 4 * mScale, center);
        pathNorth.lineTo(center, center - (mCompassRadius - 3) * mScale);
        pathNorth.close();
        canvas.drawPath(pathNorth, northPaint);

        // Red triangle pointing south
        final Path pathSouth = new Path();
        pathSouth.moveTo(center, center + (mCompassRadius - 3) * mScale);
        pathSouth.lineTo(center + 4 * mScale, center);
        pathSouth.lineTo(center - 4 * mScale, center);
        pathSouth.lineTo(center, center + (mCompassRadius - 3) * mScale);
        pathSouth.close();
        canvas.drawPath(pathSouth, southPaint);

        // Draw a little white dot in the middle
        canvas.drawCircle(center, center, 2, centerPaint);
    }
}
