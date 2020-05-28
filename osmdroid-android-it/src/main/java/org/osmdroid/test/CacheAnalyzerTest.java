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
import android.widget.TextView;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.debug.CacheAnalyzerActivity;
import org.osmdroid.tileprovider.util.Counters;

@RunWith(AndroidJUnit4.class)
public class CacheAnalyzerTest {

    @Rule
    public final ActivityTestRule<CacheAnalyzerActivity> activityTestRule =
            new ActivityTestRule<>(CacheAnalyzerActivity.class, true, false);

    @Test
    public void testActivity() {
        Counters.reset();
        final CacheAnalyzerActivity activity = activityTestRule.getActivity();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        activityTestRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView cacheStats = activity.findViewById(org.osmdroid.R.id.cacheStats);

                String txt = cacheStats.getText().toString();
                Assert.assertNotEquals(txt, activity.getString(org.osmdroid.R.string.loading_stats));
                activity.finish();
            }
        });

    }
}

