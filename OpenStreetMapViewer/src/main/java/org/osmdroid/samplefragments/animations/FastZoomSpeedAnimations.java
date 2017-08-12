package org.osmdroid.samplefragments.animations;

import org.osmdroid.config.Configuration;
import org.osmdroid.samplefragments.BaseSampleFragment;

/**
 * created on 8/11/2017.
 *
 * @author Alex O'Ree
 */

public class FastZoomSpeedAnimations extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "Super fast zoom speed";
    }

    int originalShortAnimationSpeed;
    int originalDefaultSpeed;


    @Override
    public void onResume() {
        super.onResume();
        originalDefaultSpeed = Configuration.getInstance().getAnimationSpeedDefault();
        originalShortAnimationSpeed = Configuration.getInstance().getAnimationSpeedShort();
        Configuration.getInstance().setAnimationSpeedShort(100);
        Configuration.getInstance().setAnimationSpeedDefault(100);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

}
