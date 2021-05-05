package org.osmdroid.samplefragments.models;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

public class SampleItemizedOverlay extends ItemizedOverlay<SampleOverlayItem> implements
        ItemizedOverlay.OnFocusChangeListener {

    private boolean mFocusChanged = false;
    private View mPopupView = null;
    private Context mContext = null;

    public SampleItemizedOverlay(Drawable pDefaultMarker, Context pContext) {
        super(pDefaultMarker);
        populate();
        setOnFocusChangeListener(this);
        mContext = pContext;
    }

    @Override
    protected SampleOverlayItem createItem(int i) {
        SampleOverlayItem item;
        if (i == 0)
            item = new SampleOverlayItem("CentralPark", "Central Park",
                    "Central Park in New York City", new GeoPoint(40.7820, -73.9660), null,
                    HotspotPlace.BOTTOM_CENTER);
        else
            item = new SampleOverlayItem("NorthCentralPark", "North Central Park",
                    "North of Central Park in New York City", new GeoPoint(41.7820, -73.9660),
                    mContext.getResources().getDrawable(R.drawable.person), HotspotPlace.CENTER);
        return item;
    }

    @Override
    public void onFocusChanged(ItemizedOverlay<?> overlay, OverlayItem newFocus) {
        mFocusChanged = true;
    }

    @Override
    protected boolean onTap(int index) {
        setFocus(getItem(index));
        return true;
    }


    @Override
    public void draw(Canvas c, MapView mapView, boolean shadow) {
        if (mFocusChanged) {
            mFocusChanged = false;

            // Remove any current focus
            if (mPopupView != null)
                mapView.removeView(mPopupView);

            SampleOverlayItem item = this.getFocus();
            if (item != null) {
                mPopupView = getPopupView(mapView.getContext(), item);
                MapView.LayoutParams lp = new MapView.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT, item.getPoint(),
                        MapView.LayoutParams.TOP_CENTER, 0, 0);
                mapView.addView(mPopupView, lp);
            }
        }
        super.draw(c, mapView, shadow);
    }

    protected View getPopupView(Context context, SampleOverlayItem item) {
        TextView tv = new TextView(context);
        tv.setText(item.getTitle());
        tv.setBackgroundColor(Color.BLACK);
        return tv;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3) {
        return false;
    }
}
