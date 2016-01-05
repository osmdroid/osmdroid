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

