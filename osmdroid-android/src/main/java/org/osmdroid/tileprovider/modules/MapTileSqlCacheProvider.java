package org.osmdroid.tileprovider.modules;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by alex on 1/16/16.
 */
public class MapTileSqlCacheProvider  extends MapTileFileStorageProviderBase{
    // ===========================================================
    // Constants
    // ===========================================================


    // ===========================================================
    // Fields
    // ===========================================================


    private SQLiteDatabase mDatabase;
    private final AtomicReference<ITileSource> mTileSource = new AtomicReference<ITileSource>();
    private final long mMaximumCachedFileAge;
    final String[] tile = {"tile"};

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * The tiles may be found on several media. This one works with tiles stored on the file system.
     * It and its friends are typically created and controlled by {@link MapTileProviderBase}.
     */
    public MapTileSqlCacheProvider(final IRegisterReceiver pRegisterReceiver,
                                      final ITileSource pTileSource, final long pMaximumCachedFileAge) {
        super(pRegisterReceiver, OpenStreetMapTileProviderConstants.NUMBER_OF_TILE_FILESYSTEM_THREADS,
                OpenStreetMapTileProviderConstants.TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);

        setTileSource(pTileSource);
        findArchiveFiles();
        mMaximumCachedFileAge = pMaximumCachedFileAge;


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
        findArchiveFiles();
    }

    @Override
    protected void onMediaUnmounted() {
        findArchiveFiles();
    }

    @Override
    public void setTileSource(final ITileSource pTileSource) {
        mTileSource.set(pTileSource);
    }

    @Override
    public void detach() {
        if (mDatabase!=null)
            mDatabase.close();
        super.detach();
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void findArchiveFiles(){
        try {
            File cache = new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE.getAbsolutePath() + File.separator + "cache.db");
            if (cache.exists())
                mDatabase = (SQLiteDatabase.openDatabase(cache.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY));
            else
                mDatabase = null;
        }catch (Exception ex){
            Log.w(IMapView.LOGTAG, "trouble opening sqlite database cache file",ex);
        }
    }


    public byte[] getImage(final ITileSource pTileSource, final MapTile pTile) {

        try {
            byte[] bits=null;
            final String[] tile = {"tile"};
            final long x = (long) pTile.getX();
            final long y = (long) pTile.getY();
            final long z = (long) pTile.getZoomLevel();
            final long index = ((z << z) + x << z) + y;
            final Cursor cur = mDatabase.query(DatabaseFileArchive.TABLE, tile, "key = " + index + " and provider = '" + pTileSource.name() + "'", null, null, null, null);

            if(cur.getCount() != 0) {
                cur.moveToFirst();
                bits = (cur.getBlob(0));
            }
            cur.close();
            if(bits != null) {
                return bits;
            }
        } catch(final Throwable e) {
            Log.w(IMapView.LOGTAG,"Error getting db stream: " + pTile, e);
        }

        return null;
    }


    public InputStream getInputStream(final ITileSource pTileSource, final MapTile pTile) {
        try {
            InputStream ret = null;
            byte[] bits=getImage(pTileSource, pTile);
            if (bits!=null)
                ret = new ByteArrayInputStream(bits);
            if(ret != null) {
                return ret;
            }
        } catch(final Throwable e) {
            Log.w(IMapView.LOGTAG,"Error getting db stream: " + pTile, e);
        }
        return null;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    static final String[] columns={"tile","expires"};
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
                if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
                    Log.d(IMapView.LOGTAG,"No sdcard - do nothing for tile: " + pTile);
                }
                return null;
            }

            InputStream inputStream = null;
            try {
                if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
                    Log.d(IMapView.LOGTAG,"Tile doesn't exist: " + pTile);
                }

                final long x = (long) pTile.getX();
                final long y = (long) pTile.getY();
                final long z = (long) pTile.getZoomLevel();
                final long index = ((z << z) + x << z) + y;
                final Cursor cur =mDatabase.query(DatabaseFileArchive.TABLE,columns,"key = " + index + " and provider = '" + tileSource.name() + "'", null, null, null, null);
                byte[] bits=null;
                long lastModified=0l;

                if(cur.getCount() != 0) {
                    cur.moveToFirst();
                    bits = (cur.getBlob(cur.getColumnIndex("tile")));
                    lastModified = cur.getLong(cur.getColumnIndex("expires"));
                }
                cur.close();
                if (bits==null)
                    return null;
                inputStream = new ByteArrayInputStream(bits);
                Drawable drawable = tileSource.getDrawable(inputStream);
                // Check to see if file has expired
                final long now = System.currentTimeMillis();
                final boolean fileExpired = lastModified < now - mMaximumCachedFileAge;

                if (fileExpired && drawable != null) {
                    if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
                        Log.d(IMapView.LOGTAG,"Tile expired: " + tile);
                    }
                    ExpirableBitmapDrawable.setDrawableExpired(drawable);
                    //should we remove from the database here?
                }
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
