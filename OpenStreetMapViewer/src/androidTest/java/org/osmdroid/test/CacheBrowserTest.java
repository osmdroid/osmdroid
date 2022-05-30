/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.test;

import org.junit.Rule;
import org.junit.Test;
import org.osmdroid.debug.CacheAnalyzerActivity;
import org.osmdroid.debug.browser.CacheBrowserActivity;
import org.osmdroid.tileprovider.util.Counters;

import androidx.test.rule.ActivityTestRule;

public class CacheBrowserTest {

    @Rule
    public ActivityTestRule<CacheBrowserActivity> activityRule =
            new ActivityTestRule<>(CacheBrowserActivity.class);

    @Test
    public void testActivity() {
        Counters.reset();
        CacheBrowserActivity activity = activityRule.getActivity();

        //TODO
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        activity.finish();
    }
}

