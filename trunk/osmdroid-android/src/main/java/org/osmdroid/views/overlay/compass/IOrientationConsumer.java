package org.osmdroid.views.overlay.compass;


public interface IOrientationConsumer
{
    void onOrientationChanged(float orientation, IOrientationProvider source);
}
