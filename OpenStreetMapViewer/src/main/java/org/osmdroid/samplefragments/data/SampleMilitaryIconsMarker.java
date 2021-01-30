package org.osmdroid.samplefragments.data;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * icons generated from https://github.com/missioncommand/mil-sym-java
 * demonstrates one way to show custom icons for a given point on the map
 * (Marker)
 *
 * @author alex
 */
public class SampleMilitaryIconsMarker extends BaseSampleFragment {

    // ===========================================================
    // Constants
    // ===========================================================
    public static final String TITLE = "Military Icons using Markers";

    private static final int MENU_ZOOMIN_ID = Menu.FIRST;
    private static final int MENU_ZOOMOUT_ID = MENU_ZOOMIN_ID + 1;
    private static final int MENU_ADDICONS_ID = MENU_ZOOMOUT_ID + 1;
    private static final int MENU_LAST_ID = MENU_ADDICONS_ID + 1; // Always set to last unused id

    // ===========================================================
    // Fields
    // ===========================================================
    private RotationGestureOverlay mRotationGestureOverlay;
    private List<Drawable> icons = new ArrayList<>(4);
    private final Random mRandom = new Random();

    @Override
    public String getSampleTitle() {
        return TITLE;
    }

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    protected void addOverlays() {
        super.addOverlays();

        final Context context = getActivity();


        icons.add(getResources().getDrawable(org.osmdroid.R.drawable.sfgpuci));
        icons.add(getResources().getDrawable(org.osmdroid.R.drawable.shgpuci));
        icons.add(getResources().getDrawable(org.osmdroid.R.drawable.sngpuci));
        icons.add(getResources().getDrawable(org.osmdroid.R.drawable.sugpuci));


        //generates 50 randomized points
        addIcons(50);

        mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
        mRotationGestureOverlay.setEnabled(false);
        mMapView.getOverlays().add(mRotationGestureOverlay);

        // Zoom and center on the focused item.
        mMapView.getController().setZoom(3);

        setHasOptionsMenu(true);
        Toast.makeText(context, "Icon selection and location are random!", Toast.LENGTH_SHORT).show();
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
        for (int i = 0; i < count; i++) {
            double random_lon = MapView.getTileSystem().getRandomLongitude(mRandom.nextDouble());
            double random_lat = MapView.getTileSystem().getRandomLatitude(mRandom.nextDouble());
            Marker m = new Marker(mMapView);
            m.setPosition(new GeoPoint(random_lat, random_lon));
            final int index = mRandom.nextInt(icons.size());
            m.setSnippet("A random point");
            m.setSubDescription("location: " + random_lat + "," + random_lon);
            m.setIcon(icons.get(index));
            mMapView.getOverlayManager().add(m);
        }

        mMapView.invalidate();
        Toast.makeText(getActivity(), count + " icons added! Current size: " + mMapView.getOverlayManager().size(), Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onDestroyView() {
        //itemOverlay.onDetach(mMapView);
        super.onDestroyView();
    }


    // ===========================================================
    // Methods
    // ===========================================================
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
