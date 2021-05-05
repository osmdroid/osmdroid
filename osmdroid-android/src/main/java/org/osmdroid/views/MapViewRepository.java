package org.osmdroid.views;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.osmdroid.library.R;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.HashSet;
import java.util.Set;

/**
 * Repository for a MapView
 * Designed for "singleton-like" objects that need a clean detach
 *
 * @author Fabrice Fontaine
 * @since 6.0.3
 */
public class MapViewRepository {

    private MapView mMapView;
    private MarkerInfoWindow mDefaultMarkerInfoWindow;
    private BasicInfoWindow mDefaultPolylineInfoWindow;
    private BasicInfoWindow mDefaultPolygonInfoWindow;
    private Drawable mDefaultMarkerIcon;
    private final Set<InfoWindow> mInfoWindowList = new HashSet<>();

    public MapViewRepository(final MapView pMapView) {
        mMapView = pMapView;
    }

    public void add(final InfoWindow pInfoWindow) {
        mInfoWindowList.add(pInfoWindow);
    }

    public void onDetach() {
        synchronized (mInfoWindowList) {
            for (final InfoWindow infoWindow : mInfoWindowList) {
                infoWindow.onDetach();
            }
            mInfoWindowList.clear();
        }

        mMapView = null;
        mDefaultMarkerInfoWindow = null;
        mDefaultPolylineInfoWindow = null;
        mDefaultPolygonInfoWindow = null;
        mDefaultMarkerIcon = null;
    }

    public MarkerInfoWindow getDefaultMarkerInfoWindow() {
        if (mDefaultMarkerInfoWindow == null) {
            mDefaultMarkerInfoWindow = new MarkerInfoWindow(R.layout.bonuspack_bubble, mMapView);
        }
        return mDefaultMarkerInfoWindow;
    }

    public BasicInfoWindow getDefaultPolylineInfoWindow() {
        if (mDefaultPolylineInfoWindow == null) {
            mDefaultPolylineInfoWindow = new BasicInfoWindow(R.layout.bonuspack_bubble, mMapView);
        }
        return mDefaultPolylineInfoWindow;
    }

    public BasicInfoWindow getDefaultPolygonInfoWindow() {
        if (mDefaultPolygonInfoWindow == null) {
            mDefaultPolygonInfoWindow = new BasicInfoWindow(R.layout.bonuspack_bubble, mMapView);
        }
        return mDefaultPolygonInfoWindow;
    }

    /**
     * note: it's possible for this to return null during certain lifecycle events. Such as
     * invoke this method after {@link #onDetach()} has been called
     *
     * @return
     */
    public Drawable getDefaultMarkerIcon() {
        if (mDefaultMarkerIcon == null) {
            if (mMapView != null) {
                Context context = mMapView.getContext();
                if (context != null) {
                    mDefaultMarkerIcon = context.getResources().getDrawable(R.drawable.marker_default);
                }
            }

        }
        return mDefaultMarkerIcon;
    }
}
