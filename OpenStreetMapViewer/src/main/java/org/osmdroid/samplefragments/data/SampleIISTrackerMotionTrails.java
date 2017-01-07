package org.osmdroid.samplefragments.data;

/**
 * created on 1/7/2017.
 *
 * @author Alex O'Ree
 */

public class SampleIISTrackerMotionTrails extends IISTrackerBase {
    @Override
    public String getSampleTitle() {
        return "Internal Space Station Tracker with motion trails";
    }

    @Override
    boolean isMotionTrail() {
        return true;
    }
}
