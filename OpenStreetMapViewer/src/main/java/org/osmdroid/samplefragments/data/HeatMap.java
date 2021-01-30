package org.osmdroid.samplefragments.data;

import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * EXPERIMENTAL!!
 *
 * <a href="https://github.com/osmdroid/osmdroid/issues/499">https://github.com/osmdroid/osmdroid/issues/499</a>
 * <p>
 * Demonstrates a way to generate heatmaps using osmdroid and a collection of data points.
 * There's a lot of room for improvement but this class demonstrates two things
 * <ul>
 * <li>How to load data asynchronously when the map moves/zooms</li>
 * <li>How to generate a basic heat map</li>
 * </ul>
 * <p>
 * There's probably a many options to implement this. This example basically chops up the screen
 * into cells, generates some random data, then iterates all of the data and increments up a
 * counter based on the cell that it was rendered into. Finally the cells are converted into square
 * polygons with a fill color based on the counter.
 * <p>
 * It's assumed that all required data is available on device for this example.
 * <p>
 * For future readers: other approaches
 * <ul>
 * <li>if a server/network connection is available, it would be better to have the server
 * generate a kml/kmz for the heat map, then use osmbonuspack to do the parsing. this will be much
 * better at handling higher volumes of data</li>
 * <li>use a server that generates slippy map tiles representing the overlay, then add a secondary {@link org.osmdroid.views.overlay.TilesOverlay} with that source.</li>
 * <li>locally (on device) generate an image for the slippy map tiles representing the data, then add a secondary {@link org.osmdroid.views.overlay.TilesOverlay} with that source.</li>
 * <li>make a custom {@link Overlay} class that has some custom onDraw logical to paint the image.</li>
 * </ul>
 * All of these other (and better) approaches really need some kind of geospatial index mechanism, such
 * as <a href="https://github.com/davidmoten/rtree">this</a>, only modified with some kind of running
 * estimate algorithm.
 * <p>
 * created on 1/1/2017.
 *
 * @author Alex O'Ree
 * @since 5.6.3
 */

public class HeatMap extends BaseSampleFragment implements MapListener, Runnable {
    @Override
    public String getSampleTitle() {
        return "Heatmap with Async loading";
    }

    String TAG = "heatmap";
    DisplayMetrics dm = null;

    // async loading stuff
    boolean renderJobActive = false;
    boolean running = true;
    long lastMovement = 0;
    boolean needsDataRefresh = true;
    // end async loading stuff


    /**
     * the size of the cell in density independent pixels
     * a higher value = smoother image but higher processing and rendering times
     */
    int cellSizeInDp = 20;


    //colors and alpha settings
    String alpha = "#55";
    String red = "FF0000";
    String orange = "FFA500";
    String yellow = "FFFF00";

    //a pointer to the last render overlay, so that we can remove/replace it with the new one
    FolderOverlay heatmapOverlay = null;


