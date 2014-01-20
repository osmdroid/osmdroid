package org.osmdroid.test;

import org.osmdroid.MapActivity;

import android.test.ActivityInstrumentationTestCase2;

public class MapActivityTest extends ActivityInstrumentationTestCase2<MapActivity> {

    public MapActivityTest() {
        super("org.osmdroid", MapActivity.class);
    }

    public void testActivity() {
        MapActivity activity = getActivity();
        assertNotNull(activity);
    }
}

