package org.osmdroid.tileprovider.modules;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.Counters;
import org.osmdroid.util.MapTileIndex;

import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;

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

    public static final String CONST_MAPTILEPROVIDER_FILESISTEM = "filesystem";

    // ===========================================================
    // Fields
    // ===========================================================

    private final TileWriter mWriter = new TileWriter();
    private final AtomicReference<ITileSource> mTileSource = new AtomicReference<>();
    private final TileLoader mTileLoader = new TileLoader();

    // ===========================================================
    // Constructors
    // ===========================================================

    public MapTileFilesystemProvider(@NonNull final Context context, final IRegisterReceiver pRegisterReceiver) {
        this(context, pRegisterReceiver, TileSourceFactory.DEFAULT_TILE_SOURCE);
    }
    public MapTileFilesystemProvider(@NonNull final Context context,
                                     final IRegisterReceiver pRegisterReceiver,
                                     final ITileSource aTileSource) {
        this(context, pRegisterReceiver, aTileSource, Configuration.getInstance().getExpirationExtendedDuration() + OpenStreetMapTileProviderConstants.DEFAULT_MAXIMUM_CACHED_FILE_AGE);
    }
    public MapTileFilesystemProvider(@NonNull final Context context,
                                     final IRegisterReceiver pRegisterReceiver,
                                     final ITileSource pTileSource, final long pMaximumCachedFileAge) {
        this(context, pRegisterReceiver, pTileSource, pMaximumCachedFileAge,
                Configuration.getInstance().getTileFileSystemThreads(),
                Configuration.getInstance().getTileFileSystemMaxQueueSize());
    }
    /** Provides a file system based cache tile provider. Other providers can register and store data in the cache */
    public MapTileFilesystemProvider(@NonNull final Context context,
                                     final IRegisterReceiver pRegisterReceiver,
                                     final ITileSource pTileSource, final long pMaximumCachedFileAge, int pThreadPoolSize,
                                     int pPendingQueueSize) {
        super(context, pRegisterReceiver, pThreadPoolSize, pPendingQueueSize);
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
        return CONST_MAPTILEPROVIDER_FILESISTEM;
    }

    @Override
    public TileLoader getTileLoader() {
        return mTileLoader;
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

            final ITileSource tileSource = mTileSource.get();
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

        @IMapTileProviderCallback.TILEPROVIDERTYPE
        @Override
        public final int getProviderType() { return IMapTileProviderCallback.TILEPROVIDERTYPE_FILE_SYSTEM; }
    }
}
