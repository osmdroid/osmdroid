package org.osmdroid.tileprovider.cachemanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MyMath;
import org.osmdroid.util.TileSystem;
import org.osmdroid.util.constants.GeoConstants;
import org.osmdroid.views.MapView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
 */
public class CacheManager {

    protected final MapTileProviderBase mTileProvider;
    protected final IFilesystemCache mTileWriter;
    protected final MapView mMapView;

    public CacheManager(final MapView mapView) {
        mTileProvider = mapView.getTileProvider();
        mTileWriter = mapView.getTileProvider().getTileWriter();
        mMapView = mapView;
    }

    public CacheManager(final MapView mapView, IFilesystemCache writer) {
        mTileProvider = mapView.getTileProvider();
        mTileWriter = writer;
        mMapView = mapView;
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
        final File file = new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE,
                tileSource.getTileRelativeFilenameString(tile) + OpenStreetMapTileProviderConstants.TILE_PATH_EXTENSION);
        return file;
    }

    /**
     * @return true if success, false if error
     */
    public boolean loadTile(OnlineTileSourceBase tileSource, MapTile tile) {
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
        OutputStream out = null;
        HttpURLConnection urlConnection = null;
        try {
            final String tileURLString = tileSource.getTileURLString(tile);

            urlConnection = (HttpURLConnection) new URL(tileURLString).openConnection();

            // Check to see if we got success
            if (urlConnection.getResponseCode() != 200) {
                Log.w(IMapView.LOGTAG, "Problem downloading MapTile: " + tile + " HTTP response: " + urlConnection.getResponseMessage());
                return false;
            }

            in = urlConnection.getInputStream();

            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
            StreamUtils.copy(in, out);
            out.flush();
            final byte[] data = dataStream.toByteArray();
            final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);

            // Save the data to the filesystem cache
            mTileWriter.saveFile(tileSource, tile, byteStream);
            byteStream.reset();

            //final Drawable result = tileSource.getDrawable(byteStream);
            return true;
        } catch (final UnknownHostException e) {
            // no network connection
            Log.w(IMapView.LOGTAG, "UnknownHostException downloading MapTile: " + tile + " : " + e);
        } catch (final FileNotFoundException e) {
            Log.w(IMapView.LOGTAG, "Tile not found: " + tile + " : " + e);
        } catch (final IOException e) {
            Log.w(IMapView.LOGTAG, "IOException downloading MapTile: " + tile + " : " + e);
        } catch (final Throwable e) {
            Log.e(IMapView.LOGTAG, "Error downloading MapTile: " + tile, e);
        } finally {
            StreamUtils.closeStream(in);
            StreamUtils.closeStream(out);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            urlConnection = null;
        }

        return false;
    }

    /**
     * @return the theoretical number of tiles in the specified area
     */
    public int possibleTilesInArea(BoundingBox bb, final int zoomMin, final int zoomMax) {
        int total = 0;
        for (int zoomLevel = zoomMin; zoomLevel <= zoomMax; zoomLevel++) {
            Point mLowerRight = getMapTileFromCoordinates(bb.getLatSouth(), bb.getLonEast() * 1E-6, zoomLevel);
            Point mUpperLeft = getMapTileFromCoordinates(bb.getLatNorth() , bb.getLonWest() * 1E-6, zoomLevel);
            int y = mLowerRight.y - mUpperLeft.y + 1;
            int x = mLowerRight.x - mUpperLeft.x + 1;
            int nbTilesForZoomLevel = x * y;
            total += nbTilesForZoomLevel;
        }
        return total;
    }
    /**
     * @return the theoretical number of tiles covered by the list of points
     * Calculation done based on http://www.movable-type.co.uk/scripts/latlong.html
     */
    public int possibleTilesCovered(ArrayList<GeoPoint> geoPoints, final int zoomMin, final int zoomMax) {
        int total = 0;
        ArrayList<Point> tilePoints = new ArrayList<>();
        boolean foundTilePoint;
        GeoPoint prevPoint = null, wayPoint;
        double d, leadCoef, brng, latRad, lonRad, prevLatRad, prevLonRad;
        Point tile, prevTile = null, lastPoint;

        for (int zoomLevel = zoomMin; zoomLevel <= zoomMax; zoomLevel++) {

            for (GeoPoint geoPoint : geoPoints) {

                d = TileSystem.GroundResolution(geoPoint.getLatitude(), zoomLevel);

                if (tilePoints.size() != 0) {

                    if (prevPoint != null) {

                        leadCoef = (geoPoint.getLatitude() - prevPoint.getLatitude()) / (geoPoint.getLongitude() - prevPoint.getLongitude());

                        if (geoPoint.getLongitude() > prevPoint.getLongitude()) {
                            brng = Math.PI / 2 - Math.atan(leadCoef);
                        } else {
                            brng = 3 * Math.PI / 2 - Math.atan(leadCoef);
                        }

                        wayPoint = new GeoPoint(prevPoint.getLatitude(), prevPoint.getLongitude());

                        while ((((geoPoint.getLatitude() > prevPoint.getLatitude()) && (wayPoint.getLatitude() < geoPoint.getLatitude())) ||
                                (geoPoint.getLatitude() < prevPoint.getLatitude()) && (wayPoint.getLatitude() > geoPoint.getLatitude())) &&
                                (((geoPoint.getLongitude() > prevPoint.getLongitude()) && (wayPoint.getLongitude() < geoPoint.getLongitude())) ||
                                        ((geoPoint.getLongitude() < prevPoint.getLongitude()) && (wayPoint.getLongitude() > geoPoint.getLongitude())))) {

                            lastPoint = new Point();
                            TileSystem.LatLongToPixelXY(geoPoint.getLatitude(), geoPoint.getLongitude(), zoomLevel, lastPoint);

                            prevLatRad = wayPoint.getLatitude() * Math.PI / 180.0;
                            prevLonRad = wayPoint.getLongitude() * Math.PI / 180.0;

                            latRad = Math.asin(Math.sin(prevLatRad) * Math.cos(d / GeoConstants.RADIUS_EARTH_METERS) + Math.cos(prevLatRad) * Math.sin(d / GeoConstants.RADIUS_EARTH_METERS) * Math.cos(brng));
                            lonRad = prevLonRad + Math.atan2(Math.sin(brng) * Math.sin(d / GeoConstants.RADIUS_EARTH_METERS) * Math.cos(prevLatRad), Math.cos(d / GeoConstants.RADIUS_EARTH_METERS) - Math.sin(prevLatRad) * Math.sin(latRad));

                            wayPoint.setLatitude(((latRad * 180.0 / Math.PI)));
                            wayPoint.setLongitude(((lonRad * 180.0 / Math.PI)));

                            tile = getMapTileFromCoordinates(wayPoint.getLatitude(), wayPoint.getLongitude(), zoomLevel);

                            if (!tile.equals(prevTile)) {
                                for (int xAround = tile.x - 1; xAround <= tile.x + 1; xAround++) {
                                    for (int yAround = tile.y - 1; yAround <= tile.y + 1; yAround++) {

                                        Point tileAround = new Point(xAround, yAround);
                                        foundTilePoint = false;
                                        int iterate = 0;
                                        for (Point inList : tilePoints) {
                                            iterate++;
                                            if (tileAround.equals(inList.x, inList.y)) {
                                                foundTilePoint = true;
                                                break;
                                            }
                                        }

                                        if (!foundTilePoint) {
                                            tilePoints.add(0, tileAround);
                                        }
                                    }
                                }

                                prevTile = tile;
                            }
                        }
                    }

                } else {
                    tile = getMapTileFromCoordinates(geoPoint.getLatitude(), geoPoint.getLongitude(), zoomLevel);
                    prevTile = tile;

                    for (int xAround = tile.x - 1; xAround <= tile.x + 1; xAround ++) {
                        for (int yAround = tile.y - 1; yAround <= tile.y + 1; yAround ++) {
                            Point tileAround = new Point(xAround, yAround);
                            tilePoints.add(0, tileAround);
                        }
                    }
                }

                prevPoint = geoPoint;
            }

        }
        return tilePoints.size();
    }

    protected String zoomMessage(int zoomLevel, int zoomMin, int zoomMax) {
        return "Handling zoom level: " + zoomLevel + " (from " + zoomMin + " to " + zoomMax + ")";
    }

    /**
     * Download in background all tiles of the specified area in osmdroid cache.
     *
     * @param ctx
     * @param bb
     * @param zoomMin
     * @param zoomMax
     */
    public void downloadAreaAsync(Context ctx, BoundingBox bb, final int zoomMin, final int zoomMax) {
        new DownloadingTask(ctx, bb, zoomMin, zoomMax, null, true).execute();
    }

    /**
     * Download in background all tiles of the specified area in osmdroid cache.
     *
     * @param ctx
     * @param geoPoints
     * @param zoomMin
     * @param zoomMax
     */
    public void downloadAreaAsync(Context ctx, ArrayList<GeoPoint> geoPoints, final int zoomMin, final int zoomMax) {
        new DownloadingTask(ctx, geoPoints, zoomMin, zoomMax, null, true).execute();
    }
    /**
     * Download in background all tiles of the specified area in osmdroid cache.
     *
     * @param ctx
     * @param bb
     * @param zoomMin
     * @param zoomMax
     */
    public void downloadAreaAsync(Context ctx, BoundingBox bb, final int zoomMin, final int zoomMax, final CacheManagerCallback callback) {
        new DownloadingTask(ctx, bb, zoomMin, zoomMax, callback, true).execute();
    }

    /**
     * Download in background all tiles covered by the GePoints list in osmdroid cache.
     *
     * @param ctx
     * @param geoPoints
     * @param zoomMin
     * @param zoomMax
     */
    public void downloadAreaAsync(Context ctx, ArrayList<GeoPoint> geoPoints, final int zoomMin, final int zoomMax, final CacheManagerCallback callback) {
        new DownloadingTask(ctx, geoPoints, zoomMin, zoomMax, callback, true).execute();
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
    public void downloadAreaAsyncNoUI(Context ctx, ArrayList<GeoPoint> geoPoints, final int zoomMin, final int zoomMax, final CacheManagerCallback callback) {
        new DownloadingTask(ctx, geoPoints, zoomMin, zoomMax, callback, false).execute();
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
    public void downloadAreaAsyncNoUI(Context ctx, BoundingBox bb, final int zoomMin, final int zoomMax, final CacheManagerCallback callback) {
        new DownloadingTask(ctx, bb, zoomMin, zoomMax, callback, false).execute();
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

    /**
     * generic class for common code related to AsyncTask management
     */
    protected abstract class CacheManagerTask extends AsyncTask<Object, Integer, Integer> {
        ProgressDialog mProgressDialog=null;
        boolean showUI = true;
        int mZoomMin, mZoomMax;
        BoundingBox mBB;
        ArrayList<GeoPoint> mGeoPoints;
        Context mCtx;
        CacheManagerCallback callback = null;

        public CacheManagerTask(Context pCtx, BoundingBox pBB, final int pZoomMin, final int pZoomMax, final CacheManagerCallback callback, final boolean showUI) {
            this(pCtx, pBB, pZoomMin, pZoomMax);
            this.callback = callback;
            this.showUI = showUI;
        }
        public CacheManagerTask(Context pCtx, ArrayList<GeoPoint> geoPoints, final int pZoomMin, final int pZoomMax, final CacheManagerCallback callback, final boolean showUI) {
            this(pCtx, geoPoints, pZoomMin, pZoomMax);
            this.callback = callback;
            this.showUI = showUI;
            this.mGeoPoints = geoPoints;
        }
        public CacheManagerTask(Context pCtx, ArrayList<GeoPoint> pGeoPoints, final int pZoomMin, final int pZoomMax) {
            mCtx = pCtx;
            mGeoPoints = pGeoPoints;
            mZoomMin = Math.max(pZoomMin, mMapView.getMinZoomLevel());
            mZoomMax = Math.min(pZoomMax, mMapView.getMaxZoomLevel());
        }

        public CacheManagerTask(Context pCtx, BoundingBox pBB, final int pZoomMin, final int pZoomMax) {
            mCtx = pCtx;
            mBB = pBB;
            mZoomMin = Math.max(pZoomMin, mMapView.getMinZoomLevel());
            mZoomMax = Math.min(pZoomMax, mMapView.getMaxZoomLevel());
        }

        @Override
        protected void onPreExecute(){
            if (showUI) {
                mProgressDialog = createProgressDialog(mCtx);
            }
        }

        protected ProgressDialog createProgressDialog(Context pCtx) {
            ProgressDialog pd = new ProgressDialog(pCtx);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setCancelable(true);
            pd.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
            return pd;
        }

        @Override
        protected void onProgressUpdate(final Integer... count) {
            //count[0] = tile counter, count[1] = current zoom level
            if (showUI) {
                mProgressDialog.setProgress(count[0]);
                mProgressDialog.setMessage(zoomMessage(count[1], mZoomMin, mZoomMax));
            }
            if (callback != null) {
                try {
                    callback.updateProgress(count[0], count[1], mZoomMin, mZoomMax);
                } catch (Throwable t) {
                    Log.w(IMapView.LOGTAG, "Error caught processing cachemanager callback, your implementation is faulty", t);
                }
            }
        }

    }

    protected class DownloadingTask extends CacheManagerTask {

        public DownloadingTask(Context pCtx, BoundingBox pBB, final int pZoomMin, final int pZoomMax, final CacheManagerCallback callback, final boolean showUI) {
            super(pCtx, pBB, pZoomMin, pZoomMax, callback, showUI);
        }
        public DownloadingTask(Context pCtx, ArrayList<GeoPoint> pPoints, final int pZoomMin, final int pZoomMax, final CacheManagerCallback callback, final boolean showUI) {
            super(pCtx, pPoints, pZoomMin, pZoomMax, callback, showUI);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            int total = 0;
            if (mBB != null) {
                total = possibleTilesInArea(mBB, mZoomMin, mZoomMax);
            } else if (mGeoPoints != null) {
                total = possibleTilesCovered(mGeoPoints, mZoomMin, mZoomMax);
            }
            if (showUI) {
                mProgressDialog.setTitle("Downloading tiles");
                mProgressDialog.setMessage(zoomMessage(mZoomMin, mZoomMin, mZoomMax));

                mProgressDialog.setMax(total);
                mProgressDialog.show();
            }
            if (callback != null) {
                try {
                    callback.setPossibleTilesInArea(total);
                    callback.downloadStarted();
                } catch (Throwable t) {
                    Log.w(IMapView.LOGTAG, "Error caught processing cachemanager callback, your implementation is faulty", t);
                }
            }
        }

        @Override
        protected void onPostExecute(final Integer errors) {
            if (showUI) {
                if (errors != 0) {
                    Toast.makeText(mCtx, "Loading completed with " + errors + " errors.", Toast.LENGTH_SHORT).show();
                }
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
            if (callback != null) {
                try {
                    if (errors == 0) {
                        callback.onTaskComplete();
                    } else {
                        callback.onTaskFailed(errors);
                    }
                } catch (Throwable t) {
                    Log.w(IMapView.LOGTAG, "Error caught processing cachemanager callback, your implementation is faulty", t);
                }
            }
        }

        @Override
        protected Integer doInBackground(Object... params) {
            int errors = downloadArea();
            return errors;
        }

        /**
         * Do the job. No attempt to
         *
         * @return the number of loading errors.
         */
        protected int downloadArea() {
            OnlineTileSourceBase tileSource;
            if (mTileProvider.getTileSource() instanceof OnlineTileSourceBase) {
                tileSource = (OnlineTileSourceBase) mTileProvider.getTileSource();
            } else {
                Log.e(IMapView.LOGTAG, "TileSource is not an online tile source");
                return 0;
            }

            int tileCounter = 0;
            int errors = 0;

            if (mBB != null) {
                for (int zoomLevel = mZoomMin; zoomLevel <= mZoomMax; zoomLevel++) {
                    Point mLowerRight = getMapTileFromCoordinates(mBB.getLatSouth() , mBB.getLonEast() , zoomLevel);
                    Point mUpperLeft = getMapTileFromCoordinates(mBB.getLatNorth(), mBB.getLonWest() , zoomLevel);
                    final int mapTileUpperBound = 1 << zoomLevel;
                    //Get all the MapTiles from the upper left to the lower right:
                    for (int y = mUpperLeft.y; y <= mLowerRight.y; y++) {
                        for (int x = mUpperLeft.x; x <= mLowerRight.x; x++) {
                            final int tileY = MyMath.mod(y, mapTileUpperBound);
                            final int tileX = MyMath.mod(x, mapTileUpperBound);
                            final MapTile tile = new MapTile(zoomLevel, tileX, tileY);
                            //Drawable currentMapTile = mTileProvider.getMapTile(tile);
                            boolean ok = loadTile(tileSource, tile);
                            if (!ok) {
                                errors++;
                            }
                            tileCounter++;
                            if (tileCounter % 20 == 0) {
                                if (isCancelled()) {
                                    return errors;
                                }
                                publishProgress(tileCounter, zoomLevel);
                            }
                        }
                    }
                }
            } else if (mGeoPoints != null) {
                GeoPoint prevPoint = null, wayPoint;
                double d, leadCoef, brng, latRad, lonRad, prevLatRad, prevLonRad;
                Point tile, prevTile = null, lastPoint;
                ArrayList<Point>  tilePoints = new ArrayList<>();
                boolean foundTilePoint;

                for (int zoomLevel = mZoomMin; zoomLevel <= mZoomMax; zoomLevel++) {

                    for (GeoPoint geoPoint : mGeoPoints) {

                        d = TileSystem.GroundResolution(geoPoint.getLatitude(), zoomLevel);

                        if (tileCounter != 0) {

                            if (prevPoint != null) {

                                leadCoef = (geoPoint.getLatitude() - prevPoint.getLatitude()) / (geoPoint.getLongitude() - prevPoint.getLongitude());

                                if (geoPoint.getLongitude() > prevPoint.getLongitude()) {
                                    brng = Math.PI / 2 - Math.atan(leadCoef);
                                } else {
                                    brng = 3 * Math.PI / 2 - Math.atan(leadCoef);
                                }

                                wayPoint = new GeoPoint(prevPoint.getLatitude(), prevPoint.getLongitude());

                                while ((((geoPoint.getLatitude() > prevPoint.getLatitude()) && (wayPoint.getLatitude() < geoPoint.getLatitude())) ||
                                        (geoPoint.getLatitude() < prevPoint.getLatitude()) && (wayPoint.getLatitude() > geoPoint.getLatitude())) &&
                                        (((geoPoint.getLongitude() > prevPoint.getLongitude()) && (wayPoint.getLongitude() < geoPoint.getLongitude())) ||
                                                ((geoPoint.getLongitude() < prevPoint.getLongitude()) && (wayPoint.getLongitude() > geoPoint.getLongitude())))) {

                                    lastPoint = new Point();
                                    TileSystem.LatLongToPixelXY(geoPoint.getLatitude(), geoPoint.getLongitude(), zoomLevel, lastPoint);

                                    prevLatRad = wayPoint.getLatitude() * Math.PI / 180.0;
                                    prevLonRad = wayPoint.getLongitude() * Math.PI / 180.0;

                                    latRad = Math.asin(Math.sin(prevLatRad) * Math.cos(d / GeoConstants.RADIUS_EARTH_METERS) + Math.cos(prevLatRad) * Math.sin(d / GeoConstants.RADIUS_EARTH_METERS) * Math.cos(brng));
                                    lonRad = prevLonRad + Math.atan2(Math.sin(brng) * Math.sin(d / GeoConstants.RADIUS_EARTH_METERS) * Math.cos(prevLatRad), Math.cos(d / GeoConstants.RADIUS_EARTH_METERS) - Math.sin(prevLatRad) * Math.sin(latRad));

                                    wayPoint.setLatitude(((latRad * 180.0 / Math.PI)));
                                    wayPoint.setLongitude(((lonRad * 180.0 / Math.PI)));

                                    tile = getMapTileFromCoordinates(wayPoint.getLatitude(), wayPoint.getLongitude(), zoomLevel);

                                    if (!tile.equals(prevTile)) {
                                        //Log.d(Constants.APP_TAG, "New Tile lat " + tile.x + " lon " + tile.y);
                                        for (int xAround = tile.x - 1; xAround <= tile.x + 1; xAround++) {
                                            for (int yAround = tile.y - 1; yAround <= tile.y + 1; yAround++) {
                                                Point tileAround = new Point(xAround, yAround);
                                                foundTilePoint = false;

                                                /**
                                                 * The following is only necessary to correctly update progress
                                                 * as we cannot know if a tile is already downloaded or not.
                                                 * Normally loadTile() will only download a tile if necessary
                                                 */
                                                for (Point inList : tilePoints) {

                                                    if (tileAround.equals(inList.x, inList.y)) {
                                                        foundTilePoint = true;
                                                        break;
                                                    }
                                                }

                                                if (!foundTilePoint) {
                                                    final MapTile tileToDownload = new MapTile(zoomLevel, tileAround.x, tileAround.y);
                                                    //Drawable currentMapTile = mTileProvider.getMapTile(tile);
                                                    boolean ok = loadTile(tileSource, tileToDownload);
                                                    if (!ok) {
                                                        errors++;
                                                    }
                                                    tileCounter++;
                                                    if (tileCounter % 20 == 0) {
                                                        if (isCancelled()) {
                                                            return errors;
                                                        }
                                                        publishProgress(tileCounter, zoomLevel);
                                                    }
                                                    tilePoints.add(0, tileAround);
                                                }
                                            }
                                        }

                                        prevTile = tile;
                                    }
                                }
                            }

                        } else {
                            tile = getMapTileFromCoordinates(geoPoint.getLatitude(), geoPoint.getLongitude(), zoomLevel);
                            prevTile = tile;
                            //Log.d(Constants.APP_TAG, "New Tile lat " + tile.x + " lon " + tile.y);

                            for (int xAround = tile.x - 1; xAround <= tile.x + 1; xAround ++) {
                                for (int yAround = tile.y - 1; yAround <= tile.y + 1; yAround ++) {
                                    Point tileAround = new Point(xAround, yAround);
                                    final MapTile tileToDownload = new MapTile(zoomLevel, tileAround.x, tileAround.y);

                                    //Drawable currentMapTile = mTileProvider.getMapTile(tile);
                                    boolean ok = loadTile(tileSource, tileToDownload);
                                    if (!ok) {
                                        errors++;
                                    }
                                    tileCounter++;
                                    if (tileCounter % 20 == 0) {
                                        if (isCancelled()) {
                                            return errors;
                                        }
                                        publishProgress(tileCounter, zoomLevel);
                                    }

                                    tilePoints.add(0, tileAround);
                                }
                            }
                        }

                        prevPoint = geoPoint;
                    }
                }
            }
            return errors;
        }

    } //DownloadingTask

    /**
     * Remove all cached tiles in the specified area.
     *
     * @param ctx
     * @param bb
     * @param zoomMin
     * @param zoomMax
     */
    public void cleanAreaAsync(Context ctx, BoundingBox bb, int zoomMin, int zoomMax) {
        new CleaningTask(ctx, bb, zoomMin, zoomMax).execute();
    }

    /**
     * Remove all cached tiles covered by the GeoPoints list.
     *
     * @param ctx
     * @param geoPoints
     * @param zoomMin
     * @param zoomMax
     */
    public void cleanAreaAsync(Context ctx, ArrayList<GeoPoint> geoPoints, int zoomMin, int zoomMax) {

        BoundingBox extendedBounds = extendedBoundsFromGeoPoints(geoPoints,zoomMin);

        new CleaningTask(ctx, extendedBounds, zoomMin, zoomMax).execute();
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

    protected class CleaningTask extends CacheManagerTask {

        public CleaningTask(Context pCtx, BoundingBox pBB, final int pZoomMin, final int pZoomMax) {
            super(pCtx, pBB, pZoomMin, pZoomMax);
            showUI=true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog!=null) {
                mProgressDialog.setTitle("Cleaning tiles");
                mProgressDialog.setMessage(zoomMessage(mZoomMin, mZoomMin, mZoomMax));
                int total = possibleTilesInArea(mBB, mZoomMin, mZoomMax);
                mProgressDialog.setMax(total);
                mProgressDialog.show();
            }
        }

        @Override
        protected void onPostExecute(final Integer deleted) {
            Toast.makeText(mCtx, "Cleaning completed, " + deleted + " tiles deleted.", Toast.LENGTH_SHORT).show();
            if (mProgressDialog!=null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }

        @Override
        protected Integer doInBackground(Object... params) {
            int errors = cleanArea();
            return errors;
        }

        /**
         * Do the job.
         *
         * @return the number of tiles deleted.
         */
        protected int cleanArea() {
            ITileSource tileSource = mTileProvider.getTileSource();
            int deleted = 0;
            int tileCounter = 0;
            for (int zoomLevel = mZoomMin; zoomLevel <= mZoomMax; zoomLevel++) {
                Point mLowerRight = getMapTileFromCoordinates(mBB.getLatSouth(), mBB.getLonEast() , zoomLevel);
                Point mUpperLeft = getMapTileFromCoordinates(mBB.getLatNorth() , mBB.getLonWest() , zoomLevel);

                final int mapTileUpperBound = 1 << zoomLevel;
                //Get all the MapTiles from the upper left to the lower right:
                for (int y = mUpperLeft.y; y <= mLowerRight.y; y++) {
                    for (int x = mUpperLeft.x; x <= mLowerRight.x; x++) {
                        final int tileY = MyMath.mod(y, mapTileUpperBound);
                        final int tileX = MyMath.mod(x, mapTileUpperBound);
                        final MapTile tile = new MapTile(zoomLevel, tileX, tileY);
                        File file = getFileName(tileSource, tile);
                        if (file.exists()) {
                            file.delete();
                            deleted++;
                        }
                        tileCounter++;
                        if (tileCounter % 1000 == 0) {
                            if (isCancelled()) {
                                return deleted;
                            }
                            publishProgress(tileCounter, zoomLevel);
                        }
                    }
                }
            }
            return deleted;
        }
    } //CleaningTask

    /**
     * @return volume currently use in the osmdroid local filesystem cache, in bytes.
     * Note that this method currently takes a while.
     */
    public long currentCacheUsage() {
        //return TileWriter.getUsedCacheSpace(); //returned value is not stable! Increase and decrease, for unknown reasons.
        return directorySize(OpenStreetMapTileProviderConstants.TILE_PATH_BASE);
    }

    /**
     * @return the capacity of the osmdroid local filesystem cache, in bytes.
     * This capacity is currently a hard-coded constant inside osmdroid.
     */
    public long cacheCapacity() {
        return OpenStreetMapTileProviderConstants.TILE_MAX_CACHE_SIZE_BYTES;
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
