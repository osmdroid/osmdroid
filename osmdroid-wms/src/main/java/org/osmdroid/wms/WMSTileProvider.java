package org.osmdroid.wms;

import android.util.Log;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * Created by alex on 11/5/15.
 */
public class WMSTileProvider extends OnlineTileSourceBase{


    // Web Mercator n/w corner of the map.
    private static final double[] TILE_ORIGIN = {-20037508.34789244, 20037508.34789244};
    //array indexes for that data
    private static final int ORIG_X = 0;
    private static final int ORIG_Y = 1; // "

    // Size of square world map in meters, using WebMerc projection.
    private static final double MAP_SIZE = 20037508.34789244 * 2;

    // array indexes for array to hold bounding boxes.
    protected static final int MINX = 0;
    protected static final int MAXX = 1;
    protected static final int MINY = 2;
    protected static final int MAXY = 3;
    private String layer="";

    /**
     * Constructor
     *
     * @param aName                a human-friendly name for this tile source
     * @param aBaseUrl             the base url(s) of the tile server used when constructing the url to download the tiles http://sedac.ciesin.columbia.edu/geoserver/wms
     */
    public WMSTileProvider(String aName, String[] aBaseUrl, String layername) {
        super(aName, 0, 22, 256, "png", aBaseUrl);
    }


    final String WMS_FORMAT_STRING =
            "http://%s" +
                    "?service=WMS" +
                    "&version=1.1.1" +
                    "&request=GetMap" +
                    "&layers=%s" +
                    "&bbox=%f,%f,%f,%f" +
                    "&width=256" +
                    "&height=256" +
                    "&srs=EPSG:900913" +
                    "&format=image/png" +
                    "&transparent=true";


    // Return a web Mercator bounding box given tile x/y indexes and a zoom
    // level.
    protected double[] getBoundingBox(int x, int y, int zoom) {
        double tileSize = MAP_SIZE / Math.pow(2, zoom);
        double minx = TILE_ORIGIN[ORIG_X] + x * tileSize;
        double maxx = TILE_ORIGIN[ORIG_X] + (x+1) * tileSize;
        double miny = TILE_ORIGIN[ORIG_Y] - (y+1) * tileSize;
        double maxy = TILE_ORIGIN[ORIG_Y] - y * tileSize;

        double[] bbox = new double[4];
        bbox[MINX] = minx;
        bbox[MINY] = miny;
        bbox[MAXX] = maxx;
        bbox[MAXY] = maxy;

        return bbox;
    }

    @Override
    public String getTileURLString(MapTile aTile) {
        double[] bbox = getBoundingBox(aTile.getX(), aTile.getY(), aTile.getZoomLevel());
        String s = String.format(Locale.US, WMS_FORMAT_STRING, getBaseUrl(), layer, bbox[MINX],
                bbox[MINY], bbox[MAXX], bbox[MAXY]);
        Log.d("WMSDEMO", s);
        return s;
    }
}
