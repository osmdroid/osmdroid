package org.osmdroid.debug.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.osmdroid.tileprovider.modules.SqlTileWriter;

import java.util.ArrayList;
import java.util.List;

import static org.osmdroid.tileprovider.modules.DatabaseFileArchive.COLUMN_KEY;
import static org.osmdroid.tileprovider.modules.DatabaseFileArchive.COLUMN_PROVIDER;
import static org.osmdroid.tileprovider.modules.DatabaseFileArchive.COLUMN_TILE;
import static org.osmdroid.tileprovider.modules.DatabaseFileArchive.TABLE;

/**
 * Extended the sqlite tile writer to have some additional query functions. A this point
 * it's unclear if there is a need to put these with the osmdroid-android library, thus they were
 * put here as more of an example.
 * <p>
 * created on 12/21/2016.
 *
 * @author Alex O'Ree
 * @since 5.6.2
 */

public class SqlTileWriterExt extends SqlTileWriter {

    public Cursor select(int rows, int offset) {
        final SQLiteDatabase db = getDb();
        if (db != null)
            return db.rawQuery("select " + COLUMN_KEY + "," + COLUMN_EXPIRES + "," + COLUMN_PROVIDER + " from " + TABLE + " limit ? offset ?", new String[]{rows + "", offset + ""});
        return null;
    }

    /**
     * gets all the tiles sources that we have tiles for in the cache database and their counts
     *
     * @return
     */
    public List<SourceCount> getSources() {
        final SQLiteDatabase db = getDb();
        List<SourceCount> ret = new ArrayList<>();
        if (db == null) {
            return ret;
        }
        Cursor cur = null;
        try {
            cur = db.rawQuery(
                    "select "
                            + COLUMN_PROVIDER
                            + ",count(*) "
                            + ",min(length(" + COLUMN_TILE + ")) "
                            + ",max(length(" + COLUMN_TILE + ")) "
                            + ",sum(length(" + COLUMN_TILE + ")) "
                            + "from " + TABLE + " "
                            + "group by " + COLUMN_PROVIDER, null);
            while (cur.moveToNext()) {
                final SourceCount c = new SourceCount();
                c.source = cur.getString(0);
                c.rowCount = cur.getLong(1);
                c.sizeMin = cur.getLong(2);
                c.sizeMax = cur.getLong(3);
                c.sizeTotal = cur.getLong(4);
                c.sizeAvg = c.sizeTotal / c.rowCount;
                ret.add(c);
            }
        } catch (Exception e) {
            catchException(e);
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        return ret;
    }

    public long getRowCountExpired() {
        return getRowCount(
                COLUMN_EXPIRES + "<?"
                , new String[]{String.valueOf(System.currentTimeMillis())});
    }

    public static class SourceCount {
        public long rowCount = 0;
        public String source = null;
        public long sizeTotal = 0;
        public long sizeMin = 0;
        public long sizeMax = 0;
        public long sizeAvg = 0;
    }

}
