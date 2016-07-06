package org.osmdroid.samplefragments;


public final class SampleFactory {

	private final Class<? extends BaseSampleFragment>[] mSamples;


	private static SampleFactory _instance;
	public static SampleFactory getInstance() {
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
				SampleGridlines.class,
				//19
				SampleMapEventListener.class,
				//20
				SampleAnimateTo.class,
				//21
				SampleHeadingCompassUp.class,
				//22
				SampleSplitScreen.class,
				//23
				SampleMapBootListener.class,
				//24
				SampleFollowMe.class,
				//25
				SampleBug57.class
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
