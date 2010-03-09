package org.andnav.osm.services.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.andnav.osm.exceptions.EmptyCacheException;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * The OpenStreetMapTileProviderDataBase contains a table with info for
 * the available tiles in the file system cache.
 */
class OpenStreetMapTileProviderDataBase implements OpenStreetMapViewConstants {

    final static String DEBUGTAG = "OSM_DATABASE";

	private static final String DATABASE_NAME = "osmaptilefscache_db";
	private static final int DATABASE_VERSION = 4;

	private static final String T_FSCACHE = "t_fscache";	
	private static final String T_FSCACHE_RENDERER_ID = "rendererID";
	private static final String T_FSCACHE_ZOOM_LEVEL = "zoomLevel";
	private static final String T_FSCACHE_TILE_X = "tileX";
	private static final String T_FSCACHE_TILE_Y = "tileY";
	private static final String T_FSCACHE_LINK = "link";			// TODO store link (multiple use for similar tiles)
	private static final String T_FSCACHE_TIMESTAMP = "timestamp";
	private static final String T_FSCACHE_USAGECOUNT = "countused";
	private static final String T_FSCACHE_FILESIZE = "filesize";

	// TODO remove this after some time
	private static final String T_RENDERER = "t_renderer";
	
	private static final String T_FSCACHE_CREATE_COMMAND = "CREATE TABLE IF NOT EXISTS " + T_FSCACHE
	+ " (" 
	+ T_FSCACHE_RENDERER_ID + " INTEGER NOT NULL,"
	+ T_FSCACHE_ZOOM_LEVEL + " INTEGER NOT NULL,"
	+ T_FSCACHE_TILE_X + " INTEGER NOT NULL,"
	+ T_FSCACHE_TILE_Y + " INTEGER NOT NULL,"
	+ T_FSCACHE_TIMESTAMP + " DATE NOT NULL,"
	+ T_FSCACHE_USAGECOUNT + " INTEGER NOT NULL DEFAULT 1,"
	+ T_FSCACHE_FILESIZE + " INTEGER NOT NULL,"
	+ " PRIMARY KEY(" 	+ T_FSCACHE_RENDERER_ID + ","
						+ T_FSCACHE_ZOOM_LEVEL + ","
						+ T_FSCACHE_TILE_X + ","
						+ T_FSCACHE_TILE_Y + ")"
	+ ");";
	
	private static final String SQL_ARG = "=?";
	private static final String AND = " AND ";

	private static final String T_FSCACHE_WHERE = T_FSCACHE_RENDERER_ID + SQL_ARG + AND
												+ T_FSCACHE_ZOOM_LEVEL + SQL_ARG + AND
												+ T_FSCACHE_TILE_X + SQL_ARG + AND
												+ T_FSCACHE_TILE_Y + SQL_ARG;

	
    //	private static final String T_FSCACHE_SELECT_LEAST_USED = "SELECT " + T_FSCACHE_NAME  + "," + T_FSCACHE_FILESIZE + " FROM " + T_FSCACHE + " WHERE "  + T_FSCACHE_USAGECOUNT + " = (SELECT MIN(" + T_FSCACHE_USAGECOUNT + ") FROM "  + T_FSCACHE + ")";
    //	private static final String T_FSCACHE_SELECT_OLDEST = "SELECT " + T_FSCACHE_NAME + "," + T_FSCACHE_FILESIZE + " FROM " + T_FSCACHE + " ORDER BY " + T_FSCACHE_TIMESTAMP + " ASC";
	
	// ===========================================================
	// Fields
	// ===========================================================

	protected final SQLiteDatabase mDatabase;
	protected final SimpleDateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapTileProviderDataBase(final Context context) {
		this.mDatabase = new AndNavDatabaseHelper(context).getWritableDatabase();
	}

	public boolean hasTile(final OpenStreetMapTile aTile) {
		final String[] args = new String[]{"" + aTile.rendererID, "" + aTile.zoomLevel, "" + aTile.x, "" + aTile.y};
		final Cursor c = this.mDatabase.query(T_FSCACHE, new String[]{T_FSCACHE_RENDERER_ID}, T_FSCACHE_WHERE, args, null, null, null);
		final boolean existed = c.getCount() > 0;
		c.close();
		return existed;
	}
	
