package org.osmdroid.bugtestfragments;

import android.widget.Toast;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

/**
 * created on 5/5/2019.
 *
 * @author Alex O'Ree
 */
public class Bug1322 extends BaseSampleFragment {
    final String description1 = "Line1\nLine2\nLine3\nLine4\nLine5\nLine6\nLine7\nLine8\nLine9\nLine10\nLine11\nLine12\nLine13\nLine14\nLine15";
    final String description2 = "Line01 Line02 Line03 Line04 Line05 Line06 Line07 Line08 Line09 Line10 Line11 "
            + "Line12 Line13 Line14 Line15 Line16 Line17 Line18 Line19 Line20 Line21 Line22 Line23";
    final String description5 = "Line1Line2Line3Line4Line5Line6Line7Line8Line9Line10Line11Line12Line13Line14Line15line16line17line18line19line20line21line22line23line24line25line26line27line28line29line30";
    final String description6 = "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
    final String description7 =
            "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC"
                    + "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC"
                    + "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC";
    final String description3 = "0123456789012345678901234567890123456789012345678912345678901234"
            + "0123456789012345678901234567890123456789012345678912345678901234"
            + "0123456789012345678901234567890123456789012345678912345678901234";
    final String description4 = "Line1\nLine2\n\nLine3\nLine4\n"
            + "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC01234567890123456789012345678901234567890123456789123456789012340123456789012345678901234567890123456789012345678912345678901234";

    @Override
    public String getSampleTitle() {
        return "ItemizedOverlayWithFocus with long titles";
    }

    private void message(final OverlayItem pItem) {
        Toast.makeText(getActivity(), pItem.getTitle() + ": " + pItem.getSnippet(), Toast.LENGTH_LONG).show();
    }

    private List<OverlayItem> mClicked = new ArrayList<>();

    @Override
    public void addOverlays() {
        super.addOverlays();
        final ItemizedOverlayWithFocus<OverlayItem> myLocationOverlay;

        final List<OverlayItem> items = new ArrayList<>();

        items.add(new OverlayItem("Title1", "a small descripotion", new GeoPoint(-3d, -3d)));
        items.add(new OverlayItem("Title1", description1, new GeoPoint(0d, 0d)));
        items.add(new OverlayItem("Title2", description2, new GeoPoint(3d, 3d)));
        items.add(new OverlayItem("Title3", description3, new GeoPoint(6d, 6d)));
        items.add(new OverlayItem("Title4", description4, new GeoPoint(9d, 9d)));
        items.add(new OverlayItem("Title5", description5, new GeoPoint(12d, 12d)));
        items.add(new OverlayItem("Title6", description6, new GeoPoint(15d, 15)));
        items.add(new OverlayItem("Title7", description7, new GeoPoint(18d, 18)));

        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, getContext());
        mOverlay.setFocusItemsOnTap(true);
        mMapView.getOverlays().add(mOverlay);

        /*mMapView.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
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
                for(final OverlayItem item : mClicked) {
                    titles[i] = item.getTitle();
                    items[i] = item;
                    i ++;
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
            }, getContext());
        myLocationOverlay.setMarkerBackgroundColor(Color.BLUE);
        myLocationOverlay.setMarkerTitleForegroundColor(Color.WHITE);
        myLocationOverlay.setMarkerDescriptionForegroundColor(Color.WHITE);
        myLocationOverlay.setDescriptionBoxPadding(15);
        mMapView.getOverlays().add(myLocationOverlay);
        */
    }

    @Override
    public boolean skipOnCiTests() {
        return false;
    }

}
