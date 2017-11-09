package org.osmdroid.tests;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.BuildConfig;
import org.osmdroid.MainActivity;
import org.osmdroid.StarterMapActivity;
import org.osmdroid.tileprovider.util.Counters;
import org.osmdroid.views.MapView;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)

public class StarterMapActivityTest
{
    private StarterMapActivity activity;

    @Before
    public void setUp() throws Exception
    {
        Counters.reset();
        activity = Robolectric.buildActivity( StarterMapActivity.class )
                .create()
                .resume()
                .get();
    }

    @Test
    public void shouldNotBeNull() throws Exception
    {
        assertNotNull( activity );
    }

    @Test
    public void shouldHaveMapView() throws Exception
    {
        MapView mv =  activity.getMapView();
        assertNotNull(mv);
        Counters.printToLogcat();
        if (Counters.countOOM > 0 || Counters.fileCacheOOM > 0){
            Assert.fail("OOM Detected, aborting!");
        }
    }
}