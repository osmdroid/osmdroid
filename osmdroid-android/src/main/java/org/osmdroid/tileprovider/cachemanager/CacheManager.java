package org.osmdroid.tileprovider.cachemanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Point;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.util.Counters;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MyMath;
import org.osmdroid.util.TileSystem;
import org.osmdroid.util.constants.GeoConstants;
import org.osmdroid.views.MapView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

;

/**
 * Provides various methods for managing the local filesystem cache of osmdroid tiles: <br>
 * - Dowloading of tiles inside a specified area, <br>
 * - Cleaning of tiles inside a specified area,<br>
 * - Information about cache capacity and current cache usage. <br>
 * <p></p>
 * Important note 1: <br>
 * These methods only make sense for a MapView using an OnlineTileSourceBase:
 * bitmap tiles downloaded from urls. <br>
 * <p></p>
 * Important note 2 - about Bulk Downloading:<br>
 * When using OSM Mapnik tile server as the tile source, take care about OSM Tile usage policy
 * (http://wiki.openstreetmap.org/wiki/Tile_usage_policy).
 * Do not let to end-users the ability to download significant areas of tiles. <br>
 *
 * @author M.Kergall
 * @author Alex
 * @author 2ndGAB
 * @author F.Fontaine
 */
public class CacheManager {

    protected final MapTileProviderBase mTileProvider;
    protected final IFilesystemCache mTileWriter;
    protected final int mMinZoomLevel;
    protected final int mMaxZoomLevel;
    protected Set<CacheManagerTask> mPendingTasks = new HashSet<>();

    public CacheManager(final MapView mapView) {
        this(mapView, mapView.getTileProvider().getTileWriter());
    }

    public CacheManager(final MapView mapView, IFilesystemCache writer) {
        this(mapView.getTileProvider(), writer, mapView.getMinZoomLevel(), mapView.getMaxZoomLevel());
    }

    /**
     * See https://github.com/osmdroid/osmdroid/issues/619
     * @since 5.6.5
     */
    public CacheManager(final MapTileProviderBase pTileProvider,
                        final IFilesystemCache pWriter,
                        final int pMinZoomLevel, final int pMaxZoomLevel) {
        mTileProvider = pTileProvider;
        mTileWriter = pWriter;
        mMinZoomLevel = pMinZoomLevel;
        mMaxZoomLevel = pMaxZoomLevel;
    }

    /**
     * @since 5.6.3
     * @return
     */
    public int getPendingJobs(){
        return mPendingTasks.size();
    }

    public static Point getMapTileFromCoordinates(final double aLat, final double aLon, final int zoom) {
        final int y = (int) Math.floor((1 - Math.log(Math.tan(aLat * Math.PI / 180) + 1 / Math.cos(aLat * Math.PI / 180)) / Math.PI) / 2 * (1 << zoom));
        final int x = (int) Math.floor((aLon + 180) / 360 * (1 << zoom));
        return new Point(x, y);
    }

