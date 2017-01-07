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
import org.osmdroid.samplefragments.data.SampleMarker;
import org.osmdroid.samplefragments.data.SampleMilitaryIcons;
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
        mSamples.add(//0
            SampleWithMinimapItemizedOverlayWithFocus.class);

        //1
        mSamples.add(SampleWithMinimapItemizedOverlayWithScale.class);
        mSamples.add(
            //2
            SampleLimitedScrollArea.class);
        mSamples.add(
            //3
            SampleFragmentXmlLayout.class);
        mSamples.add(
            //4
            SampleOsmPath.class);
        mSamples.add(
            //5
            SampleInvertedTiles_NightMode.class);
        mSamples.add(
            //6
            SampleOfflineOnly.class);
        mSamples.add(
            //7
            SampleAlternateCacheDir.class);
        mSamples.add(
            //7
            SampleMilitaryIcons.class);
        mSamples.add(
            //8
            SampleMapBox.class);
        mSamples.add(
            //9
            SampleJumboCache.class);
        mSamples.add(
            //10
            SampleCustomTileSource.class);
        mSamples.add(
            //11
            SampleAnimatedZoomToLocation.class);
        mSamples.add(
            //12
            SampleWhackyColorFilter.class);
        mSamples.add(
            //13
            SampleCustomIconDirectedLocationOverlay.class);
        mSamples.add(
            //14
            SampleAssetsOnly.class);
        mSamples.add(
            //15
            SampleSqliteOnly.class);
        mSamples.add(
            //16
            SampleCacheDownloader.class);
        mSamples.add(
            //17
            SampleCacheDownloaderCustomUI.class);
        mSamples.add(
            //18
            SampleCacheDownloaderArchive.class);
        mSamples.add(
            //19
            SampleGridlines.class);
        mSamples.add(
            //20
            SampleMapEventListener.class);
        mSamples.add(
            //21
            SampleAnimateTo.class);
        mSamples.add(
            //22
            SampleHeadingCompassUp.class);
        mSamples.add(
            //23
            SampleSplitScreen.class);
        mSamples.add(
            //24
            SampleMapBootListener.class);
        mSamples.add(
            //25
            SampleFollowMe.class);
        mSamples.add(
            //26
            SampleMapQuest.class);
        mSamples.add(
            //27
            SampleHereWeGo.class);
        mSamples.add(
            //28
            SampleCustomLoadingImage.class);
        mSamples.add(
            //29
            AsyncTaskDemoFragment.class);
        mSamples.add(
            //30
            CacheImport.class);
        mSamples.add(
            //31
            CachePurge.class);
        mSamples.add(
            //32
            SampleZoomToBounding.class);
        mSamples.add(
            //33
            MapInAViewPagerFragment.class);
        mSamples.add(
            //34
            ZoomToBoundsOnStartup.class);
        mSamples.add(
            //35
            SampleSimpleLocation.class);
        mSamples.add(
            //36
            SampleSimpleFastPointOverlay.class);
        mSamples.add(
            //37
            SampleOpenSeaMap.class);
        mSamples.add(
            //38
            SampleMarker.class);
        mSamples.add(
            //39
            SampleRotation.class);
        mSamples.add(
            //40
            HeatMap.class);
        mSamples.add(
            //41
            MapInScrollView.class);
        mSamples.add(
            //42
            SampleCopyrightOverlay.class);

        mSamples.add(SampleIISTracker.class);   //43


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
