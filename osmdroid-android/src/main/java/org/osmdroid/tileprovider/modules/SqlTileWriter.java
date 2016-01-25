package org.osmdroid.tileprovider.modules;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An implementation of {@link IFilesystemCache} based on the original TileWriter. It writes tiles to a sqlite database cache.
 * It supports expiration timestamps if provided by the server from which the tile was downloaded. Trimming
 * of expired
 *
 * If the database exceeds {@link OpenStreetMapTileProviderConstants#TILE_TRIM_CACHE_SIZE_BYTES}
 * cache exceeds 600 Mb then it will be trimmed to 500 Mb by deleting files that expire first.
 *
 * @author Alex O'Ree
 * @since 5.1
 */
public class SqlTileWriter implements IFilesystemCache {
    final File db_file;
    final SQLiteDatabase db;
    final int questimate=8000;

    public SqlTileWriter() {
        // do this in the background because it takes a long time
        db_file = new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE.getAbsolutePath() + File.separator + "cache.db");
        db = SQLiteDatabase.openOrCreateDatabase(db_file, null);
        try {
            db.execSQL("CREATE TABLE " + DatabaseFileArchive.TABLE+ " (key INTEGER , provider TEXT, tile BLOB, expires INTEGER, PRIMARY KEY (key, provider));");
        }
        catch (Throwable t){

            t.printStackTrace();
        }
        final Thread t = new Thread() {
            @Override
            public void run() {

                //run the reaper (remove all old expired tiles)
                //keep if now is < expiration date
                //delete if now is > expiration date
                long now=System.currentTimeMillis();
                int rows = db.delete(DatabaseFileArchive.TABLE, "expires < ?", new String[]{System.currentTimeMillis() + ""});
                Log.d(IMapView.LOGTAG, "Local storage cahce purged " + rows + " expired tiles in " + (System.currentTimeMillis()-now) + "ms, cache size is " + db_file.length() + "bytes");

                //VACUUM the database
                now=System.currentTimeMillis();
                //db.execSQL("VACUUM " + DatabaseFileArchive.TABLE + ";");
               // Log.d(IMapView.LOGTAG, "VACUUM completed in " + (System.currentTimeMillis()-now) + "ms, cache size is " + db_file.length() + "bytes");
                if (db_file.length() > OpenStreetMapTileProviderConstants.TILE_MAX_CACHE_SIZE_BYTES){
                    long diff=OpenStreetMapTileProviderConstants.TILE_MAX_CACHE_SIZE_BYTES-db_file.length();
                    long tilesToKill=diff/questimate;
                    try {
                        db.execSQL("DELETE FROM " + DatabaseFileArchive.TABLE+ " ORDER BY expires DESC LIMIT " + tilesToKill);
                    }
                    catch (Throwable t){
                        t.printStackTrace();
                    }
                    Log.d(IMapView.LOGTAG, "purge completed in " + (System.currentTimeMillis()-now) + "ms, cache size is " + db_file.length() + "bytes");
                }

                if (OpenStreetMapTileProviderConstants.DEBUGMODE) {
                    Log.d(IMapView.LOGTAG, "Finished init thread");
                }
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    @Override
    public boolean saveFile(ITileSource pTileSourceInfo, MapTile pTile, InputStream pStream) {
        try {
            ContentValues cv = new ContentValues();
            final long x = (long) pTile.getX();
            final long y = (long) pTile.getY();
            final long z = (long) pTile.getZoomLevel();
            final long index = ((z << z) + x << z) + y;
            cv.put("provider", pTileSourceInfo.name());
            BufferedInputStream bis = new BufferedInputStream(pStream);

            List<Byte> list = new ArrayList<Byte>();
            //ByteArrayBuffer baf = new ByteArrayBuffer(500);
            int current = 0;
            while ((current = bis.read()) != -1) {
                list.add((byte) current);
            }

            byte[] bits = new byte[list.size()];
            for (int i = 0; i < list.size(); i++) bits[i] = list.get(i);
            cv.put("key", index);
            cv.put("tile", bits);
            //this shouldn't happen, but just in case
            if (pTile.getExpires() != null)
                cv.put("expires", pTile.getExpires().getTime());
            db.delete(DatabaseFileArchive.TABLE, "key=? and provider=?", new String[]{index+"",pTileSourceInfo.name()});
            db.insert(DatabaseFileArchive.TABLE, null, cv);
            Log.d(IMapView.LOGTAG, "tile inserted " + pTileSourceInfo.name() +  pTile.toString());
        } catch (Throwable ex) {
            Log.e(IMapView.LOGTAG, "Unable to store cached tile from " + pTileSourceInfo.name() + " " + pTile.toString(), ex);
        }
        return false;
    }
}
