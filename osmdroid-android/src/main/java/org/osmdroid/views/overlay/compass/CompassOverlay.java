// Created by plusminus on 22:01:11 - 29.09.2008
package org.osmdroid.views.overlay.compass;

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
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.WindowManager;

import org.osmdroid.library.R;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.IOverlayMenuProvider;
import org.osmdroid.views.overlay.Overlay;

/**
 * Note: the compass overlay causes issues on API 8 devices. See https://github.com/osmdroid/osmdroid/issues/218
 * <p>
 * <br><br>
 * Note: this class can cause issues if you're also relying on {@link MapView#addOnFirstLayoutListener}
 * If you happen to be using both, see <a href="https://github.com/osmdroid/osmdroid/issues/324">Issue 324</a>
 *
 * @author Marc Kurtz
 * @author Manuel Stahl
 */
public class CompassOverlay extends Overlay implements IOverlayMenuProvider, IOrientationConsumer {
    private Paint sSmoothPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    protected MapView mMapView;
    private final Display mDisplay;

    public IOrientationProvider mOrientationProvider;

    protected Bitmap mCompassFrameBitmap;
    protected Bitmap mCompassRoseBitmap;
    private final Matrix mCompassMatrix = new Matrix();
    private boolean mIsCompassEnabled;
    private boolean wasEnabledOnPause = false;
    /**
     * +1 for conventional compass, -1 for direction indicator
     */
    private int mMode = 1;

    /**
     * The bearing, in degrees east of north, or NaN if none has been set.
     */
    private float mAzimuth = Float.NaN;
    private float mAzimuthOffset = 0.0f;

    /**
     * Ignore mCompassCenter* and put the compass in the center of the map
     */
    private boolean mInCenter = false;
    private float mCompassCenterX = 35.0f;
    private float mCompassCenterY = 35.0f;
    private final float mCompassRadius = 20.0f;

    protected final float mCompassFrameCenterX;
    protected final float mCompassFrameCenterY;
    protected final float mCompassRoseCenterX;
    protected final float mCompassRoseCenterY;
    protected long mLastRender = 0;
    public static final int MENU_COMPASS = getSafeMenuId();

    private boolean mOptionsMenuEnabled = true;

    protected final float mScale;

    /**
     * @since 6.20
     * rendering lag, in milliseconds
     * if the previous rendering was less than this value ago, we don't render again
     */
    private int mLastRenderLag = 500;

    /**
     * @since 6.20
     * azimuth/bearing precision, in degrees
     * if the previous bearing was equal to the new one, with this precision, we don't render again
     */
    private float mAzimuthPrecision = 0;

    // ===========================================================
    // Constructors
    // ===========================================================

    public CompassOverlay(Context context, MapView mapView) {
        this(context, new InternalCompassOrientationProvider(context), mapView);
    }


    public CompassOverlay(Context context, IOrientationProvider orientationProvider,
                          MapView mapView) {
        super();
        mScale = context.getResources().getDisplayMetrics().density;

        mMapView = mapView;
        final WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();

        createCompassFramePicture();
        if (mMode > 0)
            createCompassRosePicture();
        else
            createPointerPicture();

        mCompassFrameCenterX = mCompassFrameBitmap.getWidth() / 2f - 0.5f;
        mCompassFrameCenterY = mCompassFrameBitmap.getHeight() / 2f - 0.5f;
        mCompassRoseCenterX = mCompassRoseBitmap.getWidth() / 2f - 0.5f;
        mCompassRoseCenterY = mCompassRoseBitmap.getHeight() / 2f - 0.5f;

        setOrientationProvider(orientationProvider);
    }

    @Override
    public void onPause() {
        wasEnabledOnPause = mIsCompassEnabled;
        if (mOrientationProvider != null) {
            mOrientationProvider.stopOrientationProvider();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wasEnabledOnPause) {
            this.enableCompass();
        }
    }

    @Override
    public void onDetach(MapView mapView) {
        this.mMapView = null;
        sSmoothPaint = null;
        this.disableCompass();
        mOrientationProvider = null;
        mCompassFrameBitmap.recycle();
        mCompassRoseBitmap.recycle();
        super.onDetach(mapView);
    }

    /**
     * @since 6.20
     * rendering lag, in milliseconds
     */
    public void setLastRenderLag(final int pLastRenderLag) {
        mLastRenderLag = pLastRenderLag;
    }

    /**
     * @since 6.20
     * azimuth/bearing precision, in degrees
     */
    public void setAzimuthPrecision(final float pAzimuthPrecision) {
        mAzimuthPrecision = pAzimuthPrecision;
    }

