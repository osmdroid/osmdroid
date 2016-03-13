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
				SampleWithMinimapItemizedOverlayWithFocus.class,
                SampleWithMinimapItemizedOverlayWithScale.class,
                SampleLimitedScrollArea.class,
                SampleFragmentXmlLayout.class,
                SampleOsmPath.class,
                SampleInvertedTiles_NightMode.class,
                SampleOfflineOnly.class,
                SampleAlternateCacheDir.class,
                SampleMilitaryIcons.class,
                SampleMapBox.class,
                SampleJumboCache.class,
                SampleCustomTileSource.class,
				SampleAnimatedZoomToLocation.class,
				SampleWhackyColorFilter.class,
				SampleCustomIconDirectedLocationOverlay.class,
				SampleAssetsOnly.class,
				SampleSqliteOnly.class,
				SampleCacheDownloader.class,
				SampleGridlines.class,
				SampleMapEventListener.class,
				SampleAnimateTo.class
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
