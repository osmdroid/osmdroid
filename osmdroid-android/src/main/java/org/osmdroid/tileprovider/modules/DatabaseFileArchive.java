package org.osmdroid.tileprovider.modules;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;

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
 */
public class DatabaseFileArchive implements IArchiveFile {

	public static final String TABLE="tiles";
	public static final String COLUMN_PROVIDER = "provider";
	public static final String COLUMN_TILE = "tile";
	public static final String COLUMN_KEY = "key";
	static final String[] tile_column = {"tile"};
	private SQLiteDatabase mDatabase;

	public DatabaseFileArchive(){}

	private DatabaseFileArchive(final SQLiteDatabase pDatabase) {
		mDatabase = pDatabase;
	}

	public static DatabaseFileArchive getDatabaseFileArchive(final File pFile) throws SQLiteException {
		return new DatabaseFileArchive(SQLiteDatabase.openDatabase(pFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY & SQLiteDatabase.NO_LOCALIZED_COLLATORS));

	}

	public Set<String> getTileSources(){
		Set<String> ret = new HashSet<String>();
		try {
			final Cursor cur = mDatabase.rawQuery("SELECT distinct provider FROM " + TABLE, null);
			while(cur.moveToNext()) {
				ret.add(cur.getString(0));
			}
			cur.close();
		} catch (final Exception e) {
			Log.w(IMapView.LOGTAG,"Error getting tile sources: ", e);
		}
		return ret;
	}

	@Override
	public void init(File pFile) throws Exception {
		mDatabase=SQLiteDatabase.openDatabase(pFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	}

	public byte[] getImage(final ITileSource pTileSource, final MapTile pTile) {

		try {
			byte[] bits=null;
			final String[] tile = {COLUMN_TILE};
			final long x = (long) pTile.getX();
			final long y = (long) pTile.getY();
			final long z = (long) pTile.getZoomLevel();
			final long index = ((z << z) + x << z) + y;
			final Cursor cur = mDatabase.query(TABLE, tile, COLUMN_KEY+" = " + index + " and "+COLUMN_PROVIDER+" = '" + pTileSource.name() + "'", null, null, null, null);

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

	@Override
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

	@Override
	public void close() {
		mDatabase.close();
	}

	@Override
	public String toString() {
		return "DatabaseFileArchive [mDatabase=" + mDatabase.getPath() + "]";
	}

}
