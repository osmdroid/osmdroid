package org.andnav2.osm.mtp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DbCreator
{
	public static void putFolderToDb(final File pDestinationFile, final File pFolderToPut) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException
	{
		pDestinationFile.delete();
		Class.forName("org.sqlite.JDBC");
		final Connection conn = DriverManager.getConnection("jdbc:sqlite:" + pDestinationFile);
		final Statement stat = conn.createStatement();
		stat.execute("CREATE TABLE tiles (key INTEGER PRIMARY KEY, provider TEXT, tile BLOB)");
		stat.close();
		final PreparedStatement prep = conn.prepareStatement("insert into tiles values (?, ?, ?);");
		for(final File zf : pFolderToPut.listFiles())
		{
			for(final File xf : zf.listFiles())
			{
				for(final File yf : xf.listFiles())
				{
					final String[] s = yf.toString().split("/");
					final long z = Long.parseLong(s[s.length - 3]);
					final long x = Long.parseLong(s[s.length - 2]);
					final long y = Long.parseLong(s[s.length - 1].split(".png")[0]);
					final long index = (((z << z) + x) << z) + y;
					prep.setLong(1, index);
					final String provider = s[s.length - 4];
					prep.setString(2, provider);
					final byte[] image = new byte[(int) yf.length()];
					final FileInputStream str = new FileInputStream(yf);
					str.read(image);
					str.close();
					prep.setBytes(3, image);
					prep.executeUpdate();
				}
			}
		}
		conn.setAutoCommit(false);
		prep.executeBatch();
		conn.setAutoCommit(true);
		conn.close();
	}
}
