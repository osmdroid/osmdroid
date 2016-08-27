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
import org.osmdroid.ISampleFactory;
import org.osmdroid.OsmApplication;
import org.osmdroid.bugtestfragments.BugFactory;
import org.osmdroid.samplefragments.*;
import org.osmdroid.tileprovider.util.Counters;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ExtraSamplesTest extends ActivityInstrumentationTestCase2<ExtraSamplesActivity> {

    public ExtraSamplesTest() {
        super("org.osmdroid", ExtraSamplesActivity.class);
    }

    /**
     * This tests every sample fragment in the app. See implementation notes on how to increase
     * the duration and iteration count for longer running tests and memory leak testing
     */
    public void testActivity() {
        ISampleFactory sampleFactory = SampleFactory.getInstance();
        executeTest(sampleFactory);
    }

    /**
     * This tests every bug driver fragment in the app. See implementation notes on how to increase
     * the duration and iteration count for longer running tests and memory leak testing
     */
    public void testBugsDriversActivity() {
        ISampleFactory sampleFactory = BugFactory.getInstance();
        executeTest(sampleFactory);
    }

    private void executeTest(ISampleFactory sampleFactory){
        Counters.reset();
        final ExtraSamplesActivity activity = getActivity();
        assertNotNull(activity);
        final FragmentManager fm = activity.getSupportFragmentManager();
        Fragment frag = (fm.findFragmentByTag(ExtraSamplesActivity.SAMPLES_FRAGMENT_TAG));
        assertNotNull(frag);

        assertTrue(frag instanceof FragmentSamples);
        //FragmentSamples samples = (FragmentSamples) frag;


        int[] fireOrder = new int[sampleFactory.count()];
        for (int i = 0; i < sampleFactory.count(); i++) {
            fireOrder[i]=i;
        }
        shuffleArray(fireOrder);

        Log.i(FragmentSamples.TAG, "Memory allocation: INIT Free: " + Runtime.getRuntime().freeMemory() + " Total:" + Runtime.getRuntime().totalMemory() + " Max:" + Runtime.getRuntime().maxMemory());
        for (int i = 0; i < sampleFactory.count(); i++) {

            for (int k = 0; k < 5; k++) {
                Log.i(FragmentSamples.TAG, k + "Memory allocation: Before load: Free: " + Runtime.getRuntime().freeMemory() + " Total:" + Runtime.getRuntime().totalMemory() + " Max:" + Runtime.getRuntime().maxMemory());
                final BaseSampleFragment basefrag = sampleFactory.getSample(fireOrder[i]);
                Log.i(FragmentSamples.TAG, "loading fragment ("+i+"/" + sampleFactory.count()+") run " +k +" " + basefrag.getSampleTitle() + ", " + frag.getClass().getCanonicalName());

                Counters.printToLogcat();
                if (Counters.countOOM > 0 || Counters.fileCacheOOM > 0) {
                    OsmApplication.writeHprof();
                    Assert.fail("OOM Detected, aborting! this test run was " + basefrag.getSampleTitle() + ", " + basefrag.getClass().getCanonicalName() + " iteration " + k);
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            fm.beginTransaction().replace(org.osmdroid.R.id.samples_container, basefrag, ExtraSamplesActivity.SAMPLES_FRAGMENT_TAG)
                                    .addToBackStack(ExtraSamplesActivity.SAMPLES_FRAGMENT_TAG).commit();
                            fm.executePendingTransactions();
                            //this sleep is here to give the fragment enough time to start up and do something

                        } catch (Exception oom) {
                            Assert.fail("Error popping fragment " + basefrag.getSampleTitle() + basefrag.getClass().getCanonicalName() + oom);
                        }

                    }
                });
                try {
                    Thread.sleep(2000);
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
                } catch (Exception e) {
                    e.printStackTrace();
                }


                System.gc();
                Log.i(FragmentSamples.TAG, "Memory allocation: END Free: " + Runtime.getRuntime().freeMemory() + " Total:" + Runtime.getRuntime().totalMemory() + " Max:" + Runtime.getRuntime().maxMemory());
            }
        }
    }

    /**
     * src http://stackoverflow.com/questions/1519736/random-shuffling-of-an-array
     * Implementing Fisher–Yates shuffle
     * @param ar
     */
    static void shuffleArray(int[] ar)
    {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
}

