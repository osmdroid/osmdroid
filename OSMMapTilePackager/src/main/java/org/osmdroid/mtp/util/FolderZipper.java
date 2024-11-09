// Created by plusminus on 2:17:46 AM - Mar 6, 2009
package org.osmdroid.mtp.util;

import org.osmdroid.mtp.TileWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FolderZipper extends TileWriter {
    final File pDestinationFile;
    public FolderZipper(File pDestinationFile) {
        this.pDestinationFile = pDestinationFile;
    }
     ZipOutputStream out=null;



    @Override
    public void open() throws Exception {
        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(pDestinationFile));

    }

    @Override
    public void close() throws Exception {
    out.close();
    }

    @Override
    public synchronized void  write(long z, long x, long y, String name, byte[] bits) throws Exception {

        final ZipEntry zipEntry = new ZipEntry(name);
        out.putNextEntry(zipEntry);
        out.write(bits);
        out.closeEntry();
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

}
