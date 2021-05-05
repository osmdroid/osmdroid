package org.osmdroid.tileprovider.modules;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.Counters;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.util.GarbageCollector;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.util.SplashScreenable;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.osmdroid.tileprovider.modules.DatabaseFileArchive.COLUMN_KEY;
import static org.osmdroid.tileprovider.modules.DatabaseFileArchive.COLUMN_PROVIDER;
import static org.osmdroid.tileprovider.modules.DatabaseFileArchive.COLUMN_TILE;
import static org.osmdroid.tileprovider.modules.DatabaseFileArchive.TABLE;

/**
 * An implementation of {@link IFilesystemCache} based on the original TileWriter. It writes tiles to a sqlite database cache.
 * It supports expiration timestamps if provided by the server from which the tile was downloaded. Trimming
 * of expired
 * <p>
 * If the database exceeds {@link Configuration#getInstance()#getTileFileSystemCacheTrimBytes()}
 * cache exceeds 600 Mb then it will be trimmed to 500 Mb by deleting files that expire first.
 *
 * @author Alex O'Ree
 * @see DatabaseFileArchive
 * @see SqliteArchiveTileWriter
 * @since 5.1
 */
public class SqlTileWriter implements IFilesystemCache, SplashScreenable {
    public static final String DATABASE_FILENAME = "cache.db";
    public static final String COLUMN_EXPIRES = "expires";
    public static final String COLUMN_EXPIRES_INDEX = "expires_index";

    private static boolean cleanOnStartup = true;

    /*
      * disables cache purge of expired tiled on start up
     * if this is set to false, the database will only purge tiles if manually called or if
     * the storage device runs out of space.
     *
     * expired tiles will continue to be overwritten as new versions are downloaded regardless
    @since 6.0.0
     */
    public static void setCleanupOnStart(boolean value) {
        cleanOnStartup = value;
    }

    private static final Object mLock = new Object();
    protected static File db_file;
    protected static SQLiteDatabase mDb;
    protected long lastSizeCheck = 0;
    private final GarbageCollector garbageCollector = new GarbageCollector(new Runnable() {
        @Override
        public void run() {
            runCleanupOperation();
        }
    });

    static boolean hasInited = false;

    public SqlTileWriter() {

        getDb();

        if (!hasInited) {
            hasInited = true;

            if (cleanOnStartup) {
                garbageCollector.gc();
            }
        }
    }

    /**
     * this could be a long running operation, don't run on the UI thread unless necessary.
     * This function prunes the database for old or expired tiles.
     *
     * @since 5.6
     */
    public void runCleanupOperation() {
        final SQLiteDatabase db = getDb();
        if (db == null || !db.isOpen()) {
            if (Configuration.getInstance().isDebugMode()) {
                Log.d(IMapView.LOGTAG, "Finished init thread, aborted due to null database reference");
            }
            return;
        }

        // index creation is run now (regardless of the table size)
        // therefore potentially on a small table, for better index creation performances
        createIndex(db);

        final long dbLength = db_file.length();
        if (dbLength <= Configuration.getInstance().getTileFileSystemCacheMaxBytes()) {
            return;
        }

        runCleanupOperation(
                dbLength - Configuration.getInstance().getTileFileSystemCacheTrimBytes(),
                Configuration.getInstance().getTileGCBulkSize(),
                Configuration.getInstance().getTileGCBulkPauseInMillis(),
                true);
    }

