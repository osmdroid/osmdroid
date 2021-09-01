/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmdroid.samplefragments.cache;

import android.os.Bundle;

import org.osmdroid.config.Configuration;
import org.osmdroid.samplefragments.BaseSampleFragment;

/**
 * An example on increasing the in memory tile cache. This is NOT the disk cache!
 * <p>
 * Caution, setting these values too high may cause OOM errors on less capable devices!
 *
 * @author alex
 */
public class SampleJumboCache extends BaseSampleFragment {

    public SampleJumboCache() {
        Configuration.getInstance().setCacheMapTileCount((short) 12);
        Configuration.getInstance().setCacheMapTileOvershoot((short) 12);
    }
    // ===========================================================
    // Constants
    // ===========================================================

    public static final String TITLE = "Jumbo Memory Cache";

    @Override
    public String getSampleTitle() {
        return TITLE;
    }

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();

    }

    @Override
    public void onPause() {
        super.onPause();
        //reset the defaults
        Configuration.getInstance().setCacheMapTileCount((short) 9);
        Configuration.getInstance().setCacheMapTileOvershoot((short) 0);
    }
}
