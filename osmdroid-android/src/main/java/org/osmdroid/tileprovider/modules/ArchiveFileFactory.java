package org.osmdroid.tileprovider.modules;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.database.sqlite.SQLiteException;
import android.util.Log;
import org.osmdroid.api.IMapView;

public class ArchiveFileFactory {

	static Map<String, Class<? extends IArchiveFile> > extensionMap = new HashMap<String,  Class<? extends IArchiveFile>>();
	static {
		extensionMap.put(".zip", ZipFileArchive.class);
		extensionMap.put(".sqlite", DatabaseFileArchive.class);
		extensionMap.put(".mbtiles", MBTilesFileArchive.class);
		extensionMap.put(".gemf", GEMFFileArchive.class);

	}
	public static void registerArchiveFileProvier(Class<? extends IArchiveFile> provider, String fileExtension){
		extensionMap.put(fileExtension, provider);
	}
	/**
	 * Return an implementation of {@link IArchiveFile} for the specified file.
	 * @return an implementation, or null if there's no suitable implementation
	 */
	public static IArchiveFile getArchiveFile(final File pFile) {

		String extension = pFile.getName();
		if (extension.contains(".")){
			String s = extension.substring(extension.lastIndexOf("."));
			//if (s.length> 1)
				extension = s;
		}
		Class<? extends IArchiveFile> aClass = extensionMap.get(extension.toLowerCase());
		if (aClass!=null){
			try {
				IArchiveFile provider = aClass.newInstance();
				provider.init(pFile);
				return provider;
			} catch (InstantiationException e) {
				Log.e(IMapView.LOGTAG, "Error initializing archive file provider", e);
			} catch (IllegalAccessException e) {
				Log.e(IMapView.LOGTAG, "Error initializing archive file provider", e);
			} catch (final Exception e) {
				Log.e(IMapView.LOGTAG,"Error opening archive file " + pFile.getAbsolutePath(), e);
			}
		}


		return null;
	}

}
