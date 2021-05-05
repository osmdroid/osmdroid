package org.osmdroid.samplefragments.events;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import org.osmdroid.R;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Fabrice Fontaine
 * @since 6.1.1
 */
public class SampleMapCenterOffset extends SampleMapEventListener {

    private final int mOffsetX = 0;
    private final int mOffsetY = 200;
    private final Paint mPaint = new Paint();

    private int mIndex;
    private Timer t = new Timer();
    private boolean alive = true;
    private final List<GeoPoint> mList = new ArrayList<>();

    @Override
    public String getSampleTitle() {
        return "Animate To with Map Center Offset";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();

        final Drawable drawable = getResources().getDrawable(R.drawable.marker_default);

        mList.add(new GeoPoint(38.8977, -77.0365));  // white house
        mList.add(new GeoPoint(38.8719, -77.0563));  // pentagon
        mList.add(new GeoPoint(38.8895, -77.0353));  // washington monument

        for (final GeoPoint geoPoint : mList) {
            final Marker startMarker = new Marker(mMapView);
            startMarker.setPosition(geoPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setIcon(drawable);
            mMapView.getOverlays().add(startMarker);
        }

        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(5);

        mMapView.getOverlays().add(new Overlay() {

            @Override
            public void draw(Canvas pCanvas, Projection pProjection) {
                mMapView.getProjection().save(pCanvas, false, true);
                final float centerX = pCanvas.getWidth() / 2f;
                final float centerY = pCanvas.getHeight() / 2f;
                pCanvas.drawLine(centerX, centerY, centerX + mOffsetX, centerY + mOffsetY, mPaint);
                mMapView.getProjection().restore(pCanvas, true);
            }
        });

        mMapView.setMapCenterOffset(mOffsetX, mOffsetY);
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        alive = true;
        //some explanation here.
        //we using a timer task with a delayed start up to move the map around. during CI tests
        //this fragment can crash the app if you navigate away from the fragment before the initial fire
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runTask();
            }
        };

        t = new Timer();
        t.schedule(task, 4000, 4000);
    }

    @Override
    public void onPause() {
        super.onPause();
        alive = false;
        if (t != null)
            t.cancel();
        t = null;
    }


    private void runTask() {
        if (!alive)
            return;
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMapView == null || getActivity() == null) {
                    return;
                }
                show();
            }
        });
    }

    private void show() {
        show(mIndex++);
    }

    private void show(final int pIndex) {
        final double zoom = 12.5;
        final GeoPoint geoPoint = mList.get(pIndex % mList.size());
        mMapView.getController().animateTo(geoPoint, zoom, null);
    }
}
