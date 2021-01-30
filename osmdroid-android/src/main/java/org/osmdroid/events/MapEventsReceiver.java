package org.osmdroid.events;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;

/**
 * Interface for objects that need to handle map events thrown by a MapEventsOverlay.
 *
 * @author M.Kergall
 * @see MapEventsOverlay
 */
public interface MapEventsReceiver {

    /**
     * @param p the position where the event occurred.
     * @return true if the event has been "consumed" and should not be handled by other objects.
     */
    boolean singleTapConfirmedHelper(GeoPoint p);

    /**
     * @param p the position where the event occurred.
     * @return true if the event has been "consumed" and should not be handled by other objects.
     */
    boolean longPressHelper(GeoPoint p);
}
