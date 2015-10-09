package org.osmdroid.tileprovider.modules;

import java.io.File;
import java.io.IOException;

import android.database.sqlite.SQLiteException;
import android.util.Log;
import org.osmdroid.api.IMapView;

public class ArchiveFileFactory {

	/**
	 * Return an implementation of {@link IArchiveFile} for the specified file.
	 * @return an implementation, or null if there's no suitable implementation
	 */
	public static IArchiveFile getArchiveFile(final File pFile) {

		if (pFile.getName().endsWith(".zip")) {
			try {
				return ZipFileArchive.getZipFileArchive(pFile);
			} catch (final IOException e) {
				Log.e(IMapView.LOGTAG,"Error opening ZIP file", e);
			}
		}

		if (pFile.getName().endsWith(".sqlite")) {
			try {
				return DatabaseFileArchive.getDatabaseFileArchive(pFile);
			} catch (final SQLiteException e) {
				Log.e(IMapView.LOGTAG,"Error opening SQL file", e);
			}
		}

		if (pFile.getName().endsWith(".mbtiles")) {
			try {
				return MBTilesFileArchive.getDatabaseFileArchive(pFile);
			} catch (final SQLiteException e) {
				Log.e(IMapView.LOGTAG,"Error opening MBTiles SQLite file", e);
			}
		}
		
		if (pFile.getName().endsWith(".gemf")) {
			try {
				return GEMFFileArchive.getGEMFFileArchive(pFile);
			} catch (final IOException e) {
				Log.e(IMapView.LOGTAG,"Error opening GEMF file", e);
			}
		}

		return null;
	}

}
