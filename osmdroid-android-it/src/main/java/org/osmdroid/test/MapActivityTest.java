/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.test;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.StarterMapActivity;
import org.osmdroid.tileprovider.util.Counters;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class MapActivityTest {

    @Rule
    public final ActivityTestRule<StarterMapActivity> activityTestRule =
            new ActivityTestRule<>(StarterMapActivity.class, true, false);

    @Test
    public void testActivity() {
        Counters.reset();
        StarterMapActivity activity = activityTestRule.getActivity();
        assertNotNull(activity);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }
        Counters.printToLogcat();
        if (Counters.countOOM > 0 || Counters.fileCacheOOM > 0) {
            Assert.fail("OOM Detected, aborting!");
        }
        activity.finish();
    }
}

