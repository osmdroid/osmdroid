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
import org.osmdroid.samplefragments.data.SampleSimpleLocation;
import org.osmdroid.samplefragments.events.SampleAnimateTo;
import org.osmdroid.samplefragments.events.SampleAnimatedZoomToLocation;
import org.osmdroid.samplefragments.events.SampleLimitedScrollArea;
import org.osmdroid.samplefragments.events.SampleMapBootListener;
import org.osmdroid.samplefragments.events.SampleMapEventListener;
import org.osmdroid.samplefragments.events.SampleZoomToBounding;
import org.osmdroid.samplefragments.events.ZoomToBoundsOnStartup;
import org.osmdroid.samplefragments.layouts.SampleFragmentXmlLayout;
import org.osmdroid.samplefragments.layouts.SampleSplitScreen;
import org.osmdroid.samplefragments.location.SampleCustomIconDirectedLocationOverlay;
import org.osmdroid.samplefragments.location.SampleFollowMe;
import org.osmdroid.samplefragments.location.SampleHeadingCompassUp;
import org.osmdroid.samplefragments.data.AsyncTaskDemoFragment;
import org.osmdroid.samplefragments.data.SampleGridlines;
import org.osmdroid.samplefragments.data.SampleMilitaryIcons;
import org.osmdroid.samplefragments.data.SampleOsmPath;
import org.osmdroid.samplefragments.data.SampleWithMinimapItemizedOverlayWithFocus;
import org.osmdroid.samplefragments.data.SampleWithMinimapItemizedOverlayWithScale;
import org.osmdroid.samplefragments.data.SampleSimpleFastPointOverlay;
import org.osmdroid.samplefragments.tilesources.SampleAssetsOnly;
import org.osmdroid.samplefragments.tilesources.SampleCustomLoadingImage;
import org.osmdroid.samplefragments.tilesources.SampleCustomTileSource;
import org.osmdroid.samplefragments.tilesources.SampleHereWeGo;
import org.osmdroid.samplefragments.tilesources.SampleInvertedTiles_NightMode;
import org.osmdroid.samplefragments.tilesources.SampleMapBox;
import org.osmdroid.samplefragments.tilesources.SampleMapQuest;
import org.osmdroid.samplefragments.tilesources.SampleOfflineOnly;
import org.osmdroid.samplefragments.tilesources.SampleOpenSeaMap;
import org.osmdroid.samplefragments.tilesources.SampleWhackyColorFilter;
import org.osmdroid.samplefragments.pager.MapInAViewPagerFragment;

/**
 * factory for all examples
 */
public final class SampleFactory implements ISampleFactory {

	private final Class<? extends BaseSampleFragment>[] mSamples;


	private static ISampleFactory _instance;
	public static ISampleFactory getInstance() {
		if (_instance == null) {
			_instance = new SampleFactory();
		}
		return _instance;
	}

	private SampleFactory() {
		mSamples = new Class[] {
				//0
				SampleWithMinimapItemizedOverlayWithFocus.class,
				//1
                SampleWithMinimapItemizedOverlayWithScale.class,
				//2
                SampleLimitedScrollArea.class,
				//3
                SampleFragmentXmlLayout.class,
				//4
                SampleOsmPath.class,
				//5
                SampleInvertedTiles_NightMode.class,
				//6
                SampleOfflineOnly.class,
				//7
                SampleAlternateCacheDir.class,
				//7
                SampleMilitaryIcons.class,
				//8
                SampleMapBox.class,
				//9
                SampleJumboCache.class,
				//10
                SampleCustomTileSource.class,
				//11
				SampleAnimatedZoomToLocation.class,
				//12
				SampleWhackyColorFilter.class,
				//13
				SampleCustomIconDirectedLocationOverlay.class,
				//14
				SampleAssetsOnly.class,
				//15
				SampleSqliteOnly.class,
				//16
				SampleCacheDownloader.class,
				//17
				SampleCacheDownloaderCustomUI.class,
				//18
				SampleCacheDownloaderArchive.class,
				//19
				SampleGridlines.class,
				//20
				SampleMapEventListener.class,
				//21
				SampleAnimateTo.class,
				//22
				SampleHeadingCompassUp.class,
				//23
				SampleSplitScreen.class,
				//24
				SampleMapBootListener.class,
				//25
				SampleFollowMe.class,
				//26
				SampleMapQuest.class,
				//27
				SampleHereWeGo.class,
				//28
				SampleCustomLoadingImage.class,
				//29
				AsyncTaskDemoFragment.class,
				//30
				CacheImport.class,
				//31
				CachePurge.class,
				//32
				SampleZoomToBounding.class,
				//33
				MapInAViewPagerFragment.class,
				//34
				ZoomToBoundsOnStartup.class,
				//35
				SampleSimpleLocation.class,
                //36
				SampleSimpleFastPointOverlay.class,
				//37
				SampleOpenSeaMap.class,

        };
	}

	public BaseSampleFragment getSample(int index) {
		try {
			return mSamples[index].newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int count() {
		return mSamples.length;
	}
}
