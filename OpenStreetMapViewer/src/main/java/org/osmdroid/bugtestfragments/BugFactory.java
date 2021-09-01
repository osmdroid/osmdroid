package org.osmdroid.bugtestfragments;


import org.osmdroid.ISampleFactory;
import org.osmdroid.samplefragments.BaseSampleFragment;

/**
 * Factory for all bug driver classes
 */
public final class BugFactory implements ISampleFactory {

    private final Class<? extends BaseSampleFragment>[] mSamples;


    private static ISampleFactory _instance;

    public static ISampleFactory getInstance() {
        if (_instance == null) {
            _instance = new BugFactory();
        }
        return _instance;
    }

    private BugFactory() {
        mSamples = new Class[]{
                Bug82WinDeath.class,
                SampleBug57.class,
                Bug382Crash.class,
                Bug164EndlessOnScolls.class,
                Bug419Zoom.class,
                Bug445Caching.class,
                Bug512Marker.class,
                Bug512CacheManagerWp.class,
                Bug846InfiniteRedrawLoop.class,
                Bug1322.class, Issue1444.class
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
