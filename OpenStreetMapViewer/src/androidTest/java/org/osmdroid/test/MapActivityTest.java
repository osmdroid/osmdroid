/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.test;


import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.osmdroid.MainActivity;
import org.osmdroid.StarterMapActivity;
import org.osmdroid.tileprovider.util.Counters;

import androidx.test.rule.ActivityTestRule;

import static org.junit.Assert.assertNotNull;

public class MapActivityTest {


    @Rule
    public ActivityTestRule<StarterMapActivity> activityRule =
            new ActivityTestRule<>(StarterMapActivity.class);
    @Test
    public void testActivity() {
        Counters.reset();
        StarterMapActivity activity  =activityRule.getActivity();
        assertNotNull(activity);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        Counters.printToLogcat();
        if (Counters.countOOM > 0 || Counters.fileCacheOOM > 0) {
            Assert.fail("OOM Detected, aborting!");
        }
        activity.finish();
    }
}

