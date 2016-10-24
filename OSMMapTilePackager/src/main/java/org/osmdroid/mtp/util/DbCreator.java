package org.osmdroid.mtp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DbCreator {

     public static void putFolderToDb(final File pDestinationFile, final File pFolderToPut) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {
          pDestinationFile.delete();
          Class.forName("org.sqlite.JDBC");
          final Connection conn = DriverManager.getConnection("jdbc:sqlite:" + pDestinationFile);
          final Statement stat = conn.createStatement();
          stat.execute("CREATE TABLE tiles (key INTEGER PRIMARY KEY, provider TEXT, tile BLOB)");
          stat.close();
          final PreparedStatement prep = conn.prepareStatement("insert into tiles values (?, ?, ?);");
          File[] listFiles = pFolderToPut.listFiles();
          if (listFiles != null) {
               for (int zf = 0; zf < listFiles.length; zf++) {
                    File[] listFiles1 = listFiles[zf].listFiles();
                    if (listFiles1 != null) {
                         for (int xf = 0; xf < listFiles1.length; xf++) //for(final File xf : zf.listFiles())
                         {
                              File[] listFiles2 = listFiles1[xf].listFiles();
                              if (listFiles2 != null) {
                                   for (int yf = 0; yf < listFiles2.length; yf++) //for(final File yf : xf.listFiles())
                                   {
                                        // escaped path separator
                                        // Windows -> \\
                                        // Unix -> \/
                                        final String[] s = listFiles2[yf].toString().split("\\" + File.separator);
                                        final long z = Long.parseLong(s[s.length - 3]);
                                        final long x = Long.parseLong(s[s.length - 2]);
                                        final long y = Long.parseLong(s[s.length - 1].split(".png")[0]);
                                        final long index = (((z << z) + x) << z) + y;
                                        prep.setLong(1, index);
                                        final String provider = s[s.length - 4];
                                        prep.setString(2, provider);
                                        final byte[] image = new byte[(int) listFiles2[yf].length()];
                                        final FileInputStream str = new FileInputStream(listFiles2[yf]);
                                        str.read(image);
                                        str.close();
                                        prep.setBytes(3, image);
                                        prep.executeUpdate();
                                   }
                              }
                         }
                    }
               }
          }
          conn.setAutoCommit(false);
          prep.executeBatch();
          conn.setAutoCommit(true);
          conn.close();
     }
}
