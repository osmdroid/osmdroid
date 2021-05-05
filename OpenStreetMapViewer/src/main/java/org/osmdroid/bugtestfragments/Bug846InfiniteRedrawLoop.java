package org.osmdroid.bugtestfragments;

import org.osmdroid.config.Configuration;
import org.osmdroid.samplefragments.BaseSampleFragment;

/**
 * This test case will force the memory cache to be too small, effectively recreating bug
 * 846. See <a href="https://github.com/osmdroid/osmdroid/issues/846">#846</a>.
 * created on 1/15/2018.
 *
 * @author Alex O'Ree
 */

public class Bug846InfiniteRedrawLoop extends BaseSampleFragment {
    public Bug846InfiniteRedrawLoop() {
        Configuration.getInstance().setCacheMapTileCount((short) 0);
        Configuration.getInstance().setCacheMapTileOvershoot((short) -3);
    }

    @Override
    public String getSampleTitle() {
        return "Infinite Redraw Loop";
    }

    @Override
    public void addOverlays() {

        super.addOverlays();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Configuration.getInstance().setCacheMapTileCount((short) 9);
        Configuration.getInstance().setCacheMapTileOvershoot((short) 0);
    }
}
