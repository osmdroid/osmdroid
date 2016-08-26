package org.osmdroid.samplefragments;


import org.osmdroid.ISampleFactory;

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
				SampleCustomLoadingImage.class
				//29

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
