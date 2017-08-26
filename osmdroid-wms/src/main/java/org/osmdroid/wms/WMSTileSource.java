package org.osmdroid.wms;

import android.util.Log;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

import java.util.Locale;

/**
 * WMS Map Source. Use this in conjunction with the WMSParser
 * @see WMSParser
 * https://github.com/osmdroid/osmdroid/issues/177
 * @author Alex O'Ree
 * @since 5.6.6
 * 11/5/15.
 */
public class WMSTileSource extends OnlineTileSourceBase{


    public static WMSTileSource createFrom(WMSEndpoint endpoint, WMSLayer layer) {
        if (layer.getStyles().isEmpty()) {
            WMSTileSource r = new WMSTileSource(layer.getName(), new String[]{endpoint.getBaseurl()},layer.getName(), endpoint.getWmsVersion(), layer.getSrs(), null);
            return r;
        }

        WMSTileSource r = new WMSTileSource(layer.getName(), new String[]{endpoint.getBaseurl()},layer.getName(), endpoint.getWmsVersion(), layer.getSrs(),layer.getStyles().get(0));
        return r;

    }
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
    private String version="1.1.0";
    private String srs="EPSG:900913";
    private String style=null;
    /**
     * Constructor
     *
     * @param aName                a human-friendly name for this tile source
     * @param aBaseUrl             the base url(s) of the tile server used when constructing the url to download the tiles http://sedac.ciesin.columbia.edu/geoserver/wms
     */
    public WMSTileSource(String aName, String[] aBaseUrl, String layername, String version, String srs, String style) {
        super(aName, 0, 22, 256, "png", aBaseUrl);
        this.layer=layername;
        this.version=version;
        if (srs!=null)
            this.srs=srs;
        this.style=style;

    }


    final String WMS_FORMAT_STRING =
            "%s"+
                    "&version=%s" +
                    "&request=GetMap" +
                    "&layers=%s" +
                    "&bbox=%f,%f,%f,%f" +
                    "&width=256" +
                    "&height=256" +
                    "&srs=%s" +
                    "&format=image/png" +
                    "&style=%s" +
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
        String baseUrl = getBaseUrl();
        StringBuilder sb = new StringBuilder(baseUrl);
        if (!baseUrl.endsWith("&"))
            sb.append("&");
        sb.append("request=GetMap&width=256&height=256&version=").append(version);
        sb.append("&layers=").append(layer);
        sb.append("&bbox=");
        sb.append(bbox[MINX]).append(",");
        sb.append(bbox[MINY]).append(",");
        sb.append(bbox[MAXX]).append(",");
        sb.append(bbox[MAXY]);
        sb.append("&srs=").append(srs);
        sb.append("&format=image/png&transparent=true");
        if (style!=null)
            sb.append("&styles=").append(style);

        Log.d("WMSDEMO", sb.toString());
        return sb.toString();
    }
}
