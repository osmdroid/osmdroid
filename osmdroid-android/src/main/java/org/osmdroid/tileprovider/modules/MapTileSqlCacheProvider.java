package org.osmdroid.tileprovider.modules;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.Counters;
import org.osmdroid.tileprovider.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sqlite based tile cache mechansism
 *
 * @since 5.1
 * @see SqlTileWriter
 * Created by alex on 1/16/16.
 */
public class MapTileSqlCacheProvider  extends MapTileFileStorageProviderBase{
    // ===========================================================
    // Constants
    // ===========================================================


    // ===========================================================
    // Fields
    // ===========================================================

    private final AtomicReference<ITileSource> mTileSource = new AtomicReference<ITileSource>();
    private SqlTileWriter mWriter;
    private static final String[] columns = {DatabaseFileArchive.COLUMN_TILE, SqlTileWriter.COLUMN_EXPIRES};

    // ===========================================================
    // Constructors
    // ===========================================================

    @Deprecated
    public MapTileSqlCacheProvider(final IRegisterReceiver pRegisterReceiver,
                                      final ITileSource pTileSource, final long pMaximumCachedFileAge) {
        this(pRegisterReceiver, pTileSource);
    }

    /**
     * The tiles may be found on several media. This one works with tiles stored on database.
     * It and its friends are typically created and controlled by {@link MapTileProviderBase}.
     */
    public MapTileSqlCacheProvider(final IRegisterReceiver pRegisterReceiver,
                                      final ITileSource pTileSource) {
        super(pRegisterReceiver,
                Configuration.getInstance().getTileFileSystemThreads(),
                Configuration.getInstance().getTileFileSystemMaxQueueSize());

        setTileSource(pTileSource);
        mWriter = new SqlTileWriter();
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
        return "SQL Cache Archive Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return "sqlcache";
    }

    @Override
    protected Runnable getTileLoader() {
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
                : microsoft.mappoint.TileSystem.getMaximumZoomLevel();
    }

    @Override
    protected void onMediaMounted() {

    }

    @Override
    protected void onMediaUnmounted() {
        if (mWriter!=null)
            mWriter.onDetach();
        mWriter=new SqlTileWriter();
    }

    @Override
    public void setTileSource(final ITileSource pTileSource) {
        mTileSource.set(pTileSource);
    }

    @Override
    public void detach() {

        if (mWriter!=null)
            mWriter.onDetach();
        mWriter=null;
        super.detach();
    }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * returns true if the given tile for the current map source exists in the cache db
     * @param pTile
     * @return
     */
    public boolean hasTile(final MapTile pTile) {
        ITileSource tileSource = mTileSource.get();
        if (tileSource == null) {
            return false;
        }
        return mWriter.getExpirationTimestamp(tileSource, pTile) != null;
    }


    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================


    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTileRequestState pState) throws CantContinueException{

            ITileSource tileSource = mTileSource.get();
            if (tileSource == null) {
                return null;
            }

            final MapTile pTile = pState.getMapTile();

            // if there's no sdcard then don't do anything
            if (!isSdCardAvailable()) {
                if (Configuration.getInstance().isDebugMode()) {
                    Log.d(IMapView.LOGTAG,"No sdcard - do nothing for tile: " + pTile);
                }
                Counters.fileCacheMiss++;
                return null;
            }
            try {
                final Drawable result = mWriter.loadTile(tileSource, pTile);
                if (result == null) {
                    Counters.fileCacheMiss++;
                } else {
                    Counters.fileCacheHit++;
                }
                return result;
            } catch (final BitmapTileSourceBase.LowMemoryException e) {
                // low memory so empty the queue
                Log.w(IMapView.LOGTAG, "LowMemoryException downloading MapTile: " + pTile + " : " + e);
                Counters.fileCacheOOM++;
                throw new MapTileModuleProviderBase.CantContinueException(e);
            } catch (final Throwable e) {
                Log.e(IMapView.LOGTAG, "Error loading tile", e);
                return null;
            }
        }
    }
}
