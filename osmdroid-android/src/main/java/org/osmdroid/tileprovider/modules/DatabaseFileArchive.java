package org.osmdroid.tileprovider.modules;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class DatabaseFileArchive implements IArchiveFile {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseFileArchive.class);

	private final SQLiteDatabase mDatabase;

	private DatabaseFileArchive(final SQLiteDatabase pDatabase) {
		mDatabase = pDatabase;
	}

	public static DatabaseFileArchive getDatabaseFileArchive(final File pFile) throws SQLiteException {
		return new DatabaseFileArchive(SQLiteDatabase.openOrCreateDatabase(pFile, null));
	}

	@Override
	public InputStream getInputStream(final ITileSource pTileSource, final MapTile pTile) {
		try {
			InputStream ret = null;
			final String[] tile = {"tile"};
			final long x = (long) pTile.getX();
			final long y = (long) pTile.getY();
			final long z = (long) pTile.getZoomLevel();
			final long index = ((z << z) + x << z) + y;
			final Cursor cur = mDatabase.query("tiles", tile, "key = " + index + " and provider = '" + pTileSource.name() + "'", null, null, null, null);
			if(cur.getCount() != 0) {
				cur.moveToFirst();
				ret = new ByteArrayInputStream(cur.getBlob(0));
			}
			cur.close();
			if(ret != null) {
				return ret;
			}
		} catch(final Throwable e) {
			logger.warn("Error getting db stream: " + pTile, e);
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
