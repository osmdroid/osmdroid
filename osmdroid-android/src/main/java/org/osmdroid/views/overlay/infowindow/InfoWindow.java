package org.osmdroid.views.overlay.infowindow;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.drawing.MapSnapshot;

import java.util.ArrayList;

/**
 * {@link org.osmdroid.views.overlay.infowindow.InfoWindow} is a (pop-up-) View that can
 * be displayed on an {@link org.osmdroid.views.MapView}, associated to a
 * {@link org.osmdroid.api.IGeoPoint}.
 * <p>
 * Typical usage: cartoon-like bubbles displayed when clicking an overlay item (i.e. a
 * {@link org.osmdroid.views.overlay.Marker}).
 * It mimics the InfoWindow class of Google Maps JavaScript API V3.
 * Main differences are:
 * <ul>
 * <li>Structure and content of the view is let to the responsibility of the caller. </li>
 * <li>The same InfoWindow can be associated to many items. </li>
 * </ul>
 * <p>
 * This is an abstract class.
 * <p>
 * <img alt="Class diagram around Marker class" width="686" height="413" src='./doc-files/marker-infowindow-classes.png' />
 *
 * @author M.Kergall
 * @see MarkerInfoWindow
 */
public abstract class InfoWindow {

    protected View mView;
    protected boolean mIsVisible;
    protected MapView mMapView;
    protected Object mRelatedObject;
    private GeoPoint mPosition;
    private int mOffsetX, mOffsetY;

    /**
     * @param layoutResId the id of the view resource.
     * @param mapView     the mapview on which is hooked the view
     */
    public InfoWindow(int layoutResId, MapView mapView) {
        mMapView = mapView;
        mMapView.getRepository().add(this);
        mIsVisible = false;
        ViewGroup parent = (ViewGroup) mapView.getParent();
        Context context = mapView.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(layoutResId, parent, false);
        mView.setTag(this);
    }

    public InfoWindow(View v, MapView mapView) {
        mMapView = mapView;
        mIsVisible = false;
        mView = v;
        mView.setTag(this);
    }

    /**
     * Allows to link an Object (any Object) to this marker.
     * This is particularly useful to handle custom InfoWindow.
     */
    public void setRelatedObject(Object relatedObject) {
        mRelatedObject = relatedObject;
    }

    /**
     * @return the related object.
     */
    public Object getRelatedObject() {
        return mRelatedObject;
    }

    /**
     * may return null if the info window hasn't been attached yet
     *
     * @return
     */
    public MapView getMapView() {
        return mMapView;
    }

    /**
     * Returns the Android view. This allows to set its content.
     *
     * @return the Android view
     */
    public View getView() {
        return (mView);
    }

    /**
     * open the InfoWindow at the specified GeoPosition + offset.
     * If it was already opened, close it before reopening.
     *
     * @param object   the graphical object on which is hooked the view
     * @param position to place the window on the map
     * @param offsetX  (&offsetY) the offset of the view to the position, in pixels.
     *                 This allows to offset the view from the object position.
     */
    public void open(Object object, GeoPoint position, int offsetX, int offsetY) {
        close(); //if it was already opened
        mRelatedObject = object;
        mPosition = position;
        mOffsetX = offsetX;
        mOffsetY = offsetY;
        onOpen(object);
        MapView.LayoutParams lp = new MapView.LayoutParams(
                MapView.LayoutParams.WRAP_CONTENT,
                MapView.LayoutParams.WRAP_CONTENT,
                mPosition, MapView.LayoutParams.BOTTOM_CENTER,
                mOffsetX, mOffsetY);

        if (mMapView != null && mView != null) {
            mMapView.addView(mView, lp);
            mIsVisible = true;
        } else {
            Log.w(IMapView.LOGTAG, "Error trapped, InfoWindow.open mMapView: " + (mMapView == null ? "null" : "ok") + " mView: " + (mView == null ? "null" : "ok"));
        }
    }

    /**
     * refresh the infowindow drawing. Must be called every time the view changes (drag, zoom,...).
     * Best practice is to call this method in the draw method of its overlay.
     */
    public void draw() {
        if (!mIsVisible)
            return;
        try {
            MapView.LayoutParams lp = new MapView.LayoutParams(
                    MapView.LayoutParams.WRAP_CONTENT,
                    MapView.LayoutParams.WRAP_CONTENT,
                    mPosition, MapView.LayoutParams.BOTTOM_CENTER,
                    mOffsetX, mOffsetY);
            mMapView.updateViewLayout(mView, lp); // supposed to work only on the UI Thread
        } catch (Exception e) {
            if (MapSnapshot.isUIThread()) {
                throw e;
            }
            // in order to avoid a CalledFromWrongThreadException crash
        }
    }

    /**
     * hides the info window, which triggers another render of the map
     */
    public void close() {
        if (mIsVisible) {
            mIsVisible = false;
            ((ViewGroup) mView.getParent()).removeView(mView);
            onClose();
        }
    }

    /**
     * this destroys the window and all references to views
     */
    public void onDetach() {
        close();
        if (mView != null)
            mView.setTag(null);
        mView = null;
        mMapView = null;
        if (Configuration.getInstance().isDebugMode())
            Log.d(IMapView.LOGTAG, "Marked detached");
    }

    public boolean isOpen() {
        return mIsVisible;
    }

    /**
     * close all InfoWindows currently opened on this MapView
     *
     * @param mapView
     */
    public static void closeAllInfoWindowsOn(MapView mapView) {
        ArrayList<InfoWindow> opened = getOpenedInfoWindowsOn(mapView);
        for (InfoWindow infoWindow : opened) {
            infoWindow.close();
        }
    }

    /**
     * return all InfoWindows currently opened on this MapView
     *
     * @param mapView
     * @return
     */
    public static ArrayList<InfoWindow> getOpenedInfoWindowsOn(MapView mapView) {
        int count = mapView.getChildCount();
        ArrayList<InfoWindow> opened = new ArrayList<InfoWindow>(count);
        for (int i = 0; i < count; i++) {
            final View child = mapView.getChildAt(i);
            Object tag = child.getTag();
            if (tag != null && tag instanceof InfoWindow) {
                InfoWindow infoWindow = (InfoWindow) tag;
                opened.add(infoWindow);
            }
        }
        return opened;
    }

    //Abstract methods to implement in sub-classes:
    public abstract void onOpen(Object item);

    public abstract void onClose();

}
