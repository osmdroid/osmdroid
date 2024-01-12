package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * {@link org.osmdroid.views.overlay.IconOverlay} is an icon placed at a particular
 * {@link org.osmdroid.api.IGeoPoint} on the {@link org.osmdroid.views.MapView}'s surface.
 * <p>
 * Inspired by {@link Marker} but without the Datafields and the pop-window support.
 * <p>
 * Created by k3b on 16.07.2015.
 */
public class IconOverlay extends Overlay {
    /**
     * Usual values in the (U,V) coordinates system of the icon image
     */
    public static final float ANCHOR_CENTER = 0.5f, ANCHOR_LEFT = 0.0f, ANCHOR_TOP = 0.0f, ANCHOR_RIGHT = 1.0f, ANCHOR_BOTTOM = 1.0f;

    /*attributes for standard features:*/
    @Nullable
    protected Drawable mIcon = null;
    protected final GeoPoint mPosition = new GeoPoint(0d, 0d, 0d);

    protected float mBearing = 0.0f;
    protected float mAnchorU = ANCHOR_CENTER, mAnchorV = ANCHOR_CENTER;
    protected float mAlpha = 1.0f; //opaque

    protected boolean mFlat = false; //billboard;

    protected final Point mPositionPixels = new Point();
    private final Rect mRect = new Rect();

    /**
     * save to be called in non-gui-thread
     */
    public IconOverlay() {
    }

    /**
     * save to be called in non-gui-thread
     */
    public IconOverlay(@NonNull final IGeoPoint position, @NonNull final Drawable icon) {
        set(position, icon);
    }

    /**
     * Draw the icon.
     */
    @Override
    public void draw(Canvas canvas, Projection pj) {
        if (mIcon == null) return;

        pj.toPixels(mPosition, mPositionPixels);
        int width = mIcon.getIntrinsicWidth();
        int height = mIcon.getIntrinsicHeight();
        mRect.set(0, 0, width, height);
        mRect.offset(-(int) (mAnchorU * width), -(int) (mAnchorV * height));
        mIcon.setBounds(mRect);

        mIcon.setAlpha((int) (mAlpha * 255));

        float rotationOnScreen = (mFlat ? -mBearing : pj.getOrientation() - mBearing);
        drawAt(canvas, mIcon, mPositionPixels.x, mPositionPixels.y, false, rotationOnScreen);
    }

    public IGeoPoint getPosition() {
        return mPosition;
    }

    public IconOverlay set(@NonNull final IGeoPoint position, @NonNull final Drawable icon) {
        this.mPosition.setCoords(position.getLatitude(), position.getLongitude());
        this.mIcon = icon;
        return this;
    }

    @Deprecated
    public IconOverlay moveTo(@NonNull final MotionEvent event, @NonNull final MapView mapView) { return moveTo(event, mapView, null); }
    public IconOverlay moveTo(@NonNull final MotionEvent event, @NonNull final MapView mapView, @Nullable GeoPoint reuse) {
        final Projection pj = mapView.getProjection();
        if (reuse == null) reuse = new GeoPoint(0d, 0d, 0d);
        moveTo(pj.fromPixels((int) event.getX(), (int) event.getY(), reuse), mapView);
        return this;
    }

    public IconOverlay moveTo(@NonNull final IGeoPoint position, @NonNull final MapView mapView) {
        mPosition.setCoords(position.getLatitude(), position.getLongitude());
        mapView.invalidate();
        return this;
    }
}
