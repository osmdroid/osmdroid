package org.osmdroid.views.overlay.compass;


import androidx.annotation.NonNull;

public interface IOrientationProvider {
    boolean startOrientationProvider(@NonNull IOrientationConsumer orientationConsumer);

    void stopOrientationProvider();

    float getLastKnownOrientation();

    void destroy();
}
