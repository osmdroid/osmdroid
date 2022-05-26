package org.osmdroid.tileprovider.cachemanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.library.R;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.CantContinueException;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.TileDownloader;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourcePolicyException;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.IterableWithSize;
import org.osmdroid.util.MapTileArea;
import org.osmdroid.util.MapTileAreaList;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.util.MyMath;
import org.osmdroid.util.TileSystem;
import org.osmdroid.util.constants.GeoConstants;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    private TileDownloader mTileDownloader = new TileDownloader(); // default value
    protected final ITileSource mTileSource;
    protected final IFilesystemCache mTileWriter;
    protected final int mMinZoomLevel;
    protected final int mMaxZoomLevel;
    protected Set<CacheManagerTask> mPendingTasks = new HashSet<>();
    protected boolean verifyCancel = true;

    public CacheManager(final MapView mapView) throws TileSourcePolicyException {
        this(mapView, mapView.getTileProvider().getTileWriter());
    }

    public CacheManager(final MapView mapView, IFilesystemCache writer) throws TileSourcePolicyException {
        this(mapView.getTileProvider(), writer, (int) mapView.getMinZoomLevel(), (int) mapView.getMaxZoomLevel());
    }

    /**
     * See https://github.com/osmdroid/osmdroid/issues/619
     *
     * @since 5.6.5
     */
    public CacheManager(final MapTileProviderBase pTileProvider,
                        final IFilesystemCache pWriter,
                        final int pMinZoomLevel, final int pMaxZoomLevel)
            throws TileSourcePolicyException {
        this(pTileProvider.getTileSource(), pWriter, pMinZoomLevel, pMaxZoomLevel);
    }

    /**
     * @since 6.0
     */
    public CacheManager(final ITileSource pTileSource,
                        final IFilesystemCache pWriter,
                        final int pMinZoomLevel, final int pMaxZoomLevel)
            throws TileSourcePolicyException {
        mTileSource = pTileSource;
        mTileWriter = pWriter;
        mMinZoomLevel = pMinZoomLevel;
        mMaxZoomLevel = pMaxZoomLevel;
    }

    /**
     * @return
     * @since 5.6.3
     */
    public int getPendingJobs() {
        return mPendingTasks.size();
    }

    /**
     * @deprecated Use {@link TileSystem#getTileXFromLongitude(double, int)} and
     * {@link TileSystem#getTileYFromLatitude(double, int)} instead
     */
    @Deprecated
    public static Point getMapTileFromCoordinates(final double aLat, final double aLon, final int zoom) {
        final int y = MapView.getTileSystem().getTileYFromLatitude(aLat, zoom);
        final int x = MapView.getTileSystem().getTileXFromLongitude(aLon, zoom);
        return new Point(x, y);
    }

    /**
     * @deprecated Use {@link TileSystem#getLatitudeFromTileY(int, int)} and
     * {@link TileSystem#getLongitudeFromTileX(int, int)} instead
     */
    @Deprecated
    public static GeoPoint getCoordinatesFromMapTile(final int x, final int y, final int zoom) {
        final double lat = MapView.getTileSystem().getLatitudeFromTileY(y, zoom);
        final double lon = MapView.getTileSystem().getLongitudeFromTileX(x, zoom);
        return new GeoPoint(lat, lon);
    }

    public static File getFileName(ITileSource tileSource, final long pMapTileIndex) {
        final File file = new File(Configuration.getInstance().getOsmdroidTileCache(),
                tileSource.getTileRelativeFilenameString(pMapTileIndex) + OpenStreetMapTileProviderConstants.TILE_PATH_EXTENSION);
        return file;
    }

    /**
     * @return true if success, false if error
     */
    public boolean loadTile(final OnlineTileSourceBase tileSource, final long pMapTileIndex) {
        //check if file is already downloaded:
        File file = getFileName(tileSource, pMapTileIndex);
        if (file.exists()) {
            return true;
        }
        //check if the destination already has the file
        if (mTileWriter.exists(tileSource, pMapTileIndex)) {
            return true;
        }

        return forceLoadTile(tileSource, pMapTileIndex);
    }

    /**
     * Actual tile download, regardless of the tile being already present in the cache
     *
     * @return true if success, false if error
     * @since 5.6.5
     */
    public boolean forceLoadTile(final OnlineTileSourceBase tileSource, final long pMapTileIndex) {
        try {
            final Drawable drawable = mTileDownloader.downloadTile(pMapTileIndex, mTileWriter, tileSource);
            return drawable != null;
        } catch (CantContinueException e) {
            return false;
        }
    }

    public boolean deleteTile(final long pMapTileIndex) {
        return mTileWriter.exists(mTileSource, pMapTileIndex) && mTileWriter.remove(mTileSource, pMapTileIndex);
    }

    public boolean checkTile(final long pMapTileIndex) {
        return mTileWriter.exists(mTileSource, pMapTileIndex);
    }

    /**
     * "Should we download this tile?", either because it's not cached yet or because it's expired
     *
     * @since 5.6.5
     */
    public boolean isTileToBeDownloaded(final ITileSource pTileSource, final long pMapTileIndex) {
        final Long expiration = mTileWriter.getExpirationTimestamp(pTileSource, pMapTileIndex);
        if (expiration == null) {
            return true;
        }
        final long now = System.currentTimeMillis();
        return now > expiration;
    }

    /**
     * Computes the theoretical tiles covered by the bounding box
     *
     * @return list of tiles, sorted by ascending zoom level
     */
    public static List<Long> getTilesCoverage(final BoundingBox pBB,
                                              final int pZoomMin, final int pZoomMax) {
        final List<Long> result = new ArrayList<>();
        for (int zoomLevel = pZoomMin; zoomLevel <= pZoomMax; zoomLevel++) {
            final Collection<Long> resultForZoom = getTilesCoverage(pBB, zoomLevel);
            result.addAll(resultForZoom);
        }
        return result;
    }

    /**
     * Computes the theoretical tiles covered by the bounding box
     *
     * @return list of tiles for that zoom level, without any specific order
     */
    public static Collection<Long> getTilesCoverage(final BoundingBox pBB, final int pZoomLevel) {
        final Set<Long> result = new LinkedHashSet<>();
        for (Long mapTile : getTilesCoverageIterable(pBB, pZoomLevel, pZoomLevel)) {
            result.add(mapTile);
        }
        return result;
    }

    /**
     * Iterable returning tiles covered by the bounding box sorted by ascending zoom level
     *
     * @param pBB      the given bounding box
     * @param pZoomMin the given minimum zoom level
     * @param pZoomMax the given maximum zoom level
     * @return the iterable described above
     */
    static IterableWithSize<Long> getTilesCoverageIterable(final BoundingBox pBB,
                                                           final int pZoomMin, final int pZoomMax) {
        final MapTileAreaList list = new MapTileAreaList();
        for (int zoomLevel = pZoomMin; zoomLevel <= pZoomMax; zoomLevel++) {
            list.getList().add(new MapTileArea().set(zoomLevel, getTilesRect(pBB, zoomLevel)));
        }
        return list;
    }

    /**
     * Retrieve upper left and lower right points(exclusive) corresponding to the tiles coverage for
     * the selected zoom level.
     *
     * @param pBB        the given bounding box
     * @param pZoomLevel the given zoom level
     * @return the {@link Rect} reflecting the tiles coverage
     */
    public static Rect getTilesRect(final BoundingBox pBB,
                                    final int pZoomLevel) {
        final int mapTileUpperBound = 1 << pZoomLevel;
        final int right = MapView.getTileSystem().getTileXFromLongitude(pBB.getLonEast(), pZoomLevel);
        final int bottom = MapView.getTileSystem().getTileYFromLatitude(pBB.getLatSouth(), pZoomLevel);
        final int left = MapView.getTileSystem().getTileXFromLongitude(pBB.getLonWest(), pZoomLevel);
        final int top = MapView.getTileSystem().getTileYFromLatitude(pBB.getLatNorth(), pZoomLevel);
        int width = right - left + 1; // handling the modulo
        if (width <= 0) {
            width += mapTileUpperBound;
        }
        int height = bottom - top + 1; // handling the modulo
        if (height <= 0) {
            height += mapTileUpperBound;
        }
        return new Rect(left, top, left + width - 1, top + height - 1);
    }

    /**
     * Computes the theoretical tiles covered by the list of points
     *
     * @return list of tiles, sorted by ascending zoom level
     */
    public static List<Long> getTilesCoverage(final ArrayList<GeoPoint> pGeoPoints,
                                              final int pZoomMin, final int pZoomMax) {
        final List<Long> result = new ArrayList<>();
        for (int zoomLevel = pZoomMin; zoomLevel <= pZoomMax; zoomLevel++) {
            final Collection<Long> resultForZoom = getTilesCoverage(pGeoPoints, zoomLevel);
            result.addAll(resultForZoom);
        }
        return result;
    }

    /**
     * Computes the theoretical tiles covered by the list of points
     * Calculation done based on http://www.movable-type.co.uk/scripts/latlong.html
     */
    public static Collection<Long> getTilesCoverage(final ArrayList<GeoPoint> pGeoPoints,
                                                    final int pZoomLevel) {
        final Set<Long> result = new HashSet<>();

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

                        final double prevLatRad = wayPoint.getLatitude() * Math.PI / 180.0;
                        final double prevLonRad = wayPoint.getLongitude() * Math.PI / 180.0;

                        final double latRad = Math.asin(Math.sin(prevLatRad) * Math.cos(d / GeoConstants.RADIUS_EARTH_METERS) + Math.cos(prevLatRad) * Math.sin(d / GeoConstants.RADIUS_EARTH_METERS) * Math.cos(brng));
                        final double lonRad = prevLonRad + Math.atan2(Math.sin(brng) * Math.sin(d / GeoConstants.RADIUS_EARTH_METERS) * Math.cos(prevLatRad), Math.cos(d / GeoConstants.RADIUS_EARTH_METERS) - Math.sin(prevLatRad) * Math.sin(latRad));

                        wayPoint.setLatitude(((latRad * 180.0 / Math.PI)));
                        wayPoint.setLongitude(((lonRad * 180.0 / Math.PI)));

                        tile = new Point(
                                MapView.getTileSystem().getTileXFromLongitude(wayPoint.getLongitude(), pZoomLevel),
                                MapView.getTileSystem().getTileYFromLatitude(wayPoint.getLatitude(), pZoomLevel));

                        if (!tile.equals(prevTile)) {
//Log.d(Constants.APP_TAG, "New Tile lat " + tile.x + " lon " + tile.y);
                            int ofsx = tile.x >= 0 ? 0 : -tile.x;
                            int ofsy = tile.y >= 0 ? 0 : -tile.y;
                            for (int xAround = tile.x + ofsx; xAround <= tile.x + 1 + ofsx; xAround++) {
                                for (int yAround = tile.y + ofsy; yAround <= tile.y + 1 + ofsy; yAround++) {
                                    final int tileY = MyMath.mod(yAround, mapTileUpperBound);
                                    final int tileX = MyMath.mod(xAround, mapTileUpperBound);
                                    result.add(MapTileIndex.getTileIndex(pZoomLevel, tileX, tileY));
                                }
                            }

                            prevTile = tile;
                        }
                    }
                }

            } else {
                tile = new Point(
                        MapView.getTileSystem().getTileXFromLongitude(geoPoint.getLongitude(), pZoomLevel),
                        MapView.getTileSystem().getTileYFromLatitude(geoPoint.getLatitude(), pZoomLevel));
                prevTile = tile;

                int ofsx = tile.x >= 0 ? 0 : -tile.x;
                int ofsy = tile.y >= 0 ? 0 : -tile.y;
                for (int xAround = tile.x + ofsx; xAround <= tile.x + 1 + ofsx; xAround++) {
                    for (int yAround = tile.y + ofsy; yAround <= tile.y + 1 + ofsy; yAround++) {
                        final int tileY = MyMath.mod(yAround, mapTileUpperBound);
                        final int tileX = MyMath.mod(xAround, mapTileUpperBound);
                        result.add(MapTileIndex.getTileIndex(pZoomLevel, tileX, tileY));
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
        return getTilesCoverageIterable(pBB, pZoomMin, pZoomMax).size();
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
        final CacheManagerTask task = new CacheManagerTask(
                this,
                getDownloadingAction(ctx),
                bb,
                zoomMin,
                zoomMax);
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
        final CacheManagerTask task = new CacheManagerTask(
                this,
                getDownloadingAction(ctx),
                geoPoints,
                zoomMin,
                zoomMax);
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
        final CacheManagerTask task = new CacheManagerTask(
                this,
                getDownloadingAction(ctx),
                bb,
                zoomMin,
                zoomMax);
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
        final CacheManagerTask task = new CacheManagerTask(
                this,
                getDownloadingAction(ctx),
                geoPoints,
                zoomMin,
                zoomMax);
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
        final CacheManagerTask task = new CacheManagerTask(
                this,
                getDownloadingAction(ctx),
                geoPoints,
                zoomMin,
                zoomMax);
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
        final CacheManagerTask task = new CacheManagerTask(
                this,
                getDownloadingAction(ctx),
                bb,
                zoomMin,
                zoomMax);
        task.addCallback(callback);
        execute(task);
        return task;
    }

    /**
     * cancels all tasks
     *
     * @since 5.6.3
     */
    public void cancelAllJobs() {
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
    public CacheManagerTask downloadAreaAsync(Context ctx, List<Long> pTiles, final int zoomMin, final int zoomMax) {
        final CacheManagerTask task = new CacheManagerTask(
                this,
                getDownloadingAction(ctx),
                pTiles,
                zoomMin,
                zoomMax);
        task.addCallback(getDownloadingDialog(ctx, task));
        return execute(task);
    }

    /*
     * verifyCancel decides wether user has to confirm the cancel action via a alert
     *
     * @param state
     */
    public void setVerifyCancel(boolean state) {
        verifyCancel = state;
    }

    public boolean getVerifyCancel() {
        return verifyCancel;
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
         *
         * @param errors
         */
        public void onTaskFailed(int errors);
    }

    public static abstract class CacheManagerDialog implements CacheManagerCallback {

        private final CacheManagerTask mTask;
        private final ProgressDialog mProgressDialog;
        private String handleMessage;
        public CacheManagerDialog(final Context pCtx, final CacheManagerTask pTask) {
            mTask = pTask;
            handleMessage = pCtx.getString(R.string.cacheManagerHandlingMessage);
            mProgressDialog = new ProgressDialog(pCtx);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);
            // If verifyCancel is set to true, ask for verification before canceling
            if (pTask.mManager.getVerifyCancel()) {
                mProgressDialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface cancelDialog) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(pCtx);
                        builder.setTitle(pCtx.getString(R.string.cacheManagerCancelTitle));
                        builder.setMessage(pCtx.getString(R.string.cacheManagerCancelBody));
                        builder.setPositiveButton(pCtx.getString(R.string.cacheManagerYes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mTask.cancel(true);
                            }
                        });
                        builder.setNegativeButton(pCtx.getString(R.string.cacheManagerNo), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mProgressDialog.show();
                            }
                        });
                        builder.show();
                    }
                });
            } else {
                mProgressDialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mTask.cancel(true);
                    }
                });
            }
        }

        protected String zoomMessage(int zoomLevel, int zoomMin, int zoomMax) {
            return String.format(handleMessage, zoomLevel, zoomMin, zoomMax);
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
        private final IterableWithSize<Long> mTiles;
        private final int mZoomMin;
        private final int mZoomMax;
        private final ArrayList<CacheManagerCallback> mCallbacks = new ArrayList<>();

        private CacheManagerTask(final CacheManager pManager, final CacheManagerAction pAction,
                                 final IterableWithSize<Long> pTiles,
                                 final int pZoomMin, final int pZoomMax) {
            mManager = pManager;
            mAction = pAction;
            mTiles = pTiles;
            mZoomMin = Math.max(pZoomMin, pManager.mMinZoomLevel);
            mZoomMax = Math.min(pZoomMax, pManager.mMaxZoomLevel);
        }

        public CacheManagerTask(final CacheManager pManager, final CacheManagerAction pAction,
                                final List<Long> pTiles,
                                final int pZoomMin, final int pZoomMax) {
            this(pManager, pAction, new ListWrapper<>(pTiles), pZoomMin, pZoomMax);
        }

        public CacheManagerTask(final CacheManager pManager, final CacheManagerAction pAction,
                                final ArrayList<GeoPoint> pGeoPoints,
                                final int pZoomMin, final int pZoomMax) {
            this(pManager, pAction, getTilesCoverage(pGeoPoints, pZoomMin, pZoomMax), pZoomMin, pZoomMax);
        }

        public CacheManagerTask(final CacheManager pManager, final CacheManagerAction pAction,
                                final BoundingBox pBB,
                                final int pZoomMin, final int pZoomMax) {
            this(pManager, pAction, getTilesCoverageIterable(pBB, pZoomMin, pZoomMax), pZoomMin, pZoomMax);
        }

        public void addCallback(final CacheManagerCallback pCallback) {
            if (pCallback != null) {
                mCallbacks.add(pCallback);
            }
        }

        @Override
        protected void onPreExecute() {
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
        protected void onCancelled() {
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

            for (final long tile : mTiles) {
                final int zoom = MapTileIndex.getZoom(tile);
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
                    publishProgress(tileCounter, MapTileIndex.getZoom(tile));
                }

            }
            return errors;
        }
    }

    public CacheManagerDialog getDownloadingDialog(final Context pCtx, final CacheManagerTask pTask) {
        return new CacheManagerDialog(pCtx, pTask) {
            @Override
            protected String getUITitle() {
                return pCtx.getString(R.string.cacheManagerDownloadingTitle);
            }

            @Override
            public void onTaskFailed(int errors) {
                super.onTaskFailed(errors);
                Toast.makeText(pCtx,
                        String.format(pCtx.getString(R.string.cacheManagerFailed), errors+""),
                        Toast.LENGTH_SHORT).show();
            }
        };
    }

    public CacheManagerDialog getCleaningDialog(final Context pCtx, final CacheManagerTask pTask) {
        return new CacheManagerDialog(pCtx, pTask) {
            @Override
            protected String getUITitle() {
                return pCtx.getString(R.string.cacheManagerCleaningTitle);
            }

            @Override
            public void onTaskFailed(int deleted) {
                super.onTaskFailed(deleted);

                Toast.makeText(pCtx,
                        String.format(pCtx.getString(R.string.cacheManagerCleanFailed), deleted+""),
                        Toast.LENGTH_SHORT).show();
            }
        };
    }

    /**
     * Action to perform on a tile within a CacheManagerTask
     *
     * @author F.Fontaine
     */
    public interface CacheManagerAction {
        /**
         * Preconditions to check before bulk action
         *
         * @return true if we pass the check
         */
        boolean preCheck();

        /**
         * We will update the callbacks not for every tile, but at this rate
         */
        int getProgressModulo();

        /**
         * The action to perform on a single tile
         *
         * @return true if you want to increment the action counter
         */
        boolean tileAction(final long pMapTileIndex);
    }

    private static class ListWrapper<T> implements IterableWithSize<T> {
        private final List<T> list;

        private ListWrapper(List<T> list) {
            this.list = list;
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public Iterator<T> iterator() {
            return list.iterator();
        }
    }

    public CacheManagerAction getDownloadingAction(Context pCtx) {
        return new CacheManagerAction() {
            @Override
            public boolean preCheck() {
                if (mTileSource instanceof OnlineTileSourceBase) {
                    if (!((OnlineTileSourceBase) mTileSource).getTileSourcePolicy().acceptsBulkDownload()) {
                        throw new TileSourcePolicyException(pCtx.getString(R.string.cacheManagerUnsupportedSource));
                    }
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
            public boolean tileAction(final long pMapTileIndex) {
                return !loadTile((OnlineTileSourceBase) mTileSource, pMapTileIndex);
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
            public boolean tileAction(final long pMapTileIndex) {
                return deleteTile(pMapTileIndex);
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
        BoundingBox extendedBounds = extendedBoundsFromGeoPoints(geoPoints, zoomMin);
        return cleanAreaAsync(ctx, extendedBounds, zoomMin, zoomMax);
    }

    /**
     * Remove all cached tiles in the specified area.
     */
    public CacheManagerTask cleanAreaAsync(Context ctx, List<Long> tiles, int zoomMin, int zoomMax) {
        final CacheManagerTask task = new CacheManagerTask(this, getCleaningAction(), tiles, zoomMin, zoomMax);
        task.addCallback(getCleaningDialog(ctx, task));
        return execute(task);
    }

    /**
     *
     */

    public BoundingBox extendedBoundsFromGeoPoints(ArrayList<GeoPoint> geoPoints, int minZoomLevel) {
        final BoundingBox bb = BoundingBox.fromGeoPoints(geoPoints);
        final int right = MapView.getTileSystem().getTileXFromLongitude(bb.getLonEast(), minZoomLevel);
        final int bottom = MapView.getTileSystem().getTileYFromLatitude(bb.getLatSouth(), minZoomLevel);
        final int left = MapView.getTileSystem().getTileXFromLongitude(bb.getLonWest(), minZoomLevel);
        final int top = MapView.getTileSystem().getTileYFromLatitude(bb.getLatNorth(), minZoomLevel);
        return new BoundingBox(
                MapView.getTileSystem().getLatitudeFromTileY(top - 1, minZoomLevel),
                MapView.getTileSystem().getLongitudeFromTileX(right + 1, minZoomLevel),
                MapView.getTileSystem().getLatitudeFromTileY(bottom + 1, minZoomLevel),
                MapView.getTileSystem().getLongitudeFromTileX(left - 1, minZoomLevel));
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

    /**
     * @since 6.0.2
     */
    public void setTileDownloader(final TileDownloader pTileDownloader) {
        mTileDownloader = pTileDownloader;
    }
}
