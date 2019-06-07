/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.test;

import android.test.ActivityInstrumentationTestCase2;

import org.osmdroid.debug.browser.CacheBrowserActivity;
import org.osmdroid.tileprovider.util.Counters;

public class CacheBrowserTest extends ActivityInstrumentationTestCase2<CacheBrowserActivity> {

    public CacheBrowserTest() {
        super("org.osmdroid", CacheBrowserActivity.class);
    }

    public void testActivity() {
        Counters.reset();
        CacheBrowserActivity activity = getActivity();

        //TODO
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        activity.finish();
    }
}

