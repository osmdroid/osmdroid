package org.osmdroid.tileprovider.modules;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import org.osmdroid.api.IMapView;

public class ArchiveFileFactory {

	static Map<String, Class<? extends IArchiveFile> > extensionMap = new HashMap<String,  Class<? extends IArchiveFile>>();
	static {
		extensionMap.put("zip", ZipFileArchive.class);
		extensionMap.put("sqlite", DatabaseFileArchive.class);
		extensionMap.put("mbtiles", MBTilesFileArchive.class);
		extensionMap.put("gemf", GEMFFileArchive.class);

	}

	/**
	 * Returns true if and only if the extension (minus the ".") is registered, meaning that osmdroid
	 * has a driver to read map tiles/data from that source.
	 * @param extension the file extension in question, minus the "."
	 * @return
	 * @since 5.0
	 */
	public static boolean isFileExtensionRegistered(String extension){
		return extensionMap.containsKey(extension);
	}

	/**
	 * Registers a custom archive file provider
	 * @param provider
	 * @param fileExtension without the dot
	 * @since 5.0
	 */
	public static void registerArchiveFileProvider(Class<? extends IArchiveFile> provider, String fileExtension){
		extensionMap.put(fileExtension, provider);
	}

	/**
	 * Return an implementation of {@link IArchiveFile} for the specified file.
	 * @return an implementation, or null if there's no suitable implementation
	 */
	public static IArchiveFile getArchiveFile(final File pFile) {

		String extension = pFile.getName();
		if (extension.contains(".")){
			try {
				extension = extension.substring(extension.lastIndexOf(".") + 1);
			}catch (Exception ex){
				//just to catch any potential out of index errors
			}
		}
		Class<? extends IArchiveFile> aClass = extensionMap.get(extension.toLowerCase());
		if (aClass!=null){
			try {
				IArchiveFile provider = aClass.newInstance();
				provider.init(pFile);
				return provider;
			} catch (InstantiationException e) {
				Log.e(IMapView.LOGTAG, "Error initializing archive file provider " + pFile.getAbsolutePath(), e);
			} catch (IllegalAccessException e) {
				Log.e(IMapView.LOGTAG, "Error initializing archive file provider " + pFile.getAbsolutePath(), e);
			} catch (final Exception e) {
				Log.e(IMapView.LOGTAG,"Error opening archive file " + pFile.getAbsolutePath(), e);
			}
		}


		return null;
	}

}
