// Created by plusminus on 21:46:41 - 25.09.2008
package org.osmdroid.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.util.MapTileIndex;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

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


    // ===========================================================
    // Fields
    // ===========================================================

    private final ArrayList<IArchiveFile> mArchiveFiles = new ArrayList<IArchiveFile>();

    private final AtomicReference<ITileSource> mTileSource = new AtomicReference<ITileSource>();

    /**
     * Disable the search of archives if specified in constructor
     */
    private final boolean mSpecificArchivesProvided;
    private final boolean ignoreTileSource;

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * The tiles may be found on several media. This one works with tiles stored on the file system.
     * It and its friends are typically created and controlled by {@link MapTileProviderBase}.
     */
    public MapTileFileArchiveProvider(final IRegisterReceiver pRegisterReceiver,
                                      final ITileSource pTileSource, final IArchiveFile[] pArchives) {
        this(pRegisterReceiver, pTileSource, pArchives, false);
    }

    /**
     * @param pRegisterReceiver
     * @param pTileSource
     * @param pArchives
     * @param ignoreTileSource  if true, tile source is ignored
     * @since 6.0.0
     */
    public MapTileFileArchiveProvider(final IRegisterReceiver pRegisterReceiver,
                                      final ITileSource pTileSource, final IArchiveFile[] pArchives, final boolean ignoreTileSource) {
        super(pRegisterReceiver,
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

    public MapTileFileArchiveProvider(final IRegisterReceiver pRegisterReceiver,
                                      final ITileSource pTileSource) {
        this(pRegisterReceiver, pTileSource, null);
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
        return "filearchive";
    }

    @Override
    public TileLoader getTileLoader() {
        return new TileLoader();
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
    protected void onMediaMounted() {
        if (!mSpecificArchivesProvided) {
            findArchiveFiles();
        }
    }

    @Override
    protected void onMediaUnmounted() {
        if (!mSpecificArchivesProvided) {
            findArchiveFiles();
        }
    }

    @Override
    public void setTileSource(final ITileSource pTileSource) {
        mTileSource.set(pTileSource);
    }

    @Override
    public void detach() {
        clearArcives();
        super.detach();
    }

    private void clearArcives() {
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
        clearArcives();

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
                    final Drawable drawable = tileSource.getDrawable(inputStream);
                    returnValue = drawable;
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
    }
}
