package org.osmdroid.samplefragments.tilesources;

/**
 * Offline Second demo
 *
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class SampleOfflineSecond extends SampleOfflinePriority {

    @Override
    protected boolean isOfflineFirst() {
        return false;
    }
}
