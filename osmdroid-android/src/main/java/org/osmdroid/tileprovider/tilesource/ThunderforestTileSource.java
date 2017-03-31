package org.osmdroid.tileprovider.tilesource;

import android.content.Context;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.util.ManifestUtil;

/**
 * Thunderforest Maps including OpenCycleMap
 */

public class ThunderforestTileSource  extends OnlineTileSourceBase
{
    /** the meta data key in the manifest */
    //<meta-data android:name="THUNDERFOREST_MAPID" android:value="YOUR KEY" />

    private static final String THUNDERFOREST_MAPID = "THUNDERFOREST_MAPID";

    /** the available map types */
    public static final String CYCLE="cycle";
    public static final String TRANSPORT="transport";
    public static final String LANDSCAPE="landscape";
    public static final String OUTDOORS="outdoors";
    public static final String TRANSPORT_DARK="transport-dark";
    public static final String SPINAL_MAP="spinal-map";
    public static final String PIONEER="pioneer";
    public static final String MOBILE_ATLAS="mobile-atlas";
    public static final String NEIGHBOURHOOD="neighbourhood";


    private static final String[] baseUrl = new String[]{
            "https://a.tile.thunderforest.com/{map}/",
            "https://b.tile.thunderforest.com/{map}/",
            "https://c.tile.thunderforest.com/{map}/"};

    private final String mMap;
    private String mMapId = "";

    /**
     * creates a new Thunderforest tile source, loading the access token and mapid from the manifest
     * @param ctx
     * @param map choice of map to use (eg. CYCLE)
     */
    public ThunderforestTileSource(final Context ctx, final String aMap)
    {
        //TODO UI name differs from URL name
	super(aMap, 0, 17, 256, ".png", baseUrl, "Maps © Thunderforest, Data © OpenStreetMap contributors.");
        mMap=aMap;
        retrieveMapId(ctx);
        //this line will ensure uniqueness in the tile cache
        //mName="thunderforest"+aMap+mMapId;
    }

    /**
     * Reads the map id from the manifest.<br>
     */
    public final void retrieveMapId(final Context aContext)
    {
        // Retrieve the MapId from the Manifest
        mMapId = ManifestUtil.retrieveKey(aContext, THUNDERFOREST_MAPID);
    }

    @Override
    public String getTileURLString(final MapTile aMapTile)
    {
        StringBuilder url = new StringBuilder(getBaseUrl().replace("{map}",mMap));
        url.append(aMapTile.getZoomLevel());
        url.append("/");
        url.append(aMapTile.getX());
        url.append("/");
        url.append(aMapTile.getY());
        url.append(".png?");
        url.append("apikey=").append(mMapId);
        String res = url.toString();
        //Log.d(IMapView.LOGTAG, res);

        return res;
    }

    /**
     * check if we have a key in the manifest for this provider.
     * @param ctx
     */
    public static boolean haveMapId(final Context aContext)
    {
        return !ManifestUtil.retrieveKey(aContext, THUNDERFOREST_MAPID).equals("");
    }

}
