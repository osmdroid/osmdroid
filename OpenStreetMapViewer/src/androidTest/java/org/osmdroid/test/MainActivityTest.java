/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.test;



import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.osmdroid.ExtraSamplesActivity;
import org.osmdroid.MainActivity;
import org.osmdroid.tileprovider.util.Counters;

import androidx.test.rule.ActivityTestRule;

import static org.junit.Assert.assertNotNull;

public class MainActivityTest  {
    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testActivity() {
        Counters.reset();
        MainActivity activity = activityRule.getActivity();
        assertNotNull(activity);
        Counters.printToLogcat();
        if (Counters.countOOM > 0 || Counters.fileCacheOOM > 0) {
            Assert.fail("OOM Detected, aborting!");
        }
        activity.finish();
    }
}

