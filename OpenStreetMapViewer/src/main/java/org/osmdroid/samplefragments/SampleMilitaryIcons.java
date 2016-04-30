package org.osmdroid.samplefragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * icons generated from https://github.com/missioncommand/mil-sym-java
 * demonstrates one way to show custom icons for a given point on the map
 * (itemized icon)
 *
 * @author alex
 */
public class SampleMilitaryIcons extends BaseSampleFragment {

	// ===========================================================
     // Constants
     // ===========================================================
     public static final String TITLE = "Military Icons";

     private static final int MENU_ZOOMIN_ID = Menu.FIRST;
     private static final int MENU_ZOOMOUT_ID = MENU_ZOOMIN_ID + 1;
     private static final int MENU_ADDICONS_ID = MENU_ZOOMOUT_ID + 1;
     private static final int MENU_LAST_ID = MENU_ADDICONS_ID + 1; // Always set to last unused id

	// ===========================================================
     // Fields
     // ===========================================================
     private ItemizedOverlayWithFocus<OverlayItem> mMyLocationOverlay;
     private RotationGestureOverlay mRotationGestureOverlay;
     private OverlayItem overlayItem;
     List<Drawable> icons = new ArrayList<>(4);

     @Override
     public String getSampleTitle() {
          return TITLE;
     }

	// ===========================================================
     // Constructors
     // ===========================================================
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
          super.onActivityCreated(savedInstanceState);
     }

     @Override
     protected void addOverlays() {
          super.addOverlays();

          final Context context = getActivity();


          icons.add(getResources().getDrawable(org.osmdroid.R.drawable.sfgpuci));
          icons.add(getResources().getDrawable(org.osmdroid.R.drawable.shgpuci));
          icons.add(getResources().getDrawable(org.osmdroid.R.drawable.sngpuci));
          icons.add(getResources().getDrawable(org.osmdroid.R.drawable.sugpuci));

          /* Itemized Overlay */
          {
               /* OnTapListener for the Markers, shows a simple Toast. */
               mMyLocationOverlay = new ItemizedOverlayWithFocus<>(new ArrayList<OverlayItem>(),
                       new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                            @Override
                            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                 Toast.makeText(
                                         context,
                                         "Item '" + item.getTitle() + "' (index=" + index
                                                 + ") got single tapped up", Toast.LENGTH_LONG).show();
                                 return true;
                            }

                            @Override
                            public boolean onItemLongPress(final int index, final OverlayItem item) {
                                 Toast.makeText(
                                         context,
                                         "Item '" + item.getTitle() + "' (index=" + index
                                                 + ") got long pressed", Toast.LENGTH_LONG).show();
                                 return false;
                            }
                       }, context);
               mMyLocationOverlay.setFocusItemsOnTap(true);
               mMyLocationOverlay.setFocusedItem(0);

               //generates 500 randomized points
               addIcons(500);

               mMapView.getOverlays().add(mMyLocationOverlay);

               mRotationGestureOverlay = new RotationGestureOverlay(context, mMapView);
               mRotationGestureOverlay.setEnabled(false);
               mMapView.getOverlays().add(mRotationGestureOverlay);
          }

          /* MiniMap */
          {
               MinimapOverlay miniMapOverlay = new MinimapOverlay(context,
                    mMapView.getTileRequestCompleteHandler());
               mMapView.getOverlays().add(miniMapOverlay);
          }

          // Zoom and center on the focused item.
          mMapView.getController().setZoom(5);
          IGeoPoint geoPoint = mMyLocationOverlay.getFocusedItem().getPoint();
          mMapView.getController().animateTo(geoPoint);

          setHasOptionsMenu(true);
          Toast.makeText(context, "Icon selection and location are random!", Toast.LENGTH_LONG).show();
     }

	// ===========================================================
     // Getter & Setter
     // ===========================================================
	// ===========================================================
     // Methods from SuperClass/Interfaces
     // ===========================================================
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
          // Put overlay items first
          mMapView.getOverlayManager().onCreateOptionsMenu(menu, MENU_LAST_ID, mMapView);

          menu.add(0, MENU_ZOOMIN_ID, Menu.NONE, "ZoomIn");
          menu.add(0, MENU_ZOOMOUT_ID, Menu.NONE, "ZoomOut");
          menu.add(0, MENU_ZOOMOUT_ID, Menu.NONE, "ZoomOut");
          menu.add(0, MENU_ADDICONS_ID, Menu.NONE, "AddIcons");

          super.onCreateOptionsMenu(menu, inflater);
     }

     @Override
     public void onPrepareOptionsMenu(Menu menu) {
          mMapView.getOverlayManager().onPrepareOptionsMenu(menu, MENU_LAST_ID, mMapView);
          super.onPrepareOptionsMenu(menu);
     }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
          if (mMapView.getOverlayManager().onOptionsItemSelected(item, MENU_LAST_ID, mMapView)) {
               return true;
          }

          switch (item.getItemId()) {
               case MENU_ZOOMIN_ID:
                    mMapView.getController().zoomIn();
                    return true;

               case MENU_ZOOMOUT_ID:
                    mMapView.getController().zoomOut();
                    return true;
               case MENU_ADDICONS_ID:
                    addIcons(500);
                    return true;
          }
          return false;
     }

     private void addIcons(int count) {
           /* Create a static ItemizedOverlay showing some Markers on various cities. */
          final ArrayList<OverlayItem> items = new ArrayList<>();
          for (int i = 0; i < count; i++) {
               double random_lon = (Math.random() * 360) - 180;
               double random_lat = (Math.random() * 180) - 90;
               overlayItem = new OverlayItem("A random point", "SampleDescription", new GeoPoint(random_lat,
                       random_lon));
               int index = (int) (Math.random() * (icons.size()));
               if (index == icons.size()) {
                    index--;
               }
               overlayItem.setMarker(icons.get(index));
               items.add(overlayItem);

          }
          mMyLocationOverlay.addItems(items);
          Toast.makeText(getActivity(), count + " icons added! Current size: " + mMyLocationOverlay.size(), Toast.LENGTH_LONG).show();

     }

     // ===========================================================
     // Methods
     // ===========================================================
	// ===========================================================
     // Inner and Anonymous Classes
     // ===========================================================
}
