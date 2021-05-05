package org.osmdroid.samplefragments.tileproviders;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;

/**
 * Demo checking if the zoom restriction for tiles is correctly applied
 * We actually download MAPNIK tiles but only for zoom levels 14 and 15
 *
 * @author Fabrice Fontaine
 * @since 6.1.3
 */
public class SampleUnreachableOnlineTiles extends BaseSampleFragment {

    private static final int ZOOM_MIN = 14;
    private static final int ZOOM_MAX = 15;

    /**
     * cf. {@link TileSourceFactory#MAPNIK}
     */
    private static final OnlineTileSourceBase MAPNIK_FOR_TESTS = new XYTileSource("Mapnik",
            ZOOM_MIN, ZOOM_MAX, 256, ".png", new String[]{
            "https://a.tile.openstreetmap.org/",
            "https://b.tile.openstreetmap.org/",
            "https://c.tile.openstreetmap.org/"}, "© OpenStreetMap contributors",
            new TileSourcePolicy(2,
                    TileSourcePolicy.FLAG_NO_BULK
                            | TileSourcePolicy.FLAG_NO_PREVENTIVE
                            | TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                            | TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
            ));

    @Override
    public String getSampleTitle() {
        return "Zoom Restricted Online Tiles (" + ZOOM_MIN + "-" + ZOOM_MAX + ")";
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();

        mMapView.setTileSource(MAPNIK_FOR_TESTS);
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                mMapView.getController().setZoom(ZOOM_MIN * 1f);
                mMapView.setExpectedCenter(new GeoPoint(45.7597, 4.8422)); // Lyon, France
            }
        });
    }
}
