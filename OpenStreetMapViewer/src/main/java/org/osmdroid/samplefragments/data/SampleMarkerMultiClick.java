package org.osmdroid.samplefragments.data;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;

import org.osmdroid.R;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabrice Fontaine
 * Sample on how to handle a click on overlapping {@link Marker}s
 * @since 6.0.3
 */
public class SampleMarkerMultiClick extends BaseSampleFragment {

    public static final String TITLE = "Overlapping Markers' click";

    private List<Marker> mClicked = new ArrayList<>();

    @Override
    public String getSampleTitle() {
        return TITLE;
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();

        mMapView.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (mClicked.size() == 0) {
                    return false;
                }
                if (mClicked.size() == 1) {
                    message(mClicked.get(0));
                    mClicked.clear();
                    return true;
                }
                final String[] titles = new String[mClicked.size()];
                final Marker[] items = new Marker[titles.length];
                int i = 0;
                for (final Marker item : mClicked) {
                    titles[i] = item.getTitle();
                    items[i] = item;
                    i++;
                }
                new AlertDialog.Builder(getActivity())
                        .setItems(titles, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                message(items[i]);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                mClicked.clear();
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        }));

        final List<SampleItemizedOverlayMultiClick.DataContainer> datas = SampleItemizedOverlayMultiClick.getData();
        final List<IGeoPoint> geoPoints = new ArrayList<>();
        final Drawable drawable = getResources().getDrawable(R.drawable.icon);
        for (final SampleItemizedOverlayMultiClick.DataContainer data : datas) {
            geoPoints.add(data.getGeoPoint());
            final Marker marker = new MyMarker(mMapView);
            marker.setPosition(new GeoPoint(data.getGeoPoint()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(drawable);
            marker.setTitle(data.getTitle());
            marker.setSnippet(data.getSnippet());
            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    mClicked.add(marker);
                    return false;
                }
            });
            mMapView.getOverlays().add(marker);
        }

        final BoundingBox box = BoundingBox.fromGeoPoints(geoPoints);
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                mMapView.zoomToBoundingBox(box, false, 50);
            }
        });
    }

    private void message(final Marker pMarker) {
        ((MyMarker) pMarker).onMarkerClickDefault(pMarker, mMapView);
    }

    static private class MyMarker extends Marker {

        MyMarker(MapView mapView) {
            super(mapView);
        }

        @Override
        public boolean onMarkerClickDefault(Marker marker, MapView mapView) { // made public
            return super.onMarkerClickDefault(marker, mapView);
        }
    }
}
