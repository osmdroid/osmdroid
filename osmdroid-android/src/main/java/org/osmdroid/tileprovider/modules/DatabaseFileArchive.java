package org.osmdroid.tileprovider.modules;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.MapTileIndex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * This is the OSMdroid style database provider. It's an extremely simply sqlite database schema.
 * CREATE TABLE tiles (key INTEGER PRIMARY KEY, provider TEXT, tile BLOB)
 * where the key is the X/Y/Z coordinates bitshifted using the following algorithm
 * key = ((z &lt;&lt; z) + x &lt;&lt; z) + y;
 *
 * @see SqlTileWriter
 */
public class DatabaseFileArchive implements IArchiveFile {

    public static final String TABLE = "tiles";
    public static final String COLUMN_PROVIDER = "provider";
    public static final String COLUMN_TILE = "tile";
    public static final String COLUMN_KEY = "key";
    static final String[] tile_column = {"tile"};
    private SQLiteDatabase mDatabase;
    private boolean mIgnoreTileSource = false;

    public DatabaseFileArchive() {
    }

    private DatabaseFileArchive(final SQLiteDatabase pDatabase) {
        mDatabase = pDatabase;
    }

    public static DatabaseFileArchive getDatabaseFileArchive(final File pFile) throws SQLiteException {
        return new DatabaseFileArchive(SQLiteDatabase.openDatabase(pFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY & SQLiteDatabase.NO_LOCALIZED_COLLATORS));

    }

    /**
     * @since 6.0
     * If set to true, tiles from this archive will be loaded regardless of their associated tile source name
     */
    public void setIgnoreTileSource(boolean pIgnoreTileSource) {
        mIgnoreTileSource = pIgnoreTileSource;
    }

    public Set<String> getTileSources() {
        Set<String> ret = new HashSet<String>();
        try {
            final Cursor cur = mDatabase.rawQuery("SELECT distinct provider FROM " + TABLE, null);
            while (cur.moveToNext()) {
                ret.add(cur.getString(0));
            }
            cur.close();
        } catch (final Exception e) {
            Log.w(IMapView.LOGTAG, "Error getting tile sources: ", e);
        }
        return ret;
    }

    @Override
    public void init(File pFile) throws Exception {
        mDatabase = SQLiteDatabase.openDatabase(pFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
    }

    public byte[] getImage(final ITileSource pTileSource, final long pMapTileIndex) {

        if (mDatabase == null || !mDatabase.isOpen()) {
            if (Configuration.getInstance().isDebugTileProviders())
                Log.d(IMapView.LOGTAG, "Skipping DatabaseFileArchive lookup, database is closed");
            return null;
        }
        try {
            byte[] bits = null;
            final String[] tile = {COLUMN_TILE};
            final long x = MapTileIndex.getX(pMapTileIndex);
            final long y = MapTileIndex.getY(pMapTileIndex);
            final long z = MapTileIndex.getZoom(pMapTileIndex);
            final long index = ((z << z) + x << z) + y;

            Cursor cur;
            if (!mIgnoreTileSource) {
                cur = mDatabase.query(TABLE, tile, COLUMN_KEY + " = " + index + " and "
                        + COLUMN_PROVIDER + " = ?", new String[]{pTileSource.name()}, null, null, null);
            } else {
                cur = mDatabase.query(TABLE, tile, COLUMN_KEY + " = " + index, null, null, null, null);
            }

            if (cur.getCount() != 0) {
                cur.moveToFirst();
                bits = (cur.getBlob(0));
            }
            cur.close();
            if (bits != null) {
                return bits;
            }
        } catch (final Throwable e) {
            Log.w(IMapView.LOGTAG, "Error getting db stream: " + MapTileIndex.toString(pMapTileIndex), e);
        }

        return null;
    }

    @Override
    public InputStream getInputStream(final ITileSource pTileSource, final long pMapTileIndex) {
        try {
            InputStream ret = null;
            byte[] bits = getImage(pTileSource, pMapTileIndex);
            if (bits != null)
                ret = new ByteArrayInputStream(bits);
            if (ret != null) {
                return ret;
            }
        } catch (final Throwable e) {
            Log.w(IMapView.LOGTAG, "Error getting db stream: " + MapTileIndex.toString(pMapTileIndex), e);
        }
        return null;
    }

    @Override
    public void close() {
        mDatabase.close();
    }

    @Override
    public String toString() {
        return "DatabaseFileArchive [mDatabase=" + mDatabase.getPath() + "]";
    }

}
