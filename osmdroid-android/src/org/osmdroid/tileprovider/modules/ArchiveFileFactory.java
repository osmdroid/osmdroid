package org.osmdroid.tileprovider.modules;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.database.sqlite.SQLiteException;

public class ArchiveFileFactory {

	private static final Logger logger = LoggerFactory.getLogger(ArchiveFileFactory.class);

	/**
	 * Return an implementation of {@link IArchiveFile} for the specified file.
	 * @return an implementation, or null if there's no suitable implementation
	 */
	public static IArchiveFile getArchiveFile(final File pFile) {

		if (pFile.getName().endsWith(".zip")) {
			try {
				return ZipFileArchive.getZipFileArchive(pFile);
			} catch (final IOException e) {
				logger.error("Error opening ZIP file", e);
			}
		}

		if (pFile.getName().endsWith(".sqlite")) {
			try {
				return DatabaseFileArchive.getDatabaseFileArchive(pFile);
			} catch (final SQLiteException e) {
				logger.error("Error opening SQL file", e);
			}
		}

		if (pFile.getName().endsWith(".gemf")) {
			try {
				return GEMFFileArchive.getGEMFFileArchive(pFile);
			} catch (final IOException e) {
				logger.error("Error opening GEMF file", e);
			}
		}

		return null;
	}

}
