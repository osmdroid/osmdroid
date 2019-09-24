package org.osmdroid.samplefragments.tilesources;

/**
 * Offline Second demo
 * @since 6.1.0
 * @author Fabrice Fontaine
 */
public class SampleOfflineSecond extends SampleOfflinePriority {

    @Override
    protected boolean isOfflineFirst() {
        return false;
    }
}
