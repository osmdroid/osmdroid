package org.osmdroid.tileprovider.modules;

import java.io.File;

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

		// TODO gzip, gemf
		// maybe we can even do a filesystem archive instead of file system provider

		return null;
	}

}