    public static GeoPoint getCoordinatesFromMapTile(final int x, final int y, final int zoom) {

        double n = Math.PI - 2 * Math.PI * y / (1 << zoom);
        final double lat = (180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n))));
        final double lon = (360.0 * x / (1 << zoom)) - 180.0;
        return new GeoPoint(lat, lon);
    }

    public static File getFileName(ITileSource tileSource, MapTile tile) {
        final File file = new File(Configuration.getInstance().getOsmdroidTileCache(),
                tileSource.getTileRelativeFilenameString(tile) + OpenStreetMapTileProviderConstants.TILE_PATH_EXTENSION);
        return file;
    }

    /**
     * @return true if success, false if error
     */
    public boolean loadTile(final OnlineTileSourceBase tileSource, final MapTile tile) {
        //check if file is already downloaded:
        File file = getFileName(tileSource, tile);
        if (file.exists()) {
            return true;
        }
        //check if the destination already has the file
        if (mTileWriter.exists(tileSource,tile)){
            return true;
        }

        InputStream in = null;
        HttpURLConnection c=null;

        try {


            final String tileURLString = tileSource.getTileURLString(tile);

            if (Configuration.getInstance().isDebugMode()) {
                Log.d(IMapView.LOGTAG,"Downloading Maptile from url: " + tileURLString);
            }

            if (TextUtils.isEmpty(tileURLString)) {
                return false;
            }

            c = (HttpURLConnection) new URL(tileURLString).openConnection();
            c.setUseCaches(true);
            c.setRequestProperty(Configuration.getInstance().getUserAgentHttpHeader(),Configuration.getInstance().getUserAgentValue());
            for (final Map.Entry<String, String> entry : Configuration.getInstance().getAdditionalHttpRequestProperties().entrySet()) {
                c.setRequestProperty(entry.getKey(), entry.getValue());
            }
            c.connect();


            // Check to see if we got success

            if (c.getResponseCode() != 200) {
                Log.w(IMapView.LOGTAG, "Problem downloading MapTile: " + tile + " HTTP response: " + c.getResponseMessage());
                Counters.tileDownloadErrors++;
                return false;
            }


            in = c.getInputStream();

            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();

            //default is 1 week from now
            Date dateExpires;
            Long override=Configuration.getInstance().getExpirationOverrideDuration();
            if (override!=null) {
                dateExpires= new Date(System.currentTimeMillis() + override);
            } else {
                dateExpires = new Date(System.currentTimeMillis() + OpenStreetMapTileProviderConstants.DEFAULT_MAXIMUM_CACHED_FILE_AGE + Configuration.getInstance().getExpirationExtendedDuration());
                final String expires = c.getHeaderField(OpenStreetMapTileProviderConstants.HTTP_EXPIRES_HEADER);
                if (expires != null && expires.length() > 0) {
                    try {
                        dateExpires = Configuration.getInstance().getHttpHeaderDateTimeFormat().parse(expires);
                        dateExpires.setTime(dateExpires.getTime() + Configuration.getInstance().getExpirationExtendedDuration());
                    } catch (Exception ex) {
                        if (Configuration.getInstance().isDebugMapTileDownloader())
                            Log.d(IMapView.LOGTAG, "Unable to parse expiration tag for tile, using default, server returned " + expires, ex);
                    }
                }
            }
            tile.setExpires(dateExpires);
            // Save the data to the filesystem cache
            mTileWriter.saveFile(tileSource, tile, in);
            return true;
        } catch (final UnknownHostException e) {
            // no network connection so empty the queue
            Log.w(IMapView.LOGTAG,"UnknownHostException downloading MapTile: " + tile + " : " + e);
            Counters.tileDownloadErrors++;
            return false;
        } catch (final FileNotFoundException e) {
            Counters.tileDownloadErrors++;
            Log.w(IMapView.LOGTAG,"Tile not found: " + tile + " : " + e);
        } catch (final IOException e) {
            Counters.tileDownloadErrors++;
            Log.w(IMapView.LOGTAG,"IOException downloading MapTile: " + tile + " : " + e);
        } catch (final Throwable e) {
            Counters.tileDownloadErrors++;
            Log.e(IMapView.LOGTAG,"Error downloading MapTile: " + tile, e);
        } finally {
            StreamUtils.closeStream(in);
            try{
                c.disconnect();
            } catch (Exception ex){}
        }
        return false;
    }

    public boolean deleteTile(final MapTile pTile) {
        final ITileSource tileSource = mTileProvider.getTileSource();
        return mTileWriter.exists(tileSource, pTile) && mTileWriter.remove(tileSource, pTile);
    }

    public boolean checkTile(final MapTile pTile) {
        return mTileWriter.exists(mTileProvider.getTileSource(), pTile);
    }

    /**
     * Computes the theoretical tiles covered by the bounding box
     * @return list of tiles, sorted by ascending zoom level
     */
    public static List<MapTile> getTilesCoverage(final BoundingBox pBB,
                                                 final int pZoomMin, final int pZoomMax) {
        final List<MapTile> result = new ArrayList<>();
        for (int zoomLevel = pZoomMin; zoomLevel <= pZoomMax; zoomLevel++) {
            final Collection<MapTile> resultForZoom = getTilesCoverage(pBB, zoomLevel);
            result.addAll(resultForZoom);
        }
        return result;
    }

    /**
     * Computes the theoretical tiles covered by the bounding box
     * @return list of tiles for that zoom level, without any specific order
     */
    public static Collection<MapTile> getTilesCoverage(final BoundingBox pBB, final int pZoomLevel){
        final Set<MapTile> result = new HashSet<>();
        final int mapTileUpperBound = 1 << pZoomLevel;
        final Point lowerRight = getMapTileFromCoordinates(
                pBB.getLatSouth(), pBB.getLonEast(), pZoomLevel);
        final Point upperLeft = getMapTileFromCoordinates(
                pBB.getLatNorth(), pBB.getLonWest(), pZoomLevel);
        int width = lowerRight.x - upperLeft.x + 1; // handling the modulo
        if (width <= 0) {
            width += mapTileUpperBound;
        }
        int height = lowerRight.y - upperLeft.y + 1; // handling the modulo
        if (height <= 0) {
            height += mapTileUpperBound;
        }
        for (int i = 0 ; i < width ; i ++) {
            for (int j = 0 ; j < height ; j ++) {
                final int x = MyMath.mod(upperLeft.x + i, mapTileUpperBound);
                final int y = MyMath.mod(upperLeft.y + j, mapTileUpperBound);
                result.add(new MapTile(pZoomLevel, x, y));
            }
        }
        return result;
    }

    /**
     * Computes the theoretical tiles covered by the list of points
     * @return list of tiles, sorted by ascending zoom level
     */
    public static List<MapTile> getTilesCoverage(final ArrayList<GeoPoint> pGeoPoints,
                                                 final int pZoomMin, final int pZoomMax) {
        final List<MapTile> result = new ArrayList<>();
        for (int zoomLevel = pZoomMin; zoomLevel <= pZoomMax; zoomLevel++) {
            final Collection<MapTile> resultForZoom = getTilesCoverage(pGeoPoints, zoomLevel);
            result.addAll(resultForZoom);
        }
        return result;
    }

    /**
     * Computes the theoretical tiles covered by the list of points
     * Calculation done based on http://www.movable-type.co.uk/scripts/latlong.html
     */
    public static Collection<MapTile> getTilesCoverage(final ArrayList<GeoPoint> pGeoPoints,
                                                       final int pZoomLevel) {
        final Set<MapTile> result = new HashSet<>();

        GeoPoint prevPoint = null;
        Point tile, prevTile = null;

        final int mapTileUpperBound = 1 << pZoomLevel;
        for (GeoPoint geoPoint : pGeoPoints) {

            final double d = TileSystem.GroundResolution(geoPoint.getLatitude(), pZoomLevel);

            if (result.size() != 0) {

                if (prevPoint != null) {

                    final double leadCoef = (geoPoint.getLatitude() - prevPoint.getLatitude()) / (geoPoint.getLongitude() - prevPoint.getLongitude());
                    final double brng;
                    if (geoPoint.getLongitude() > prevPoint.getLongitude()) {
                        brng = Math.PI / 2 - Math.atan(leadCoef);
                    } else {
                        brng = 3 * Math.PI / 2 - Math.atan(leadCoef);
                    }

                    final GeoPoint wayPoint = new GeoPoint(prevPoint.getLatitude(), prevPoint.getLongitude());

                    while ((((geoPoint.getLatitude() > prevPoint.getLatitude()) && (wayPoint.getLatitude() < geoPoint.getLatitude())) ||
                            (geoPoint.getLatitude() < prevPoint.getLatitude()) && (wayPoint.getLatitude() > geoPoint.getLatitude())) &&
                            (((geoPoint.getLongitude() > prevPoint.getLongitude()) && (wayPoint.getLongitude() < geoPoint.getLongitude())) ||
                                    ((geoPoint.getLongitude() < prevPoint.getLongitude()) && (wayPoint.getLongitude() > geoPoint.getLongitude())))) {

                        final Point lastPoint = new Point();
                        TileSystem.LatLongToPixelXY(geoPoint.getLatitude(), geoPoint.getLongitude(), pZoomLevel, lastPoint);

                        final double prevLatRad = wayPoint.getLatitude() * Math.PI / 180.0;
                        final double prevLonRad = wayPoint.getLongitude() * Math.PI / 180.0;

                        final double latRad = Math.asin(Math.sin(prevLatRad) * Math.cos(d / GeoConstants.RADIUS_EARTH_METERS) + Math.cos(prevLatRad) * Math.sin(d / GeoConstants.RADIUS_EARTH_METERS) * Math.cos(brng));
                        final double lonRad = prevLonRad + Math.atan2(Math.sin(brng) * Math.sin(d / GeoConstants.RADIUS_EARTH_METERS) * Math.cos(prevLatRad), Math.cos(d / GeoConstants.RADIUS_EARTH_METERS) - Math.sin(prevLatRad) * Math.sin(latRad));

                        wayPoint.setLatitude(((latRad * 180.0 / Math.PI)));
                        wayPoint.setLongitude(((lonRad * 180.0 / Math.PI)));

                        tile = getMapTileFromCoordinates(wayPoint.getLatitude(), wayPoint.getLongitude(), pZoomLevel);

                        if (!tile.equals(prevTile)) {
//Log.d(Constants.APP_TAG, "New Tile lat " + tile.x + " lon " + tile.y);
                            int ofsx = tile.x >= 0 ? 0 : -tile.x;
                            int ofsy = tile.y >= 0 ? 0 : -tile.y;
                            for (int xAround = tile.x + ofsx; xAround <= tile.x + 1 + ofsx; xAround++) {
                                for (int yAround = tile.y + ofsy; yAround <= tile.y + 1 + ofsy; yAround++) {
                                    final int tileY = MyMath.mod(yAround, mapTileUpperBound);
                                    final int tileX = MyMath.mod(xAround, mapTileUpperBound);
                                    result.add(new MapTile(pZoomLevel, tileX, tileY));
                                }
                            }

                            prevTile = tile;
                        }
                    }
                }

            } else {
                tile = getMapTileFromCoordinates(geoPoint.getLatitude(), geoPoint.getLongitude(), pZoomLevel);
                prevTile = tile;

                int ofsx = tile.x >= 0 ? 0 : -tile.x;
                int ofsy = tile.y >= 0 ? 0 : -tile.y;
                for (int xAround = tile.x + ofsx; xAround <= tile.x + 1 + ofsx; xAround++) {
                    for (int yAround = tile.y + ofsy; yAround <= tile.y + 1 + ofsy; yAround++) {
                        final int tileY = MyMath.mod(yAround, mapTileUpperBound);
                        final int tileX = MyMath.mod(xAround, mapTileUpperBound);
                        result.add(new MapTile(pZoomLevel, tileX, tileY));
                    }
                }
            }

            prevPoint = geoPoint;
        }
        return result;
    }

    /**
     * @return the theoretical number of tiles in the specified area
     */
    public int possibleTilesInArea(final BoundingBox pBB, final int pZoomMin, final int pZoomMax) {
        return getTilesCoverage(pBB, pZoomMin, pZoomMax).size();
    }
    /**
     * @return the theoretical number of tiles covered by the list of points
     * Calculation done based on http://www.movable-type.co.uk/scripts/latlong.html
     */
    public int possibleTilesCovered(final ArrayList<GeoPoint> pGeoPoints,
                                    final int pZoomMin, final int pZoomMax) {
        return getTilesCoverage(pGeoPoints, pZoomMin, pZoomMax).size();
    }

    public CacheManagerTask execute(final CacheManagerTask pTask) {
        pTask.execute();
        mPendingTasks.add(pTask);
        return pTask;
    }

    /**
     * Download in background all tiles of the specified area in osmdroid cache.
     *
     * @param ctx
     * @param bb
     * @param zoomMin
     * @param zoomMax
     */
    public CacheManagerTask downloadAreaAsync(Context ctx, BoundingBox bb, final int zoomMin, final int zoomMax) {
        final CacheManagerTask task = new CacheManagerTask(this, getDownloadingAction(), bb, zoomMin, zoomMax);
        task.addCallback(getDownloadingDialog(ctx, task));
        return execute(task);
    }

    /**
     * Download in background all tiles of the specified area in osmdroid cache.
     *
     * @param ctx
     * @param geoPoints
     * @param zoomMin
     * @param zoomMax
     */
    public CacheManagerTask downloadAreaAsync(Context ctx, ArrayList<GeoPoint> geoPoints, final int zoomMin, final int zoomMax) {
        final CacheManagerTask task = new CacheManagerTask(this, getDownloadingAction(), geoPoints, zoomMin, zoomMax);
        task.addCallback(getDownloadingDialog(ctx, task));
        return execute(task);
    }
    /**
     * Download in background all tiles of the specified area in osmdroid cache.
     *
     * @param ctx
     * @param bb
     * @param zoomMin
     * @param zoomMax
     */
    public CacheManagerTask downloadAreaAsync(Context ctx, BoundingBox bb, final int zoomMin, final int zoomMax, final CacheManagerCallback callback) {
        final CacheManagerTask task = new CacheManagerTask(this, getDownloadingAction(), bb, zoomMin, zoomMax);
        task.addCallback(callback);
        task.addCallback(getDownloadingDialog(ctx, task));
        return execute(task);
    }

    /**
     * Download in background all tiles covered by the GePoints list in osmdroid cache.
     *
     * @param ctx
     * @param geoPoints
     * @param zoomMin
     * @param zoomMax
     */
    public CacheManagerTask downloadAreaAsync(Context ctx, ArrayList<GeoPoint> geoPoints, final int zoomMin, final int zoomMax, final CacheManagerCallback callback) {
        final CacheManagerTask task = new CacheManagerTask(this, getDownloadingAction(), geoPoints, zoomMin, zoomMax);
        task.addCallback(callback);
        task.addCallback(getDownloadingDialog(ctx, task));
        return execute(task);
    }

    /**
     * Download in background all tiles covered by the GeoPoints list in osmdroid cache without a user interface.
     *
     * @param ctx
     * @param geoPoints
     * @param zoomMin
     * @param zoomMax
     * @since
     */
    public CacheManagerTask downloadAreaAsyncNoUI(Context ctx, ArrayList<GeoPoint> geoPoints, final int zoomMin, final int zoomMax, final CacheManagerCallback callback) {
        final CacheManagerTask task = new CacheManagerTask(this, getDownloadingAction(), geoPoints, zoomMin, zoomMax);
        task.addCallback(callback);
        return execute(task);
    }

    /**
     * Download in background all tiles of the specified area in osmdroid cache without a user interface.
     *
     * @param ctx
     * @param bb
     * @param zoomMin
     * @param zoomMax
     * @since 5.3
     */
    public CacheManagerTask downloadAreaAsyncNoUI(Context ctx, BoundingBox bb, final int zoomMin, final int zoomMax, final CacheManagerCallback callback) {
        final CacheManagerTask task = new CacheManagerTask(this, getDownloadingAction(), bb, zoomMin, zoomMax);
        task.addCallback(callback);
        execute(task);
        return task;
    }

    /**
     * cancels all tasks
     * @since 5.6.3
     */
    public void cancelAllJobs(){
        Iterator<CacheManagerTask> iterator = mPendingTasks.iterator();
        while (iterator.hasNext()) {
            CacheManagerTask next = iterator.next();
            next.cancel(true);
        }
        mPendingTasks.clear();
    }

    /**
     * Download in background all tiles of the specified area in osmdroid cache.
     *
     * @param ctx
     * @param pTiles
     * @param zoomMin
     * @param zoomMax
     */
    public CacheManagerTask downloadAreaAsync(Context ctx, List<MapTile> pTiles, final int zoomMin, final int zoomMax) {
        final CacheManagerTask task = new CacheManagerTask(this, getDownloadingAction(), pTiles, zoomMin, zoomMax);
        task.addCallback(getDownloadingDialog(ctx, task));
        return execute(task);
    }

    /**
     *
     */
    public interface CacheManagerCallback {

        /**
         * fired when the download job is done.
         */
        public void onTaskComplete();

        /**
         * this is fired periodically, useful for updating dialogs, progress bars, etc
         *
         * @param progress
         * @param currentZoomLevel
         * @param zoomMin
         * @param zoomMax
         */
        public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax);

        /**
         * as soon as the download is started, this is fired
         */
        public void downloadStarted();

        /**
         * this is fired right before the download starts
         *
         * @param total
         */
        public void setPossibleTilesInArea(int total);

        /**
         * this is fired when the task has been completed but had at least one download error.
         * @param errors
         */
        public void onTaskFailed(int errors);
    }

    public static abstract class CacheManagerDialog implements CacheManagerCallback {

        private final CacheManagerTask mTask;
        private final ProgressDialog mProgressDialog;

        public CacheManagerDialog(final Context pCtx, final CacheManagerTask pTask) {
            mTask = pTask;
            mProgressDialog = new ProgressDialog(pCtx);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mTask.cancel(true);
                }
            });
        }

        protected String zoomMessage(int zoomLevel, int zoomMin, int zoomMax) {
            return "Handling zoom level: " + zoomLevel + " (from " + zoomMin + " to " + zoomMax + ")";
        }

        abstract protected String getUITitle();

        @Override
        public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
            mProgressDialog.setProgress(progress);
            mProgressDialog.setMessage(zoomMessage(currentZoomLevel, zoomMin, zoomMax));
        }

        @Override
        public void downloadStarted() {
            mProgressDialog.setTitle(getUITitle());
            mProgressDialog.show();
        }

        @Override
        public void setPossibleTilesInArea(int total) {
            mProgressDialog.setMax(total);
        }

        @Override
        public void onTaskComplete() {
            dismiss();
        }

        @Override
        public void onTaskFailed(int errors) {
            dismiss();
        }

        private void dismiss() {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    /**
     * generic class for common code related to AsyncTask management
     * - performing an action
     * - within a manager
     * - on a list of tiles (potentially sorted by ascending zoom level)
     * - and with callbacks for task progression
     */
    public static class CacheManagerTask extends AsyncTask<Object, Integer, Integer> {
        private final CacheManager mManager;
        private final CacheManagerAction mAction;
        private final List<MapTile> mTiles;
        private final int mZoomMin;
        private final int mZoomMax;
        private final ArrayList<CacheManagerCallback> mCallbacks = new ArrayList<>();

        public CacheManagerTask(final CacheManager pManager, final CacheManagerAction pAction,
                                final List<MapTile> pTiles,
                                final int pZoomMin, final int pZoomMax) {
            mManager = pManager;
            mAction = pAction;
            mTiles = pTiles;
            mZoomMin = Math.max(pZoomMin, pManager.mMinZoomLevel);
            mZoomMax = Math.min(pZoomMax, pManager.mMaxZoomLevel);
        }

        public CacheManagerTask(final CacheManager pManager,  final CacheManagerAction pAction,
                                final ArrayList<GeoPoint> pGeoPoints,
                                final int pZoomMin, final int pZoomMax) {
            this(pManager, pAction, getTilesCoverage(pGeoPoints, pZoomMin, pZoomMax), pZoomMin, pZoomMax);
        }

        public CacheManagerTask(final CacheManager pManager,  final CacheManagerAction pAction,
                                final BoundingBox pBB,
                                final int pZoomMin, final int pZoomMax) {
            this(pManager, pAction, getTilesCoverage(pBB, pZoomMin, pZoomMax), pZoomMin, pZoomMax);
        }

        public void addCallback(final CacheManagerCallback pCallback) {
            if (pCallback != null) {
                mCallbacks.add(pCallback);
            }
        }

        @Override
        protected void onPreExecute(){
            final int total = mTiles.size();
            for (final CacheManagerCallback callback : mCallbacks) {
                try {
                    callback.setPossibleTilesInArea(total);
                    callback.downloadStarted();
                    callback.updateProgress(0, mZoomMin, mZoomMin, mZoomMax);
                } catch (Throwable t) {
                    logFaultyCallback(t);
                }
            }
        }

        private void logFaultyCallback(Throwable pThrowable) {
            Log.w(IMapView.LOGTAG, "Error caught processing cachemanager callback, your implementation is faulty", pThrowable);
        }

        @Override
        protected void onProgressUpdate(final Integer... count) {
            //count[0] = tile counter, count[1] = current zoom level
            for (final CacheManagerCallback callback : mCallbacks) {
                try {
                    callback.updateProgress(count[0], count[1], mZoomMin, mZoomMax);
                } catch (Throwable t) {
                    logFaultyCallback(t);
                }
            }
        }

        @Override
        protected void onCancelled(){
            mManager.mPendingTasks.remove(this);
        }

        @Override
        protected void onPostExecute(final Integer specialCount) {
            mManager.mPendingTasks.remove(this);
            for (final CacheManagerCallback callback : mCallbacks) {
                try {
                    if (specialCount == 0) {
                        callback.onTaskComplete();
                    } else {
                        callback.onTaskFailed(specialCount);
                    }
                } catch (Throwable t) {
                    logFaultyCallback(t);
                }
            }
        }

        @Override
        protected Integer doInBackground(Object... params) {
            if (!mAction.preCheck()) {
                return 0;
            }

            int tileCounter = 0;
            int errors = 0;

            for (final MapTile tile : mTiles) {
                final int zoom = tile.getZoomLevel();
                if (zoom >= mZoomMin && zoom <= mZoomMax) {
                    if (mAction.tileAction(tile)) {
                        errors++;
                    }
                }
                tileCounter++;
                if (tileCounter % mAction.getProgressModulo() == 0) {
                    if (isCancelled()) {
                        return errors;
                    }
                    publishProgress(tileCounter, tile.getZoomLevel());
                }

            }
            return errors;
        }
    }

    public CacheManagerDialog getDownloadingDialog(final Context pCtx, final CacheManagerTask pTask) {
        return new CacheManagerDialog(pCtx, pTask) {
            @Override
            protected String getUITitle() {
                return "Downloading tiles";
            }

            @Override
            public void onTaskFailed(int errors) {
                super.onTaskFailed(errors);
                Toast.makeText(pCtx, "Loading completed with " + errors + " errors.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public CacheManagerDialog getCleaningDialog(final Context pCtx, final CacheManagerTask pTask) {
        return new CacheManagerDialog(pCtx, pTask) {
            @Override
            protected String getUITitle() {
                return "Cleaning tiles";
            }

            @Override
            public void onTaskFailed(int deleted) {
                super.onTaskFailed(deleted);
                Toast.makeText(pCtx, "Cleaning completed, " + deleted + " tiles deleted.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    /**
     * Action to perform on a tile within a CacheManagerTask
     * @author F.Fontaine
     */
    public interface CacheManagerAction {
        /**
         * Preconditions to check before bulk action
         * @return true if we pass the check
         */
        boolean preCheck();

        /**
         * We will update the callbacks not for every tile, but at this rate
         */
        int getProgressModulo();

        /**
         * The action to perform on a single tile
         * @return true if you want to increment the action counter
         */
        boolean tileAction(final MapTile pTile);
    }

    public CacheManagerAction getDownloadingAction() {
        return new CacheManagerAction() {
            @Override
            public boolean preCheck() {
                if (mTileProvider.getTileSource() instanceof OnlineTileSourceBase) {
                    return true;
                } else {
                    Log.e(IMapView.LOGTAG, "TileSource is not an online tile source");
                    return false;
                }
            }

            @Override
            public int getProgressModulo() {
                return 10;
            }

            @Override
            public boolean tileAction(MapTile pTile) {
                return !loadTile((OnlineTileSourceBase) mTileProvider.getTileSource(), pTile);
            }
        };
    }

    public CacheManagerAction getCleaningAction() {
        return new CacheManagerAction() {
            @Override
            public boolean preCheck() {
                return true;
            }

            @Override
            public int getProgressModulo() {
                return 1000;
            }

            @Override
            public boolean tileAction(MapTile pTile) {
                return deleteTile(pTile);
            }
        };
    }

    /**
     * Remove all cached tiles in the specified area.
     *
     * @param ctx
     * @param bb
     * @param zoomMin
     * @param zoomMax
     */
    public CacheManagerTask cleanAreaAsync(Context ctx, BoundingBox bb, int zoomMin, int zoomMax) {
        final CacheManagerTask task = new CacheManagerTask(this, getCleaningAction(), bb, zoomMin, zoomMax);
        task.addCallback(getCleaningDialog(ctx, task));
        return execute(task);
    }

    /**
     * Remove all cached tiles covered by the GeoPoints list.
     *
     * @param ctx
     * @param geoPoints
     * @param zoomMin
     * @param zoomMax
     */
    public CacheManagerTask cleanAreaAsync(final Context ctx, ArrayList<GeoPoint> geoPoints, int zoomMin, int zoomMax) {
        BoundingBox extendedBounds = extendedBoundsFromGeoPoints(geoPoints,zoomMin);
        return cleanAreaAsync(ctx, extendedBounds, zoomMin, zoomMax);
    }
    /**
     * Remove all cached tiles in the specified area.
     */
    public CacheManagerTask cleanAreaAsync(Context ctx, List<MapTile> tiles, int zoomMin, int zoomMax) {
        final CacheManagerTask task = new CacheManagerTask(this, getCleaningAction(), tiles, zoomMin, zoomMax);
        task.addCallback(getCleaningDialog(ctx, task));
        return execute(task);
    }

    /**
     *
     */

    public BoundingBox extendedBoundsFromGeoPoints(ArrayList<GeoPoint> geoPoints, int minZoomLevel) {
        BoundingBox bb = BoundingBox.fromGeoPoints(geoPoints);

        Point mLowerRight = getMapTileFromCoordinates(bb.getLatSouth() , bb.getLonEast() , minZoomLevel);
        GeoPoint lowerRightPoint = getCoordinatesFromMapTile(mLowerRight.x+1, mLowerRight.y+1, minZoomLevel);
        Point mUpperLeft = getMapTileFromCoordinates(bb.getLatNorth() , bb.getLonWest(), minZoomLevel);
        GeoPoint upperLeftPoint = getCoordinatesFromMapTile(mUpperLeft.x-1, mUpperLeft.y-1, minZoomLevel);

        BoundingBox extendedBounds = new BoundingBox(upperLeftPoint.getLatitude(), upperLeftPoint.getLongitude(), lowerRightPoint.getLatitude(), lowerRightPoint.getLongitude());

        return extendedBounds;
    }

    /**
     * @return volume currently use in the osmdroid local filesystem cache, in bytes.
     * Note that this method currently takes a while.
     */
    public long currentCacheUsage() {
        //return TileWriter.getUsedCacheSpace(); //returned value is not stable! Increase and decrease, for unknown reasons.
        return directorySize(Configuration.getInstance().getOsmdroidTileCache());
    }

    /**
     * @return the capacity of the osmdroid local filesystem cache, in bytes.
     * This capacity is currently a hard-coded constant inside osmdroid.
     */
    public long cacheCapacity() {
        return Configuration.getInstance().getTileFileSystemCacheMaxBytes();
    }

    /**
     * @return the total size of a directory and of its whole content, recursively
     */
    public long directorySize(final File pDirectory) {
        long usedCacheSpace = 0;
        final File[] z = pDirectory.listFiles();
        if (z != null) {
            for (final File file : z) {
                if (file.isFile()) {
                    usedCacheSpace += file.length();
                } else {
                    if (file.isDirectory()) {
                        usedCacheSpace += directorySize(file);
                    }
                }
            }
        }
        return usedCacheSpace;
    }

}