    private void invalidateCompass() {
        if (mLastRender + mLastRenderLag > System.currentTimeMillis())
            return;
        mLastRender = System.currentTimeMillis();
        Rect screenRect = mMapView.getProjection().getScreenRect();
        int frameLeft;
        int frameRight;
        int frameTop;
        int frameBottom;
        if (mInCenter) {
            frameLeft = screenRect.left
                    + (int) Math.ceil(screenRect.exactCenterX() - mCompassFrameCenterX);
            frameTop = screenRect.top
                    + (int) Math.ceil(screenRect.exactCenterY() - mCompassFrameCenterY);
            frameRight = screenRect.left
                    + (int) Math.ceil(screenRect.exactCenterX() + mCompassFrameCenterX);
            frameBottom = screenRect.top
                    + (int) Math.ceil(screenRect.exactCenterY() + mCompassFrameCenterY);
        } else {
            frameLeft = screenRect.left
                    + (int) Math.ceil(mCompassCenterX * mScale - mCompassFrameCenterX);
            frameTop = screenRect.top
                    + (int) Math.ceil(mCompassCenterY * mScale - mCompassFrameCenterY);
            frameRight = screenRect.left
                    + (int) Math.ceil(mCompassCenterX * mScale + mCompassFrameCenterX);
            frameBottom = screenRect.top
                    + (int) Math.ceil(mCompassCenterY * mScale + mCompassFrameCenterY);
        }

        // Expand by 2 to cover stroke width
        mMapView.postInvalidateMapCoordinates(frameLeft - 2, frameTop - 2, frameRight + 2,
                frameBottom + 2);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void setCompassCenter(final float x, final float y) {
        mCompassCenterX = x;
        mCompassCenterY = y;
    }

    /**
     * Put the compass in the center of the map regardless of the supplied coordinates.
     */
    public void setCompassInCenter(boolean b) {
        mInCenter = b;
    }

    public boolean isCompassInCenter() {
        return mInCenter;
    }

    /**
     * An offset added to the bearing when drawing the compass.
     * eg. to account for local magnetic declination to indicate true north
     */
    public void setAzimuthOffset(float f) {
        mAzimuthOffset = f;
    }

    public float getAzimuthOffset() {
        return mAzimuthOffset;
    }

    public IOrientationProvider getOrientationProvider() {
        return mOrientationProvider;
    }

    public void setOrientationProvider(IOrientationProvider orientationProvider) throws RuntimeException {
        if (orientationProvider == null)
            throw new RuntimeException(
                    "You must pass an IOrientationProvider to setOrientationProvider()");

        if (isCompassEnabled())
            mOrientationProvider.stopOrientationProvider();

        mOrientationProvider = orientationProvider;
    }

    protected void drawCompass(final Canvas canvas, final float bearing, final Rect screenRect) {
        final Projection proj = mMapView.getProjection();

        float centerX;
        float centerY;
        if (mInCenter) {
            final Rect rect = proj.getScreenRect();
            centerX = rect.exactCenterX();
            centerY = rect.exactCenterY();
        } else {
            centerX = mCompassCenterX * mScale;
            centerY = mCompassCenterY * mScale;
        }

        mCompassMatrix.setTranslate(-mCompassFrameCenterX, -mCompassFrameCenterY);
        mCompassMatrix.postTranslate(centerX, centerY);

        proj.save(canvas, false, true);
        canvas.concat(mCompassMatrix);
        canvas.drawBitmap(mCompassFrameBitmap, 0, 0, sSmoothPaint);
        proj.restore(canvas, true);

        mCompassMatrix.setRotate(-bearing, mCompassRoseCenterX, mCompassRoseCenterY);
        mCompassMatrix.postTranslate(-mCompassRoseCenterX, -mCompassRoseCenterY);
        mCompassMatrix.postTranslate(centerX, centerY);

        proj.save(canvas, false, true);
        canvas.concat(mCompassMatrix);
        canvas.drawBitmap(mCompassRoseBitmap, 0, 0, sSmoothPaint);
        proj.restore(canvas, true);
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public void draw(Canvas c, Projection pProjection) {
        if (isCompassEnabled() && !Float.isNaN(mAzimuth)) {
            drawCompass(c, mMode * (mAzimuth + mAzimuthOffset + getDisplayOrientation()), pProjection
                    .getScreenRect());
        }
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
        pMenu.add(0, MENU_COMPASS + pMenuIdOffset, Menu.NONE,
                pMapView.getContext().getResources().getString(R.string.compass))

                .setIcon(pMapView.getContext().getResources().getDrawable(R.drawable.ic_menu_compass))
                .setCheckable(true);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
                                        final MapView pMapView) {
        pMenu.findItem(MENU_COMPASS + pMenuIdOffset).setChecked(this.isCompassEnabled());
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem pItem, final int pMenuIdOffset,
                                         final MapView pMapView) {
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
    public void onOrientationChanged(float orientation, IOrientationProvider source) {
        if (Float.isNaN(mAzimuth) || Math.abs(mAzimuth - orientation) >= mAzimuthPrecision) {
            mAzimuth = orientation;
            this.invalidateCompass();
        }
    }

    public boolean enableCompass(IOrientationProvider orientationProvider) {
        // Set the orientation provider. This will call stopOrientationProvider().
        setOrientationProvider(orientationProvider);

        boolean success = mOrientationProvider.startOrientationProvider(this);
        mIsCompassEnabled = success;

        // Update the screen to see changes take effect
        if (mMapView != null) {
            this.invalidateCompass();
        }

        return success;
    }

    /**
     * Enable receiving orientation updates from the provided IOrientationProvider and show a
     * compass on the map. You will likely want to call enableCompass() from your Activity's
     * Activity.onResume() method, to enable the features of this overlay. Remember to call the
     * corresponding disableCompass() in your Activity's Activity.onPause() method to turn off
     * updates when in the background.
     */
    public boolean enableCompass() {
        return enableCompass(mOrientationProvider);
    }

    /**
     * Disable orientation updates.
     * <p>
     * Note the behavior has changed since v6.0.0. This method no longer releases
     * references to the orientation provider. Instead, that happens in the onDetached
     * method.
     */
    public void disableCompass() {
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
    public boolean isCompassEnabled() {
        return mIsCompassEnabled;
    }

    public float getOrientation() {
        return mAzimuth;
    }

    /**
     * The compass can operate in two modes.
     * <ul>
     * <li>false - a conventional compass needle pointing north/south (false, default)</li>
     * <li>true - a pointer arrow that indicates the device's real world orientation on the map (true)</li>
     * </ul>
     * A different picture is used in each case.
     *
     * @param usePointArrow if true the pointer arrow is used, otherwise a compass rose is used
     * @since 6.0.0
     */
    public void setPointerMode(boolean usePointArrow) {
        if (usePointArrow) {
            mMode = -1;
            createPointerPicture();
        } else {
            mMode = 1;
            createCompassRosePicture();
        }
    }

    /**
     * @return true if we are in pointer mode, instead of compass mode
     * @since 6.0.0
     */
    public boolean isPointerMode() {
        return mMode < 0;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private Point calculatePointOnCircle(final float centerX, final float centerY,
                                         final float radius, final float degrees) {
        // for trigonometry, 0 is pointing east, so subtract 90
        // compass degrees are the wrong way round
        final double dblRadians = Math.toRadians(-degrees + 90);

        final int intX = (int) (radius * Math.cos(dblRadians));
        final int intY = (int) (radius * Math.sin(dblRadians));

        return new Point((int) centerX + intX, (int) centerY - intY);
    }

    private void drawTriangle(final Canvas canvas, final float x, final float y,
                              final float radius, final float degrees, final Paint paint) {
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

    private int getDisplayOrientation() {
        switch (mDisplay.getRotation()) {
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_0:
            default:
                return 0;
        }
    }

    private void createCompassFramePicture() {
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
        if (mCompassFrameBitmap != null)
            mCompassFrameBitmap.recycle();
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

    /**
     * A conventional red and black compass needle.
     */
    private void createCompassRosePicture() {
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

        if (mCompassRoseBitmap != null)
            mCompassRoseBitmap.recycle();
        mCompassRoseBitmap = Bitmap.createBitmap(picBorderWidthAndHeight, picBorderWidthAndHeight,
                Config.ARGB_8888);
        final Canvas canvas = new Canvas(mCompassRoseBitmap);

        // Triangle pointing north
        final Path pathNorth = new Path();
        pathNorth.moveTo(center, center - (mCompassRadius - 3) * mScale);
        pathNorth.lineTo(center + 4 * mScale, center);
        pathNorth.lineTo(center - 4 * mScale, center);
        pathNorth.lineTo(center, center - (mCompassRadius - 3) * mScale);
        pathNorth.close();
        canvas.drawPath(pathNorth, northPaint);

        // Triangle pointing south
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

    /**
     * A black pointer arrow.
     */
    private void createPointerPicture() {
        final Paint arrowPaint = new Paint();
        arrowPaint.setColor(Color.BLACK);
        arrowPaint.setAntiAlias(true);
        arrowPaint.setStyle(Style.FILL);
        arrowPaint.setAlpha(220);

        // Create a little white dot in the middle of the compass rose
        final Paint centerPaint = new Paint();
        centerPaint.setColor(Color.WHITE);
        centerPaint.setAntiAlias(true);
        centerPaint.setStyle(Style.FILL);
        centerPaint.setAlpha(220);

        final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2 * mScale);
        final int center = picBorderWidthAndHeight / 2;

        if (mCompassRoseBitmap != null)
            mCompassRoseBitmap.recycle();
        mCompassRoseBitmap = Bitmap.createBitmap(picBorderWidthAndHeight, picBorderWidthAndHeight,
                Config.ARGB_8888);
        final Canvas canvas = new Canvas(mCompassRoseBitmap);

        // Arrow comprised of 2 triangles
        final Path pathArrow = new Path();
        pathArrow.moveTo(center, center - (mCompassRadius - 3) * mScale);
        pathArrow.lineTo(center + 4 * mScale, center + (mCompassRadius - 3) * mScale);
        pathArrow.lineTo(center, center + 0.5f * (mCompassRadius - 3) * mScale);
        pathArrow.lineTo(center - 4 * mScale, center + (mCompassRadius - 3) * mScale);
        pathArrow.lineTo(center, center - (mCompassRadius - 3) * mScale);
        pathArrow.close();
        canvas.drawPath(pathArrow, arrowPaint);

        // Draw a little white dot in the middle
        canvas.drawCircle(center, center, 2, centerPaint);
    }
}
