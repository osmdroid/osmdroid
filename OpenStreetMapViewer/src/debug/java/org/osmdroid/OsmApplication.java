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

    @Override
    protected void onCreation() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

}
