package org.osmdroid.samplefragments.data;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabrice Fontaine
 * Sample on how to handle a click on overlapping {@link OverlayItem}s
 * @since 6.0.3
 */
public class SampleItemizedOverlayMultiClick extends BaseSampleFragment {

    public static final String TITLE = "Overlapping ItemizedOverlays' click";

    private List<OverlayItem> mClicked = new ArrayList<>();

    @Override
    public String getSampleTitle() {
        return TITLE;
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();

        final Context context = getActivity();

        final List<DataContainer> datas = getData();
        final List<OverlayItem> items = new ArrayList<>();
        final List<IGeoPoint> geoPoints = new ArrayList<>();
        for (final DataContainer data : datas) {
            geoPoints.add(data.getGeoPoint());
            items.add(new OverlayItem(data.getTitle(), data.getSnippet(), data.getGeoPoint()));
        }
        final BoundingBox box = BoundingBox.fromGeoPoints(geoPoints);

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
                final OverlayItem[] items = new OverlayItem[titles.length];
                int i = 0;
                for (final OverlayItem item : mClicked) {
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

        final ItemizedOverlayWithFocus<OverlayItem> myLocationOverlay;
        myLocationOverlay = new ItemizedOverlayWithFocus<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        mClicked.add(item);
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, context);
        myLocationOverlay.setMarkerBackgroundColor(Color.BLUE);
        myLocationOverlay.setMarkerTitleForegroundColor(Color.WHITE);
        myLocationOverlay.setMarkerDescriptionForegroundColor(Color.WHITE);
        myLocationOverlay.setDescriptionBoxPadding(15);
        mMapView.getOverlays().add(myLocationOverlay);

        mMapView.post(new Runnable() {
            @Override
            public void run() {
                mMapView.zoomToBoundingBox(box, false, 50);
            }
        });
    }

    private void message(final OverlayItem pItem) {
        Toast.makeText(getActivity(), pItem.getTitle() + ": " + pItem.getSnippet(), Toast.LENGTH_LONG).show();
    }

    public static List<DataContainer> getData() {
        final List<DataContainer> items = new ArrayList<>();
        items.add(new DataContainer(
                "Bode Museum",
                "The sculpture collection shows art of the Christian Orient, sculptures from "
                        + "Byzantium and Ravenna, sculptures of the Middle Ages, the Italian Gothic, and the early Renaissance.",
                new GeoPoint(52.521944, 13.394722)));
        items.add(new DataContainer(
                "Altes Museum",
                "It houses the Antikensammlung (antiquities collection) of the Berlin State Museums.",
                new GeoPoint(52.519444, 13.398333)));
        items.add(new DataContainer(
                "Neues Museum",
                "Exhibits include the Egyptian and Prehistory and Early History collections,"
                        + "as it did before the war. The artifacts it houses include the iconic bust of the Egyptian queen Nefertiti.",
                new GeoPoint(52.520555, 13.397777)));
        items.add(new DataContainer(
                "Alte Nationalgalerie",
                "The collection contains works of the Neoclassical and Romantic movements,"
                        + " of the Biedermeier, French Impressionism and early Modernism.",
                new GeoPoint(52.520833, 13.398055)));
        items.add(new DataContainer(
                "Pergamon Museum",
                "The Pergamon Museum houses monumental buildings such as the Pergamon Altar,"
                        + " the Ishtar Gate of Babylon, the Market Gate of Miletus reconstructed from the ruins"
                        + " found in Anatolia, as well as the Mshatta Facade.",
                new GeoPoint(52.521, 13.396)));
        items.add(new DataContainer(
                "Gemäldegalerie",
                "It holds one of the world's leading collections of European paintings from the 13th to the 18th centuries.",
                new GeoPoint(52.508472, 13.365416)));
        items.add(new DataContainer(
                "Kunstgewerbemuseum",
                "It's an internationally important museum of the decorative arts.",
                new GeoPoint(52.5097, 13.3674)));
        items.add(new DataContainer(
                "Musical Instrument Museum",
                "The Museum holds over 3,500 musical instruments from the 16th century onward "
                        + "and is one of the largest and most representative musical instrument collections in Germany.",
                new GeoPoint(52.510277, 13.370833)));
        items.add(new DataContainer(
                "Kupferstichkabinett",
                "It is the largest museum of graphic art in Germany, with more than 500,000 prints"
                        + "and around 110,000 individual works on paper.",
                new GeoPoint(52.508333, 13.366944)));
        return items;
    }

    public static class DataContainer {
        private final String mTitle;
        private final String mSnippet;
        private final IGeoPoint mGeoPoint;

        DataContainer(final String pTitle, final String pSnippet, final IGeoPoint pGeoPoint) {
            mTitle = pTitle;
            mSnippet = pSnippet;
            mGeoPoint = pGeoPoint;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getSnippet() {
            return mSnippet;
        }

        public IGeoPoint getGeoPoint() {
            return mGeoPoint;
        }
    }
}
