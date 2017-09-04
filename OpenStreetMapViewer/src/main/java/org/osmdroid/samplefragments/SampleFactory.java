package org.osmdroid.samplefragments;


import android.os.Build;

import org.osmdroid.ISampleFactory;
import org.osmdroid.samplefragments.animations.AnimatedMarkerHandler;
import org.osmdroid.samplefragments.animations.AnimatedMarkerTypeEvaluator;
import org.osmdroid.samplefragments.animations.AnimatedMarkerValueAnimator;
import org.osmdroid.samplefragments.animations.FastZoomSpeedAnimations;
import org.osmdroid.samplefragments.cache.CacheImport;
import org.osmdroid.samplefragments.cache.CachePurge;
import org.osmdroid.samplefragments.cache.SampleAlternateCacheDir;
import org.osmdroid.samplefragments.cache.SampleCacheDownloader;
import org.osmdroid.samplefragments.cache.SampleCacheDownloaderArchive;
import org.osmdroid.samplefragments.cache.SampleCacheDownloaderCustomUI;
import org.osmdroid.samplefragments.cache.SampleJumboCache;
import org.osmdroid.samplefragments.cache.SampleSqliteOnly;
import org.osmdroid.samplefragments.animations.AnimatedMarkerTimer;
import org.osmdroid.samplefragments.data.AsyncTaskDemoFragment;
import org.osmdroid.samplefragments.geopackage.GeopackageFeatureTiles;
import org.osmdroid.samplefragments.geopackage.GeopackageFeatures;
import org.osmdroid.samplefragments.data.Gridlines2;
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
import org.osmdroid.samplefragments.drawing.DrawPolygon;
import org.osmdroid.samplefragments.drawing.SampleDrawPolyline;
import org.osmdroid.samplefragments.events.SampleAnimateTo;
import org.osmdroid.samplefragments.events.SampleAnimatedZoomToLocation;
import org.osmdroid.samplefragments.events.SampleLimitedScrollArea;
import org.osmdroid.samplefragments.events.SampleMapBootListener;
import org.osmdroid.samplefragments.events.SampleMapEventListener;
import org.osmdroid.samplefragments.events.SampleZoomToBounding;
import org.osmdroid.samplefragments.events.ZoomToBoundsOnStartup;
import org.osmdroid.samplefragments.layouts.MapInAViewPagerFragment;
import org.osmdroid.samplefragments.layouts.MapInScrollView;
import org.osmdroid.samplefragments.layouts.RecyclerCardView;
import org.osmdroid.samplefragments.layouts.SampleFragmentXmlLayout;
import org.osmdroid.samplefragments.layouts.SampleSplitScreen;
import org.osmdroid.samplefragments.layouts.ScaleBarOnBottom;
import org.osmdroid.samplefragments.location.SampleCustomIconDirectedLocationOverlay;
import org.osmdroid.samplefragments.location.SampleFollowMe;
import org.osmdroid.samplefragments.location.SampleHeadingCompassUp;
import org.osmdroid.samplefragments.location.SampleMyLocationWithClick;
import org.osmdroid.samplefragments.location.SampleRotation;
import org.osmdroid.samplefragments.geopackage.GeopackageSample;
import org.osmdroid.samplefragments.tileproviders.MapsforgeTileProviderSample;
import org.osmdroid.samplefragments.tileproviders.OfflinePickerSample;
import org.osmdroid.samplefragments.tileproviders.SampleAssetsOnly;
import org.osmdroid.samplefragments.tileproviders.SampleOfflineGemfOnly;
import org.osmdroid.samplefragments.tilesources.SampleBingHybrid;
import org.osmdroid.samplefragments.tilesources.SampleBingRoad;
import org.osmdroid.samplefragments.tilesources.SampleCopyrightOverlay;
import org.osmdroid.samplefragments.tilesources.SampleCustomLoadingImage;
import org.osmdroid.samplefragments.tilesources.SampleCustomTileSource;
import org.osmdroid.samplefragments.tilesources.SampleHereWeGo;
import org.osmdroid.samplefragments.tilesources.SampleInvertedTiles_NightMode;
import org.osmdroid.samplefragments.tilesources.SampleMapBox;
import org.osmdroid.samplefragments.tilesources.SampleMapQuest;
import org.osmdroid.samplefragments.tileproviders.SampleOfflineOnly;
import org.osmdroid.samplefragments.tilesources.SampleOpenSeaMap;
import org.osmdroid.samplefragments.tilesources.SampleWhackyColorFilter;
import org.osmdroid.samplefragments.tilesources.SepiaToneTiles;

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

        //these are indexed with comments to make life easier when running
        //stress/memory leak testing
        //0
        mSamples.add(SampleWithMinimapItemizedOverlayWithFocus.class);
        //1
        mSamples.add(SampleWithMinimapItemizedOverlayWithScale.class);
        //2
        mSamples.add(SampleLimitedScrollArea.class);
        //3
        mSamples.add(SampleFragmentXmlLayout.class);
        //4
        mSamples.add(SampleOsmPath.class);
        //5
        mSamples.add(SampleInvertedTiles_NightMode.class);
        //6
        mSamples.add(SampleOfflineOnly.class);
        //7
        mSamples.add(SampleAlternateCacheDir.class);
        //8
        mSamples.add(SampleMilitaryIconsItemizedIcons.class);
        //9
        mSamples.add(SampleMilitaryIconsMarker.class);
        //10
        mSamples.add(SampleMapBox.class);
        //11
        mSamples.add(SampleJumboCache.class);
        //12
        mSamples.add(SampleCustomTileSource.class);
        //13
        mSamples.add(SampleAnimatedZoomToLocation.class);
        //14
        mSamples.add(SampleWhackyColorFilter.class);
        //15
        mSamples.add(SampleCustomIconDirectedLocationOverlay.class);
        //16
        mSamples.add(SampleAssetsOnly.class);
        //17
        mSamples.add(SampleSqliteOnly.class);
        //18
        mSamples.add(SampleCacheDownloader.class);
        //19
        mSamples.add(SampleCacheDownloaderCustomUI.class);
        //20
        mSamples.add(SampleCacheDownloaderArchive.class);
        //21
        mSamples.add(SampleGridlines.class);
        //22
        mSamples.add(SampleMapEventListener.class);
        //23
        mSamples.add(SampleAnimateTo.class);
        //24
        mSamples.add(SampleHeadingCompassUp.class);
        //25
        mSamples.add(SampleSplitScreen.class);
        //26
        mSamples.add(SampleMapBootListener.class);
        //27
        mSamples.add(SampleFollowMe.class);
        //28
        mSamples.add(SampleMapQuest.class);
        //29
        mSamples.add(SampleHereWeGo.class);
        //30
        mSamples.add(SampleCustomLoadingImage.class);
        //31
        mSamples.add(AsyncTaskDemoFragment.class);
        //32
        mSamples.add(CacheImport.class);
        //33
        mSamples.add(CachePurge.class);
        //34
        mSamples.add(SampleZoomToBounding.class);
        //35
        mSamples.add(MapInAViewPagerFragment.class);
        //36
        mSamples.add(ZoomToBoundsOnStartup.class);
        //37
        mSamples.add(SampleSimpleLocation.class);
        //38
        mSamples.add(SampleSimpleFastPointOverlay.class);
        //39
        mSamples.add(SampleOpenSeaMap.class);
        //40
        mSamples.add(SampleMarker.class);
        //41
        mSamples.add(SampleRotation.class);
        //42
        mSamples.add(HeatMap.class);
        //43
        mSamples.add(MapInScrollView.class);
        //44
        mSamples.add(SampleCopyrightOverlay.class);
        //45
        mSamples.add(SampleIISTracker.class);
        //46
        mSamples.add(SampleIISTrackerMotionTrails.class);
        //47
        mSamples.add(SampleMyLocationWithClick.class);
        //48
        mSamples.add(SampleDrawPolyline.class);
        //49
        if (Build.VERSION.SDK_INT >= 9)
        mSamples.add(RecyclerCardView.class);
        //50
        mSamples.add(ScaleBarOnBottom.class);
        //51
        mSamples.add(SampleBingHybrid.class);
        //52
        mSamples.add(SampleBingRoad.class);
        //53
        mSamples.add(Gridlines2.class);
        //54
        mSamples.add(SepiaToneTiles.class);
        //55
        mSamples.add(AnimatedMarkerTimer.class);
        //56
        mSamples.add(FastZoomSpeedAnimations.class);
        //57
        mSamples.add(SampleOfflineGemfOnly.class);
        //58
        mSamples.add(DrawPolygon.class);

        if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.GINGERBREAD )
            mSamples.add(AnimatedMarkerHandler.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            mSamples.add(AnimatedMarkerTypeEvaluator.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
            mSamples.add(AnimatedMarkerValueAnimator.class);

        if (Build.VERSION.SDK_INT >= 10)
            mSamples.add(MapsforgeTileProviderSample.class);
        if (Build.VERSION.SDK_INT >= 9)
            mSamples.add(OfflinePickerSample.class);
        //59
        if (Build.VERSION.SDK_INT >= 14) {
            mSamples.add(GeopackageSample.class);
            mSamples.add(GeopackageFeatures.class);
            mSamples.add(GeopackageFeatureTiles.class);
        }
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
