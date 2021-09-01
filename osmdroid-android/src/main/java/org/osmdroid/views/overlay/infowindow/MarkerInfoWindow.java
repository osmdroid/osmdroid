package org.osmdroid.views.overlay.infowindow;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.osmdroid.api.IMapView;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

/**
 * {@link org.osmdroid.views.overlay.infowindow.MarkerInfoWindow} is the default
 * implementation of {@link org.osmdroid.views.overlay.infowindow.InfoWindow} for a
 * {@link org.osmdroid.views.overlay.Marker}.
 * <p>
 * It handles
 * <p>
 * R.id.bubble_title          = {@link org.osmdroid.views.overlay.OverlayWithIW#getTitle()},
 * R.id.bubble_subdescription = {@link org.osmdroid.views.overlay.OverlayWithIW#getSubDescription()},
 * R.id.bubble_description    = {@link org.osmdroid.views.overlay.OverlayWithIW#getSnippet()},
 * R.id.bubble_image          = {@link org.osmdroid.views.overlay.Marker#getImage()}
 * <p>
 * Description and sub-description interpret HTML tags (in the limits of the Html.fromHtml(String) API).
 * Clicking on the bubble will close it.
 *
 * <img alt="Class diagram around Marker class" width="686" height="413" src='./doc-files/marker-infowindow-classes.png' />
 *
 * @author M.Kergall
 */
public class MarkerInfoWindow extends BasicInfoWindow {

    protected Marker mMarkerRef; //reference to the Marker on which it is opened. Null if none.

    /**
     * @param layoutResId layout that must contain these ids: bubble_title,bubble_description,
     *                    bubble_subdescription, bubble_image
     * @param mapView
     */
    public MarkerInfoWindow(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
        //mMarkerRef = null;
    }

    /**
     * reference to the Marker on which it is opened. Null if none.
     *
     * @return
     */
    public Marker getMarkerReference() {
        return mMarkerRef;
    }

    @Override
    public void onOpen(Object item) {
        super.onOpen(item);

        mMarkerRef = (Marker) item;
        if (mView == null) {
            Log.w(IMapView.LOGTAG, "Error trapped, MarkerInfoWindow.open, mView is null!");
            return;
        }
        //handle image
        ImageView imageView = (ImageView) mView.findViewById(mImageId /*R.id.image*/);
        Drawable image = mMarkerRef.getImage();
        if (image != null) {
            imageView.setImageDrawable(image); //or setBackgroundDrawable(image)?
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setVisibility(View.VISIBLE);
        } else
            imageView.setVisibility(View.GONE);
    }

    @Override
    public void onClose() {
        super.onClose();
        mMarkerRef = null;
        //by default, do nothing else
    }

}
