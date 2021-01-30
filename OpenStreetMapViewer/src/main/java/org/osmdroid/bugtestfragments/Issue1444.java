package org.osmdroid.bugtestfragments;

import android.graphics.drawable.Drawable;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

/**
 * created on 12/6/2019.
 *
 * @author Alex O'Ree
 */
public class Issue1444 extends BaseSampleFragment {

    @Override
    public String getSampleTitle() {
        return "Issue 1444 stuck label with itemized icon overlay";
    }

    private List<Drawable> icons = new ArrayList<>(4);


    @Override
    public void addOverlays() {

        super.addOverlays();

        icons.add(getResources().getDrawable(org.osmdroid.R.drawable.sfgpuci));
        icons.add(getResources().getDrawable(org.osmdroid.R.drawable.shgpuci));
        icons.add(getResources().getDrawable(org.osmdroid.R.drawable.sngpuci));
        icons.add(getResources().getDrawable(org.osmdroid.R.drawable.sugpuci));
        GeoPoint myGeoPoint = new GeoPoint(32d, -74d);

        OverlayItem MY_OverlayItem = new OverlayItem("1", "LABEL", "", myGeoPoint);
        MY_OverlayItem.setMarker(icons.get(1));

        ArrayList<OverlayItem> ARRAY_Of_OverlayItems = new ArrayList<>();
        ARRAY_Of_OverlayItems.add(MY_OverlayItem);

        ItemizedOverlayWithFocus<OverlayItem> myItemizedOverlayWithFocus = new ItemizedOverlayWithFocus<>(ARRAY_Of_OverlayItems,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {

                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                }, getContext());

        myItemizedOverlayWithFocus.setFocusItemsOnTap(true);
        myItemizedOverlayWithFocus.setFocusedItem(0);
        mMapView.getOverlays().add(myItemizedOverlayWithFocus);
        mMapView.invalidate();


    }
}
