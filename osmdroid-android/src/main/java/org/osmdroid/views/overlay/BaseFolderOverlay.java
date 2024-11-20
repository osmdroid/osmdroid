package org.osmdroid.views.overlay;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.MotionEvent;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for folder-style overlays that contain multiple {@link Overlay} objects.
 *
 * <p>This class is designed to be extended for managing groups of overlays. It includes functionality for
 * adding, removing, and interacting with contained overlays, as well as recalculating geographical bounds
 * for the group of overlays.</p>
 *
 * @param <T> the type of overlay contained within the folder.
 *
 * @author Daniil Timofeev (hestonic)
 * @author M.Kergall
 */
public abstract class BaseFolderOverlay<T extends Overlay> extends Overlay {

    /** The class type of the overlay, used for casting and type safety. */
    protected final Class<T> mClazz;

    protected OverlayManager mOverlayManager;
    protected String mName, mDescription;

    /**
     * Constructs a new {@link BaseFolderOverlay} with a specific overlay class type.
     *
     * @param mClazz the class type of the overlay, used for internal type checking and casting.
     */
    public BaseFolderOverlay(Class<T> mClazz) {
        this.mClazz = mClazz;
        mOverlayManager = new DefaultOverlayManager(null);
        mName = "";
        mDescription = "";
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    /**
     * Retrieves the list of overlays contained in this folder.
     *
     * <p>This method returns the actual list managed by the overlay manager, not a copy.</p>
     *
     * @return the list of overlays contained in this folder.
     *
     * @author M.Kergall
     */
    public List<T> getItems() {
        List<Overlay> overlays = mOverlayManager != null ? mOverlayManager.overlays() : new ArrayList<>();
        List<T> result = new ArrayList<>();
        for (Overlay overlay : overlays) {
            if (mClazz.isInstance(overlay)) {
                result.add(mClazz.cast(overlay));
            }
        }
        return result;
    }

    /**
     * Adds an overlay to this folder.
     *
     * <p>If the overlay is successfully added, the folder's geographical bounds are recalculated.</p>
     *
     * @param item the overlay to add.
     * @return {@code true} if the overlay was added successfully, {@code false} otherwise.
     *
     * @author M.Kergall
     */
    public boolean add(T item) {
        boolean added = mOverlayManager.add(item);
        if (added) {
            recalculateBounds();
        }
        return added;
    }

    /**
     * Removes an overlay from this folder.
     *
     * <p>If the overlay is successfully removed, the folder's geographical bounds are recalculated.</p>
     *
     * @param item the overlay to remove.
     * @return {@code true} if the overlay was removed successfully, {@code false} otherwise.
     *
     * @author M.Kergall
     */
    public boolean remove(T item) {
        boolean removed = mOverlayManager.remove(item);
        if (removed) {
            recalculateBounds();
        }
        return removed;
    }

    /**
     * Recalculates the geographical bounds of this folder based on its contained overlays.
     *
     * <p>This method calculates the minimum and maximum latitude and longitude values
     * from the bounds of each overlay in the folder.</p>
     *
     * @author M.Kergall
     */
    protected void recalculateBounds() {
        double minLat = Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;
        for (Overlay overlay : mOverlayManager) {
            BoundingBox box = overlay.getBounds();
            minLat = Math.min(minLat, box.getLatSouth());
            minLon = Math.min(minLon, box.getLonWest());
            maxLat = Math.max(maxLat, box.getLatNorth());
            maxLon = Math.max(maxLon, box.getLonEast());
        }

        if (minLat == Double.MAX_VALUE) {
            TileSystem tileSystem = MapView.getTileSystem();
            mBounds = new BoundingBox(
                    tileSystem.getMaxLatitude(),
                    tileSystem.getMaxLongitude(),
                    tileSystem.getMinLatitude(),
                    tileSystem.getMinLongitude()
            );
        } else {
            mBounds = new BoundingBox(maxLat, maxLon, minLat, minLon);
        }
    }

    @SuppressLint("WrongCall")
    @Override
    public void draw(Canvas pCanvas, Projection pProjection) {
        if (mOverlayManager != null) mOverlayManager.onDraw(pCanvas, pProjection);
    }

    @SuppressLint("WrongCall")
    @Override
    public void draw(Canvas pCanvas, MapView pMapView, boolean pShadow) {
        if (!pShadow && mOverlayManager != null) {
            mOverlayManager.onDraw(pCanvas, pMapView);
        }
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e, MapView mapView) {
        return isEnabled() && mOverlayManager != null && mOverlayManager.onSingleTapUp(e, mapView);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
        return isEnabled() && mOverlayManager != null && mOverlayManager.onSingleTapConfirmed(e, mapView);
    }

    @Override
    public boolean onLongPress(MotionEvent e, MapView mapView) {
        return isEnabled() && mOverlayManager != null && mOverlayManager.onLongPress(e, mapView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e, MapView mapView) {
        return isEnabled() && mOverlayManager != null && mOverlayManager.onTouchEvent(e, mapView);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e, MapView mapView) {
        return isEnabled() && mOverlayManager != null && mOverlayManager.onDoubleTap(e, mapView);
    }

    //TODO: implement other events...

    /**
     * Closes all open {@link org.osmdroid.views.overlay.infowindow.InfoWindow} instances within the contained overlays.
     *
     * <p>This method is only effective for overlays that inherit from {@link OverlayWithIW},
     * which provide info windows.</p>
     *
     * @author M.Kergall
     */
    public void closeAllInfoWindows() {
        for (Overlay overlay : mOverlayManager) {
            if (overlay instanceof BaseFolderOverlay<?>) {
                ((BaseFolderOverlay<?>) overlay).closeAllInfoWindows();
            } else if (overlay instanceof OverlayWithIW) {
                ((OverlayWithIW) overlay).closeInfoWindow();
            }
        }
    }

    @Override
    public void onDetach(MapView mapView) {
        if (mOverlayManager != null) {
            mOverlayManager.onDetach(mapView);
        }
        mOverlayManager = null;
    }
}