package org.osmdroid.views.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.RectL;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapViewRepository;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.LinkedHashMap;

/**
 * A marker is an icon placed at a particular point on the map's surface that can have a popup-{@link org.osmdroid.views.overlay.infowindow.InfoWindow} (a bubble)
 * Mimics the Marker class from Google Maps Android API v2 as much as possible. Main differences:<br>
 * <p>
 * - Doesn't support Z-Index: as other osmdroid overlays, Marker is drawn in the order of appearance. <br>
 * - The icon can be any standard Android Drawable, instead of the BitmapDescriptor introduced in Google Maps API v2. <br>
 * - The icon can be changed at any time. <br>
 * - The InfoWindow hosts a standard Android View. It can handle Android widgets like buttons and so on. <br>
 * - Supports a "sub-description", to be displayed in the InfoWindow, under the snippet, in a smaller text font. <br>
 * - Supports an image, to be displayed in the InfoWindow. <br>
 * - Supports "panning to view" on/off option (when touching a marker, center the map on marker position). <br>
 * - Opening a Marker InfoWindow automatically close others only if it's the same InfoWindow shared between Markers. <br>
 * - Events listeners are set per marker, not per map. <br>
 *
 * <img alt="Class diagram around Marker class" width="686" height="413" src='src='./doc-files/marker-infowindow-classes.png' />
 *
 * @author M.Kergall
 * @see MarkerInfoWindow
 * see also <a href="http://developer.android.com/reference/com/google/android/gms/maps/model/Marker.html">Google Maps Marker</a>
 */
public class Marker extends OverlayWithIW {

    /* attributes for text labels, used for osmdroid gridlines */
    protected int mTextLabelBackgroundColor = Color.WHITE;
    protected int mTextLabelForegroundColor = Color.BLACK;
    protected int mTextLabelFontSize = 24;

    /*attributes for standard features:*/
    @Nullable
    protected Drawable mIcon;
    protected final GeoPoint mPosition = new GeoPoint(0.0d, 0.0d);
    protected float mBearing;
    protected float mAnchorU, mAnchorV;
    protected float mIWAnchorU, mIWAnchorV;
    protected float mAlpha;
    protected boolean mDraggable, mIsDragged;
    protected boolean mFlat;
    protected OnMarkerClickListener mOnMarkerClickListener;
    protected OnMarkerDragListener mOnMarkerDragListener;

    /*attributes for non-standard features:*/
    @Nullable
    protected Drawable mImage;
    protected boolean mPanToView;
    protected float mDragOffsetY;

    /*internals*/
    protected Point mPositionPixels;

    /**
     * Usual values in the (U,V) coordinates system of the icon image
     */
    public static final float ANCHOR_CENTER = 0.5f, ANCHOR_LEFT = 0.0f, ANCHOR_TOP = 0.0f, ANCHOR_RIGHT = 1.0f, ANCHOR_BOTTOM = 1.0f;

    /**
     * @since 6.0.3
     */
    private boolean mDisplayed;
    private final Rect mRect = new Rect();
    private final Rect mOrientedMarkerRect = new Rect();
    private final Rect mCanvasClipBounds = new Rect();
    //private Paint mPaint;
    private final Paint mBackgroundPaint = new Paint();
    private final Paint mTextPaint = new Paint();
    private final Canvas mImageCanvas = new Canvas();
    private static final int CONST_TEXT_ICONS_CACHE_CAPACITY = 32;
    private final LinkedHashMap<Long, BitmapDrawable> mTextIconsCache = new LinkedHashMap<>(CONST_TEXT_ICONS_CACHE_CAPACITY, 0.1f, true) {
        @Override
        protected boolean removeEldestEntry(Entry eldest) {
            return (mTextIconsCache.size() >= CONST_TEXT_ICONS_CACHE_CAPACITY);
        }
    };
    private final GeoPoint mLongPressReusableGeoPoint = new GeoPoint(0d, 0d, 0d);
    private final GeoPoint mMoveReusableGeoPoint = new GeoPoint(0d, 0d, 0d);
    @Nullable
    private static Drawable mDefaultMarkerIcon = null;

