/*
 * WARNING, All test cases exist in osmdroid-android-it/src/main/java (maven project)
 *
 * During build time (with gradle), these tests are copied from osmdroid-android-it to OpenStreetMapViewer/src/androidTest/java
 * DO NOT Modify files in OpenSteetMapViewer/src/androidTest. You will loose your changes when building!
 *
 */


package org.osmdroid.test;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import junit.framework.Assert;

import org.osmdroid.ExtraSamplesActivity;
import org.osmdroid.samplefragments.*;
import org.osmdroid.tileprovider.util.Counters;

public class ExtraSamplesTest extends ActivityInstrumentationTestCase2<ExtraSamplesActivity> {

    public ExtraSamplesTest() {
        super("org.osmdroid", ExtraSamplesActivity.class);
    }
/*
    public void testActivity() {
        //if (Build.VERSION.SDK_INT == 10)
        //    return; //FIXME dirty fix for travis ci
        ExtraSamplesActivity activity = getActivity();
        assertNotNull(activity);
        FragmentManager fm = activity.getSupportFragmentManager();
        Fragment frag = (fm.findFragmentByTag(ExtraSamplesActivity.SAMPLES_FRAGMENT_TAG));
        assertNotNull(frag);

        assertTrue(frag instanceof FragmentSamples);
        //FragmentSamples samples = (FragmentSamples) frag;

        SampleFactory sampleFactory = SampleFactory.getInstance();
        long startFree= Runtime.getRuntime().freeMemory();
        Log.i(FragmentSamples.TAG, "Memory allocation: INIT Free: " + Runtime.getRuntime().freeMemory() +" Total:" + Runtime.getRuntime().totalMemory() +" Max:"+ Runtime.getRuntime().maxMemory());
        for (int i = 0; i < sampleFactory.count(); i++) {
            Log.i(FragmentSamples.TAG, "Memory allocation: Before load: Free: " + Runtime.getRuntime().freeMemory() +" Total:" + Runtime.getRuntime().totalMemory() +" Max:"+ Runtime.getRuntime().maxMemory());
            BaseSampleFragment basefrag = sampleFactory.getSample(i);
            Log.i(FragmentSamples.TAG, "loading fragment " + basefrag.getSampleTitle() + ", " + frag.getClass().getCanonicalName());
            if (Build.VERSION.SDK_INT == 10 && basefrag instanceof SampleJumboCache)
                continue;
            if (Build.VERSION.SDK_INT == 10 && basefrag instanceof SampleOsmPath)
                continue;
            if (Build.VERSION.SDK_INT == 10 && basefrag instanceof SampleMilitaryIcons)
                continue;
            if (Build.VERSION.SDK_INT == 10 && basefrag instanceof SampleGridlines)
                continue;
            if (Build.VERSION.SDK_INT == 10 && basefrag instanceof SampleJumboCache)
                continue;
            try {
                fm.beginTransaction().replace(org.osmdroid.R.id.samples_container, basefrag, ExtraSamplesActivity.SAMPLES_FRAGMENT_TAG)
                        .addToBackStack(null).commit();
                //this sleep is here to give the fragment enough time to start up and do something
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (java.lang.OutOfMemoryError oom) {
                Assert.fail("OOM error! " + sampleFactory.getSample(i).getSampleTitle() + sampleFactory.getSample(i).getClass().getCanonicalName());
            }
            System.gc();
            Log.i(FragmentSamples.TAG, "Memory allocation: END Free: " + Runtime.getRuntime().freeMemory() +" Total:" + Runtime.getRuntime().totalMemory() +" Max:"+ Runtime.getRuntime().maxMemory());
        }
    }

*/

    public void testActivity() {
        final ExtraSamplesActivity activity = getActivity();
        assertNotNull(activity);
        final FragmentManager fm = activity.getSupportFragmentManager();
        Fragment frag = (fm.findFragmentByTag(ExtraSamplesActivity.SAMPLES_FRAGMENT_TAG));
        assertNotNull(frag);

        assertTrue(frag instanceof FragmentSamples);
        //FragmentSamples samples = (FragmentSamples) frag;

        final SampleFactory sampleFactory = SampleFactory.getInstance();
        Log.i(FragmentSamples.TAG, "Memory allocation: INIT Free: " + Runtime.getRuntime().freeMemory() + " Total:" + Runtime.getRuntime().totalMemory() + " Max:" + Runtime.getRuntime().maxMemory());

        for (int i = 0; i < sampleFactory.count(); i++) {

            for (int k = 0; k < 100; k++) {
                Log.i(FragmentSamples.TAG, k + "Memory allocation: Before load: Free: " + Runtime.getRuntime().freeMemory() + " Total:" + Runtime.getRuntime().totalMemory() + " Max:" + Runtime.getRuntime().maxMemory());
                final BaseSampleFragment basefrag = sampleFactory.getSample(i);
                Log.i(FragmentSamples.TAG, "loading fragment " + basefrag.getSampleTitle() + ", " + frag.getClass().getCanonicalName());

                Counters.printToLogcat();
                if (Counters.countOOM > 0 || Counters.fileCacheOOM > 0){
                    Assert.fail("OOM Detected, aborting! this test run was " +  basefrag.getSampleTitle() + ", " + basefrag.getClass().getCanonicalName());
                }


                if (Build.VERSION.SDK_INT <= 10 && basefrag instanceof SampleJumboCache) {
                    continue;
                }
                if (Build.VERSION.SDK_INT <= 10 && basefrag instanceof SampleOsmPath) {
                    continue;
                }
                if (Build.VERSION.SDK_INT <= 10 && basefrag instanceof SampleMilitaryIcons) {
                    continue;
                }
                //if (Build.VERSION.SDK_INT <= 10 && basefrag instanceof SampleGridlines) {
                  //  continue;
                //}

                try {
                    fm.beginTransaction().replace(org.osmdroid.R.id.samples_container, basefrag, ExtraSamplesActivity.SAMPLES_FRAGMENT_TAG)
                            .addToBackStack(ExtraSamplesActivity.SAMPLES_FRAGMENT_TAG).commit();
                    //this sleep is here to give the fragment enough time to start up and do something
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception oom) {
                    Assert.fail("Error popping fragment " + basefrag.getSampleTitle() + basefrag.getClass().getCanonicalName()+oom);
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            fm.popBackStackImmediate();
                        } catch (java.lang.Exception oom) {
                //            Assert.fail("Error popping fragment " + basefrag.getSampleTitle() + basefrag.getClass().getCanonicalName()+oom);
                        }
                    }
                });



                System.gc();
                Log.i(FragmentSamples.TAG, "Memory allocation: END Free: " + Runtime.getRuntime().freeMemory() + " Total:" + Runtime.getRuntime().totalMemory() + " Max:" + Runtime.getRuntime().maxMemory());
            }
        }
    }
}

