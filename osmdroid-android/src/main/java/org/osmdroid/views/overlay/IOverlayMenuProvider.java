package org.osmdroid.views.overlay;

import android.view.Menu;
import android.view.MenuItem;

import org.osmdroid.views.MapView;

public interface IOverlayMenuProvider {
    public boolean onCreateOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
                                       final MapView pMapView);

    public boolean onPrepareOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
                                        final MapView pMapView);

    public boolean onOptionsItemSelected(final MenuItem pItem, final int pMenuIdOffset,
                                         final MapView pMapView);

    /**
     * Can be used to signal to external callers that this Overlay should not be used for providing
     * option menu items.
     */
    public boolean isOptionsMenuEnabled();

    public void setOptionsMenuEnabled(final boolean pOptionsMenuEnabled);
}