    public Marker(@NonNull final MapView mapView) {
        super();
        final MapViewRepository cMapViewRepository = mapView.getRepository();
        if (mDefaultMarkerIcon == null) mDefaultMarkerIcon = cMapViewRepository.getDefaultMarkerIcon();
        mBearing = 0.0f;
        mAlpha = 1.0f; //opaque
        mAnchorU = ANCHOR_CENTER;
        mAnchorV = ANCHOR_CENTER;
        mIWAnchorU = ANCHOR_CENTER;
        mIWAnchorV = ANCHOR_TOP;
        mDraggable = false;
        mIsDragged = false;
        mPositionPixels = new Point();
        mPanToView = true;
        mDragOffsetY = 0.0f;
        mFlat = false; //billboard
        mOnMarkerClickListener = null;
        mOnMarkerDragListener = null;
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        this.setTextLabelFontSize(mTextLabelFontSize);
        this.setTextLabelBackgroundColor(mTextLabelBackgroundColor);
        this.setTextLabelForegroundColor(mTextLabelForegroundColor);
        setDefaultIcon();
        setInfoWindow(cMapViewRepository.getDefaultMarkerInfoWindow());
    }

    /**
     * Sets the icon for the marker. Can be changed at any time.
     * This is used on the map view.
     * The anchor will be left unchanged; you may need to call {@link #setAnchor(float, float)}
     * Two exceptions:
     * - for text icons, the anchor is set to (center, center)
     * - for the default icon, the anchor is set to the corresponding position (the tip of the teardrop)
     * Related methods: {@link #setTextIcon(Context, String)}, {@link #setDefaultIcon()} and {@link #setAnchor(float, float)}
     *
     * @param icon if null, the default osmdroid marker is used.
     */
    public void setIcon(@Nullable final Drawable icon) {
        if (icon != null) {
            mIcon = icon;
        } else {
            setDefaultIcon();
        }
    }

    /**
     * @since 6.0.3
     */
    public void setDefaultIcon() {
        mIcon = mDefaultMarkerIcon;
        setAnchor(ANCHOR_CENTER, ANCHOR_BOTTOM);
    }

    /**
     * @since 6.0.3
     */
    @UiThread @MainThread
    public void setTextIcon(@NonNull final Context context, final String pText) {
        final int width = (int) (mTextPaint.measureText(pText) + 0.5f);
        final float baseline = (int) (-mTextPaint.ascent() + 0.5f);
        final int height = (int) (baseline + mTextPaint.descent() + 0.5f);
        BitmapDrawable imageDrawable = null;
        final long cKey = (((long)width) << 32) | (height & 0xffffffffL);
        if (mTextIconsCache.containsKey(cKey)) imageDrawable = mTextIconsCache.get(cKey);
        Bitmap image;
        if ((imageDrawable != null) && ((image = imageDrawable.getBitmap()) != null) &&
                (width <= image.getWidth()) && (height <= image.getHeight())
        ) {
            image.reconfigure(width, height, image.getConfig());
        } else image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mImageCanvas.setBitmap(image);
        mImageCanvas.drawPaint(mBackgroundPaint);
        mImageCanvas.drawText(pText, 0, baseline, mTextPaint);
        mTextIconsCache.put(cKey, (imageDrawable = new BitmapDrawable(context.getResources(), image)));
        mIcon = imageDrawable;
        setAnchor(ANCHOR_CENTER, ANCHOR_CENTER);
    }

    /**
     * @since 6.0.0?
     */
    @Nullable
    public Drawable getIcon() {
        return mIcon;
    }

    public GeoPoint getPosition() {
        return mPosition;
    }

    /**
     * sets the location on the planet where the icon is rendered
     */
    public void setPosition(@NonNull final GeoPoint position) {
        mPosition.setCoords(position.getLatitude(), position.getLongitude());
        mPosition.setAltitude(position.getAltitude());
        if (isInfoWindowShown()) {
            closeInfoWindow();
            showInfoWindow();
        }
        mBounds.set(position);
    }

    public float getRotation() {
        return mBearing;
    }