	public boolean incrementUse(final OpenStreetMapTile aTile) {
		final String[] args = new String[]{"" + aTile.rendererID, "" + aTile.zoomLevel, "" + aTile.x, "" + aTile.y};
		ContentValues cv = new ContentValues();
		cv.put(T_FSCACHE_USAGECOUNT, T_FSCACHE_USAGECOUNT + " + 1");
		cv.put(T_FSCACHE_TIMESTAMP, getNowAsIso8601());
		return this.mDatabase.update(T_FSCACHE, cv, T_FSCACHE_WHERE, args) > 0;
	}

	public int addTileOrIncrement(final OpenStreetMapTile aTile, final int aByteFilesize) {
		if (incrementUse(aTile)) {
			if(DEBUGMODE)
				Log.d(OpenStreetMapTileFilesystemProvider.DEBUGTAG, "Tile existed");
			return 0;
		} else {
			insertNewTileInfo(aTile, aByteFilesize);
			return aByteFilesize;
		}
	}

	private void insertNewTileInfo(final OpenStreetMapTile aTile, final int aByteFilesize) {
		final ContentValues cv = new ContentValues();
		cv.put(T_FSCACHE_RENDERER_ID, aTile.rendererID);
		cv.put(T_FSCACHE_ZOOM_LEVEL, aTile.zoomLevel);
		cv.put(T_FSCACHE_TILE_X, aTile.x);
		cv.put(T_FSCACHE_TILE_Y, aTile.y);
		cv.put(T_FSCACHE_TIMESTAMP, getNowAsIso8601());
		cv.put(T_FSCACHE_FILESIZE, aByteFilesize);
		this.mDatabase.insert(T_FSCACHE, null, cv);
	}

	int deleteOldest(final int pSizeNeeded) throws EmptyCacheException {
	    
	    // TODO fix this so that it does what it's supposed to
	    
//		final Cursor c = this.mDatabase.rawQuery(T_FSCACHE_SELECT_OLDEST, null);
//		final ArrayList<String> deleteFromDB = new ArrayList<String>();
//		int sizeGained = 0;
//		if(c != null){
//			String fileNameOfDeleted; 
//			if(c.moveToFirst()){
//				do{
//					final int sizeItem = c.getInt(c.getColumnIndexOrThrow(T_FSCACHE_FILESIZE));
//					sizeGained += sizeItem;
//					fileNameOfDeleted = c.getString(c.getColumnIndexOrThrow(T_FSCACHE_NAME));
//
//					deleteFromDB.add(fileNameOfDeleted);
//					this.mCtx.deleteFile(fileNameOfDeleted);
//
//					if(DEBUGMODE)
//						Log.i(OpenStreetMapTileFilesystemProvider.DEBUGTAG, "Deleted from FS: " + fileNameOfDeleted + " for " + sizeItem + " Bytes");
//				}while(c.moveToNext() && sizeGained < pSizeNeeded);
//			}else{
//				c.close();
//				throw new EmptyCacheException("Cache seems to be empty.");
//			}
//			c.close();
//
//			for(String fn : deleteFromDB)
//				this.mDatabase.delete(T_FSCACHE, T_FSCACHE_NAME + "='" + fn + "'", null);
//		}
//		return sizeGained;
		return 0;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	private String TMP_COLUMN = "tmp"; 
	public int getCurrentFSCacheByteSize() {
		final Cursor c = this.mDatabase.rawQuery("SELECT SUM(" + T_FSCACHE_FILESIZE + ") AS " + TMP_COLUMN + " FROM " + T_FSCACHE, null);
		final int ret;
		if(c != null){
			if(c.moveToFirst()){
				ret = c.getInt(c.getColumnIndexOrThrow(TMP_COLUMN));
			}else{
				ret = 0;
			}
		}else{
			ret = 0;
		}
		c.close();

		return ret;
	}

	/**
	 * Get at the moment within ISO8601 format.
	 * @return
	 * Date and time in ISO8601 format.
	 */
	private String getNowAsIso8601() {
		return DATE_FORMAT_ISO8601.format(new Date(System.currentTimeMillis()));
	} 

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class AndNavDatabaseHelper extends SQLiteOpenHelper {
		AndNavDatabaseHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(T_FSCACHE_CREATE_COMMAND);
			} catch (Exception e) {
				Log.e(DEBUGTAG, "Error creating database", e);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(DEBUGMODE)
				Log.w(OpenStreetMapTileFilesystemProvider.DEBUGTAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

			db.execSQL("DROP TABLE IF EXISTS " + T_RENDERER);
			db.execSQL("DROP TABLE IF EXISTS " + T_FSCACHE);

			onCreate(db);
		}
	}
}