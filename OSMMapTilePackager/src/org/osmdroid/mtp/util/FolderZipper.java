// Created by plusminus on 2:17:46 AM - Mar 6, 2009
package org.osmdroid.mtp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.osmdroid.tileprovider.util.StreamUtils;

public class FolderZipper {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public static void zipFolderToFile(final File pDestinationFile, final File pFolderToZip){
		try {
			//create ZipOutputStream object
			final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(pDestinationFile));

			//get path prefix so that the zip file does not contain the whole path
			// eg. if folder to be zipped is /home/lalit/test
			// the zip file when opened will have test folder and not home/lalit/test folder
			final int len = pDestinationFile.getAbsolutePath().lastIndexOf(File.separator);
			final String baseName = pFolderToZip.getAbsolutePath().substring(0,len+1);

			addFolderToZip(pFolderToZip, out, baseName);

			StreamUtils.closeStream(out);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}


	private static void addFolderToZip(final File folder, final ZipOutputStream zip, final String baseName) throws IOException {
		final File[] files = folder.listFiles();
		/* For each child (subdirectory/child-file). */
		for (final File file : files) {
			if (file.isDirectory()) {
				/* If the file is a folder, do recursrion with this folder.*/
				addFolderToZip(file, zip, baseName);
			} else {
				/* Otherwise zip it as usual. */
				final String name = file.getAbsolutePath().substring(baseName.length());
				final ZipEntry zipEntry = new ZipEntry(name);
				zip.putNextEntry(zipEntry);
				final FileInputStream fileIn = new FileInputStream(file);
				StreamUtils.copy(fileIn, zip);
				StreamUtils.closeStream(fileIn);
				zip.closeEntry();
			}
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