    @Override
    public boolean saveFile(final ITileSource pTileSourceInfo, final long pMapTileIndex, final InputStream pStream, final Long pExpirationTime) {
        final SQLiteDatabase db = getDb();
        if (db == null || !db.isOpen()) {
            Log.d(IMapView.LOGTAG, "Unable to store cached tile from " + pTileSourceInfo.name() + " " + MapTileIndex.toString(pMapTileIndex) + ", database not available.");
            Counters.fileCacheSaveErrors++;
            return false;
        }
        ByteArrayOutputStream bos = null;
        try {
            ContentValues cv = new ContentValues();
            final long index = getIndex(pMapTileIndex);
            cv.put(DatabaseFileArchive.COLUMN_PROVIDER, pTileSourceInfo.name());

            byte[] buffer = new byte[512];
            int l;
            bos = new ByteArrayOutputStream();
            while ((l = pStream.read(buffer)) != -1)
                bos.write(buffer, 0, l);
            byte[] bits = bos.toByteArray(); // if a variable is required at all

            cv.put(DatabaseFileArchive.COLUMN_KEY, index);
            cv.put(DatabaseFileArchive.COLUMN_TILE, bits);
            if (pExpirationTime != null)
                cv.put(COLUMN_EXPIRES, pExpirationTime);
            db.replaceOrThrow(TABLE, null, cv);
            if (Configuration.getInstance().isDebugMode())
                Log.d(IMapView.LOGTAG, "tile inserted " + pTileSourceInfo.name() + MapTileIndex.toString(pMapTileIndex));
            if (System.currentTimeMillis() > lastSizeCheck + Configuration.getInstance().getTileGCFrequencyInMillis()) {
                lastSizeCheck = System.currentTimeMillis();
                garbageCollector.gc();
            }
        } catch (SQLiteFullException ex) {
            //the drive is full! trigger the clean up operation
            //may want to consider reducing the trim size automagically
            Log.e(IMapView.LOGTAG, "SQLiteFullException while saving tile.", ex);
            garbageCollector.gc();
            catchException(ex);
        } catch (Exception ex) {
            //note, although we check for db null state at the beginning of this method, it's possible for the
            //db to be closed during the execution of this method
            Log.e(IMapView.LOGTAG, "Unable to store cached tile from " + pTileSourceInfo.name() + " " + MapTileIndex.toString(pMapTileIndex) + " db is " + (db == null ? "null" : "not null"), ex);
            Counters.fileCacheSaveErrors++;
            catchException(ex);
        } finally {
            try {
                bos.close();
            } catch (IOException e) {

            }
        }
        return false;
    }

    /**
     * Returns true if the given tile source and tile coordinates exist in the cache
     *
     * @since 5.6
     */
    public boolean exists(final String pTileSource, final long pMapTileIndex) {
        return 1 == getRowCount(primaryKey, getPrimaryKeyParameters(getIndex(pMapTileIndex), pTileSource));
    }

    /**
     * Returns true if the given tile source and tile coordinates exist in the cache
     *
     * @since 5.6
     */
    @Override
    public boolean exists(final ITileSource pTileSource, final long pMapTileIndex) {
        return exists(pTileSource.name(), pMapTileIndex);
    }

    /**
     * Now we use only one static instance of database, which should never be closed
     */
    @Override
    public void onDetach() {
    }

    /**
     * purges and deletes everything from the cache database
     *
     * @return
     * @since 5.6
     */
    public boolean purgeCache() {
        final SQLiteDatabase db = getDb();
        if (db != null && db.isOpen()) {
            try {
                db.delete(TABLE, null, null);
                return true;
            } catch (Exception e) {
                Log.w(IMapView.LOGTAG, "Error purging the db", e);
                catchException(e);
            }
        }
        return false;
    }

    /**
     * purges and deletes all tiles from the given tile source name from the cache database
     *
     * @return
     * @since 5.6.1
     */
    public boolean purgeCache(String mTileSourceName) {
        final SQLiteDatabase db = getDb();
        if (db != null && db.isOpen()) {
            try {
                db.delete(TABLE, COLUMN_PROVIDER + " = ?", new String[]{mTileSourceName});
                return true;
            } catch (Exception e) {
                Log.w(IMapView.LOGTAG, "Error purging the db", e);
                catchException(e);
            }
        }
        return false;
    }

