package org.osmdroid.config;

import android.content.Context;
import android.content.SharedPreferences;

import org.osmdroid.tileprovider.util.StorageUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.HTTP_EXPIRES_HEADER_FORMAT;

/**
 * Default configuration provider for osmdroid
 * <a href="https://github.com/osmdroid/osmdroid/issues/481">Issue 481</a>
 * Created on 11/29/2016.
 * @author Alex O'Ree
 * @see IConfigurationProvider
 * @see Configuration
 */
public class DefaultConfigurationProvider implements IConfigurationProvider {

    private long gpsWaitTime =20000;
    private boolean debugMode= false;
    private boolean debugMapView = false;
    private boolean debugTileProviders = false;
    private boolean debugMapTileDownloader=false;
    private boolean isMapViewHardwareAccelerated=false;
    private String userAgentValue="osmdroid";
    private String userAgentHttpHeader = "User-Agent";
    private short cacheMapTileCount = 9;
    private short tileDownloadThreads = 2;
    private short tileFileSystemThreads = 8;
    private short tileDownloadMaxQueueSize = 40;
    private short tileFileSystemMaxQueueSize = 40;
    private long tileFileSystemCacheMaxBytes = 600L * 1024 * 1024;
    private long tileFileSystemCacheTrimBytes = 500L * 1024 * 1024;
    private SimpleDateFormat httpHeaderDateTimeFormat = new SimpleDateFormat(HTTP_EXPIRES_HEADER_FORMAT, Locale.US);
    private File osmdroidBasePath = new File(StorageUtils.getStorage().getAbsolutePath(), "osmdroid");
    private File osmdroidTileCache =  new File(getOsmdroidBasePath(), "tiles");

    /**
     * default is 20 seconds
     * @return time in ms
     */
    @Override
    public long getGpsWaitTime() {
        return gpsWaitTime;
    }

    @Override
    public void setGpsWaitTime(long gpsWaitTime) {
        this.gpsWaitTime = gpsWaitTime;
    }

    @Override
    public boolean isDebugMode() {
        return debugMode;
    }

    @Override
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public boolean isDebugMapView() {
        return debugMapView;
    }

    @Override
    public void setDebugMapView(boolean debugMapView) {
        this.debugMapView = debugMapView;
    }

    @Override
    public boolean isDebugTileProviders() {
        return debugTileProviders;
    }

    @Override
    public void setDebugTileProviders(boolean debugTileProviders) {
        this.debugTileProviders = debugTileProviders;
    }

    @Override
    public boolean isDebugMapTileDownloader() {
        return debugMapTileDownloader;
    }

    @Override
    public void setDebugMapTileDownloader(boolean debugMapTileDownloader) {
        this.debugMapTileDownloader = debugMapTileDownloader;
    }

    @Override
    public boolean isMapViewHardwareAccelerated() {
        return isMapViewHardwareAccelerated;
    }

    @Override
    public void setMapViewHardwareAccelerated(boolean mapViewHardwareAccelerated) {
        isMapViewHardwareAccelerated = mapViewHardwareAccelerated;
    }

    @Override
    public String getUserAgentValue() {
        return userAgentValue;
    }

    @Override
    public void setUserAgentValue(String userAgentValue) {
        this.userAgentValue = userAgentValue;
    }

    @Override
    public short getCacheMapTileCount() {
        return cacheMapTileCount;
    }

    @Override
    public void setCacheMapTileCount(short cacheMapTileCount) {
        this.cacheMapTileCount = cacheMapTileCount;
    }

    @Override
    public short getTileDownloadThreads() {
        return tileDownloadThreads;
    }

    @Override
    public void setTileDownloadThreads(short tileDownloadThreads) {
        this.tileDownloadThreads = tileDownloadThreads;
    }

    @Override
    public short getTileFileSystemThreads() {
        return tileFileSystemThreads;
    }

    @Override
    public void setTileFileSystemThreads(short tileFileSystemThreads) {
        this.tileFileSystemThreads = tileFileSystemThreads;
    }

    @Override
    public short getTileDownloadMaxQueueSize() {
        return tileDownloadMaxQueueSize;
    }

    @Override
    public void setTileDownloadMaxQueueSize(short tileDownloadMaxQueueSize) {
        this.tileDownloadMaxQueueSize = tileDownloadMaxQueueSize;
    }

    @Override
    public short getTileFileSystemMaxQueueSize() {
        return tileFileSystemMaxQueueSize;
    }

    @Override
    public void setTileFileSystemMaxQueueSize(short tileFileSystemMaxQueueSize) {
        this.tileFileSystemMaxQueueSize = tileFileSystemMaxQueueSize;
    }

    @Override
    public long getTileFileSystemCacheMaxBytes() {
        return tileFileSystemCacheMaxBytes;
    }

    @Override
    public void setTileFileSystemCacheMaxBytes(long tileFileSystemCacheMaxBytes) {
        this.tileFileSystemCacheMaxBytes = tileFileSystemCacheMaxBytes;
    }

    @Override
    public long getTileFileSystemCacheTrimBytes() {
        return tileFileSystemCacheTrimBytes;
    }

    @Override
    public void setTileFileSystemCacheTrimBytes(long tileFileSystemCacheTrimBytes) {
        this.tileFileSystemCacheTrimBytes = tileFileSystemCacheTrimBytes;
    }

    @Override
    public SimpleDateFormat getHttpHeaderDateTimeFormat() {
        return httpHeaderDateTimeFormat;
    }

    @Override
    public void setHttpHeaderDateTimeFormat(SimpleDateFormat httpHeaderDateTimeFormat) {
        this.httpHeaderDateTimeFormat = httpHeaderDateTimeFormat;
    }

    @Override
    public File getOsmdroidBasePath() {
        return osmdroidBasePath;
    }

    @Override
    public void setOsmdroidBasePath(File osmdroidBasePath) {
        this.osmdroidBasePath = osmdroidBasePath;
    }

    @Override
    public File getOsmdroidTileCache() {
        return osmdroidTileCache;
    }

    @Override
    public void setOsmdroidTileCache(File osmdroidTileCache) {
        this.osmdroidTileCache = osmdroidTileCache;
    }


    @Override
    public String getUserAgentHttpHeader() {
        return userAgentHttpHeader;
    }

    @Override
    public void setUserAgentHttpHeader(String userAgentHttpHeader) {
        this.userAgentHttpHeader = userAgentHttpHeader;
    }

    @Override
    public void load(Context ctx, SharedPreferences preferences) {

    }

    @Override
    public void save(Context ctx, SharedPreferences preferences) {

    }
}
