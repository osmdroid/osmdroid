package org.osmdroid.samplefragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

/**
 * @author Tyrone Tudehope
 */
public class SampleAnimatedZoomToLocation extends BaseSampleFragment {

  public static final String TITLE = "Animated Zoom to Location";

  private ItemizedOverlayWithFocus<OverlayItem> mMyLocationOverlay;
  private RotationGestureOverlay mRotationGestureOverlay;

  @Override
  public String getSampleTitle() {
    return TITLE;
  }

  @Override
  protected void addOverlays() {
    super.addOverlays();

    final Context context = getActivity();

    new GpsMyLocationProvider(context).startLocationProvider(new IMyLocationConsumer() {
      @Override
      public void onLocationChanged(Location location, IMyLocationProvider source) {
        if(mMyLocationOverlay == null) {
          final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
          items.add(new OverlayItem("Me", "My Location",
            new GeoPoint(location)));

          mMyLocationOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
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
            }, mResourceProxy);

          mMyLocationOverlay.setFocusItemsOnTap(true);
          mMyLocationOverlay.setFocusedItem(0);

          mMapView.getOverlays().add(mMyLocationOverlay);

          mMapView.getController().setZoom(10);
          IGeoPoint geoPoint = mMyLocationOverlay.getFocusedItem().getPoint();
          mMapView.getController().animateTo(geoPoint);
        }
      }
    });

    mRotationGestureOverlay = new RotationGestureOverlay(context, mMapView);
    mRotationGestureOverlay.setEnabled(false);
    mMapView.getOverlays().add(mRotationGestureOverlay);

    MinimapOverlay miniMapOverlay = new MinimapOverlay(context,
      mMapView.getTileRequestCompleteHandler());
    mMapView.getOverlays().add(miniMapOverlay);
  }

}
