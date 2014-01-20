package org.osmdroid.test;

import android.test.ActivityInstrumentationTestCase2;

public class MapActivityTest extends ActivityInstrumentationTestCase2<MapActivity> {

    public HelloAndroidActivityTest() {
        super(MapActivity.class);
    }

    public void testActivity() {
        MapActivity activity = getActivity();
        assertNotNull(activity);
    }
}

