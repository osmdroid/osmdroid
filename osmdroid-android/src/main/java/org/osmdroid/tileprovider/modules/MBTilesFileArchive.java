package org.osmdroid.tileprovider.modules;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.MapTileIndex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

/**
 * supports raster imagery in the MBTiles 1.1 spec
 * https://sourceforge.net/p/mobac/code/HEAD/tree/trunk/MOBAC/src/main/java/mobac/program/atlascreators/MBTiles.java
 * https://github.com/mapbox/mbtiles-spec/tree/master/1.1
 *
 * @author neilboyd circa 2011
 */
public class MBTilesFileArchive implements IArchiveFile {

    private SQLiteDatabase mDatabase;

    public MBTilesFileArchive() {
    }

    //	TABLE tiles (zoom_level INTEGER, tile_column INTEGER, tile_row INTEGER, tile_data BLOB);
    public final static String TABLE_TILES = "tiles";
    public final static String COL_TILES_ZOOM_LEVEL = "zoom_level";
    public final static String COL_TILES_TILE_COLUMN = "tile_column";
    public final static String COL_TILES_TILE_ROW = "tile_row";
    public final static String COL_TILES_TILE_DATA = "tile_data";

    private MBTilesFileArchive(final SQLiteDatabase pDatabase) {
        mDatabase = pDatabase;
    }

    public static MBTilesFileArchive getDatabaseFileArchive(final File pFile) throws SQLiteException {
        return new MBTilesFileArchive(
                SQLiteDatabase.openDatabase(
                        pFile.getAbsolutePath(),
                        null,
                        SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY));
    }

    @Override
    public void init(File pFile) throws Exception {
        mDatabase = SQLiteDatabase.openDatabase(
                pFile.getAbsolutePath(),
                null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public InputStream getInputStream(final ITileSource pTileSource, final long pMapTileIndex) {
        try {
            InputStream ret = null;
            final String[] tile = {COL_TILES_TILE_DATA};
            final String[] xyz = {
                    Integer.toString(MapTileIndex.getX(pMapTileIndex))
                    , Double.toString(Math.pow(2, MapTileIndex.getZoom(pMapTileIndex)) - MapTileIndex.getY(pMapTileIndex) - 1)  // Use Google Tiling Spec
                    , Integer.toString(MapTileIndex.getZoom(pMapTileIndex))
            };

            final Cursor cur = mDatabase.query(TABLE_TILES, tile, "tile_column=? and tile_row=? and zoom_level=?", xyz, null, null, null);

            if (cur.getCount() != 0) {
                cur.moveToFirst();
                ret = new ByteArrayInputStream(cur.getBlob(0));
            }
            cur.close();
            if (ret != null) {
                return ret;
            }
        } catch (final Throwable e) {
            Log.w(IMapView.LOGTAG, "Error getting db stream: " + MapTileIndex.toString(pMapTileIndex), e);
        }

        return null;
    }

    public Set<String> getTileSources() {
        //the MBTiles spec doesn't store source information in it, so we can't return anything
        return Collections.EMPTY_SET;
    }

    @Override
    public void setIgnoreTileSource(boolean pIgnoreTileSource) {

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
