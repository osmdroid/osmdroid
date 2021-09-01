package org.osmdroid.events;

/*
 * An abstract adapter class for receiving map events. The methods in this class are empty.
 * This class exists as convenience for creating listener objects.
 *
 * @author Theodore Hong
 */
public abstract class MapAdapter implements MapListener {
    @Override
    public boolean onScroll(final ScrollEvent event) {
        // do nothing
        return false;
    }

    @Override
    public boolean onZoom(final ZoomEvent event) {
        // do nothing
        return false;
    }
}
