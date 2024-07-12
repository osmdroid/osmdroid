package org.osmdroid.tileprovider.modules;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.Counters;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.util.ReusablePoolDynamic;

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
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    private static final Object mUsedCacheSpaceSyncObj = new Object();
    /** amount of disk space used by tile cache **/
    private static long mUsedCacheSpace = 0;
    @Nullable
    private WorkingThread mWorkingThread = null;
    private long mMaximumCachedFileAge;

    // ===========================================================
    // Constructors
    // ===========================================================

    public TileWriter() {
        ensureThreadIsRunning(WorkingThread.TH_MESSAGE_INITIALIZATION, true);
    }

    /** @noinspection SynchronizationOnLocalVariableOrMethodParameter*/
    private void ensureThreadIsRunning(final int startingWHAT, final boolean waitComplete) {
        if ((mWorkingThread == null) || !mWorkingThread.isAlive() || mWorkingThread.isInterrupted()) {
            final ReusablePoolDynamic.SyncObj<Boolean> cSyncObj = new ReusablePoolDynamic.SyncObj<>(Boolean.FALSE);
            mWorkingThread = new WorkingThread(cSyncObj, startingWHAT);
            mWorkingThread.start();
            while ((cSyncObj.get() != Boolean.TRUE) && waitComplete) {
                try { synchronized (cSyncObj) { cSyncObj.wait(); } } catch (Throwable e) { /*nothing*/ }
            }
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
        synchronized (mUsedCacheSpaceSyncObj) {
            return mUsedCacheSpace;
        }
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
        if ((parent == null) || (!parent.exists() && !createFolderAndCheckIfExists(parent))) {
            return false;
        }

        BufferedOutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file.getPath()), StreamUtils.IO_BUFFER_SIZE);
            final long length = StreamUtils.copy(pStream, outputStream);

            final long cSize;
            synchronized (mUsedCacheSpaceSyncObj) {
                mUsedCacheSpace += length;
                cSize = mUsedCacheSpace;
            }
            if (cSize > Configuration.getInstance().getTileFileSystemCacheMaxBytes()) {
                ensureThreadIsRunning(WorkingThread.TH_MESSAGE_EXECUTE_CUT_CURRENT_CACHE, false);
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
    public void onDetach(@Nullable final Context context) {
        if (mWorkingThread != null) {
            try {
                mWorkingThread.interrupt();
            } catch (Throwable ignored) { /*nothing*/ }
        }
    }

    @Override
    public boolean remove(final ITileSource pTileSource, final long pMapTileIndex) {
        final File file = getFile(pTileSource, pMapTileIndex);
        if (file.exists()) {
            try {
                return file.delete();
            } catch (Exception ex) {
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

    @Override
    public Long getExpirationTimestamp(final ITileSource pTileSource, final long pMapTileIndex) {
        return null;
    }

    @Override
    public Drawable loadTile(@NonNull final ITileSource pTileSource, final long pMapTileIndex) throws Exception {
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

    private static class WorkingThread extends Thread {
        private static final String TAG = "WorkingThread";
        private static final int TH_MESSAGE_INITIALIZATION = 1;
        private static final int TH_MESSAGE_EXECUTE_CUT_CURRENT_CACHE = 2;
        private final ReusablePoolDynamic.SyncObj<Boolean> mSyncObj;
        private final int mStartingWHAT;
        private Handler mHandler;
        private final Comparator<File> mLastModifiedFileComparator = (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified());
        private WorkingThread(@NonNull final ReusablePoolDynamic.SyncObj<Boolean> syncObj, final int startingWHAT) {
            super(TAG + ".init");
            setPriority(Thread.MIN_PRIORITY);
            this.mSyncObj = syncObj;
            this.mStartingWHAT = startingWHAT;
        }
        @Override
        public void run() {
            if (Configuration.getInstance().isDebugMode()) {
                Log.d(IMapView.LOGTAG, "Started " + TAG + " thread");
            }

            Looper.prepare();

            this.mHandler = new Handler(Objects.requireNonNull(Looper.myLooper()), new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull final Message message) {
                    switch (message.what) {
                        case TH_MESSAGE_INITIALIZATION: {
                            WorkingThread.this.calculateDirectorySize(Configuration.getInstance().getOsmdroidTileCache());
                            final long cSize;
                            synchronized (mUsedCacheSpaceSyncObj) {
                                cSize = mUsedCacheSpace;
                            }
                            if (cSize > Configuration.getInstance().getTileFileSystemCacheMaxBytes()) {
                                WorkingThread.this.cutCurrentCache();
                            }
                            synchronized (WorkingThread.this.mSyncObj) {
                                WorkingThread.this.mSyncObj.set(Boolean.TRUE);
                                WorkingThread.this.mSyncObj.notifyAll();
                            }
                            break;
                        }
                        case TH_MESSAGE_EXECUTE_CUT_CURRENT_CACHE: {
                            cutCurrentCache();
                            break;
                        }
                    }
                    this.quitThread();
                    return true;
                }
                private void quitThread() {
                    final Looper cLooper = Looper.myLooper();
                    if (cLooper == null) return;
                    cLooper.quit();
                }
            });
            this.postToWorkingThread(this.mStartingWHAT);

            Looper.loop();

            if (Configuration.getInstance().isDebugMode()) {
                Log.d(IMapView.LOGTAG, "Finished " + TAG + " thread");
            }
        }

        /**
         * If the cache size is greater than the max then trim it down to the trim level. This method is
         * synchronized so that only one thread can run it at a time.
         */
        private void cutCurrentCache() {
            final IConfigurationProvider cConfiguration = Configuration.getInstance();
            long cSize;
            synchronized (mUsedCacheSpaceSyncObj) {
                cSize = mUsedCacheSpace;
            }
            if (cSize > cConfiguration.getTileFileSystemCacheTrimBytes()) {
                Log.d(IMapView.LOGTAG, "Trimming tile cache from " + cSize + " to "
                        + cConfiguration.getTileFileSystemCacheTrimBytes());

                final List<File> z = getDirectoryFileList(cConfiguration.getOsmdroidTileCache());

                // order list by files day created from old to new
                final File[] files = z.toArray(new File[0]);
                Arrays.sort(files, this.mLastModifiedFileComparator);

                for (final File file : files) {
                    if (cSize <= cConfiguration.getTileFileSystemCacheTrimBytes()) break;

                    final long length = file.length();
                    if (file.delete()) {
                        if (cConfiguration.isDebugTileProviders()) {
                            Log.d(IMapView.LOGTAG, "Cache trim deleting " + file.getAbsolutePath());
                        }
                        synchronized (mUsedCacheSpaceSyncObj) {
                            mUsedCacheSpace -= length;
                            cSize = mUsedCacheSpace;
                        }
                    }
                }
                Log.d(IMapView.LOGTAG, "Finished trimming tile cache");
            }
        }

        private boolean postToWorkingThread(final int what) { return mHandler.sendEmptyMessage(what); }

        private void calculateDirectorySize(final File pDirectory) {
            final File[] z = pDirectory.listFiles();
            if (z != null) {
                for (final File file : z) {
                    if (file.isFile()) {
                        synchronized (mUsedCacheSpaceSyncObj) {
                            mUsedCacheSpace += file.length();
                        }
                    }
                    if (file.isDirectory() && !this.isSymbolicDirectoryLink(pDirectory, file)) {
                        this.calculateDirectorySize(file); // *** recurse ***
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
            final List<File> files = new ArrayList<>();
            final File[] z = aDirectory.listFiles();
            if (z != null) {
                for (final File file : z) {
                    if (file.isFile()) files.add(file);
                    if (file.isDirectory()) files.addAll(getDirectoryFileList(file));
                }
            }
            return files;
        }
    }

}
