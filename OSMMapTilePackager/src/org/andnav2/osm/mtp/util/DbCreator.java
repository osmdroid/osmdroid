package org.andnav2.osm.mtp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;

public class DbCreator
{
	public static void putFolderToDb(final File pDestinationFile, final File pFolderToPut) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException
	{
		pDestinationFile.delete();
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:" + pDestinationFile);
		Statement stat = conn.createStatement();
		stat.execute("CREATE TABLE tiles (key INTEGER PRIMARY KEY, provider TEXT, tile BLOB)");
		stat.close();
		PreparedStatement prep = conn.prepareStatement("insert into tiles values (?, ?, ?);");
		for(File zf : pFolderToPut.listFiles())
		{
			for(File xf : zf.listFiles())
			{
				for(File yf : xf.listFiles())
				{
					String[] s = yf.toString().split("/");
					int z = Integer.parseInt(s[s.length - 3]);
					int x = Integer.parseInt(s[s.length - 2]);
					int y = Integer.parseInt(s[s.length - 1].split(".png")[0]);
					int index = (((z << z) + x) << z) + y;
					prep.setInt(1, index);
					String provider = s[s.length - 4];
					prep.setString(2, provider);
					byte[] image = new byte[(int) yf.length()];
					new FileInputStream(yf).read(image);
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
