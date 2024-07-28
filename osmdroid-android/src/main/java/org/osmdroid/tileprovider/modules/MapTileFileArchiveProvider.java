// Created by plusminus on 21:46:41 - 25.09.2008
package org.osmdroid.tileprovider.modules;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.util.MapTileIndex;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A tile provider that can serve tiles from an archive using the supplied tile source. The tile
 * provider will automatically find existing archives and use each one that it finds.
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 */
public class MapTileFileArchiveProvider extends MapTileFileStorageProviderBase {

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String CONST_MAPTILEPROVIDER_FILEARCHIVE = "filearchive";

    // ===========================================================
    // Fields
    // ===========================================================

    private final List<IArchiveFile> mArchiveFiles = new ArrayList<>();
    private final AtomicReference<ITileSource> mTileSource = new AtomicReference<>();
    /**
     * Disable the search of archives if specified in constructor
     */
    private final boolean mSpecificArchivesProvided;
    private final boolean ignoreTileSource;
    private final TileLoader mTileLoader = new TileLoader();

    // ===========================================================
    // Constructors
    // ===========================================================

    public MapTileFileArchiveProvider(@NonNull final Context context,
                                      final IRegisterReceiver pRegisterReceiver,
                                      final ITileSource pTileSource) {
        this(context, pRegisterReceiver, pTileSource, null);
    }
    /**
     * The tiles may be found on several media. This one works with tiles stored on the file system.
     * It and its friends are typically created and controlled by {@link MapTileProviderBase}.
     */
    public MapTileFileArchiveProvider(@NonNull final Context context, final IRegisterReceiver pRegisterReceiver,
                                      final ITileSource pTileSource, final IArchiveFile[] pArchives) {
        this(context, pRegisterReceiver, pTileSource, pArchives, false);
    }
    /**
     * @param ignoreTileSource  if true, tile source is ignored
     */
    public MapTileFileArchiveProvider(@NonNull final Context context, final IRegisterReceiver pRegisterReceiver,
                                      final ITileSource pTileSource, final IArchiveFile[] pArchives, final boolean ignoreTileSource) {
        super(context, pRegisterReceiver,
                Configuration.getInstance().getTileFileSystemThreads(),
                Configuration.getInstance().getTileFileSystemMaxQueueSize());

        this.ignoreTileSource = ignoreTileSource;
        setTileSource(pTileSource);

        if (pArchives == null) {
            mSpecificArchivesProvided = false;
            findArchiveFiles();
        } else {
            mSpecificArchivesProvided = true;
            for (int i = pArchives.length - 1; i >= 0; i--) {
                mArchiveFiles.add(pArchives[i]);
            }
        }

    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public boolean getUsesDataConnection() {
        return false;
    }

    @Override
    protected String getName() {
        return "File Archive Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return CONST_MAPTILEPROVIDER_FILEARCHIVE;
    }

    @Override
    public TileLoader getTileLoader() {
        return mTileLoader;
    }

    @Override
    public int getMinimumZoomLevel() {
        ITileSource tileSource = mTileSource.get();
        return tileSource != null ? tileSource.getMinimumZoomLevel() : OpenStreetMapTileProviderConstants.MINIMUM_ZOOMLEVEL;
    }

    @Override
    public int getMaximumZoomLevel() {
        ITileSource tileSource = mTileSource.get();
        return tileSource != null ? tileSource.getMaximumZoomLevel()
                : org.osmdroid.util.TileSystem.getMaximumZoomLevel();
    }

    @Override
    protected void onMediaMounted(@NonNull final Context context) {
        if (!mSpecificArchivesProvided) {
            findArchiveFiles();
        }
    }

    @Override
    protected void onMediaUnmounted(@NonNull final Context context) {
        if (!mSpecificArchivesProvided) {
            findArchiveFiles();
        }
    }

    @Override
    public void setTileSource(final ITileSource pTileSource) {
        mTileSource.set(pTileSource);
    }

    @Override
    protected void onDetach(@Nullable final Context context) {
        clearArchives();
        super.onDetach(context);
    }

    private void clearArchives() {
        while (!mArchiveFiles.isEmpty()) {
            IArchiveFile t = mArchiveFiles.get(0);
            if (t != null)
                t.close();
            mArchiveFiles.remove(0);
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void findArchiveFiles() {
        clearArchives();

        // path should be optionally configurable
        File cachePaths = Configuration.getInstance().getOsmdroidBasePath();
        if (cachePaths != null) {
            final File[] files = cachePaths.listFiles();
            if (files != null) {
                for (final File file : files) {
                    final IArchiveFile archiveFile = ArchiveFileFactory.getArchiveFile(file);
                    if (archiveFile != null) {
                        archiveFile.setIgnoreTileSource(ignoreTileSource);
                        mArchiveFiles.add(archiveFile);
                    }
                }
            }
        }
    }

    private synchronized InputStream getInputStream(final long pMapTileIndex,
                                                    final ITileSource tileSource) {
        for (final IArchiveFile archiveFile : mArchiveFiles) {
            if (archiveFile != null) {
                final InputStream in = archiveFile.getInputStream(tileSource, pMapTileIndex);
                if (in != null) {
                    if (Configuration.getInstance().isDebugMode()) {
                        Log.d(IMapView.LOGTAG, "Found tile " + MapTileIndex.toString(pMapTileIndex) + " in " + archiveFile);
                    }
                    return in;
                }
            }
        }

        return null;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final long pMapTileIndex) {

            Drawable returnValue = null;
            ITileSource tileSource = mTileSource.get();
            if (tileSource == null) {
                return null;
            }

            InputStream inputStream = null;
            try {
                if (Configuration.getInstance().isDebugMode()) {
                    Log.d(IMapView.LOGTAG, "Archives - Tile doesn't exist: " + MapTileIndex.toString(pMapTileIndex));
                }

                inputStream = getInputStream(pMapTileIndex, tileSource);
                if (inputStream != null) {
                    if (Configuration.getInstance().isDebugMode()) {
                        Log.d(IMapView.LOGTAG, "Use tile from archive: " + MapTileIndex.toString(pMapTileIndex));
                    }
                    returnValue = tileSource.getDrawable(inputStream);
                }
            } catch (final Throwable e) {
                Log.e(IMapView.LOGTAG, "Error loading tile", e);
            } finally {
                if (inputStream != null) {
                    StreamUtils.closeStream(inputStream);
                }
            }

            return returnValue;
        }

        @IMapTileProviderCallback.TILEPROVIDERTYPE
        @Override
        public final int getProviderType() { return IMapTileProviderCallback.TILEPROVIDERTYPE_FILE_ARCHIVE; }
    }
}
