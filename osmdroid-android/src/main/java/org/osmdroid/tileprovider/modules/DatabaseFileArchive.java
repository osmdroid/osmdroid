package org.osmdroid.tileprovider.modules;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import org.osmdroid.api.IMapView;

/**
 * This is the OSMdroid style database provider. It's an extremely simply sqlite database schema.
 * CREATE TABLE tiles (key INTEGER PRIMARY KEY, provider TEXT, tile BLOB)
 * where the key is the X/Y/Z coordinates bitshifted using the following algorithm
 * key = ((z << z) + x << z) + y;
 */
public class DatabaseFileArchive implements IArchiveFile {

	public static final String TABLE="tiles";
	private SQLiteDatabase mDatabase;

	public DatabaseFileArchive(){}

	private DatabaseFileArchive(final SQLiteDatabase pDatabase) {
		mDatabase = pDatabase;
	}

	public static DatabaseFileArchive getDatabaseFileArchive(final File pFile) throws SQLiteException {
		//return new DatabaseFileArchive(SQLiteDatabase.openOrCreateDatabase(pFile, null));
		return new DatabaseFileArchive(SQLiteDatabase.openDatabase(pFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY));

	}

	public Set<String> getTileSources(){
		Set<String> ret = new HashSet<String>();
		try {
			final String[] tile = {"provider"};
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
		mDatabase=SQLiteDatabase.openDatabase(pFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
	}

	public byte[] getImage(final ITileSource pTileSource, final MapTile pTile) {

		try {
			byte[] bits=null;
			final String[] tile = {"tile"};
			final long x = (long) pTile.getX();
			final long y = (long) pTile.getY();
			final long z = (long) pTile.getZoomLevel();
			final long index = ((z << z) + x << z) + y;
			final Cursor cur = mDatabase.query(TABLE, tile, "key = " + index + " and provider = '" + pTileSource.name() + "'", null, null, null, null);

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
