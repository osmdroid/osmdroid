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
    private final long mMaximumCachedFileAge;
    //FIXME constants with #348
    private static final String[] tile = {"tile"};
    private static final String[] columns={"tile","expires"};

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * The tiles may be found on several media. This one works with tiles stored on the file system.
     * It and its friends are typically created and controlled by {@link MapTileProviderBase}.
     */
    public MapTileSqlCacheProvider(final IRegisterReceiver pRegisterReceiver,
                                      final ITileSource pTileSource, final long pMaximumCachedFileAge) {
        super(pRegisterReceiver,
            Configuration.getInstance().getTileFileSystemThreads(),
            Configuration.getInstance().getTileFileSystemMaxQueueSize());

        setTileSource(pTileSource);
        mMaximumCachedFileAge = pMaximumCachedFileAge;
        mWriter = new SqlTileWriter();

    }

    public MapTileSqlCacheProvider(final IRegisterReceiver pRegisterReceiver,
                                      final ITileSource pTileSource) {
        this(pRegisterReceiver, pTileSource, OpenStreetMapTileProviderConstants.DEFAULT_MAXIMUM_CACHED_FILE_AGE);
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
        final long x = (long) pTile.getX();
        final long y = (long) pTile.getY();
        final long z = (long) pTile.getZoomLevel();
        final long index = ((z << z) + x << z) + y;
        final Cursor cur =mWriter.db.query(DatabaseFileArchive.TABLE,columns,"key = " + index + " and provider = '" + tileSource.name() + "'", null, null, null, null);
        if(cur.getCount() != 0) {
            cur.close();
            return true;
        }
        return false;
    }


    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================


    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTileRequestState pState) {

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
                return null;
            }
            if (mWriter==null || mWriter.db == null) {
                if (Configuration.getInstance().isDebugMode()) {
                    Log.d(IMapView.LOGTAG,"Sqlwriter cache is offline - do nothing for tile: " + pTile);
                }
                return null;
            }

            InputStream inputStream = null;
            try {


                final long x = (long) pTile.getX();
                final long y = (long) pTile.getY();
                final long z = (long) pTile.getZoomLevel();
                final long index = ((z << z) + x << z) + y;
                final Cursor cur =mWriter.db.query(DatabaseFileArchive.TABLE,columns,"key = " + index + " and provider = '" + tileSource.name() + "'", null, null, null, null);
                byte[] bits=null;
                long lastModified=0l;

                if(cur.getCount() != 0) {
                    cur.moveToFirst();
                    bits = (cur.getBlob(cur.getColumnIndex("tile")));
                    lastModified = cur.getLong(cur.getColumnIndex("expires"));
                }
                cur.close();
                if (bits==null) {
                    if (Configuration.getInstance().isDebugMode()) {
                        Log.d(IMapView.LOGTAG,"SqlCache - Tile doesn't exist: " +tileSource.name() + pTile);
                        Counters.fileCacheMiss++;
                    }
                    return null;
                }
                inputStream = new ByteArrayInputStream(bits);
                Drawable drawable = tileSource.getDrawable(inputStream);
                // Check to see if file has expired
                final long now = System.currentTimeMillis();
                final boolean fileExpired = lastModified < now - mMaximumCachedFileAge;

                if (fileExpired && drawable != null) {
                    if (Configuration.getInstance().isDebugMode()) {
                        Log.d(IMapView.LOGTAG,"Tile expired: " + tileSource.name() +pTile);
                    }
                    ExpirableBitmapDrawable.setDrawableExpired(drawable);
                    //should we remove from the database here?
                }
                Counters.fileCacheHit++;
                return drawable;
            } catch (final Throwable e) {
                Log.e(IMapView.LOGTAG,"Error loading tile", e);
            } finally {
                if (inputStream != null) {
                    StreamUtils.closeStream(inputStream);
                }
            }

            return null;
        }
    }
}