    @Override
    public void addOverlays() {
        super.addOverlays();
        dm = getResources().getDisplayMetrics();
        mMapView.getController().setCenter(new GeoPoint(38.8977, -77.0365));
        mMapView.getController().setZoom(14);
        mMapView.setMapListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        running = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        running = true;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * this generates the heatmap off of the main thread, loads the data, makes the overlay, then
     * adds it to the map
     */
    private void generateMap() {

        if (getActivity() == null)  //java.lang.IllegalStateException: Fragment HeatMap{44f341d0} not attached to Activity
            return;
        if (renderJobActive)
            return;
        renderJobActive = true;


        int densityDpi = (int) (dm.density * cellSizeInDp);
        //10 dpi sized cells

        IGeoPoint iGeoPoint = mMapView.getProjection().fromPixels(0, 0);
        IGeoPoint iGeoPoint2 = mMapView.getProjection().fromPixels(densityDpi, densityDpi);
        //delta is the size of our cell in lat,lon
        //since this is zoom dependent, rerun the calculations on zoom changes
        double xCellSizeLongitude = Math.abs(iGeoPoint.getLongitude() - iGeoPoint2.getLongitude());
        double yCellSizeLatitude = Math.abs(iGeoPoint.getLatitude() - iGeoPoint2.getLatitude());

        BoundingBox view = mMapView.getBoundingBox();
        //a set of a GeoPoints representing what we want a heat map of.
        List<IGeoPoint> pts = loadPoints(view);

        //the highest value in our collection of stuff
        int maxHeat = 0;
        //a temp container of all grid cells and their hit count (which turns into a color on render)

        //the lower the cell size the more cells and items in the map.
        Map<BoundingBox, Integer> heatmap = new HashMap<BoundingBox, Integer>();
        //create the grid

        Log.i(TAG, "heatmap builder " + yCellSizeLatitude + " " + xCellSizeLongitude);
        Log.i(TAG, "heatmap builder " + view);

        //populate the cells
        for (double lat = view.getLatNorth(); lat >= view.getLatSouth(); lat = lat - yCellSizeLatitude) {
            for (double lon = view.getLonEast(); lon >= view.getLonWest(); lon = lon - xCellSizeLongitude) {
                //Log.i(TAG,"heatmap builder " + lat + "," + lon);
                heatmap.put(new BoundingBox(lat, lon, lat - yCellSizeLatitude, lon - xCellSizeLongitude), 0);
            }
        }


        Log.i(TAG, "generating the heatmap");
        long now = System.currentTimeMillis();

        //generate the map, put the items in each cell
        for (int i = 0; i < pts.size(); i++) {
            //get the box for this pt's coordinates
            int x = increment(pts.get(i), heatmap);
            if (x > maxHeat)
                maxHeat = x;

        }
        Log.i(TAG, "generating the heatmap, done " + (System.currentTimeMillis() - now));

        //figure out the color scheme
        //if you need a more logirthmic scale, this is the place to do it.
        //cells with a 0 value are blank
        //cells 1 to 1/3 of the max value are yellow
        //cells from 1/3 to 2/3 are organge
        //cells 2/3 or higher are red
        int redthreshold = maxHeat * 2 / 3; //upper 1/3
        int orangethreshold = maxHeat * 1 / 3; //middle 1/3

        //render the map
        Log.i(TAG, "rendering");
        now = System.currentTimeMillis();
        //each bounding box if the hit count > 0 create a polygon with the bounding box coordinates with the right fill color
        final FolderOverlay group = new FolderOverlay();
        Iterator<Map.Entry<BoundingBox, Integer>> iterator = heatmap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BoundingBox, Integer> next = iterator.next();
            if (next.getValue() > 0) {
                group.add(createPolygon(next.getKey(), next.getValue(), redthreshold, orangethreshold));
            }
        }
        Log.i(TAG, "render done , done " + (System.currentTimeMillis() - now));
        if (getActivity() == null)    //java.lang.IllegalStateException: Fragment HeatMap{44f341d0} not attached to Activity
            return;
        if (mMapView == null)  //java.lang.IllegalStateException: Fragment HeatMap{44f341d0} not attached to Activity
            return;
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                if (heatmapOverlay != null)
                    mMapView.getOverlayManager().remove(heatmapOverlay);
                mMapView.getOverlayManager().add(group);
                heatmapOverlay = group;

                mMapView.invalidate();
                renderJobActive = false;
            }
        });
    }


    /**
     * generates a bunch of random data
     *
     * @param view
     * @return
     */
    private List<IGeoPoint> loadPoints(BoundingBox view) {
        List<IGeoPoint> pts = new ArrayList<IGeoPoint>();

        for (int i = 0; i < 10000; i++) {
            pts.add(new GeoPoint((Math.random() * view.getLatitudeSpan()) + view.getLatSouth(),
                    (Math.random() * view.getLongitudeSpan()) + view.getLonWest()));
        }
        pts.add(new GeoPoint(0d, 0d));
        pts.add(new GeoPoint(0d, 0d));
        pts.add(new GeoPoint(0d, 0d));
        pts.add(new GeoPoint(0d, 0d));
        pts.add(new GeoPoint(0d, 0d));
        pts.add(new GeoPoint(0d, 0d));
        pts.add(new GeoPoint(0d, 0d));
        pts.add(new GeoPoint(0d, 0d));
        pts.add(new GeoPoint(0d, 0d));
        pts.add(new GeoPoint(0d, 0d));
        pts.add(new GeoPoint(0d, 0d));
        pts.add(new GeoPoint(0d, 0d));
        pts.add(new GeoPoint(0d, 0d));
        pts.add(new GeoPoint(0d, 0d));


        pts.add(new GeoPoint(1.1d * cellSizeInDp, 1.1d * cellSizeInDp));
        pts.add(new GeoPoint(1.1d * cellSizeInDp, 1.1d * cellSizeInDp));
        pts.add(new GeoPoint(1.1d * cellSizeInDp, 1.1d * cellSizeInDp));
        pts.add(new GeoPoint(1.1d * cellSizeInDp, 1.1d * cellSizeInDp));
        pts.add(new GeoPoint(1.1d * cellSizeInDp, 1.1d * cellSizeInDp));
        pts.add(new GeoPoint(1.1d * cellSizeInDp, 1.1d * cellSizeInDp));
        pts.add(new GeoPoint(1.1d * cellSizeInDp, 1.1d * cellSizeInDp));

        pts.add(new GeoPoint(-1.1d * cellSizeInDp, -1.1d * cellSizeInDp));
        pts.add(new GeoPoint(-1.1d * cellSizeInDp, -1.1d * cellSizeInDp));
        pts.add(new GeoPoint(-1.1d * cellSizeInDp, -1.1d * cellSizeInDp));
        pts.add(new GeoPoint(-1.1d * cellSizeInDp, -1.1d * cellSizeInDp));
        pts.add(new GeoPoint(-1.1d * cellSizeInDp, -1.1d * cellSizeInDp));
        pts.add(new GeoPoint(-1.1d * cellSizeInDp, -1.1d * cellSizeInDp));
        pts.add(new GeoPoint(-1.1d * cellSizeInDp, -1.1d * cellSizeInDp));

        pts.add(new GeoPoint(-1.1d * cellSizeInDp, 1.1d * cellSizeInDp));
        pts.add(new GeoPoint(-1.1d * cellSizeInDp, 1.1d * cellSizeInDp));
        pts.add(new GeoPoint(-1.1d * cellSizeInDp, 1.1d * cellSizeInDp));
        pts.add(new GeoPoint(-1.1d * cellSizeInDp, 1.1d * cellSizeInDp));


        pts.add(new GeoPoint(1.1d * cellSizeInDp, -1.1d * cellSizeInDp));
        pts.add(new GeoPoint(1.1d * cellSizeInDp, -1.1d * cellSizeInDp));
        pts.add(new GeoPoint(1.1d * cellSizeInDp, -1.1d * cellSizeInDp));

        return pts;
    }

    /**
     * converts the bounding box into a color filled polygon
     *
     * @param key
     * @param value
     * @param redthreshold
     * @param orangethreshold
     * @return
     */
    private Overlay createPolygon(BoundingBox key, Integer value, int redthreshold, int orangethreshold) {
        Polygon polygon = new Polygon(mMapView);
        if (value < orangethreshold)
            polygon.getFillPaint().setColor(Color.parseColor(alpha + yellow));
        else if (value < redthreshold)
            polygon.getFillPaint().setColor(Color.parseColor(alpha + orange));
        else if (value >= redthreshold)
            polygon.getFillPaint().setColor(Color.parseColor(alpha + red));
        else {
            //no polygon
        }
        polygon.getOutlinePaint().setColor(polygon.getFillPaint().getColor());

        //if you set this to something like 20f and have a low alpha setting,
        // you'll end with a gaussian blur like effect
        polygon.getOutlinePaint().setStrokeWidth(0f);
        List<GeoPoint> pts = new ArrayList<GeoPoint>();
        pts.add(new GeoPoint(key.getLatNorth(), key.getLonWest()));
        pts.add(new GeoPoint(key.getLatNorth(), key.getLonEast()));
        pts.add(new GeoPoint(key.getLatSouth(), key.getLonEast()));
        pts.add(new GeoPoint(key.getLatSouth(), key.getLonWest()));
        polygon.setPoints(pts);
        return polygon;
    }

    /**
     * For each data point, find the corresponding cell, then increment the count. This is the
     * most inefficient portion of this example.
     * <p>
     * room for improvement: replace with some kind of geospatial indexing mechanism
     *
     * @param iGeoPoint
     * @param heatmap
     * @return
     */
    private int increment(IGeoPoint iGeoPoint, Map<BoundingBox, Integer> heatmap) {

        Iterator<Map.Entry<BoundingBox, Integer>> iterator = heatmap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BoundingBox, Integer> next = iterator.next();
            if (next.getKey().contains(iGeoPoint)) {
                int newval = next.getValue() + 1;
                heatmap.put(next.getKey(), newval);
                return newval;
            }
        }
        return 0;
    }

    /**
     * handles the map movement rendering portions, prevents more than one render at a time,
     * waits for the user to stop moving the map before triggering the render
     */
    @Override
    public boolean onScroll(ScrollEvent event) {
        lastMovement = System.currentTimeMillis();
        needsDataRefresh = true;
        return false;
    }

    /**
     * handles the map movement rendering portions, prevents more than one render at a time,
     * waits for the user to stop moving the map before triggering the render
     */
    @Override
    public boolean onZoom(ZoomEvent event) {
        lastMovement = System.currentTimeMillis();
        needsDataRefresh = true;
        return false;
    }

    /**
     * handles the map movement rendering portions, prevents more than one render at a time,
     * waits for the user to stop moving the map before triggering the render
     */
    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
        //TODO replace me with a timer task
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (needsDataRefresh) {
                if (System.currentTimeMillis() - lastMovement > 500) {
                    generateMap();
                    needsDataRefresh = false;
                }
            }
        }
    }
}
