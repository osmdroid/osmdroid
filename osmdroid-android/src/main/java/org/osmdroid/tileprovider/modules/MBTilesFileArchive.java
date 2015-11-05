package org.osmdroid.tileprovider.modules;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import org.osmdroid.api.IMapView;

public class MBTilesFileArchive implements IArchiveFile {

	private SQLiteDatabase mDatabase;

	public MBTilesFileArchive(){}

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
		mDatabase=SQLiteDatabase.openDatabase(
				pFile.getAbsolutePath(),
				null,
				SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
	}

	@Override
	public InputStream getInputStream(final ITileSource pTileSource, final MapTile pTile) {
		try {
			InputStream ret = null;
			final String[] tile = { COL_TILES_TILE_DATA };
			final String[] xyz = {
					  Integer.toString(pTile.getX())
					, Double.toString(Math.pow(2, pTile.getZoomLevel()) - pTile.getY() - 1)  // Use Google Tiling Spec
					, Integer.toString(pTile.getZoomLevel())
			};

			final Cursor cur = mDatabase.query(TABLE_TILES, tile, "tile_column=? and tile_row=? and zoom_level=?", xyz, null, null, null);

			if(cur.getCount() != 0) {
				cur.moveToFirst();
				ret = new ByteArrayInputStream(cur.getBlob(0));
			}
			cur.close();
			if(ret != null) {
				return ret;
			}
		} catch(final Throwable e) {
               Log.w(IMapView.LOGTAG,"Error getting db stream: " + pTile, e);
		}

		return null;
	}

	public Set<String> getTileSources(){
		//the MBTiles spec doesn't store source information in it, so we can't return anything
		return Collections.EMPTY_SET;
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
