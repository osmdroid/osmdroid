package org.osmdroid.samplefragments.tilesources;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.CantContinueException;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.MapTileApproximater;
import org.osmdroid.tileprovider.modules.MapTileAssetsProvider;
import org.osmdroid.tileprovider.modules.MapTileDownloaderProvider;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Lie Fi demo: we emulate a slow online source in order to show the offline first behavior
 *
 * @author Fabrice Fontaine
 * @since 6.0.2
 */
public class SampleLieFi extends BaseSampleFragment {

    private final GeoPoint mInitialCenter = new GeoPoint(41.8905495, 12.4924348); // Rome, Italy
    private final double mInitialZoomLevel = 5;
    private final int mLieFieLagInMillis = 1000;

    @Override
    public String getSampleTitle() {
        return "Lie Fi - slow online source";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final MapTileProviderArray provider = new MapTileProviderLieFi(inflater.getContext());
        mMapView = new MapView(inflater.getContext(), provider);
        return mMapView;
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();

        mMapView.post(new Runnable() { // "post" because we need View.getWidth() to be set
            @Override
            public void run() {
                mMapView.getController().setZoom(mInitialZoomLevel);
                mMapView.setExpectedCenter(mInitialCenter);
            }
        });
    }

    private class MapTileProviderLieFi extends MapTileProviderArray implements IMapTileProviderCallback {

        private IFilesystemCache tileWriter;
        private final INetworkAvailablityCheck mNetworkAvailabilityCheck;

        private MapTileProviderLieFi(@NonNull final Context pContext) {
            this(new SimpleRegisterReceiver(), new NetworkAvailabliltyCheck(pContext), TileSourceFactory.DEFAULT_TILE_SOURCE, pContext, null);
        }

        private MapTileProviderLieFi(final IRegisterReceiver pRegisterReceiver,
                                     final INetworkAvailablityCheck aNetworkAvailablityCheck, final ITileSource pTileSource,
                                     final Context pContext, final IFilesystemCache cacheWriter) {
            super(pContext, pTileSource, pRegisterReceiver);
            mNetworkAvailabilityCheck = aNetworkAvailablityCheck;

            if (cacheWriter != null) {
                tileWriter = cacheWriter;
            } else {
                tileWriter = new SqlTileWriter();
            }
            final MapTileAssetsProvider assetsProvider = new MapTileAssetsProvider(pContext, pRegisterReceiver, pContext.getAssets(), pTileSource);
            mTileProviderList.add(assetsProvider);

            final MapTileFileStorageProviderBase cacheProvider = MapTileProviderBasic.getMapTileFileStorageProviderBase(pContext, pRegisterReceiver, pTileSource, tileWriter);
            mTileProviderList.add(cacheProvider);

            final MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(pContext, pRegisterReceiver, pTileSource);
            mTileProviderList.add(archiveProvider);

            final MapTileApproximater approximationProvider = new MapTileApproximater();
            mTileProviderList.add(approximationProvider);
            approximationProvider.addProvider(assetsProvider);
            approximationProvider.addProvider(cacheProvider);
            approximationProvider.addProvider(archiveProvider);

            final MapTileDownloaderProvider downloaderProvider = new MapTileDownloaderLieFiProvider(pContext, pTileSource, tileWriter, aNetworkAvailablityCheck);
            mTileProviderList.add(downloaderProvider);

            getTileCache().getProtectedTileContainers().add(this);
        }

        @Override
        public IFilesystemCache getTileWriter() {
            return tileWriter;
        }

        @Override
        public void onDetach(@Nullable final Context context) {
            //https://github.com/osmdroid/osmdroid/issues/213
            //close the writer
            if (tileWriter != null)
                tileWriter.onDetach(context);
            tileWriter = null;
            super.onDetach(context);
        }

        /**
         * @since 6.0.3
         */
        @Override
        protected boolean isDowngradedMode(final long pMapTileIndex) {
            return (mNetworkAvailabilityCheck != null && !mNetworkAvailabilityCheck.getNetworkAvailable()) || !useDataConnection();
        }
    }

    private class MapTileDownloaderLieFiProvider extends MapTileDownloaderProvider {

        private final MapTileDownloaderProvider.TileLoader mTileLoader = new TileLoader();

        MapTileDownloaderLieFiProvider(@NonNull final Context context, ITileSource pTileSource, IFilesystemCache pFilesystemCache, INetworkAvailablityCheck pNetworkAvailablityCheck) {
            super(context, pTileSource, pFilesystemCache, pNetworkAvailablityCheck);
        }

        @Override
        public MapTileDownloaderProvider.TileLoader getTileLoader() {
            return mTileLoader;
        }

        private class TileLoader extends MapTileDownloaderProvider.TileLoader {
            @Override
            protected Drawable downloadTile(long pMapTileIndex, int redirectCount, String targetUrl) throws CantContinueException {
                try {
                    Thread.sleep(mLieFieLagInMillis);
                } catch (InterruptedException e) {
                    //
                }
                return super.downloadTile(pMapTileIndex, redirectCount, targetUrl);
            }
        }
    }
}
