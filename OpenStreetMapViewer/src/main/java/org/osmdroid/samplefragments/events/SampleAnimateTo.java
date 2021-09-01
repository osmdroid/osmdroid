package org.osmdroid.samplefragments.events;

import android.util.DisplayMetrics;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.data.DataRegion;
import org.osmdroid.data.DataRegionLoader;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * https://github.com/osmdroid/osmdroid/issues/264
 * extends gridlines to provide visual confirmation
 * Created by alex on 2/22/16.
 */
public class SampleAnimateTo extends SampleMapEventListener {

    private int mIndex;
    private ScaleBarOverlay mScaleBarOverlay;
    private Timer t = new Timer();
    private boolean alive = true;
    private final List<DataRegion> mList = new ArrayList<>();

    @Override
    public String getSampleTitle() {
        return "Animate To";
    }


    @Override
    public void addOverlays() {
        super.addOverlays();

        final DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(mMapView);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        mMapView.getOverlays().add(mScaleBarOverlay);

        // according to https://www.flickr.com/places/info/12589342
        final GeoPoint manhattanCenter = new GeoPoint(40.7909, -73.9664);
        final BoundingBox manhattanBoundingBox = new BoundingBox(40.8820, -73.9067, 40.6829, -74.0479);
        // testing a "single point bounding box" (actually using zoom fallback in animateTo)
        mList.add(new DataRegion("dummy1", "Manhattan - single point",
                new BoundingBox(manhattanCenter.getLatitude(), manhattanCenter.getLongitude(), manhattanCenter.getLatitude(), manhattanCenter.getLongitude())));
        // testing a "single latitude bounding box"
        mList.add(new DataRegion("dummy2", "Manhattan - single latitude",
                new BoundingBox(manhattanCenter.getLatitude(), manhattanBoundingBox.getLonEast(), manhattanCenter.getLatitude(), manhattanBoundingBox.getLonWest())));
        // testing a "single longitude bounding box"
        mList.add(new DataRegion("dummy3", "Manhattan - single longitude",
                new BoundingBox(manhattanBoundingBox.getLatNorth(), manhattanCenter.getLongitude(), manhattanBoundingBox.getLatSouth(), manhattanCenter.getLongitude())));
        // testing a "single longitude bounding box"
        mList.add(new DataRegion("dummy4", "Manhattan - box", manhattanBoundingBox));

        try {
            mList.addAll(new DataRegionLoader(getActivity(), R.raw.data_region_usstates).getList().values());
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

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

    /**
     * @since 6.0.2
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mScaleBarOverlay = null;
    }

    /**
     * @since 6.0.2
     */
    private void show() {
        show(mIndex++);
    }

    /**
     * @since 6.0.2
     */
    private void show(final int pIndex) {
        final int borderSizeInPixels = 20;
        final double zoomFallback = 12;
        final long animationSpeed = 2000;
        final boolean animated = true;
        final DataRegion state = mList.get(pIndex % mList.size());
        final BoundingBox box = state.getBox();
        mMapView.zoomToBoundingBox(box, animated, borderSizeInPixels, zoomFallback, animationSpeed);
        Toast.makeText(getActivity(), state.getName(), Toast.LENGTH_SHORT).show();
    }
}
