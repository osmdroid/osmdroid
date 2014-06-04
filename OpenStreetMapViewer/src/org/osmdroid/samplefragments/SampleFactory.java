package org.osmdroid.samplefragments;


public final class SampleFactory {

	private final BaseSampleFragment[] mSamples;

	private static SampleFactory _instance;
	public static SampleFactory getInstance() {
		if (_instance == null) {
			_instance = new SampleFactory();
		}
		return _instance;
	}

	private SampleFactory() {
		mSamples = new BaseSampleFragment[] { new SampleWithMinimapItemizedOverlayWithFocus(),
				new SampleLimitedScrollArea(), new SampleFragmentXmlLayout(), new SampleOsmPath() };
	}

	public BaseSampleFragment getSample(int index) {
		return mSamples[index];
	}

	public int count() {
		return mSamples.length;
	}
}
