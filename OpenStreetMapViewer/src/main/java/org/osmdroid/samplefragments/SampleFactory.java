package org.osmdroid.samplefragments;


import android.os.Build;

import org.osmdroid.ISampleFactory;
import org.osmdroid.samplefragments.animations.AnimatedMarkerHandler;
import org.osmdroid.samplefragments.animations.AnimatedMarkerTimer;
import org.osmdroid.samplefragments.animations.AnimatedMarkerTypeEvaluator;
import org.osmdroid.samplefragments.animations.AnimatedMarkerValueAnimator;
import org.osmdroid.samplefragments.animations.FastZoomSpeedAnimations;
import org.osmdroid.samplefragments.animations.MinMaxZoomLevel;
import org.osmdroid.samplefragments.bookmarks.BookmarkSample;
import org.osmdroid.samplefragments.cache.CacheImport;
import org.osmdroid.samplefragments.cache.CachePurge;
import org.osmdroid.samplefragments.cache.SampleAlternateCacheDir;
import org.osmdroid.samplefragments.cache.SampleCacheDelete;
import org.osmdroid.samplefragments.cache.SampleCacheDownloader;
import org.osmdroid.samplefragments.cache.SampleCacheDownloaderArchive;
import org.osmdroid.samplefragments.cache.SampleCacheDownloaderCustomUI;
import org.osmdroid.samplefragments.cache.SampleJumboCache;
import org.osmdroid.samplefragments.cache.SampleSqliteOnly;
import org.osmdroid.samplefragments.data.AsyncTaskDemoFragment;
import org.osmdroid.samplefragments.data.Gridlines2;
import org.osmdroid.samplefragments.data.HeatMap;
import org.osmdroid.samplefragments.data.SampleGridlines;
import org.osmdroid.samplefragments.data.SampleIISTracker;
import org.osmdroid.samplefragments.data.SampleIISTrackerMotionTrails;
import org.osmdroid.samplefragments.data.SampleItemizedOverlayMultiClick;
import org.osmdroid.samplefragments.data.SampleMapSnapshot;
import org.osmdroid.samplefragments.data.SampleMarker;
import org.osmdroid.samplefragments.data.SampleMarkerMultiClick;
import org.osmdroid.samplefragments.data.SampleMilestonesNonRepetitive;
import org.osmdroid.samplefragments.data.SampleMilitaryIconsItemizedIcons;
import org.osmdroid.samplefragments.data.SampleMilitaryIconsMarker;
import org.osmdroid.samplefragments.data.SampleOsmPath;
import org.osmdroid.samplefragments.data.SampleRace;
import org.osmdroid.samplefragments.data.SampleShapeFile;
import org.osmdroid.samplefragments.data.SampleSimpleFastPointOverlay;
import org.osmdroid.samplefragments.data.SampleSimpleLocation;
import org.osmdroid.samplefragments.data.SampleSpeechBalloon;
import org.osmdroid.samplefragments.data.SampleWithMinimapItemizedOverlayWithFocus;
import org.osmdroid.samplefragments.data.SampleWithMinimapItemizedOverlayWithScale;
import org.osmdroid.samplefragments.data.WeatherGroundOverlaySample;
import org.osmdroid.samplefragments.drawing.DrawCircle10km;
import org.osmdroid.samplefragments.drawing.DrawPolygon;
import org.osmdroid.samplefragments.drawing.DrawPolygonHoles;
import org.osmdroid.samplefragments.drawing.DrawPolygonWithArrows;
import org.osmdroid.samplefragments.drawing.DrawPolygonWithoutVerticalWrapping;
import org.osmdroid.samplefragments.drawing.DrawPolygonWithoutWrapping;
import org.osmdroid.samplefragments.drawing.DrawPolylineWithArrows;
import org.osmdroid.samplefragments.drawing.PressToPlot;
import org.osmdroid.samplefragments.drawing.PressToPlotWithoutWrapping;
import org.osmdroid.samplefragments.drawing.SampleDrawPolyline;
import org.osmdroid.samplefragments.drawing.SampleDrawPolylineAsPath;
import org.osmdroid.samplefragments.drawing.SampleDrawPolylineWithoutVerticalWrapping;
import org.osmdroid.samplefragments.drawing.SampleDrawPolylineWithoutWrapping;
import org.osmdroid.samplefragments.drawing.ShowAdvancedPolylineStyles;
import org.osmdroid.samplefragments.drawing.ShowAdvancedPolylineStylesInvalidation;
import org.osmdroid.samplefragments.events.MarkerDrag;
import org.osmdroid.samplefragments.events.SampleAnimateTo;
import org.osmdroid.samplefragments.events.SampleAnimateToWithOrientation;
import org.osmdroid.samplefragments.events.SampleAnimatedZoomToLocation;
import org.osmdroid.samplefragments.events.SampleLimitedScrollArea;
import org.osmdroid.samplefragments.events.SampleMapBootListener;
import org.osmdroid.samplefragments.events.SampleMapCenterOffset;
import org.osmdroid.samplefragments.events.SampleMapEventListener;
import org.osmdroid.samplefragments.events.SampleSnappable;
import org.osmdroid.samplefragments.events.SampleZoomRounding;
import org.osmdroid.samplefragments.events.SampleZoomToBounding;
import org.osmdroid.samplefragments.events.ZoomToBoundsOnStartup;
import org.osmdroid.samplefragments.geopackage.GeopackageFeatureTiles;
import org.osmdroid.samplefragments.geopackage.GeopackageFeatures;
import org.osmdroid.samplefragments.geopackage.GeopackageSample;
import org.osmdroid.samplefragments.layers.LayerManager;
import org.osmdroid.samplefragments.layouts.MapInAViewPagerFragment;
import org.osmdroid.samplefragments.layouts.MapInScrollView;
import org.osmdroid.samplefragments.layouts.RecyclerCardView;
import org.osmdroid.samplefragments.layouts.SampleFragmentXmlLayout;
import org.osmdroid.samplefragments.layouts.SampleSplitScreen;
import org.osmdroid.samplefragments.layouts.ScaleBarOnBottom;
import org.osmdroid.samplefragments.layouts.StreetAddressFragment;
import org.osmdroid.samplefragments.location.CompassPointerSample;
import org.osmdroid.samplefragments.location.CompassRoseSample;
import org.osmdroid.samplefragments.location.SampleCustomIconDirectedLocationOverlay;
import org.osmdroid.samplefragments.location.SampleCustomMyLocation;
import org.osmdroid.samplefragments.location.SampleFollowMe;
import org.osmdroid.samplefragments.location.SampleHeadingCompassUp;
import org.osmdroid.samplefragments.location.SampleMyLocationWithClick;
import org.osmdroid.samplefragments.location.SampleRotation;
import org.osmdroid.samplefragments.milstd2525.Plotter;
import org.osmdroid.samplefragments.tileproviders.MapsforgeTileProviderSample;
import org.osmdroid.samplefragments.tileproviders.OfflinePickerSample;
import org.osmdroid.samplefragments.tileproviders.SampleAssetsOnly;
import org.osmdroid.samplefragments.tileproviders.SampleAssetsOnlyRepetitionModes;
import org.osmdroid.samplefragments.tileproviders.SampleOfflineGemfOnly;
import org.osmdroid.samplefragments.tileproviders.SampleOfflineOnly;
import org.osmdroid.samplefragments.tileproviders.SampleTileStates;
import org.osmdroid.samplefragments.tileproviders.SampleUnreachableOnlineTiles;
import org.osmdroid.samplefragments.tileproviders.SampleVeryHighZoomLevel;
import org.osmdroid.samplefragments.tilesources.SampleCopyrightOverlay;
import org.osmdroid.samplefragments.tilesources.SampleCustomLoadingImage;
import org.osmdroid.samplefragments.tilesources.SampleCustomTileSource;
import org.osmdroid.samplefragments.tilesources.SampleInvertedTiles_NightMode;
import org.osmdroid.samplefragments.tilesources.SampleLieFi;
import org.osmdroid.samplefragments.tilesources.SampleOfflineFirst;
import org.osmdroid.samplefragments.tilesources.SampleOfflineSecond;
import org.osmdroid.samplefragments.tilesources.SampleOpenSeaMap;
import org.osmdroid.samplefragments.tilesources.SampleWMSSource;
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
        mSamples.add(SampleRace.class);
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
        //mSamples.add(SampleMapBox.class);
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
        //mSamples.add(SampleMapQuest.class);
        //29
        //mSamples.add(SampleHereWeGo.class);
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
        mSamples.add(SampleDrawPolylineAsPath.class);
        //49
        mSamples.add(RecyclerCardView.class);
        //50
        mSamples.add(ScaleBarOnBottom.class);
        //51
        //mSamples.add(SampleBingHybrid.class);
        //52
        //mSamples.add(SampleBingRoad.class);
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
        mSamples.add(DrawPolygonHoles.class);
        mSamples.add(SampleWMSSource.class);
        mSamples.add(SampleAssetsOnlyRepetitionModes.class);
        mSamples.add(SampleDrawPolylineWithoutWrapping.class);
        mSamples.add(DrawPolygonWithoutWrapping.class);

        //mSamples.add(NasaWms111Source.class);
        //mSamples.add(NasaWms130Source.class);
        //mSamples.add(NasaWmsSrs.class);
        mSamples.add(AnimatedMarkerHandler.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            mSamples.add(AnimatedMarkerTypeEvaluator.class);
        mSamples.add(AnimatedMarkerValueAnimator.class);

        mSamples.add(MapsforgeTileProviderSample.class);
        mSamples.add(OfflinePickerSample.class);
        //59
        if (Build.VERSION.SDK_INT >= 14) {
            mSamples.add(GeopackageSample.class);
            mSamples.add(GeopackageFeatures.class);
            mSamples.add(GeopackageFeatureTiles.class);
        }
        // 60
        mSamples.add(SampleVeryHighZoomLevel.class);
        mSamples.add(MinMaxZoomLevel.class);
        mSamples.add(PressToPlot.class);
        mSamples.add(PressToPlotWithoutWrapping.class);
        mSamples.add(DrawPolygonWithoutVerticalWrapping.class);
        mSamples.add(SampleDrawPolylineWithoutVerticalWrapping.class);
        mSamples.add(DrawPolylineWithArrows.class);
        mSamples.add(ShowAdvancedPolylineStyles.class);
        mSamples.add(ShowAdvancedPolylineStylesInvalidation.class);
        mSamples.add(DrawPolygonWithArrows.class);

        mSamples.add(StreetAddressFragment.class);  //map in a list view

        mSamples.add(SampleCustomMyLocation.class);
        mSamples.add(DrawCircle10km.class);
        mSamples.add(MarkerDrag.class);
        mSamples.add(SampleCacheDelete.class);
        if (Build.VERSION.SDK_INT >= 15)
            mSamples.add(Plotter.class);
        mSamples.add(WeatherGroundOverlaySample.class);
        mSamples.add(SampleShapeFile.class);
        mSamples.add(CompassPointerSample.class);
        mSamples.add(CompassRoseSample.class);
        mSamples.add(SampleZoomRounding.class);
        mSamples.add(LayerManager.class);
        mSamples.add(BookmarkSample.class);
        mSamples.add(SampleLieFi.class);
        mSamples.add(SampleItemizedOverlayMultiClick.class);
        mSamples.add(SampleMarkerMultiClick.class);
        mSamples.add(SampleMilestonesNonRepetitive.class);
        mSamples.add(SampleOfflineFirst.class);
        mSamples.add(SampleOfflineSecond.class);
        mSamples.add(SampleTileStates.class);
        mSamples.add(SampleAnimateToWithOrientation.class);
        mSamples.add(SampleMapSnapshot.class);
        mSamples.add(SampleSpeechBalloon.class);
        mSamples.add(SampleMapCenterOffset.class);
        mSamples.add(SampleSnappable.class);
        mSamples.add(SampleUnreachableOnlineTiles.class);
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
