package org.osmdroid.tileprovider.modules;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.osmdroid.tileprovider.modules.DatabaseFileArchive.TABLE;

/**
 * An implementation of {@link IFilesystemCache} based on the original TileWriter. It writes tiles to a sqlite database.
 * It does NOT support expiration and provides more of a MOBAC like functionality (non-expiring file archives).
 * Uses the same schema as MOBAC osm sqlite and the {@link DatabaseFileArchive}
 * <p>
 * https://github.com/osmdroid/osmdroid/issues/348
 * @see SqlTileWriter
 * @see DatabaseFileArchive
 * @author Alex O'Ree
 * @since 5.2 7/8/16.
 */
public class SqliteArchiveTileWriter implements IFilesystemCache {
    final File db_file;
    final SQLiteDatabase db;
    final int questimate = 8000;
    static boolean hasInited = false;

    public SqliteArchiveTileWriter(String outputFile) throws Exception {
        // do this in the background because it takes a long time
        db_file = new File(outputFile);
        try {
            db = SQLiteDatabase.openOrCreateDatabase(db_file.getAbsolutePath(), null);
        } catch (Exception ex) {
            throw new Exception("Trouble creating database file at " + outputFile, ex);
        }
        try {

            db.execSQL("CREATE TABLE IF NOT EXISTS " + DatabaseFileArchive.TABLE + " (key INTEGER , provider TEXT, tile BLOB, PRIMARY KEY (key, provider));");
        } catch (Throwable t) {
            t.printStackTrace();
            Log.d(IMapView.LOGTAG, "error setting db schema, it probably exists already", t);
            // throw new IOException("Trouble creating database file"+ t.getMessage());
        }
    }

    @Override
    public boolean saveFile(ITileSource pTileSourceInfo, MapTile pTile, InputStream pStream) {
        try {
            ContentValues cv = new ContentValues();
            final long x = (long) pTile.getX();
            final long y = (long) pTile.getY();
            final long z = (long) pTile.getZoomLevel();
            final long index = ((z << z) + x << z) + y;
            cv.put(DatabaseFileArchive.COLUMN_PROVIDER, pTileSourceInfo.name());
            BufferedInputStream bis = new BufferedInputStream(pStream);

            List<Byte> list = new ArrayList<Byte>();
            //ByteArrayBuffer baf = new ByteArrayBuffer(500);
            int current = 0;
            while ((current = bis.read()) != -1) {
                list.add((byte) current);
            }

            byte[] bits = new byte[list.size()];
            for (int i = 0; i < list.size(); i++) bits[i] = list.get(i);
            cv.put(DatabaseFileArchive.COLUMN_KEY, index);
            cv.put(DatabaseFileArchive.COLUMN_TILE, bits);
            //this shouldn't happen, but just in case
            db.insert(DatabaseFileArchive.TABLE, null, cv);
            if (Configuration.getInstance().isDebugMode())
                Log.d(IMapView.LOGTAG, "tile inserted " + pTileSourceInfo.name() + pTile.toString());
        } catch (Throwable ex) {
            Log.e(IMapView.LOGTAG, "Unable to store cached tile from " + pTileSourceInfo.name() + " " + pTile.toString(), ex);
        }
        return false;
    }


    @Override
    public boolean exists(ITileSource pTileSource, MapTile pTile) {
        try {
            final String[] tile = {DatabaseFileArchive.COLUMN_TILE};
            final long x = (long) pTile.getX();
            final long y = (long) pTile.getY();
            final long z = (long) pTile.getZoomLevel();
            final long index = ((z << z) + x << z) + y;
            final Cursor cur = db.query(TABLE, tile, DatabaseFileArchive.COLUMN_KEY + " = " + index + " and " + DatabaseFileArchive.COLUMN_PROVIDER + " = '" + pTileSource.name() + "'", null, null, null, null);

            if (cur.getCount() != 0) {
                cur.close();
                return true;
            }
            cur.close();
        } catch (Throwable ex) {
            Log.e(IMapView.LOGTAG, "Unable to store cached tile from " + pTileSource.name() + " " + pTile.toString(), ex);
        }
        return false;
    }

    @Override
    public void onDetach() {
        if (db != null)
            db.close();
    }

    @Override
    public boolean remove(ITileSource tileSource, MapTile tile) {
        //not supported
        return false;
    }
}
