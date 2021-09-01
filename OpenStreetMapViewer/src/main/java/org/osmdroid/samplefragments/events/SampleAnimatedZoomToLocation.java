package org.osmdroid.samplefragments.events;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.ArrayList;

/**
 * @author Tyrone Tudehope
 */
public class SampleAnimatedZoomToLocation extends BaseSampleFragment {

    public static final String TITLE = "Animated Zoom to Location";

    private ItemizedOverlayWithFocus<OverlayItem> mMyLocationOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    private GpsMyLocationProvider mGpsMyLocationProvider;

    @Override
    public void onPause() {
        super.onPause();
        if (mGpsMyLocationProvider != null) {
            mGpsMyLocationProvider.stopLocationProvider();
        }
    }

    @Override
    public String getSampleTitle() {
        return TITLE;
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();

        final Context context = getActivity();
        Toast.makeText(getActivity(), "Make sure location services are enabled!", Toast.LENGTH_LONG).show();
        mGpsMyLocationProvider = new GpsMyLocationProvider(context);
        mGpsMyLocationProvider.startLocationProvider(new IMyLocationConsumer() {
            @Override
            public void onLocationChanged(Location location, IMyLocationProvider source) {
                mGpsMyLocationProvider.stopLocationProvider();
                if (mMyLocationOverlay == null) {
                    final ArrayList<OverlayItem> items = new ArrayList<>();
                    items.add(new OverlayItem("Me", "My Location",
                            new GeoPoint(location)));

                    mMyLocationOverlay = new ItemizedOverlayWithFocus<>(items,
                            new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                                @Override
                                public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                    IMapController mapController = mMapView.getController();
                                    mapController.setCenter(item.getPoint());
                                    mapController.zoomTo(mMapView.getMaxZoomLevel());
                                    return true;
                                }

                                @Override
                                public boolean onItemLongPress(final int index, final OverlayItem item) {
                                    return false;
                                }
                            }, context);

                    mMyLocationOverlay.setFocusItemsOnTap(true);
                    mMyLocationOverlay.setFocusedItem(0);

                    mMapView.getOverlays().add(mMyLocationOverlay);

                    mMapView.getController().setZoom(10);
                    IGeoPoint geoPoint = mMyLocationOverlay.getFocusedItem().getPoint();
                    mMapView.getController().animateTo(geoPoint);
                }
            }
        });

        mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
        mRotationGestureOverlay.setEnabled(false);
        mMapView.getOverlays().add(mRotationGestureOverlay);

        MinimapOverlay miniMapOverlay = new MinimapOverlay(context,
                mMapView.getTileRequestCompleteHandler());
        mMapView.getOverlays().add(miniMapOverlay);
    }

}
