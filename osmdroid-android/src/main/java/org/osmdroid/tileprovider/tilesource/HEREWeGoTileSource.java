package org.osmdroid.tileprovider.tilesource;

import android.content.Context;

import org.osmdroid.tileprovider.util.ManifestUtil;
import org.osmdroid.util.MapTileIndex;

/**
 * HERE We Go
 *
 * @since 5.3
 * Created by alex on 8/11/16.
 */

public class HEREWeGoTileSource extends OnlineTileSourceBase {
    /**
     * the meta data key in the manifest
     */
    //<meta-data android:name="HEREWEGO_MAPID" android:value="YOUR KEY" />

    private static final String HEREWEGO_MAPID = "HEREWEGO_MAPID";

    //<meta-data android:name="HEREWEGO_ACCESS_TOKEN" android:value="YOUR TOKEN" />
    private static final String HEREWEGO_APPID = "HEREWEGO_APPID";

    //<meta-data android:name="HEREWEGO_APPCODE" android:value="YOUR TOKEN" />
    private static final String APPCODE = "HEREWEGO_APPCODE";

    //<meta-data android:name="HEREWEGO_DOMAIN_OVERRIDE" android:value="aerial.maps.cit.api.here.com" />
    private static final String HEREWEGO_DOMAIN_OVERRIDE = "HEREWEGO_OVERRIDE";

    private static final String COPYRIGHT = "© 1987 - 2019 HERE. All rights reserved.";
    private static final String[] mapBoxBaseUrl = new String[]{
            "http://1.{domain}/maptile/2.1/maptile/newest/",
            "http://2.{domain}/maptile/2.1/maptile/newest/",
            "http://3.{domain}/maptile/2.1/maptile/newest/",
            "http://4.{domain}/maptile/2.1/maptile/newest/"};

    private String herewegoMapId = "hybrid.day";
    private String appId = "";
    private String appCode = "";
    private String domainOverride = "aerial.maps.cit.api.here.com";

    /**
     * Creates a MapBox TileSource. You won't be able to use it until you set the access token and map id.
     */
    public HEREWeGoTileSource() {
        super("herewego", 1, 20, 256, ".png", mapBoxBaseUrl, COPYRIGHT);
    }

    /**
     * creates a new mapbox tile source, loading the access token and mapid from the manifest
     *
     * @param ctx
     * @since 5.1
     */
    public HEREWeGoTileSource(final Context ctx) {
        super("herewego", 1, 20, 256, ".png", mapBoxBaseUrl, COPYRIGHT);
        retrieveAppId(ctx);
        retrieveMapBoxMapId(ctx);
        retrieveAppCode(ctx);
        retrieveDomainOverride(ctx);
        //this line will ensure uniqueness in the tile cache
        mName = "herewego" + herewegoMapId;
    }

    private void retrieveDomainOverride(final Context aContext) {
        String temp = ManifestUtil.retrieveKey(aContext, HEREWEGO_DOMAIN_OVERRIDE);
        if (temp != null && temp.length() > 0)
            domainOverride = temp;
    }

    public void setDomainOverride(String hostname) {
        domainOverride = hostname;
    }

    /**
     * creates a new mapbox tile source, using the specified access token and mapbox id
     *
     * @since 5.1
     */
    public HEREWeGoTileSource(final String herewegoMapId, final String accesstoken, final String appCode) {
        super("herewego" + herewegoMapId, 1, 20, 256, ".png", mapBoxBaseUrl, COPYRIGHT);
        this.appId = accesstoken;
        this.herewegoMapId = herewegoMapId;
        this.appCode = appCode;
    }

    /**
     * TileSource allowing majority of options (sans url) to be user selected.
     * <br> <b>Warning, the static method {@link #retrieveMapBoxMapId(android.content.Context)} should have been invoked once before constructor invocation</b>
     *
     * @param name                Name
     * @param zoomMinLevel        Minimum Zoom Level
     * @param zoomMaxLevel        Maximum Zoom Level
     * @param tileSizePixels      Size of Tile Pixels
     * @param imageFilenameEnding Image File Extension
     */
    public HEREWeGoTileSource(String name, int zoomMinLevel, int zoomMaxLevel, int tileSizePixels, String imageFilenameEnding) {
        super(name, zoomMinLevel, zoomMaxLevel, tileSizePixels, imageFilenameEnding, mapBoxBaseUrl, COPYRIGHT);
    }

    /**
     * TileSource allowing all options to be user selected.
     * <br> <b>Warning, the static method {@link #retrieveMapBoxMapId(android.content.Context)} should have been invoked once before constructor invocation</b>
     *
     * @param name                 Name
     * @param zoomMinLevel         Minimum Zoom Level
     * @param zoomMaxLevel         Maximum Zoom Level
     * @param tileSizePixels       Size of Tile Pixels
     * @param imageFilenameEnding  Image File Extension
     * @param mapBoxVersionBaseUrl MapBox Version Base Url @see https://www.mapbox.com/developers/api/#Versions
     */
    public HEREWeGoTileSource(String name, int zoomMinLevel, int zoomMaxLevel, int tileSizePixels, String imageFilenameEnding, String mapBoxMapId, String mapBoxVersionBaseUrl) {
        super(name, zoomMinLevel, zoomMaxLevel, tileSizePixels, imageFilenameEnding,
                new String[]{mapBoxVersionBaseUrl}, "© 1987 - 2017 HERE. All rights reserved.");
    }

    public final void retrieveAppCode(final Context aContext) {
        // Retrieve the MapId from the Manifest
        appCode = ManifestUtil.retrieveKey(aContext, APPCODE);
    }

    /**
     * Reads the mapbox map id from the manifest.<br>
     */
    public final void retrieveMapBoxMapId(final Context aContext) {
        // Retrieve the MapId from the Manifest
        herewegoMapId = ManifestUtil.retrieveKey(aContext, HEREWEGO_MAPID);
    }

    /**
     * Reads the access token from the manifest.
     */
    public final void retrieveAppId(final Context aContext) {
        // Retrieve the MapId from the Manifest
        appId = ManifestUtil.retrieveKey(aContext, HEREWEGO_APPID);
    }

    public void setHereWeGoMapid(String key) {
        herewegoMapId = key;
        mName = "herewego" + herewegoMapId;
    }

    public String getHerewegoMapId() {
        return herewegoMapId;
    }

    @Override
    public String getTileURLString(final long pMapTileIndex) {
        StringBuilder url = new StringBuilder(getBaseUrl().replace("{domain}", domainOverride));
        url.append(getHerewegoMapId());
        url.append("/");
        url.append(MapTileIndex.getZoom(pMapTileIndex));
        url.append("/");
        url.append(MapTileIndex.getX(pMapTileIndex));
        url.append("/");
        url.append(MapTileIndex.getY(pMapTileIndex));
        url.append("/").append(getTileSizePixels()).append("/png8?");
        url.append("app_id=").append(getAppId());
        url.append("&app_code=").append(getAppCode());
        url.append("&lg=pt-BR");
        String res = url.toString();
        //System.out.println(res);

        return res;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String accessTokeninput) {
        appId = accessTokeninput;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }
}