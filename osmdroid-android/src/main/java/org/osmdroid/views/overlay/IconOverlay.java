package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

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
    protected Drawable mIcon = null;
    protected IGeoPoint mPosition = null;

    protected float mBearing = 0.0f;
    protected float mAnchorU = ANCHOR_CENTER, mAnchorV = ANCHOR_CENTER;
    protected float mAlpha = 1.0f; //opaque

    protected boolean mFlat = false; //billboard;

    protected Point mPositionPixels = new Point();

    /**
     * save to be called in non-gui-thread
     */
    public IconOverlay() {
    }

    /**
     * save to be called in non-gui-thread
     */
    public IconOverlay(IGeoPoint position, Drawable icon) {
        set(position, icon);
    }

    /**
     * Draw the icon.
     */
    @Override
    public void draw(Canvas canvas, Projection pj) {
        if (mIcon == null)
            return;
        if (mPosition == null)
            return;

        pj.toPixels(mPosition, mPositionPixels);
        int width = mIcon.getIntrinsicWidth();
        int height = mIcon.getIntrinsicHeight();
        Rect rect = new Rect(0, 0, width, height);
        rect.offset(-(int) (mAnchorU * width), -(int) (mAnchorV * height));
        mIcon.setBounds(rect);

        mIcon.setAlpha((int) (mAlpha * 255));

        float rotationOnScreen = (mFlat ? -mBearing : pj.getOrientation() - mBearing);
        drawAt(canvas, mIcon, mPositionPixels.x, mPositionPixels.y, false, rotationOnScreen);
    }

    public IGeoPoint getPosition() {
        return mPosition;
    }

    public IconOverlay set(IGeoPoint position, Drawable icon) {
        this.mPosition = position;
        this.mIcon = icon;
        return this;
    }

    public IconOverlay moveTo(final MotionEvent event, final MapView mapView) {
        final Projection pj = mapView.getProjection();
        moveTo(pj.fromPixels((int) event.getX(), (int) event.getY()), mapView);
        return this;
    }

    public IconOverlay moveTo(final IGeoPoint position, final MapView mapView) {
        mPosition = position;
        mapView.invalidate();
        return this;
    }
}
