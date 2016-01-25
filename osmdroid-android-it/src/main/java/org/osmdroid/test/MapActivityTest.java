/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */
package org.osmdroid.test;

import org.osmdroid.StarterMapActivity;

import android.test.ActivityInstrumentationTestCase2;

public class MapActivityTest extends ActivityInstrumentationTestCase2<StarterMapActivity> {

    public MapActivityTest() {
        super("org.osmdroid", StarterMapActivity.class);
    }

    public void testActivity() {
        StarterMapActivity activity = getActivity();
        assertNotNull(activity);
    }
}

