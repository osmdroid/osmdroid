package org.osmdroid.mtp.util;

import org.osmdroid.mtp.TileWriter;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class DbCreator extends TileWriter {
    private final File pDestinationFile;
    private Connection conn;
    private PreparedStatement prep;

    public DbCreator(final File pDestinationFile) {
        this.pDestinationFile = pDestinationFile;

    }

    @Override
    public void open() throws Exception {
        pDestinationFile.delete();
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:" + pDestinationFile);
        final Statement stat = conn.createStatement();
        try {
            stat.execute("CREATE TABLE tiles (key INTEGER PRIMARY KEY, provider TEXT, tile BLOB)");
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        stat.close();
        prep = conn.prepareStatement("insert into tiles values (?, ?, ?);");
    }

    @Override
    public void close() throws Exception {
        prep.close();
        conn.close();
    }

    @Override
    public void write(long z, long x, long y, String name, byte[] image) throws Exception {
        synchronized (prep) {
            final long index = (((z << z) + x) << z) + y;
            prep.setLong(1, index);
            prep.setString(2, name);
            prep.setBytes(3, image);
            prep.executeUpdate();
            prep.clearParameters();
        }
    }
}
