package org.osmdroid.forge.app;

import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;
import android.test.UiThreadTest;

import org.osmdroid.util.GeoPoint;

import static org.junit.Assert.*;

/**
 * Created by alex on 1/29/16.
 */
public class MainActivityTest  extends ActivityInstrumentationTestCase2<MainActivity> {



    public MainActivityTest() {
        super(MainActivity.class);
    }

    @UiThreadTest
    public void testActivity() {
        final MainActivity activity = getActivity();
        assertNotNull(activity);
        assertNotNull(activity.mMap);

        activity.mMap.getController().setZoom(3);
        activity.mMap.getController().setCenter(new GeoPoint(0d,0d));

        //activity.mMap.getOverlayManager().getTilesOverlay()



    }
}