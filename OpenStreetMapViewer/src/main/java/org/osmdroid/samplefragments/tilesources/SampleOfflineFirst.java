package org.osmdroid.samplefragments.tilesources;

/**
 * Offline First demo
 * @since 6.1.0
 * @author Fabrice Fontaine
 */
public class SampleOfflineFirst extends SampleOfflinePriority {

    @Override
    protected boolean isOfflineFirst() {
        return true;
    }
}
