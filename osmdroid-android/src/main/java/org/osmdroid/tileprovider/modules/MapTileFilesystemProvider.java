package org.osmdroid.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.Counters;
import org.osmdroid.util.MapTileIndex;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Implements a file system cache and provides cached tiles. This functions as a tile provider by
 * serving cached tiles for the supplied tile source.
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 */
public class MapTileFilesystemProvider extends MapTileFileStorageProviderBase {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private final TileWriter mWriter = new TileWriter();
    private final AtomicReference<ITileSource> mTileSource = new AtomicReference<ITileSource>();

    // ===========================================================
    // Constructors
    // ===========================================================

    public MapTileFilesystemProvider(final IRegisterReceiver pRegisterReceiver) {
        this(pRegisterReceiver, TileSourceFactory.DEFAULT_TILE_SOURCE);
    }

    public MapTileFilesystemProvider(final IRegisterReceiver pRegisterReceiver,
                                     final ITileSource aTileSource) {
        this(pRegisterReceiver, aTileSource, Configuration.getInstance().getExpirationExtendedDuration() + OpenStreetMapTileProviderConstants.DEFAULT_MAXIMUM_CACHED_FILE_AGE);
    }

    public MapTileFilesystemProvider(final IRegisterReceiver pRegisterReceiver,
                                     final ITileSource pTileSource, final long pMaximumCachedFileAge) {
        this(pRegisterReceiver, pTileSource, pMaximumCachedFileAge,
                Configuration.getInstance().getTileFileSystemThreads(),
                Configuration.getInstance().getTileFileSystemMaxQueueSize());
    }

    /**
     * Provides a file system based cache tile provider. Other providers can register and store data
     * in the cache.
     *
     * @param pRegisterReceiver
     */
    public MapTileFilesystemProvider(final IRegisterReceiver pRegisterReceiver,
                                     final ITileSource pTileSource, final long pMaximumCachedFileAge, int pThreadPoolSize,
                                     int pPendingQueueSize) {
        super(pRegisterReceiver, pThreadPoolSize, pPendingQueueSize);
        setTileSource(pTileSource);

        mWriter.setMaximumCachedFileAge(pMaximumCachedFileAge);
    }
    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public boolean getUsesDataConnection() {
        return false;
    }

    @Override
    protected String getName() {
        return "File System Cache Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return "filesystem";
    }

    @Override
    public TileLoader getTileLoader() {
        return new TileLoader();
    }

    @Override
    public int getMinimumZoomLevel() {
        ITileSource tileSource = mTileSource.get();
        return tileSource != null ? tileSource.getMinimumZoomLevel() : OpenStreetMapTileProviderConstants.MINIMUM_ZOOMLEVEL;
    }

    @Override
    public int getMaximumZoomLevel() {
        ITileSource tileSource = mTileSource.get();
        return tileSource != null ? tileSource.getMaximumZoomLevel()
                : org.osmdroid.util.TileSystem.getMaximumZoomLevel();
    }

    @Override
    public void setTileSource(final ITileSource pTileSource) {
        mTileSource.set(pTileSource);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final long pMapTileIndex) throws CantContinueException {

            ITileSource tileSource = mTileSource.get();
            if (tileSource == null) {
                return null;
            }

            try {
                final Drawable result = mWriter.loadTile(tileSource, pMapTileIndex);
                if (result == null) {
                    Counters.fileCacheMiss++;
                } else {
                    Counters.fileCacheHit++;
                }
                return result;
            } catch (final BitmapTileSourceBase.LowMemoryException e) {
                // low memory so empty the queue
                Log.w(IMapView.LOGTAG, "LowMemoryException downloading MapTile: " + MapTileIndex.toString(pMapTileIndex) + " : " + e);
                Counters.fileCacheOOM++;
                throw new CantContinueException(e);
            } catch (final Throwable e) {
                Log.e(IMapView.LOGTAG, "Error loading tile", e);
                return null;
            }
        }
    }
}
