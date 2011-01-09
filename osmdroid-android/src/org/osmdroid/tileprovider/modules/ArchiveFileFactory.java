package org.osmdroid.tileprovider.modules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ArchiveFileFactory {

	/**
	 * Return an implementation of {@link IArchiveFile} for the specified file.
	 * @return an implementation, or null if there's no suitable implementation
	 */
	public static IArchiveFile getArchiveFile(final File pFile) {

		if (pFile.getName().endsWith(".zip")) {
			return ZipFileArchive.getZipFileArchive(pFile);
		}

		if (pFile.getName().endsWith(".sqlite")) {
			return DatabaseFileArchive.getDatabaseFileArchive(pFile);
		}
		
		if (pFile.getName().endsWith(".gemf")) {
			try {
				return GEMFFileArchive.getGEMFFileArchive(pFile);
			} catch (FileNotFoundException e) {
				// Error in file search system
			} catch (IOException e) {
				// Something wrong with the GEMF archive.
			}
		}

		// TODO gzip
		// maybe we can even do a filesystem archive instead of file system provider

		return null;
	}

}
