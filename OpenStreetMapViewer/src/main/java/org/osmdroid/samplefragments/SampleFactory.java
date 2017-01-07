package org.osmdroid.samplefragments;


import org.osmdroid.ISampleFactory;
import org.osmdroid.samplefragments.cache.CacheImport;
import org.osmdroid.samplefragments.cache.CachePurge;
import org.osmdroid.samplefragments.cache.SampleAlternateCacheDir;
import org.osmdroid.samplefragments.cache.SampleCacheDownloader;
import org.osmdroid.samplefragments.cache.SampleCacheDownloaderArchive;
import org.osmdroid.samplefragments.cache.SampleCacheDownloaderCustomUI;
import org.osmdroid.samplefragments.cache.SampleJumboCache;
import org.osmdroid.samplefragments.cache.SampleSqliteOnly;
import org.osmdroid.samplefragments.data.AsyncTaskDemoFragment;
import org.osmdroid.samplefragments.data.HeatMap;
import org.osmdroid.samplefragments.data.SampleGridlines;
import org.osmdroid.samplefragments.data.SampleIISTracker;
import org.osmdroid.samplefragments.data.SampleIISTrackerMotionTrails;
import org.osmdroid.samplefragments.data.SampleMarker;
import org.osmdroid.samplefragments.data.SampleMilitaryIconsItemizedIcons;
import org.osmdroid.samplefragments.data.SampleMilitaryIconsMarker;
import org.osmdroid.samplefragments.data.SampleOsmPath;
import org.osmdroid.samplefragments.data.SampleSimpleFastPointOverlay;
import org.osmdroid.samplefragments.data.SampleSimpleLocation;
import org.osmdroid.samplefragments.data.SampleWithMinimapItemizedOverlayWithFocus;
import org.osmdroid.samplefragments.data.SampleWithMinimapItemizedOverlayWithScale;
import org.osmdroid.samplefragments.events.SampleAnimateTo;
import org.osmdroid.samplefragments.events.SampleAnimatedZoomToLocation;
import org.osmdroid.samplefragments.events.SampleLimitedScrollArea;
import org.osmdroid.samplefragments.events.SampleMapBootListener;
import org.osmdroid.samplefragments.events.SampleMapEventListener;
import org.osmdroid.samplefragments.events.SampleZoomToBounding;
import org.osmdroid.samplefragments.events.ZoomToBoundsOnStartup;
import org.osmdroid.samplefragments.layouts.MapInAViewPagerFragment;
import org.osmdroid.samplefragments.layouts.MapInScrollView;
import org.osmdroid.samplefragments.layouts.SampleFragmentXmlLayout;
import org.osmdroid.samplefragments.layouts.SampleSplitScreen;
import org.osmdroid.samplefragments.location.SampleCustomIconDirectedLocationOverlay;
import org.osmdroid.samplefragments.location.SampleFollowMe;
import org.osmdroid.samplefragments.location.SampleHeadingCompassUp;
import org.osmdroid.samplefragments.location.SampleRotation;
import org.osmdroid.samplefragments.tilesources.SampleAssetsOnly;
import org.osmdroid.samplefragments.tilesources.SampleCopyrightOverlay;
import org.osmdroid.samplefragments.tilesources.SampleCustomLoadingImage;
import org.osmdroid.samplefragments.tilesources.SampleCustomTileSource;
import org.osmdroid.samplefragments.tilesources.SampleHereWeGo;
import org.osmdroid.samplefragments.tilesources.SampleInvertedTiles_NightMode;
import org.osmdroid.samplefragments.tilesources.SampleMapBox;
import org.osmdroid.samplefragments.tilesources.SampleMapQuest;
import org.osmdroid.samplefragments.tilesources.SampleOfflineOnly;
import org.osmdroid.samplefragments.tilesources.SampleOpenSeaMap;
import org.osmdroid.samplefragments.tilesources.SampleWhackyColorFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * factory for all examples
 */
public final class SampleFactory implements ISampleFactory {

    private final List<Class<? extends BaseSampleFragment>> mSamples = new ArrayList<>();


    private static ISampleFactory _instance;

    public static ISampleFactory getInstance() {
        if (_instance == null) {
            _instance = new SampleFactory();
        }
        return _instance;
    }

    private SampleFactory() {
        mSamples.add(SampleWithMinimapItemizedOverlayWithFocus.class);
        mSamples.add(SampleWithMinimapItemizedOverlayWithScale.class);
        mSamples.add(SampleLimitedScrollArea.class);
        mSamples.add(SampleFragmentXmlLayout.class);
        mSamples.add(SampleOsmPath.class);
        mSamples.add(SampleInvertedTiles_NightMode.class);
        mSamples.add(SampleOfflineOnly.class);
        mSamples.add(SampleAlternateCacheDir.class);
        mSamples.add(SampleMilitaryIconsItemizedIcons.class);
        mSamples.add(SampleMilitaryIconsMarker.class);
        mSamples.add(SampleMapBox.class);
        mSamples.add(SampleJumboCache.class);
        mSamples.add(SampleCustomTileSource.class);
        mSamples.add(SampleAnimatedZoomToLocation.class);
        mSamples.add(SampleWhackyColorFilter.class);
        mSamples.add(SampleCustomIconDirectedLocationOverlay.class);
        mSamples.add(SampleAssetsOnly.class);
        mSamples.add(SampleSqliteOnly.class);
        mSamples.add(SampleCacheDownloader.class);
        mSamples.add(SampleCacheDownloaderCustomUI.class);
        mSamples.add(SampleCacheDownloaderArchive.class);
        mSamples.add(SampleGridlines.class);
        mSamples.add(SampleMapEventListener.class);
        mSamples.add(SampleAnimateTo.class);
        mSamples.add(SampleHeadingCompassUp.class);
        mSamples.add(SampleSplitScreen.class);
        mSamples.add(SampleMapBootListener.class);
        mSamples.add(SampleFollowMe.class);
        mSamples.add(SampleMapQuest.class);
        mSamples.add(SampleHereWeGo.class);
        mSamples.add(SampleCustomLoadingImage.class);
        mSamples.add(AsyncTaskDemoFragment.class);
        mSamples.add(CacheImport.class);
        mSamples.add(CachePurge.class);
        mSamples.add(SampleZoomToBounding.class);
        mSamples.add(MapInAViewPagerFragment.class);
        mSamples.add(ZoomToBoundsOnStartup.class);
        mSamples.add(SampleSimpleLocation.class);
        mSamples.add(SampleSimpleFastPointOverlay.class);
        mSamples.add(SampleOpenSeaMap.class);
        mSamples.add(SampleMarker.class);
        mSamples.add(SampleRotation.class);
        mSamples.add(HeatMap.class);
        mSamples.add(MapInScrollView.class);
        mSamples.add(SampleCopyrightOverlay.class);
        mSamples.add(SampleIISTracker.class);
        mSamples.add(SampleIISTrackerMotionTrails.class);




    }

    public void addSample(Class<? extends BaseSampleFragment> clz) {
        mSamples.add(clz);
    }

    public BaseSampleFragment getSample(int index) {
        try {
            return mSamples.get(index).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int count() {
        return mSamples.size();
    }
}