    /**
     * a helper method to import file system stored map tiles into the sql tile cache
     * on successful import, the tiles are removed from the file system.
     * <p>
     * This can take a long time, so consider running this off of the main thread.
     *
     * @return
     */
    public int[] importFromFileCache(boolean removeFromFileSystem) {
        final SQLiteDatabase db = getDb();
        int[] ret = new int[]{0, 0, 0, 0};
        //inserts
        //insert failures
        //deletes
        //delete failures
        File tilePathBase = Configuration.getInstance().getOsmdroidTileCache();
        if (tilePathBase.exists()) {
            File[] tileSources = tilePathBase.listFiles();
            if (tileSources != null) {
                for (int i = 0; i < tileSources.length; i++) {
                    if (tileSources[i].isDirectory() && !tileSources[i].isHidden()) {
                        //proceed
                        File[] z = tileSources[i].listFiles();
                        if (z != null)
                            for (int zz = 0; zz < z.length; zz++) {
                                if (z[zz].isDirectory() && !z[zz].isHidden()) {
                                    File[] x = z[zz].listFiles();
                                    if (x != null)
                                        for (int xx = 0; xx < x.length; xx++) {
                                            if (x[xx].isDirectory() && !x[xx].isHidden()) {
                                                File[] y = x[xx].listFiles();
                                                if (x != null)
                                                    for (int yy = 0; yy < y.length; yy++) {
                                                        if (!y[yy].isHidden() && !y[yy].isDirectory()) {

                                                            try {
                                                                ContentValues cv = new ContentValues();
                                                                final long x1 = Long.parseLong(x[xx].getName());
                                                                final long y1 = Long.parseLong(y[yy].getName().substring(0, y[yy].getName().indexOf(".")));
                                                                final long z1 = Long.parseLong(z[zz].getName());
                                                                final long index = getIndex(x1, y1, z1);
                                                                cv.put(DatabaseFileArchive.COLUMN_PROVIDER, tileSources[i].getName());
                                                                if (!exists(tileSources[i].getName(), MapTileIndex.getTileIndex((int) z1, (int) x1, (int) y1))) {

                                                                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(y[yy]));

                                                                    List<Byte> list = new ArrayList<Byte>();
                                                                    //ByteArrayBuffer baf = new ByteArrayBuffer(500);
                                                                    int current = 0;
                                                                    while ((current = bis.read()) != -1) {
                                                                        list.add((byte) current);
                                                                    }

                                                                    byte[] bits = new byte[list.size()];
                                                                    for (int bi = 0; bi < list.size(); bi++) {
                                                                        bits[bi] = list.get(bi);
                                                                    }
                                                                    cv.put(DatabaseFileArchive.COLUMN_KEY, index);
                                                                    cv.put(DatabaseFileArchive.COLUMN_TILE, bits);

                                                                    long insert = db.insert(TABLE, null, cv);
                                                                    if (insert > 0) {
                                                                        if (Configuration.getInstance().isDebugMode())
                                                                            Log.d(IMapView.LOGTAG, "tile inserted " + tileSources[i].getName() + "/" + z1 + "/" + x1 + "/" + y1);
                                                                        ret[0]++;
                                                                        if (removeFromFileSystem) {
                                                                            try {
                                                                                y[yy].delete();
                                                                                ret[2]++;
                                                                                ;
                                                                            } catch (Exception ex) {
                                                                                ret[3]++;
                                                                                ;
                                                                            }
                                                                        }
                                                                    } else {
                                                                        Log.w(IMapView.LOGTAG, "tile NOT inserted " + tileSources[i].getName() + "/" + z1 + "/" + x1 + "/" + y1);
                                                                    }
                                                                }

                                                            } catch (Exception ex) {
                                                                //note, although we check for db null state at the beginning of this method, it's possible for the
                                                                //db to be closed during the execution of this method
                                                                Log.e(IMapView.LOGTAG, "Unable to store cached tile from " + tileSources[i].getName() + " db is " + (db == null ? "null" : "not null"), ex);
                                                                ret[1]++;
                                                                catchException(ex);
                                                            }
                                                        }
                                                    }
                                            }
                                            if (removeFromFileSystem) {
                                                //clean up the directories
                                                try {
                                                    x[xx].delete();
                                                } catch (Exception ex) {
                                                    Log.e(IMapView.LOGTAG, "Unable to delete directory from " + x[xx].getAbsolutePath(), ex);
                                                    ret[3]++;
                                                }
                                            }
                                        }
                                }
                                if (removeFromFileSystem) {
                                    //clean up the directories
                                    try {
                                        z[zz].delete();
                                    } catch (Exception ex) {
                                        Log.e(IMapView.LOGTAG, "Unable to delete directory from " + z[zz].getAbsolutePath(), ex);
                                        ret[3]++;
                                    }
                                }
                            }


                        if (removeFromFileSystem) {
                            //clean up the directories
                            try {
                                tileSources[i].delete();
                            } catch (Exception ex) {
                                Log.e(IMapView.LOGTAG, "Unable to delete directory from " + tileSources[i].getAbsolutePath(), ex);
                                ret[3]++;
                            }
                        }

                    } else {
                        //it's a file, nothing for us to do here
                    }
                }
            }

        }
        return ret;
    }


    /**
     * Removes a specific tile from the cache
     *
     * @since 5.6
     */
    @Override
    public boolean remove(final ITileSource pTileSourceInfo, final long pMapTileIndex) {
        final SQLiteDatabase db = getDb();
        if (db == null || !db.isOpen()) {
            Log.d(IMapView.LOGTAG, "Unable to delete cached tile from " + pTileSourceInfo.name() + " " + MapTileIndex.toString(pMapTileIndex) + ", database not available.");
            Counters.fileCacheSaveErrors++;
            return false;
        }
        try {
            final long index = getIndex(pMapTileIndex);
            db.delete(DatabaseFileArchive.TABLE, primaryKey, getPrimaryKeyParameters(index, pTileSourceInfo));
            return true;
        } catch (Exception ex) {
            //note, although we check for db null state at the beginning of this method, it's possible for the
            //db to be closed during the execution of this method
            Log.e(IMapView.LOGTAG, "Unable to delete cached tile from " + pTileSourceInfo.name() + " " + MapTileIndex.toString(pMapTileIndex) + " db is " + (db == null ? "null" : "not null"), ex);
            Counters.fileCacheSaveErrors++;
            catchException(ex);
        }
        return false;
    }

    /**
     * Returns the number of tiles in the cache for the specified tile source name
     *
     * @param tileSourceName
     * @return
     * @since 5.6
     */
    public long getRowCount(String tileSourceName) {
        if (tileSourceName == null) {
            return getRowCount(null, null);
        }
        return getRowCount(COLUMN_PROVIDER + "=?", new String[]{tileSourceName});
    }


    /**
     * Count cache tiles: helper method
     *
     * @return the number of tiles, or -1 if a problem occurred
     * @since 6.0.2
     */
    protected long getRowCount(final String pWhereClause, final String[] pWhereClauseArgs) {
        Cursor cursor = null;
        try {
            final SQLiteDatabase db = getDb();
            if (db == null || !db.isOpen()) {
                return -1;
            }
            cursor = db.rawQuery(
                    "select count(*) from " + TABLE
                            + (pWhereClause == null ? "" : " where " + pWhereClause), pWhereClauseArgs);
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } catch (Exception ex) {
            catchException(ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    /**
     * Count cache tiles
     *
     * @param pTileSourceName the tile source name (possibly null)
     * @param pZoom           the zoom level
     * @param pInclude        a collection of bounding boxes to include (possibly null/empty)
     * @param pExclude        a collection of bounding boxes to exclude (possibly null/empty)
     * @return the number of corresponding tiles in the cache
     * @since 6.0.2
     */
    public long getRowCount(final String pTileSourceName, final int pZoom,
                            final Collection<Rect> pInclude, final Collection<Rect> pExclude) {
        return getRowCount(
                getWhereClause(pZoom, pInclude, pExclude)
                        + (pTileSourceName != null ? " and " + COLUMN_PROVIDER + "=?" : "")
                , pTileSourceName != null ? new String[]{pTileSourceName} : null);
    }

    /**
     * Returns the size of the database file in bytes.
     */
    public long getSize() {
        return db_file.length();
    }

    /**
     * Returns the expiry time of the tile that expires first.
     */
    public long getFirstExpiry() {
        final SQLiteDatabase db = getDb();
        if (db == null || !db.isOpen()) {
            return 0;
        }
        try {
            Cursor cursor = db.rawQuery("select min(" + COLUMN_EXPIRES + ") from " + TABLE, null);
            cursor.moveToFirst();
            long time = cursor.getLong(0);
            cursor.close();
            return time;
        } catch (Exception ex) {
            Log.e(IMapView.LOGTAG, "Unable to query for oldest tile", ex);
            catchException(ex);
        }
        return 0;
    }

    /**
     * @return a composite key designed as a SQL primary key that includes X, Y and zoom
     * @see #extractXFromKeyInSQL(int)
     * @see #extractYFromKeyInSQL(int)
     * @since 5.6.5
     */
    public static long getIndex(final long pX, final long pY, final long pZ) {
        return ((pZ << pZ) + pX << pZ) + pY;
    }

    /**
     * @return the SQL formula to extract X from the table key for a given zoom level
     * @see #getIndex(long, long, long)
     * @since 6.0.2
     */
    protected static String extractXFromKeyInSQL(final int pZoom) {
        return "((" + COLUMN_KEY + ">>" + pZoom + ")%" + (1 << pZoom) + ")";
    }

    /**
     * @return the SQL formula to extract Y from the table key for a given zoom level
     * @see #getIndex(long, long, long)
     * @since 6.0.2
     */
    protected static String extractYFromKeyInSQL(final int pZoom) {
        return "(" + COLUMN_KEY + "%" + (1 << pZoom) + ")";
    }

    /**
     * Gets the single column index value for a map tile
     * Unluckily, "map tile index" and "sql pk" don't match
     *
     * @param pMapTileIndex
     * @since 5.6.5
     */
    public static long getIndex(final long pMapTileIndex) {
        return getIndex(MapTileIndex.getX(pMapTileIndex), MapTileIndex.getY(pMapTileIndex), MapTileIndex.getZoom(pMapTileIndex));
    }

    @Override
    public Long getExpirationTimestamp(final ITileSource pTileSource, final long pMapTileIndex) {
        Cursor cursor = null;
        try {
            cursor = getTileCursor(getPrimaryKeyParameters(getIndex(pMapTileIndex), pTileSource), expireQueryColumn);
            if (cursor.moveToNext()) {
                return cursor.getLong(0);
            }
        } catch (Exception ex) {
            Log.e(IMapView.LOGTAG, "error getting expiration date from the tile cache", ex);
            catchException(ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @since 5.6.5
     */
    private static final String primaryKey = DatabaseFileArchive.COLUMN_KEY + "=? and " + DatabaseFileArchive.COLUMN_PROVIDER + "=?";

    public static String getPrimaryKey() {
        return primaryKey;
    }

    /**
     * @param pIndex
     * @param pTileSourceInfo
     * @return
     * @since 5.6.5
     */
    public static String[] getPrimaryKeyParameters(final long pIndex, final ITileSource pTileSourceInfo) {
        return getPrimaryKeyParameters(pIndex, pTileSourceInfo.name());
    }

    /**
     * @param pIndex
     * @param pTileSourceInfo
     * @return
     * @since 5.6.5
     */
    public static String[] getPrimaryKeyParameters(final long pIndex, final String pTileSourceInfo) {
        return new String[]{String.valueOf(pIndex), pTileSourceInfo};
    }

    /**
     * @param pPrimaryKeyParameters
     * @param pColumns
     * @return
     * @since 5.6.5
     */
    public Cursor getTileCursor(final String[] pPrimaryKeyParameters, final String[] pColumns) {
        final SQLiteDatabase db = getDb();
        return db.query(DatabaseFileArchive.TABLE, pColumns, primaryKey, pPrimaryKeyParameters, null, null, null);
    }

    /**
     * For optimization reasons
     *
     * @since 5.6.5
     */
    private static final String[] queryColumns = {DatabaseFileArchive.COLUMN_TILE, SqlTileWriter.COLUMN_EXPIRES};

    /**
     * For optimization reasons
     *
     * @since 5.6.5
     */
    private static final String[] expireQueryColumn = {SqlTileWriter.COLUMN_EXPIRES};

    @Override
    public Drawable loadTile(final ITileSource pTileSource, final long pMapTileIndex) throws Exception {
        byte[] bits = null;
        long expirationTimestamp = 0;
        Cursor cur = null;
        try {
            final long index = getIndex(pMapTileIndex);
            cur = getTileCursor(getPrimaryKeyParameters(index, pTileSource), queryColumns);
            if (cur.moveToFirst()) {
                bits = cur.getBlob(0);
                expirationTimestamp = cur.getLong(1);
            }
            if (bits == null) {
                if (Configuration.getInstance().isDebugMode()) {
                    Log.d(IMapView.LOGTAG, "SqlCache - Tile doesn't exist: " + pTileSource.name() + MapTileIndex.toString(pMapTileIndex));
                }
                return null;
            }
        } catch (Exception ex) {
            catchException(ex);
            throw ex;
        } finally {
            if (cur != null) {
                cur.close();
            }
        }

        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(bits);
            final Drawable drawable = pTileSource.getDrawable(inputStream);
            // Check to see if file has expired
            final long now = System.currentTimeMillis();
            final boolean fileExpired = expirationTimestamp < now;

            if (fileExpired && drawable != null) {
                if (Configuration.getInstance().isDebugMode()) {
                    Log.d(IMapView.LOGTAG, "Tile expired: " + pTileSource.name() + MapTileIndex.toString(pMapTileIndex));
                }
                ExpirableBitmapDrawable.setState(drawable, ExpirableBitmapDrawable.EXPIRED);
            }
            return drawable;
        } finally {
            if (inputStream != null) {
                StreamUtils.closeStream(inputStream);
            }
        }
    }

    /**
     * @param pToBeDeleted      Amount of bytes to delete (as tile blob size)
     * @param pBulkSize         Number of tiles to delete in bulk
     * @param pPauseMillis      Pause between bulk actions, in order not to play it not aggressive on the CPU
     * @param pIncludeUnexpired Should we also delete tiles that are not expired?
     * @since 6.0.2
     */
    public void runCleanupOperation(final long pToBeDeleted, final int pBulkSize,
                                    final long pPauseMillis, final boolean pIncludeUnexpired) {
        long diff = pToBeDeleted;
        final StringBuilder where = new StringBuilder();
        String sep;
        boolean first = true;
        final SQLiteDatabase db = getDb();
        while (diff > 0) {
            if (first) {
                first = false;
            } else {
                if (pPauseMillis > 0) {
                    try {
                        Thread.sleep(pPauseMillis);
                    } catch (InterruptedException e) {
                        //
                    }
                }
            }
            final long now = System.currentTimeMillis();
            final Cursor cur;
            try {
                cur = db.rawQuery(
                        "SELECT " + COLUMN_KEY + ",LENGTH(HEX(" + COLUMN_TILE + "))/2 " +
                                "FROM " + DatabaseFileArchive.TABLE + " " +
                                "WHERE " +
                                COLUMN_EXPIRES + " IS NOT NULL " +
                                (pIncludeUnexpired ? "" : "AND " + COLUMN_EXPIRES + " < " + now + " ") +
                                "ORDER BY " + COLUMN_EXPIRES + " ASC " +
                                "LIMIT " + pBulkSize, null);
            } catch (Exception e) {
                catchException(e);
                return;
            }
            cur.moveToFirst();
            where.setLength(0);
            where.append(COLUMN_KEY + " in (");
            sep = "";
            while (!cur.isAfterLast()) {
                final long key = cur.getLong(0);
                final long size = cur.getLong(1);
                cur.moveToNext();

                where.append(sep).append(key);
                sep = ",";
                diff -= size;
                if (diff <= 0) { // we already have enough tiles to delete
                    break;
                }
            }
            cur.close();
            if ("".equals(sep)) { // nothing to delete
                return;
            }
            where.append(')');
            try {
                db.delete(TABLE, where.toString(), null);
            } catch (SQLiteFullException e) {
                Log.e(IMapView.LOGTAG, "SQLiteFullException while cleanup.", e);
                catchException(e);
            } catch (Exception e) {
                catchException(e);
                return;
            }
        }
    }

    /**
     * @since 6.0.2
     */
    protected SQLiteDatabase getDb() {
        if (mDb != null) {
            return mDb;
        }
        synchronized (mLock) {
            Configuration.getInstance().getOsmdroidTileCache().mkdirs();
            db_file = new File(Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + File.separator + DATABASE_FILENAME);
            if (mDb == null) {
                try {
                    mDb = SQLiteDatabase.openOrCreateDatabase(db_file, null);
                    mDb.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " (" + DatabaseFileArchive.COLUMN_KEY + " INTEGER , " + DatabaseFileArchive.COLUMN_PROVIDER + " TEXT, " + DatabaseFileArchive.COLUMN_TILE + " BLOB, " + COLUMN_EXPIRES + " INTEGER, PRIMARY KEY (" + DatabaseFileArchive.COLUMN_KEY + ", " + DatabaseFileArchive.COLUMN_PROVIDER + "));");
                } catch (Exception ex) {
                    Log.e(IMapView.LOGTAG, "Unable to start the sqlite tile writer. Check external storage availability.", ex);
                    catchException(ex);
                    return null;
                }
            }
        }
        return mDb;
    }

    /**
     * @since 6.0.2
     */
    public void refreshDb() {
        synchronized (mLock) {
            if (mDb != null) {
                mDb.close();
                mDb = null;
            }
        }
    }

    /**
     * @since 6.0.2
     */
    protected void catchException(final Exception pException) {
        if (pException instanceof SQLiteException) {
            if (!isFunctionalException((SQLiteException) pException)) {
                refreshDb();
            }
        }
    }

    /**
     * @return true if it's a mere functional exception (poor SQL code for instance)
     * and false if it's something potentially more serious (no more SQLite database for instance)
     * @since 6.0.2
     */
    public static boolean isFunctionalException(final SQLiteException pSQLiteException) {
        switch (pSQLiteException.getClass().getSimpleName()) {
            case "SQLiteBindOrColumnIndexOutOfRangeException":
            case "SQLiteBlobTooBigException":
            case "SQLiteConstraintException":
            case "SQLiteDatatypeMismatchException":
            case "SQLiteFullException":
            case "SQLiteMisuseException":
            case "SQLiteTableLockedException":
                return true;
            case "SQLiteAbortException":
            case "SQLiteAccessPermException":
            case "SQLiteCantOpenDatabaseException":
            case "SQLiteDatabaseCorruptException":
            case "SQLiteDatabaseLockedException":
            case "SQLiteDiskIOException":
            case "SQLiteDoneException":
            case "SQLiteOutOfMemoryException":
            case "SQLiteReadOnlyDatabaseException":
                return false;
            default:
                return false;
        }
    }

    /**
     * @since 6.0.2
     */
    private void createIndex(final SQLiteDatabase pDb) {
        pDb.execSQL("CREATE INDEX IF NOT EXISTS " + COLUMN_EXPIRES_INDEX + " ON " + TABLE + " (" + COLUMN_EXPIRES + ");");
    }

    /**
     * @since 6.0.2
     */
    @Override
    public void runDuringSplashScreen() {
        final SQLiteDatabase db = getDb();
        createIndex(db);
    }

    /**
     * @return the part of a SQL where clause used to restrict the selected tiles to
     * - a zoom
     * - a bounding box (possibly null)
     * @since 6.0.2
     */
    protected StringBuilder getWhereClause(final int pZoom, final Rect pRect) {
        final long maxValueForZoom = -1 + (1 << (pZoom + 1));
        final long firstIndexForZoom = getIndex(0, 0, pZoom);
        final long lastIndexForZoom = getIndex(maxValueForZoom, maxValueForZoom, pZoom);
        final String xForZoom = extractXFromKeyInSQL(pZoom);
        final String yForZoom = extractYFromKeyInSQL(pZoom);

        final StringBuilder buffer = new StringBuilder();
        buffer.append('(');
        buffer.append(COLUMN_KEY).append(" between ")
                .append(firstIndexForZoom).append(" and ").append(lastIndexForZoom);
        if (pRect != null) {
            buffer.append(" and ");
            if (pRect.left == pRect.right) {
                buffer.append(xForZoom).append("=").append(pRect.left);
            } else {
                buffer.append("(")
                        .append(xForZoom).append(">=").append(pRect.left)
                        .append(pRect.left < pRect.right ? " and " : " or ")
                        .append(xForZoom).append("<=").append(pRect.right)
                        .append(")");
            }
            buffer.append(" and ");
            if (pRect.top == pRect.bottom) {
                buffer.append(yForZoom).append("=").append(pRect.top);
            } else {
                buffer.append("(")
                        .append(yForZoom).append(">=").append(pRect.top)
                        .append(pRect.top < pRect.bottom ? " and " : " or ")
                        .append(yForZoom).append("<=").append(pRect.bottom)
                        .append(")");
            }
        }
        buffer.append(')');
        return buffer;
    }

    /**
     * @return the part of a SQL where clause used to restrict the selected tiles to
     * - a zoom
     * - a collection of bounding boxes to include (possibly null/empty)
     * - a collection of bounding boxes to exclude (possibly null/empty)
     * @since 6.0.2
     */
    protected StringBuilder getWhereClause(final int pZoom,
                                           final Collection<Rect> pInclude,
                                           final Collection<Rect> pExclude) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('(');
        buffer.append(getWhereClause(pZoom, null));
        if (pInclude != null && pInclude.size() > 0) {
            buffer.append(" and (");
            String coordinator = "";
            for (final Rect rect : pInclude) {
                buffer.append(coordinator).append('(').append(getWhereClause(pZoom, rect)).append(')');
                coordinator = " or ";
            }
            buffer.append(")");
        }
        if (pExclude != null && pExclude.size() > 0) {
            buffer.append(" and not(");
            String coordinator = "";
            for (final Rect rect : pExclude) {
                buffer.append(coordinator).append('(').append(getWhereClause(pZoom, rect)).append(')');
                coordinator = " or ";
            }
            buffer.append(")");
        }
        buffer.append(')');
        return buffer;
    }

    /**
     * Delete cache tiles
     *
     * @param pTileSourceName the tile source name (possibly null)
     * @param pZoom           the zoom level
     * @param pInclude        a collection of bounding boxes to include (possibly null/empty)
     * @param pExclude        a collection of bounding boxes to exclude (possibly null/empty)
     * @return the number of corresponding tiles deleted from the cache, or -1 if a problem occurred
     * @since 6.0.2
     */
    public long delete(final String pTileSourceName, final int pZoom,
                       final Collection<Rect> pInclude, final Collection<Rect> pExclude) {
        try {
            final SQLiteDatabase db = getDb();
            if (db == null || !db.isOpen()) {
                return -1;
            }
            return db.delete(TABLE,
                    getWhereClause(pZoom, pInclude, pExclude)
                            + (pTileSourceName != null ? " and " + COLUMN_PROVIDER + "=?" : ""),
                    pTileSourceName != null ? new String[]{pTileSourceName} : null);
        } catch (Exception ex) {
            catchException(ex);
            return 0;
        }
    }
}
