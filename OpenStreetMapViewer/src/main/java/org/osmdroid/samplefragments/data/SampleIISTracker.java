package org.osmdroid.samplefragments.data;

/**
 * Calls a public rest endpoint for the current location of the IIS, but's an icon at that location
 * and centers the map at that location.
 * <p>
 * http://api.open-notify.org/iss-now.json
 * <p>
 * <p>
 * created on 1/6/2017.
 *
 * @author Alex O'Ree
 * @since 5.6.3
 */


public class SampleIISTracker extends IISTrackerBase {

    @Override
    public String getSampleTitle() {
        return "Internal Space Station Tracker (Network connection required)";
    }

    @Override
    boolean isMotionTrail() {
        return false;
    }
}
