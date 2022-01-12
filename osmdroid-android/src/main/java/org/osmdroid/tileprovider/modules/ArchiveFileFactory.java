package org.osmdroid.tileprovider.modules;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.util.Log;

import org.osmdroid.api.IMapView;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ArchiveFileFactory {

    static Map<String, Class<? extends IArchiveFile>> extensionMap = new HashMap<String, Class<? extends IArchiveFile>>();

    static {
        extensionMap.put("zip", ZipFileArchive.class);
        if (Build.VERSION.SDK_INT >= 10) {
            extensionMap.put("sqlite", DatabaseFileArchive.class);
            extensionMap.put("mbtiles", MBTilesFileArchive.class);
            extensionMap.put("gemf", GEMFFileArchive.class);
        }

    }

    /**
     * Returns true if and only if the extension (minus the ".") is registered, meaning that osmdroid
     * has a driver to read map tiles/data from that source.
     *
     * @param extension the file extension in question, minus the "."
     * @return
     * @since 5.0
     */
    public static boolean isFileExtensionRegistered(String extension) {
        return extensionMap.containsKey(extension);
    }

    /**
     * Registers a custom archive file provider
     *
     * @param provider
     * @param fileExtension without the dot
     * @since 5.0
     */
    public static void registerArchiveFileProvider(Class<? extends IArchiveFile> provider, String fileExtension) {
        extensionMap.put(fileExtension, provider);
    }

    /**
     * Return an implementation of {@link IArchiveFile} for the specified file.
     *
     * @return an implementation, or null if there's no suitable implementation
     */
    public static IArchiveFile getArchiveFile(final File pFile) {

        String extension = pFile.getName();
        if (extension.contains(".")) {
            try {
                extension = extension.substring(extension.lastIndexOf(".") + 1);
            } catch (Exception ex) {
                //just to catch any potential out of index errors
            }
        }
        Class<? extends IArchiveFile> aClass = extensionMap.get(extension.toLowerCase());
        if (aClass != null) {
            try {
                IArchiveFile provider = aClass.newInstance();
                provider.init(pFile);
                return provider;
            } catch (InstantiationException e) {
                Log.e(IMapView.LOGTAG, "Error initializing archive file provider " + pFile.getAbsolutePath(), e);
            } catch (IllegalAccessException e) {
                Log.e(IMapView.LOGTAG, "Error initializing archive file provider " + pFile.getAbsolutePath(), e);
            } catch (final Exception e) {
                Log.e(IMapView.LOGTAG, "Error opening archive file " + pFile.getAbsolutePath(), e);
            }
        }


        return null;
    }

    public static String getFileName(Uri uri, Context ctx) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = ctx.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static IArchiveFile getArchiveFile(final Uri pFile, Context ctx) {

        String extension = getFileName(pFile, ctx); //pFile.getName();
        if (extension.contains(".")) {
            try {
                extension = extension.substring(extension.lastIndexOf(".") + 1);
            } catch (Exception ex) {
                //just to catch any potential out of index errors
            }
        }
        Class<? extends IArchiveFile> aClass = extensionMap.get(extension.toLowerCase());
        if (aClass != null) {
            try {
                IArchiveFile provider = aClass.newInstance();
                provider.init(pFile, ctx);
                return provider;
            } catch (InstantiationException | IllegalAccessException e) {
                Log.e(IMapView.LOGTAG, "Error initializing archive file provider " + pFile.getPath(), e);
            } catch (final Exception e) {
                Log.e(IMapView.LOGTAG, "Error opening archive file " + pFile.getPath(), e);
            }
        }


        return null;
    }

    /**
     * @return
     * @since 6.0.0
     */
    public static Set<String> getRegisteredExtensions() {
        Set<String> r = new HashSet<>();
        r.addAll(extensionMap.keySet());
        return r;
    }
}
