package org.osmdroid.views.drawing;

import android.os.Handler;
import android.os.Message;

import org.osmdroid.tileprovider.MapTileProviderBase;

/**
 * Custom-made {@link Handler} for {@link MapSnapshot}
 *
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class MapSnapshotHandler extends Handler {

    private MapSnapshot mMapSnapshot;

    public MapSnapshotHandler(final MapSnapshot pMapSnapshot) {
        super();
        mMapSnapshot = pMapSnapshot;
    }

    @Override
    public void handleMessage(final Message msg) {
        switch (msg.what) {
            case MapTileProviderBase.MAPTILE_SUCCESS_ID:
                final MapSnapshot mapSnapshot = mMapSnapshot;
                if (mapSnapshot != null) { // in case it was destroyed just before
                    mapSnapshot.refreshASAP();
                }
                break;
        }
    }

    public void destroy() {
        mMapSnapshot = null;
    }
}
