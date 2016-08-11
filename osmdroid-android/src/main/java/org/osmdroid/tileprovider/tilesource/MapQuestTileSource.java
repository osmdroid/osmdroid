package org.osmdroid.tileprovider.tilesource;

import android.content.Context;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.util.ManifestUtil;

/**
 * MapQuest tile source, revised as 2016 July to meet the new way to access tiles, via api key
 * @author alex
 * @since 5.3
 */
public class MapQuestTileSource extends OnlineTileSourceBase
{
    /** the meta data key in the manifest */
    //<meta-data android:name="MAPQUEST_MAPID" android:value="YOUR KEY" />

    private static final String MAPBOX_MAPID = "MAPQUEST_MAPID";
    //<meta-data android:name="MAPQUEST_ACCESS_TOKEN" android:value="YOUR TOKEN" />
    private static final String ACCESS_TOKEN = "MAPQUEST_ACCESS_TOKEN";

	private static final String[] mapBoxBaseUrl = new String[]{
            "http://api.tiles.mapbox.com/v4/",};

	private String mapBoxMapId = "mapquest.streets-mb";
     private String accessToken;

    /**
     * creates a new mapbox tile source, loading the access token and mapid from the manifest
     * @param ctx
     * @since 5.1
     */
    public MapQuestTileSource(final Context ctx)
    {
		super("mapbox", 1, 19, 256, ".png", mapBoxBaseUrl);
          retrieveAccessToken(ctx);
          retrieveMapBoxMapId(ctx);
        mName="mapbox" + mapBoxMapId;
    }

    /**
     * creates a new mapbox tile source, using the specified access token and mapbox id
     * @param mapboxid
     * @param accesstoken
     * @since 5.1
     */
    public MapQuestTileSource(final String mapboxid, final String accesstoken)
    {
		super("mapbox"+mapboxid, 1, 19, 256, ".png", mapBoxBaseUrl);
          this.accessToken=accesstoken;
          this.mapBoxMapId=mapboxid;
    }

    /**
     * TileSource allowing majority of options (sans url) to be user selected.
     * <br> <b>Warning, the static method {@link #retrieveMapBoxMapId(Context)} should have been invoked once before constructor invocation</b>
	 * @param name Name
	 * @param zoomMinLevel Minimum Zoom Level
	 * @param zoomMaxLevel Maximum Zoom Level
	 * @param tileSizePixels Size of Tile Pixels
	 * @param imageFilenameEnding Image File Extension
	 */
    public MapQuestTileSource(String name, int zoomMinLevel, int zoomMaxLevel, int tileSizePixels, String imageFilenameEnding)
    {
		super(name, zoomMinLevel, zoomMaxLevel, tileSizePixels, imageFilenameEnding, mapBoxBaseUrl);
    }

    /**
     * TileSource allowing all options to be user selected.
     * <br> <b>Warning, the static method {@link #retrieveMapBoxMapId(Context)} should have been invoked once before constructor invocation</b>
     * @param name Name
     * @param zoomMinLevel Minimum Zoom Level
     * @param zoomMaxLevel Maximum Zoom Level
     * @param tileSizePixels Size of Tile Pixels
     * @param imageFilenameEnding Image File Extension
     * @param mapBoxVersionBaseUrl MapBox Version Base Url @see https://www.mapbox.com/developers/api/#Versions
     */
    public MapQuestTileSource(String name, int zoomMinLevel, int zoomMaxLevel, int tileSizePixels, String imageFilenameEnding, String mapBoxMapId, String mapBoxVersionBaseUrl)
    {
		super(name, zoomMinLevel, zoomMaxLevel, tileSizePixels, imageFilenameEnding,
				new String[] { mapBoxVersionBaseUrl });
    }

    /**
     * Reads the mapbox map id from the manifest.<br>
     *     It will use the default value of mapquest if not defined
     */
    public final void retrieveMapBoxMapId(final Context aContext)
    {
        // Retrieve the MapId from the Manifest
        String temp = ManifestUtil.retrieveKey(aContext, MAPBOX_MAPID);
        if (temp!=null && temp.length()>0)
            mapBoxMapId=temp;
    }
    
    /**
     * Reads the access token from the manifest.
     */
    public final void retrieveAccessToken(final Context aContext)
    {
        // Retrieve the MapId from the Manifest
        accessToken = ManifestUtil.retrieveKey(aContext, ACCESS_TOKEN);
    }

    public void setMapboxMapid(String key){
        mapBoxMapId=key;
    }

    public String getMapBoxMapId()
    {
        return mapBoxMapId;
    }

    @Override
    public String getTileURLString(final MapTile aMapTile)
    {
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append(getMapBoxMapId());
        url.append("/");
        url.append(aMapTile.getZoomLevel());
        url.append("/");
        url.append(aMapTile.getX());
        url.append("/");
        url.append(aMapTile.getY());
        url.append(".png");
        url.append("?access_token=").append(getAccessToken());
        String res = url.toString();

        return res;
    }
    
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessTokeninput) {
        accessToken = accessTokeninput;
    }
}