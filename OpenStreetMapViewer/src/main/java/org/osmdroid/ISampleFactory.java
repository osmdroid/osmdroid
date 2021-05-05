package org.osmdroid;

import org.osmdroid.samplefragments.BaseSampleFragment;

/**
 * Created by alex on 6/29/16.
 */
public interface ISampleFactory {

    public BaseSampleFragment getSample(int index);

    public int count();
}
