/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import org.junit.Assert;
import org.osmdroid.debug.CacheAnalyzerActivity;
import org.osmdroid.tileprovider.util.Counters;

public class CacheAnalyzerTest extends ActivityInstrumentationTestCase2<CacheAnalyzerActivity> {

    public CacheAnalyzerTest() {
        super("org.osmdroid", CacheAnalyzerActivity.class);
    }

    public void testActivity() throws Throwable{
        Counters.reset();
        final CacheAnalyzerActivity activity = getActivity();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView cacheStats = activity.findViewById(org.osmdroid.R.id.cacheStats);

                String txt=cacheStats.getText().toString();
                Assert.assertNotEquals(txt, activity.getString(org.osmdroid.R.string.loading_stats));
                activity.finish();
            }
        });

    }
}

