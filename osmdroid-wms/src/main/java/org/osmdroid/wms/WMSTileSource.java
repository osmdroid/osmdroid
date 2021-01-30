package org.osmdroid.wms;

import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.MapTileIndex;


/**
 * WMS Map Source. Use this in conjunction with the WMSParser.
 * <p>
 * This class is known to work with GeoServer, but only supports a subset of possible
 * WMS configurations.
 *
 * @author Alex O'Ree
 * @see WMSParser
 * https://github.com/osmdroid/osmdroid/issues/177
 * @since 6.0.0
 * 11/5/15.
 */
public class WMSTileSource extends OnlineTileSourceBase {


    // array indexes for array to hold bounding boxes.
    protected static final int MINX = 0;
    protected static final int MAXX = 1;
    protected static final int MINY = 2;
    protected static final int MAXY = 3;
    // Web Mercator n/w corner of the map.
    private static final double[] TILE_ORIGIN = {-20037508.34789244, 20037508.34789244};
    //array indexes for that data
    private static final int ORIG_X = 0;
    private static final int ORIG_Y = 1; // "
    // Size of square world map in meters, using WebMerc projection.
    private static final double MAP_SIZE = 20037508.34789244 * 2;
    final String WMS_FORMAT_STRING =
            "%s" +
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
    private String layer = "";
    private String version = "1.1.0";
    private String srs = "EPSG:900913";   //used by geo server
    private String style = null;
    private boolean forceHttps = false;
    private boolean forceHttp = false;

    /**
     * Constructor
     *
     * @param aName    a human-friendly name for this tile source
     * @param aBaseUrl the base url(s) of the tile server used when constructing the url to download the tiles http://sedac.ciesin.columbia.edu/geoserver/wms
     */
    public WMSTileSource(String aName, String[] aBaseUrl, String layername, String version, String srs, String style, int size) {
        super(aName, 0, 22, size, "png", aBaseUrl);
        Log.i(IMapView.LOGTAG, "WMS support is BETA. Please report any issues");
        this.layer = layername;
        this.version = version;
        if (srs != null)
            this.srs = srs;
        this.style = style;

    }

    public static WMSTileSource createFrom(WMSEndpoint endpoint, WMSLayer layer) {
        String srs = "EPSG:900913";
        if (!layer.getSrs().isEmpty()) {
            srs = layer.getSrs().get(0);
        }
        if (layer.getStyles().isEmpty()) {
            WMSTileSource r = new WMSTileSource(layer.getName(), new String[]{endpoint.getBaseurl()}, layer.getName(),
                    endpoint.getWmsVersion(), srs, null, layer.getPixelSize());
            return r;
        }

        WMSTileSource r = new WMSTileSource(layer.getName(), new String[]{endpoint.getBaseurl()}, layer.getName(),
                endpoint.getWmsVersion(), srs, layer.getStyles().get(0), layer.getPixelSize());
        return r;

    }

    public static BoundingBox tile2boundingBox(final int x, final int y, final int zoom) {

        BoundingBox bb = new BoundingBox(tile2lat(y, zoom), tile2lon(x + 1, zoom), tile2lat(y + 1, zoom), tile2lon(x, zoom));
        return bb;
    }

    public static double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    public static double tile2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    // Return a web Mercator bounding box given tile x/y indexes and a zoom
    // level.
    public double[] getBoundingBox(int x, int y, int zoom) {
        double tileSize = MAP_SIZE / Math.pow(2, zoom);
        double minx = TILE_ORIGIN[ORIG_X] + x * tileSize;
        double maxx = TILE_ORIGIN[ORIG_X] + (x + 1) * tileSize;
        double miny = TILE_ORIGIN[ORIG_Y] - (y + 1) * tileSize;
        double maxy = TILE_ORIGIN[ORIG_Y] - y * tileSize;

        double[] bbox = new double[4];
        bbox[MINX] = minx;
        bbox[MINY] = miny;
        bbox[MAXX] = maxx;
        bbox[MAXY] = maxy;

        return bbox;
    }

    public boolean isForceHttps() {
        return forceHttps;
    }

    public void setForceHttps(boolean forceHttps) {
        this.forceHttps = forceHttps;
    }

    public boolean isForceHttp() {
        return forceHttp;
    }

    public void setForceHttp(boolean forceHttp) {
        this.forceHttp = forceHttp;
    }

    @Override
    public String getTileURLString(final long pMapTileIndex) {

        String baseUrl = getBaseUrl();
        if (forceHttps)
            baseUrl = baseUrl.replace("http://", "https://");
        if (forceHttp)
            baseUrl = baseUrl.replace("https://", "http://");
        StringBuilder sb = new StringBuilder(baseUrl);
        if (!baseUrl.endsWith("&"))
            sb.append("&");

        sb.append("request=GetMap&width=").append(getTileSizePixels()).append("&height=").append(getTileSizePixels()).append("&version=").append(version);
        sb.append("&layers=").append(layer);
        sb.append("&bbox=");
        if (srs.equals("EPSG:900913")) {
            //geoserver style
            double[] bbox = getBoundingBox(MapTileIndex.getX(pMapTileIndex), MapTileIndex.getY(pMapTileIndex), MapTileIndex.getZoom(pMapTileIndex));
            sb.append(bbox[MINX]).append(",");
            sb.append(bbox[MINY]).append(",");
            sb.append(bbox[MAXX]).append(",");
            sb.append(bbox[MAXY]);
        } else {
            BoundingBox boundingBox = tile2boundingBox(MapTileIndex.getX(pMapTileIndex), MapTileIndex.getY(pMapTileIndex), MapTileIndex.getZoom(pMapTileIndex));
            sb.append(boundingBox.getLonWest()).append(",");
            sb.append(boundingBox.getLatSouth()).append(",");
            sb.append(boundingBox.getLonEast()).append(",");
            sb.append(boundingBox.getLatNorth());
        }
        sb.append("&srs=").append(srs);
        sb.append("&format=image/png&transparent=true");
        if (style != null)
            sb.append("&styles=").append(style);

        Log.i(IMapView.LOGTAG, sb.toString());
        return sb.toString();
    }
}
