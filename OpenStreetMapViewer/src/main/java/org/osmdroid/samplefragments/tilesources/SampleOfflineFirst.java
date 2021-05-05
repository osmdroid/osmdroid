package org.osmdroid.samplefragments.tilesources;

/**
 * Offline First demo
 *
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class SampleOfflineFirst extends SampleOfflinePriority {

    @Override
    protected boolean isOfflineFirst() {
        return true;
    }
}
