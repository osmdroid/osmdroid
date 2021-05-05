package org.osmdroid.views.overlay.compass;


public interface IOrientationConsumer {
    /**
     * @param orientation this is magnetic north, not true north
     * @param source
     */
    void onOrientationChanged(float orientation, IOrientationProvider source);
}
