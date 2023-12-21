package org.osmdroid.views.overlay;

import android.graphics.Rect;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

public interface IViewBoundingBoxChangedListener {

    /**
     * Event raised when view bouding box changes.<br>
     * <br>
     * (all coords are in Tile units)
     */
    @UiThread @MainThread
    public void onViewBoundingBoxChanged(@NonNull Rect fromBounds, int fromZoom, @NonNull Rect toBounds, int toZoom);

}
