package org.osmdroid.tileprovider.modules;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implements a file system cache and provides cached tiles from Assets. This
 * functions as a tile provider by serving cached tiles for the supplied tile
 * source.
 * <p>
 * tiles should be put into apk's assets directory just like following example:
 * <p>
 * assets/Mapnik/11/1316/806.png
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 * @author Behrooz Shabani (everplays)
 */
public class MapTileAssetsProvider extends MapTileFileStorageProviderBase {

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String CONST_MAPTILEPROVIDER_ASSETS = "assets";

    // ===========================================================
    // Fields
    // ===========================================================

    private final AssetManager mAssets;
    private final AtomicReference<ITileSource> mTileSource = new AtomicReference<>();
    private final TileLoader mTileLoader;

    // ===========================================================
    // Constructors
    // ===========================================================

    public MapTileAssetsProvider(final IRegisterReceiver pRegisterReceiver, final AssetManager pAssets) {
        this(pRegisterReceiver, pAssets, TileSourceFactory.DEFAULT_TILE_SOURCE);
    }

    public MapTileAssetsProvider(final IRegisterReceiver pRegisterReceiver,
                                 final AssetManager pAssets,
                                 final ITileSource pTileSource) {
        this(pRegisterReceiver, pAssets, pTileSource,
                Configuration.getInstance().getTileDownloadThreads(),
                Configuration.getInstance().getTileDownloadMaxQueueSize()
        );
    }

    public MapTileAssetsProvider(final IRegisterReceiver pRegisterReceiver,
                                 final AssetManager pAssets,
                                 final ITileSource pTileSource, int pThreadPoolSize,
                                 int pPendingQueueSize) {
        super(pRegisterReceiver, pThreadPoolSize, pPendingQueueSize);
        setTileSource(pTileSource);

        mAssets = pAssets;
        mTileLoader = new TileLoader(mAssets);
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
        return "Assets Cache Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return CONST_MAPTILEPROVIDER_ASSETS;
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
    public void setTileSource(final ITileSource pTileSource) {
        mTileSource.set(pTileSource);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {
        private final AssetManager mAssets;

        public TileLoader(AssetManager pAssets) {
            mAssets = pAssets;
        }

        @Override
        public Drawable loadTile(final long pMapTileIndex) throws CantContinueException {
            ITileSource tileSource = mTileSource.get();
            if (tileSource == null) {
                return null;
            }

            InputStream is = null;
            try {
                is = mAssets.open(tileSource.getTileRelativeFilenameString(pMapTileIndex));
                final Drawable drawable = tileSource.getDrawable(is);
                if (drawable != null) {
                    //https://github.com/osmdroid/osmdroid/issues/272 why was this set to expired?
                    //ExpirableBitmapDrawable.setDrawableExpired(drawable);
                }
                return drawable;
            } catch (IOException ignored) {
            } catch (final LowMemoryException e) {
                throw new CantContinueException(e);
            } finally {
                if (is != null) {
                    try { is.close(); } catch (Exception e) { /*nothing*/ }
                }
            }

            // If we get here then there is no file in the file cache
            return null;
        }

        @IMapTileProviderCallback.TILEPROVIDERTYPE
        @Override
        public final int getProviderType() { return IMapTileProviderCallback.TILEPROVIDERTYPE_ASSET; }
    }
}
