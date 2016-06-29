package org.osmdroid.bugtestfragments;


import org.osmdroid.ISampleFactory;
import org.osmdroid.samplefragments.BaseSampleFragment;

public final class SampleFactory implements ISampleFactory{

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
			Bug82WinDeath.class
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
