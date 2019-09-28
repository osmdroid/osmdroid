/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.test;

import org.osmdroid.StarterMapActivity;
import org.osmdroid.tileprovider.util.Counters;

import android.test.ActivityInstrumentationTestCase2;

import junit.framework.Assert;

public class MapActivityTest extends ActivityInstrumentationTestCase2<StarterMapActivity> {

    public MapActivityTest() {
        super("org.osmdroid", StarterMapActivity.class);
    }

    public void testActivity() {
        Counters.reset();
        StarterMapActivity activity = getActivity();
        assertNotNull(activity);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        Counters.printToLogcat();
        if (Counters.countOOM > 0 || Counters.fileCacheOOM > 0){
            Assert.fail("OOM Detected, aborting!");
        }
        activity.finish();
    }
}

