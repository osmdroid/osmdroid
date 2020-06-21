/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.test;

import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.debug.browser.CacheBrowserActivity;
import org.osmdroid.tileprovider.util.Counters;


@RunWith(AndroidJUnit4.class)
public class CacheBrowserTest {

    @Rule
    public final ActivityTestRule<CacheBrowserActivity> activityTestRule =
            new ActivityTestRule<>(CacheBrowserActivity.class, true, false);

    @Test
    public void testActivity() {
        Counters.reset();
        CacheBrowserActivity activity = activityTestRule.getActivity();

        //TODO
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        activity.finish();
    }
}

