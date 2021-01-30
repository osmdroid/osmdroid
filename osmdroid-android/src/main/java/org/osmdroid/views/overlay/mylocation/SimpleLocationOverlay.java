// Created by plusminus on 22:01:11 - 29.09.2008
package org.osmdroid.views.overlay.mylocation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;

import org.osmdroid.library.R;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

/**
 * @author Nicolas Gramlich
 */
public class SimpleLocationOverlay extends Overlay {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    protected final Paint mPaint = new Paint();

    protected Bitmap PERSON_ICON;
    /**
     * Coordinates the feet of the person are located.
     */
    protected android.graphics.Point PERSON_HOTSPOT = new android.graphics.Point(24, 39);

    protected GeoPoint mLocation;
    private final Point screenCoords = new Point();

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * Use {@link #SimpleLocationOverlay(Bitmap) SimpleLocationOverlay}(((BitmapDrawable)ctx.getResources().getDrawable(R.drawable.person)).getBitmap()) instead.
     */
    @Deprecated
    public SimpleLocationOverlay(final Context ctx) {
        this(((BitmapDrawable) ctx.getResources().getDrawable(R.drawable.person)).getBitmap());
    }

    public SimpleLocationOverlay(final Bitmap theIcon) {
        super();
        this.PERSON_ICON = theIcon;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void setLocation(final GeoPoint mp) {
        this.mLocation = mp;
    }

    public GeoPoint getMyLocation() {
        return this.mLocation;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public void onDetach(MapView mapView) {
        //https://github.com/osmdroid/osmdroid/issues/477
        //commented out to prevent issues
        //this.PERSON_ICON.recycle();
    }

    @Override
    public void draw(final Canvas c, final Projection pj) {
        if (this.mLocation != null) {
            pj.toPixels(this.mLocation, screenCoords);

            c.drawBitmap(PERSON_ICON, screenCoords.x - PERSON_HOTSPOT.x, screenCoords.y
                    - PERSON_HOTSPOT.y, this.mPaint);
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * Coordinates the feet of the person are located.
     */
    public void setPersonIcon(Bitmap bmp, Point hotspot) {
        this.PERSON_ICON = bmp;
        this.PERSON_HOTSPOT = hotspot;
    }
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
