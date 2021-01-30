package org.osmdroid.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.UrlBackoff;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The {@link MapTileDownloader} loads tiles from an HTTP server. It saves downloaded tiles to an
 * IFilesystemCache if available.
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 * @author Manuel Stahl
 */
public class MapTileDownloader extends MapTileModuleProviderBase {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private final IFilesystemCache mFilesystemCache;

    private final AtomicReference<OnlineTileSourceBase> mTileSource = new AtomicReference<>();

    private final INetworkAvailablityCheck mNetworkAvailablityCheck;

    /**
     * @since 6.0.2
     */
    private final TileLoader mTileLoader = new TileLoader();

    private final UrlBackoff mUrlBackoff = new UrlBackoff();

    private TileDownloader mTileDownloader = new TileDownloader(); // default value

    // ===========================================================
    // Constructors
    // ===========================================================

    public MapTileDownloader(final ITileSource pTileSource) {
        this(pTileSource, null, null);
    }

    public MapTileDownloader(final ITileSource pTileSource, final IFilesystemCache pFilesystemCache) {
        this(pTileSource, pFilesystemCache, null);
    }

    public MapTileDownloader(final ITileSource pTileSource,
                             final IFilesystemCache pFilesystemCache,
                             final INetworkAvailablityCheck pNetworkAvailablityCheck) {
        this(pTileSource, pFilesystemCache, pNetworkAvailablityCheck,
                Configuration.getInstance().getTileDownloadThreads(),
                Configuration.getInstance().getTileDownloadMaxQueueSize());
    }

    public MapTileDownloader(final ITileSource pTileSource,
                             final IFilesystemCache pFilesystemCache,
                             final INetworkAvailablityCheck pNetworkAvailablityCheck, int pThreadPoolSize,
                             int pPendingQueueSize) {
        super(pThreadPoolSize, pPendingQueueSize);

        mFilesystemCache = pFilesystemCache;
        mNetworkAvailablityCheck = pNetworkAvailablityCheck;
        setTileSource(pTileSource);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public ITileSource getTileSource() {
        return mTileSource.get();
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public boolean getUsesDataConnection() {
        return true;
    }

    @Override
    protected String getName() {
        return "Online Tile Download Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return "downloader";
    }

    @Override
    public TileLoader getTileLoader() {
        return mTileLoader;
    }

    @Override
    public void detach() {
        super.detach();
        if (this.mFilesystemCache != null)
            this.mFilesystemCache.onDetach();
    }

    @Override
    public int getMinimumZoomLevel() {
        OnlineTileSourceBase tileSource = mTileSource.get();
        return (tileSource != null ? tileSource.getMinimumZoomLevel() : OpenStreetMapTileProviderConstants.MINIMUM_ZOOMLEVEL);
    }

    @Override
    public int getMaximumZoomLevel() {
        OnlineTileSourceBase tileSource = mTileSource.get();
        return (tileSource != null ? tileSource.getMaximumZoomLevel()
                : org.osmdroid.util.TileSystem.getMaximumZoomLevel());
    }

    @Override
    public void setTileSource(final ITileSource tileSource) {
        // We are only interested in OnlineTileSourceBase tile sources
        if (tileSource instanceof OnlineTileSourceBase) {
            mTileSource.set((OnlineTileSourceBase) tileSource);
        } else {
            // Otherwise shut down the tile downloader
            mTileSource.set(null);
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {

        /**
         * downloads a tile and follows http redirects
         */
        protected Drawable downloadTile(final long pMapTileIndex, final int redirectCount, final String targetUrl) throws CantContinueException {
            final OnlineTileSourceBase tileSource = mTileSource.get();
            if (tileSource == null) {
                return null;
            }
            try {
                tileSource.acquire();
            } catch (final InterruptedException e) {
                return null;
            }
            try {
                return mTileDownloader.downloadTile(pMapTileIndex, redirectCount, targetUrl, mFilesystemCache, tileSource);
            } finally {
                tileSource.release();
            }
        }

        @Override
        public Drawable loadTile(final long pMapTileIndex) throws CantContinueException {

            OnlineTileSourceBase tileSource = mTileSource.get();
            if (tileSource == null) {
                return null;
            }


            if (mNetworkAvailablityCheck != null
                    && !mNetworkAvailablityCheck.getNetworkAvailable()) {
                if (Configuration.getInstance().isDebugMode()) {
                    Log.d(IMapView.LOGTAG, "Skipping " + getName() + " due to NetworkAvailabliltyCheck.");
                }
                return null;
            }

            final String tileURLString = tileSource.getTileURLString(pMapTileIndex);

            if (TextUtils.isEmpty(tileURLString)) {
                return null;    //unlikely but just in case
            }

            if (mUrlBackoff.shouldWait(tileURLString)) {
                return null;
            }
            final Drawable result = downloadTile(pMapTileIndex, 0, tileURLString);
            if (result == null) {
                mUrlBackoff.next(tileURLString);
            } else {
                mUrlBackoff.remove(tileURLString);
            }
            return result;

        }

        @Override
        protected void tileLoaded(final MapTileRequestState pState, final Drawable pDrawable) {
            removeTileFromQueues(pState.getMapTile());
            // don't return the tile because we'll wait for the fs provider to ask for it
            // this prevent flickering when a load of delayed downloads complete for tiles
            // that we might not even be interested in any more
            pState.getCallback().mapTileRequestCompleted(pState, null);
            // We want to return the Bitmap to the BitmapPool if applicable
            BitmapPool.getInstance().asyncRecycle(pDrawable);
        }

    }

    /**
     * @since 6.0.2
     */
    public void setTileDownloader(final TileDownloader pTileDownloader) {
        mTileDownloader = pTileDownloader;
    }
}
