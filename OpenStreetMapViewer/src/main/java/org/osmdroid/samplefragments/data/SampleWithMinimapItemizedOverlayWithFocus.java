// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid.samplefragments.data;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;

/**
 * @author Nicolas Gramlich
 */
public class SampleWithMinimapItemizedOverlayWithFocus extends BaseSampleFragment {

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String TITLE = "Itemized overlay w/focus";

    private static final int MENU_ZOOMIN_ID = Menu.FIRST;
    private static final int MENU_ZOOMOUT_ID = MENU_ZOOMIN_ID + 1;
    private static final int MENU_LAST_ID = MENU_ZOOMOUT_ID + 1; // Always set to last unused id

    // ===========================================================
    // Fields
    // ===========================================================

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

        /* Itemized Overlay */
        final ItemizedOverlayWithFocus<OverlayItem> mMyLocationOverlay;
        {
            /* Create a static ItemizedOverlay showing some Markers on various cities. */
            final ArrayList<OverlayItem> items = new ArrayList<>();
            items.add(new OverlayItem("Hannover", "Tiny SampleDescription", new GeoPoint(52.370816,
                    9.735936))); // Hannover
            items.add(new OverlayItem("Berlin", "This is a relatively short SampleDescription.",
                    new GeoPoint(52.518333, 13.408333))); // Berlin
            items.add(new OverlayItem(
                    "Washington",
                    "This SampleDescription is a pretty long one. Almost as long as a the great wall in china.",
                    new GeoPoint(38.895, -77.036667))); // Washington
            items.add(new OverlayItem("San Francisco", "SampleDescription", new GeoPoint(37.7793,
                    -122.4192))); // San Francisco

            /* OnTapListener for the Markers, shows a simple Toast. */
            mMyLocationOverlay = new ItemizedOverlayWithFocus<>(items,
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
                            return true;
                        }
                    }, context);
            mMyLocationOverlay.setFocusItemsOnTap(true);
            mMyLocationOverlay.setFocusedItem(0);
            //https://github.com/osmdroid/osmdroid/issues/317
            //you can override the drawing characteristics with this
            mMyLocationOverlay.setMarkerBackgroundColor(Color.BLUE);
            mMyLocationOverlay.setMarkerTitleForegroundColor(Color.WHITE);
            mMyLocationOverlay.setMarkerDescriptionForegroundColor(Color.WHITE);
            mMyLocationOverlay.setDescriptionBoxPadding(15);

            mMapView.getOverlays().add(mMyLocationOverlay);

            final RotationGestureOverlay mRotationGestureOverlay;
            mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
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
        mMapView.getController().setZoom(5.);
        IGeoPoint geoPoint = mMyLocationOverlay.getFocusedItem().getPoint();
        mMapView.getController().animateTo(geoPoint);

        setHasOptionsMenu(true);
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

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mMapView.getOverlayManager().onPrepareOptionsMenu(menu, MENU_LAST_ID, mMapView);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mMapView.getOverlayManager().onOptionsItemSelected(item, MENU_LAST_ID, mMapView))
            return true;

        switch (item.getItemId()) {
            case MENU_ZOOMIN_ID:
                mMapView.getController().zoomIn();
                return true;

            case MENU_ZOOMOUT_ID:
                mMapView.getController().zoomOut();
                return true;
        }
        return false;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
