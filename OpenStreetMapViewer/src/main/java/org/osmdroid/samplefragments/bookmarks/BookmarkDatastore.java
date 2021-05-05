package org.osmdroid.samplefragments.bookmarks;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * created on 2/11/2018.
 *
 * @author Alex O'Ree
 */

public class BookmarkDatastore {
    protected File db_file;

    public static final String TABLE = "bookmarks";
    public static final String COLUMN_ID = "markerid";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LON = "lon";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESC = "description";
    protected SQLiteDatabase mDatabase;
    public static final String DATABASE_FILENAME = "bookmarks.mDatabase";

    public BookmarkDatastore() {

        Configuration.getInstance().getOsmdroidTileCache().mkdirs();
        db_file = new File(Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + File.separator + DATABASE_FILENAME);


        try {
            mDatabase = SQLiteDatabase.openOrCreateDatabase(db_file, null);
            mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                    COLUMN_LAT + " INTEGER , " +
                    COLUMN_LON + " INTEGER, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_ID + " TEXT, " +
                    COLUMN_DESC + " TEXT, PRIMARY KEY (" + COLUMN_ID + ") );");
        } catch (Throwable ex) {
            Log.e(IMapView.LOGTAG, "Unable to start the bookmark database. Check external storage availability.", ex);
        }
    }

    //TODO geopgrahpic bounding box?
    public List<Marker> getBookmarksAsMarkers(MapView view) {
        List<Marker> markers = new ArrayList<>();
        try {
            //TODO order by title
            final Cursor cur = mDatabase.rawQuery("SELECT * FROM " + TABLE, null);
            while (cur.moveToNext()) {
                Marker m = new Marker(view);
                m.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
                m.setTitle(cur.getString(cur.getColumnIndex(COLUMN_TITLE)));
                m.setSubDescription(cur.getString(cur.getColumnIndex(COLUMN_DESC)));
                m.setPosition(new GeoPoint(cur.getDouble(cur.getColumnIndex(COLUMN_LAT)), cur.getDouble(cur.getColumnIndex(COLUMN_LON))));
                m.setSnippet(m.getPosition().toDoubleString());

                markers.add(m);
            }
            cur.close();
        } catch (final Exception e) {
            Log.w(IMapView.LOGTAG, "Error getting tile sources: ", e);
        }
        return markers;
    }


    public void addBookmark(Marker bookmark) {
        addBookmark(bookmark.getId(), bookmark.getPosition().getLatitude(), bookmark.getPosition().getLongitude(), bookmark.getTitle(), bookmark.getSubDescription());
    }


    public void removeBookmark(Marker bookmark) {
        removeBookmark(bookmark.getId());
    }


    public void removeBookmark(String id) {
        mDatabase.delete(TABLE, COLUMN_ID, new String[]{COLUMN_ID});
    }


    public void addBookmark(String id, double lat, double lon, String title, String description) {

        ContentValues cv = new ContentValues();
        if (id == null || id.length() == 0)
            cv.put(COLUMN_ID, UUID.randomUUID().toString());
        else {
            mDatabase.delete(TABLE, COLUMN_ID + "=?", new String[]{id});
            cv.put(COLUMN_ID, id);
        }

        cv.put(COLUMN_LAT, lat);
        cv.put(COLUMN_LON, lon);
        cv.put(COLUMN_DESC, description);
        cv.put(COLUMN_TITLE, title);
        mDatabase.insert(TABLE, null, cv);
    }

    public void close() {
        db_file = null;
        mDatabase.close();
        mDatabase = null;
    }
}