    /**
     * rotates the icon in relation to the map
     */
    public void setRotation(float rotation) {
        mBearing = rotation;
    }

    /**
     * @param anchorU WIDTH 0.0-1.0 percentage of the icon that offsets the logical center from the actual pixel center point
     * @param anchorV HEIGHT 0.0-1.0 percentage of the icon that offsets the logical center from the actual pixel center point
     */
    public void setAnchor(float anchorU, float anchorV) {
        mAnchorU = anchorU;
        mAnchorV = anchorV;
    }

    public void setInfoWindowAnchor(float anchorU, float anchorV) {
        mIWAnchorU = anchorU;
        mIWAnchorV = anchorV;
    }

    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }

    public float getAlpha() {
        return mAlpha;
    }

    public void setDraggable(boolean draggable) {
        mDraggable = draggable;
    }

    public boolean isDraggable() {
        return mDraggable;
    }

    public void setFlat(boolean flat) {
        mFlat = flat;
    }

    public boolean isFlat() {
        return mFlat;
    }

    /**
     * Removes this Marker from the MapView.
     * Note that this method will operate only if the Marker is in the MapView overlays
     * (it should not be included in a container like a FolderOverlay).
     */
    public void remove(@NonNull final MapView mapView) {
        mapView.getOverlayManager().remove(this);
    }

    public void setOnMarkerClickListener(@Nullable final OnMarkerClickListener listener) {
        mOnMarkerClickListener = listener;
    }

    public void setOnMarkerDragListener(@Nullable final OnMarkerDragListener listener) {
        mOnMarkerDragListener = listener;
    }

    /**
     * set an image to be shown in the InfoWindow  - this is not the marker icon
     */
    public void setImage(@Nullable final Drawable image) {
        mImage = image;
    }

    /**
     * get the image to be shown in the InfoWindow - this is not the marker icon
     */
    @Nullable
    public Drawable getImage() {
        return mImage;
    }

    /**
     * set the offset in millimeters that the marker is moved up while dragging
     */
    public void setDragOffset(float mmUp) {
        mDragOffsetY = mmUp;
    }

    /**
     * get the offset in millimeters that the marker is moved up while dragging
     */
    public float getDragOffset() {
        return mDragOffsetY;
    }

    /**
     * Set the InfoWindow to be used.
     * Default is a MarkerInfoWindow, with the layout named "bonuspack_bubble".
     * You can use this method either to use your own layout, or to use your own sub-class of InfoWindow.
     * Note that this InfoWindow will receive the Marker object as an input, so it MUST be able to handle Marker attributes.
     * If you don't want any InfoWindow to open, you can set it to null.
     */
    public void setInfoWindow(MarkerInfoWindow infoWindow) {
        mInfoWindow = infoWindow;
    }

    /**
     * If set to true, when clicking the marker, the map will be centered on the marker position.
     * Default is true.
     */
    public void setPanToView(boolean panToView) {
        mPanToView = panToView;
    }

    /**
     * shows the info window, if it's open, this will close and reopen it
     */
    public void showInfoWindow() {
        if ((mInfoWindow == null) || (mIcon == null))
            return;
        final int markerWidth = mIcon.getIntrinsicWidth();
        final int markerHeight = mIcon.getIntrinsicHeight();
        final int offsetX = (int) (markerWidth * (mIWAnchorU - mAnchorU));
        final int offsetY = (int) (markerHeight * (mIWAnchorV - mAnchorV));
        if (mBearing == 0) {
            mInfoWindow.open(this, mPosition, offsetX, offsetY);
            return;
        }
        final int centerX = 0;
        final int centerY = 0;
        final double radians = -mBearing * Math.PI / 180.;
        final double cos = Math.cos(radians);
        final double sin = Math.sin(radians);
        final int rotatedX = (int) RectL.getRotatedX(offsetX, offsetY, centerX, centerY, cos, sin);
        final int rotatedY = (int) RectL.getRotatedY(offsetX, offsetY, centerX, centerY, cos, sin);
        mInfoWindow.open(this, mPosition, rotatedX, rotatedY);
    }

    public boolean isInfoWindowShown() {
        if (mInfoWindow instanceof MarkerInfoWindow iw) {
            return iw.isOpen() && (iw.getMarkerReference() == this);
        } else
            return super.isInfoWindowOpen();
    }

    @Override
    public void draw(Canvas canvas, Projection pj) {
        if (mIcon == null)
            return;
        if (!isEnabled())
            return;

        pj.toPixels(mPosition, mPositionPixels);

        float rotationOnScreen = (mFlat ? -mBearing : -pj.getOrientation() - mBearing);
        drawAt(canvas, mPositionPixels.x, mPositionPixels.y, rotationOnScreen);
        if (isInfoWindowShown() && (mInfoWindow != null)) {
            mInfoWindow.draw();
        }
    }

    @Override
    public void onDestroy(@Nullable final MapView mapView) {
        BitmapPool.getInstance().asyncRecycle(mIcon);
        mIcon = null;
        BitmapPool.getInstance().asyncRecycle(mImage);
        mImage = null;
        //cleanDefaults();
        this.mOnMarkerClickListener = null;
        this.mOnMarkerDragListener = null;
        setRelatedObject(null);
        if (isInfoWindowShown())
            closeInfoWindow();

        setInfoWindow(null);
        super.onDestroy(mapView);
    }

    /**
     * Prevent memory leaks and call this when you're done with the map
     * reference <a href="https://github.com/MKergall/osmbonuspack/pull/210">...</a>
     */
    @Deprecated
    public static void cleanDefaults() { /*nothing*/ }

    public boolean hitTest(@NonNull final MotionEvent event, @Nullable final MapView mapView) {
        return mIcon != null && mDisplayed && mOrientedMarkerRect.contains((int) event.getX(), (int) event.getY()); // "!=null": fix for #1078
    }

    @Override
    public boolean onSingleTapConfirmed(@NonNull final MotionEvent event, @NonNull final MapView mapView) {
        boolean touched = hitTest(event, mapView);
        if (touched) {
            if (mOnMarkerClickListener == null) {
                return onMarkerClickDefault(this, mapView);
            } else {
                return mOnMarkerClickListener.onMarkerClick(this, mapView);
            }
        } else
            return touched;
    }

    public void moveToEventPosition(@NonNull final MotionEvent event, @NonNull final MapView mapView, @NonNull final GeoPoint reuse) {
        final float offsetY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mDragOffsetY, mapView.getContext().getResources().getDisplayMetrics());
        final Projection pj = mapView.getProjection();
        setPosition((GeoPoint) pj.fromPixels((int) event.getX(), (int) (event.getY() - offsetY), reuse));
        mapView.invalidate();
    }

    @Override
    public boolean onLongPress(@NonNull final MotionEvent event, @NonNull final MapView mapView) {
        final boolean touched = hitTest(event, mapView);
        if (touched) {
            if (mDraggable) {
                //starts dragging mode:
                mIsDragged = true;
                closeInfoWindow();
                if (mOnMarkerDragListener != null)
                    mOnMarkerDragListener.onMarkerDragStart(this);
                moveToEventPosition(event, mapView, mLongPressReusableGeoPoint);
            }
        }
        return touched;
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event, @NonNull final MapView mapView) {
        if (mDraggable && mIsDragged) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mIsDragged = false;
                if (mOnMarkerDragListener != null)
                    mOnMarkerDragListener.onMarkerDragEnd(this);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                moveToEventPosition(event, mapView, mMoveReusableGeoPoint);
                if (mOnMarkerDragListener != null)
                    mOnMarkerDragListener.onMarkerDrag(this);
                return true;
            } else
                return false;
        } else
            return false;
    }

    public void setVisible(final boolean visible) {
        if (visible)
            setAlpha(1f);
        else setAlpha(0f);
    }

    //-- Marker events listener interfaces ------------------------------------

    public interface OnMarkerClickListener {
        boolean onMarkerClick(@NonNull Marker marker, @NonNull MapView mapView);
    }

    public interface OnMarkerDragListener {
        void onMarkerDrag(@NonNull Marker marker);
        void onMarkerDragEnd(@NonNull Marker marker);
        void onMarkerDragStart(@NonNull Marker marker);
    }

    /**
     * default behaviour when no click listener is set
     */
    protected boolean onMarkerClickDefault(@NonNull final Marker marker, @NonNull final MapView mapView) {
        marker.showInfoWindow();
        if (marker.mPanToView)
            mapView.getController().animateTo(marker.getPosition());
        return true;
    }

    /**
     * used for when the icon is explicitly set to null and the title is not, this will
     * style the rendered text label
     */
    public int getTextLabelBackgroundColor() {
        return mTextLabelBackgroundColor;
    }

    /**
     * used for when the icon is explicitly set to null and the title is not, this will
     * style the rendered text label
     */
    public void setTextLabelBackgroundColor(@ColorInt final int color) {
        mTextLabelBackgroundColor = color;
        mBackgroundPaint.setColor(color);
    }

    /**
     * used for when the icon is explicitly set to null and the title is not, this will
     * style the rendered text label
     */
    public int getTextLabelForegroundColor() {
        return mTextLabelForegroundColor;
    }

    /**
     * used for when the icon is explicitly set to null and the title is not, this will
     * style the rendered text label
     */
    public void setTextLabelForegroundColor(@ColorInt final int color) {
        mTextLabelForegroundColor = color;
        mTextPaint.setColor(color);
    }

    /**
     * used for when the icon is explicitly set to null and the title is not, this will
     * style the rendered text label
     */
    public int getTextLabelFontSize() {
        return mTextLabelFontSize;
    }

    /**
     * used for when the icon is explicitly set to null and the title is not, this will
     * style the rendered text label
     */
    public void setTextLabelFontSize(final int size) {
        mTextLabelFontSize = size;
        mTextPaint.setTextSize(size);
    }

    /**
     * @since 6.0.3
     */
    public boolean isDisplayed() {
        return mDisplayed;
    }

    /**
     * Optimized drawing
     *
     * @since 6.0.3
     */
    protected void drawAt(@NonNull final Canvas pCanvas, final int pX, final int pY, final float pOrientation) {
        if (mIcon == null) return;
        final int markerWidth = mIcon.getIntrinsicWidth();
        final int markerHeight = mIcon.getIntrinsicHeight();
        final int offsetX = pX - Math.round(markerWidth * mAnchorU);
        final int offsetY = pY - Math.round(markerHeight * mAnchorV);
        mRect.set(offsetX, offsetY, offsetX + markerWidth, offsetY + markerHeight);
        RectL.getBounds(mRect, pX, pY, pOrientation, mOrientedMarkerRect);
        pCanvas.getClipBounds(mCanvasClipBounds);
        mDisplayed = Rect.intersects(mOrientedMarkerRect, mCanvasClipBounds);
        if (!mDisplayed) { // optimization 1: (much faster, depending on the proportions) don't try to display if the Marker is not visible
            return;
        }
        if (mAlpha == 0) {
            return;
        }
        if (pOrientation != 0) { // optimization 2: don't manipulate the Canvas if not needed (about 25% faster) - step 1/2
            pCanvas.save();
            pCanvas.rotate(pOrientation, pX, pY);
        }
        /*
        if (mIcon instanceof BitmapDrawable) { 
            // optimization 3: (about 15% faster) - Unfortunate optimization with displayed size side effects: introduces issue #1738 
            final Paint paint;
            if (mAlpha == 1) {
                paint = null;
            } else {
                if (mPaint == null) {
                    mPaint = new Paint();
                }
                mPaint.setAlpha((int) (mAlpha * 255));
                paint = mPaint;
            }
            pCanvas.drawBitmap(((BitmapDrawable) mIcon).getBitmap(), offsetX, offsetY, paint);
        } else {
        */
            mIcon.setAlpha((int) (mAlpha * 255));
            mIcon.setBounds(mRect);
            mIcon.draw(pCanvas);
        //}
        if (pOrientation != 0) { // optimization 2: step 2/2
            pCanvas.restore();
        }
    }
}
