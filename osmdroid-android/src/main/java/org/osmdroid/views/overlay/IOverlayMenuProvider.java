package org.osmdroid.views.overlay;

import android.view.Menu;
import android.view.MenuItem;

import org.osmdroid.views.MapView;

import androidx.annotation.NonNull;

public interface IOverlayMenuProvider {

    boolean onCreateOptionsMenu(Menu pMenu, int pMenuIdOffset, @NonNull MapView pMapView);

    boolean onPrepareOptionsMenu(Menu pMenu, int pMenuIdOffset, @NonNull MapView pMapView);

    boolean onOptionsItemSelected(MenuItem pItem, int pMenuIdOffset, @NonNull MapView pMapView);

    /** Can be used to signal to external callers that this Overlay should not be used for providing option menu items */
    boolean isOptionsMenuEnabled();

    void setOptionsMenuEnabled(boolean pOptionsMenuEnabled);

}
