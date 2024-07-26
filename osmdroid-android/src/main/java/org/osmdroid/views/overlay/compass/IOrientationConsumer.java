package org.osmdroid.views.overlay.compass;


public interface IOrientationConsumer {
    /**
     * @param azimuth this is magnetic north, not true north
     */
    void onOrientationChanged(float azimuth, IOrientationProvider source);
}
