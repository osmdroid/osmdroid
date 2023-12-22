package org.osmdroid;

import android.os.StrictMode;

import org.acra.annotation.ReportsCrashes;

/**
 * This is the base application for the sample app. We only use to catch errors during development cycles
 * <p>
 * Also see note on setting the UserAgent value
 * Created by alex on 7/4/16.
 */
@ReportsCrashes(formUri = "")
public class OsmApplication extends OsmApplicationBase {

    @Override
    public void onCreate() {
        super.onCreate();
		//....
    }

}
