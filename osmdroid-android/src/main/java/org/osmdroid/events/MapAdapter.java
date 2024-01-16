package org.osmdroid.events;

import android.graphics.Rect;

import androidx.annotation.NonNull;

/**
 * An abstract adapter class for receiving map events. The methods in this class are empty.
 * This class exists as convenience for creating listener objects.
 *
 * @author Theodore Hong
 */
public abstract class MapAdapter implements MapListener {

    /** {@inheritDoc} */
    @Override
    public boolean onScroll(final ScrollEvent event) {
        // do nothing
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onZoom(final ZoomEvent event) {
        // do nothing
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void onViewBoundingBoxChanged(@NonNull Rect fromBounds, int fromZoom, @NonNull Rect toBounds, int toZoom) {

    }

}
