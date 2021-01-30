package org.osmdroid.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.Counters;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.util.MapTileIndex;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An implementation of {@link IFilesystemCache}. It writes tiles to the file system cache. If the
 * cache exceeds 600 Mb then it will be trimmed to 500 Mb.
 *
 * @author Neil Boyd
 * @see OpenStreetMapTileProviderConstants
 */
public class TileWriter implements IFilesystemCache {

    // ===========================================================
    // Constants
    // ===========================================================


    // ===========================================================
    // Fields
    // ===========================================================

    /**
     * amount of disk space used by tile cache
     **/
    private static long mUsedCacheSpace;
    static boolean hasInited = false;
    Thread initThread = null;
    private long mMaximumCachedFileAge;

    // ===========================================================
    // Constructors
    // ===========================================================

    public TileWriter() {

        if (!hasInited) {
            hasInited = true;
            // do this in the background because it takes a long time
            initThread = new Thread() {
                @Override
                public void run() {
                    mUsedCacheSpace = 0; // because it's static

                    calculateDirectorySize(Configuration.getInstance().getOsmdroidTileCache());

                    if (mUsedCacheSpace > Configuration.getInstance().getTileFileSystemCacheMaxBytes()) {
                        cutCurrentCache();
                    }
                    if (Configuration.getInstance().isDebugMode()) {
                        Log.d(IMapView.LOGTAG, "Finished init thread");
                    }
                }
            };
            initThread.setName("TileWriter#init");
            initThread.setPriority(Thread.MIN_PRIORITY);
            initThread.start();
        }
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    /**
     * Get the amount of disk space used by the tile cache. This will initially be zero since the
     * used space is calculated in the background.
     *
     * @return size in bytes
     */
    public static long getUsedCacheSpace() {
        return mUsedCacheSpace;
    }

    public void setMaximumCachedFileAge(long mMaximumCachedFileAge) {
        this.mMaximumCachedFileAge = mMaximumCachedFileAge;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public boolean saveFile(final ITileSource pTileSource, final long pMapTileIndex,
                            final InputStream pStream, final Long pExpirationTime) {

        final File file = getFile(pTileSource, pMapTileIndex);

        if (Configuration.getInstance().isDebugTileProviders()) {
            Log.d(IMapView.LOGTAG, "TileWrite " + file.getAbsolutePath());
        }
        final File parent = file.getParentFile();
        if (!parent.exists() && !createFolderAndCheckIfExists(parent)) {
            return false;
        }

        BufferedOutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file.getPath()),
                    StreamUtils.IO_BUFFER_SIZE);
            final long length = StreamUtils.copy(pStream, outputStream);

            mUsedCacheSpace += length;
            if (mUsedCacheSpace > Configuration.getInstance().getTileFileSystemCacheMaxBytes()) {
                cutCurrentCache(); // TODO perhaps we should do this in the background
            }
        } catch (final IOException e) {
            Counters.fileCacheSaveErrors++;
            return false;
        } finally {
            if (outputStream != null) {
                StreamUtils.closeStream(outputStream);
            }
        }
        return true;
    }

    @Override
    public void onDetach() {

        if (initThread != null) {
            try {
                initThread.interrupt();
            } catch (Throwable t) {
            }
        }
    }

    @Override
    public boolean remove(final ITileSource pTileSource, final long pMapTileIndex) {
        final File file = getFile(pTileSource, pMapTileIndex);

        if (file.exists()) {
            try {
                return file.delete();
            } catch (Exception ex) {
                //potential io exception
                Log.i(IMapView.LOGTAG, "Unable to delete cached tile from " + pTileSource.name() + " " + MapTileIndex.toString(pMapTileIndex), ex);
            }
        }
        return false;
    }

    /**
     * @since 5.6.5
     */
    public File getFile(final ITileSource pTileSource, final long pMapTileIndex) {
        return new File(Configuration.getInstance().getOsmdroidTileCache(), pTileSource.getTileRelativeFilenameString(pMapTileIndex)
                + OpenStreetMapTileProviderConstants.TILE_PATH_EXTENSION);
    }

    @Override
    public boolean exists(final ITileSource pTileSource, final long pMapTileIndex) {
        return getFile(pTileSource, pMapTileIndex).exists();
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private boolean createFolderAndCheckIfExists(final File pFile) {
        if (pFile.mkdirs()) {
            return true;
        }
        if (Configuration.getInstance().isDebugMode()) {
            Log.d(IMapView.LOGTAG, "Failed to create " + pFile + " - wait and check again");
        }

        // if create failed, wait a bit in case another thread created it
        try {
            Thread.sleep(500);
        } catch (final InterruptedException ignore) {
        }
        // and then check again
        if (pFile.exists()) {
            if (Configuration.getInstance().isDebugMode()) {
                Log.d(IMapView.LOGTAG, "Seems like another thread created " + pFile);
            }
            return true;
        } else {
            if (Configuration.getInstance().isDebugMode()) {
                Log.d(IMapView.LOGTAG, "File still doesn't exist: " + pFile);
            }
            return false;
        }
    }

    private void calculateDirectorySize(final File pDirectory) {
        final File[] z = pDirectory.listFiles();
        if (z != null) {
            for (final File file : z) {
                if (file.isFile()) {
                    mUsedCacheSpace += file.length();
                }
                if (file.isDirectory() && !isSymbolicDirectoryLink(pDirectory, file)) {
                    calculateDirectorySize(file); // *** recurse ***
                }
            }
        }
    }

    /**
     * Checks to see if it appears that a directory is a symbolic link. It does this by comparing
     * the canonical path of the parent directory and the parent directory of the directory's
     * canonical path. If they are equal, then they come from the same true parent. If not, then
     * pDirectory is a symbolic link. If we get an exception, we err on the side of caution and
     * return "true" expecting the calculateDirectorySize to now skip further processing since
     * something went goofy.
     */
    private boolean isSymbolicDirectoryLink(final File pParentDirectory, final File pDirectory) {
        try {
            final String canonicalParentPath1 = pParentDirectory.getCanonicalPath();
            final String canonicalParentPath2 = pDirectory.getCanonicalFile().getParent();
            return !canonicalParentPath1.equals(canonicalParentPath2);
        } catch (final IOException e) {
            return true;
        } catch (final NoSuchElementException e) {
            // See: http://code.google.com/p/android/issues/detail?id=4961
            // See: http://code.google.com/p/android/issues/detail?id=5807
            return true;
        }

    }

    private List<File> getDirectoryFileList(final File aDirectory) {
        final List<File> files = new ArrayList<File>();

        final File[] z = aDirectory.listFiles();
        if (z != null) {
            for (final File file : z) {
                if (file.isFile()) {
                    files.add(file);
                }
                if (file.isDirectory()) {
                    files.addAll(getDirectoryFileList(file));
                }
            }
        }

        return files;
    }

    /**
     * If the cache size is greater than the max then trim it down to the trim level. This method is
     * synchronized so that only one thread can run it at a time.
     */
    private void cutCurrentCache() {

        final File lock = Configuration.getInstance().getOsmdroidTileCache();
        synchronized (lock) {

            if (mUsedCacheSpace > Configuration.getInstance().getTileFileSystemCacheTrimBytes()) {

                Log.d(IMapView.LOGTAG, "Trimming tile cache from " + mUsedCacheSpace + " to "
                        + Configuration.getInstance().getTileFileSystemCacheTrimBytes());

                final List<File> z = getDirectoryFileList(Configuration.getInstance().getOsmdroidTileCache());

                // order list by files day created from old to new
                final File[] files = z.toArray(new File[0]);
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(final File f1, final File f2) {
                        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                    }
                });

                for (final File file : files) {
                    if (mUsedCacheSpace <= Configuration.getInstance().getTileFileSystemCacheTrimBytes()) {
                        break;
                    }

                    final long length = file.length();
                    if (file.delete()) {
                        if (Configuration.getInstance().isDebugTileProviders()) {
                            Log.d(IMapView.LOGTAG, "Cache trim deleting " + file.getAbsolutePath());
                        }
                        mUsedCacheSpace -= length;
                    }
                }

                Log.d(IMapView.LOGTAG, "Finished trimming tile cache");
            }
        }
    }

    @Override
    public Long getExpirationTimestamp(final ITileSource pTileSource, final long pMapTileIndex) {
        return null;
    }

    @Override
    public Drawable loadTile(final ITileSource pTileSource, final long pMapTileIndex) throws Exception {
        // Check the tile source to see if its file is available and if so, then render the
        // drawable and return the tile
        final File file = getFile(pTileSource, pMapTileIndex);
        if (!file.exists()) {
            return null;
        }

        final Drawable drawable = pTileSource.getDrawable(file.getPath());

        // Check to see if file has expired
        final long now = System.currentTimeMillis();
        final long lastModified = file.lastModified();
        final boolean fileExpired = lastModified < now - mMaximumCachedFileAge;

        if (fileExpired && drawable != null) {
            if (Configuration.getInstance().isDebugMode()) {
                Log.d(IMapView.LOGTAG, "Tile expired: " + MapTileIndex.toString(pMapTileIndex));
            }
            ExpirableBitmapDrawable.setState(drawable, ExpirableBitmapDrawable.EXPIRED);
        }

        return drawable;
    }
}
