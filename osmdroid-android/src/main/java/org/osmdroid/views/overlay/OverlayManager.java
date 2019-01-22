package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import org.osmdroid.api.IMapView;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.drawing.MapSnapshot;

import java.util.List;

public interface OverlayManager extends List<Overlay> {
    Overlay get(int pIndex);

    int size();

    void add(int pIndex, Overlay pElement);

    Overlay remove(int pIndex);

    Overlay set(int pIndex, Overlay pElement);

    /**
     * Gets the optional TilesOverlay class.
     *
     * @return the tilesOverlay
     */
    TilesOverlay getTilesOverlay();

    /**
     * Sets the optional TilesOverlay class. If set, this overlay will be drawn before all other
     * overlays and will not be included in the editable list of overlays and can't be cleared
     * except by a subsequent call to setTilesOverlay().
     *
     * @param tilesOverlay the tilesOverlay to set
     */
    void setTilesOverlay(TilesOverlay tilesOverlay);

    List<Overlay> overlays();

    Iterable<Overlay> overlaysReversed();

    /**
     * If possible, use {@link #onDraw(Canvas, Projection)} instead (cf. {@link MapSnapshot}
     */
    void onDraw(Canvas c, MapView pMapView);

    /**
     * @since 6.1.0
     */
    void onDraw(Canvas c, Projection pProjection);

    void onDetach(MapView pMapView);

    boolean onKeyDown(int keyCode, KeyEvent event, MapView pMapView);

    boolean onKeyUp(int keyCode, KeyEvent event, MapView pMapView);

    boolean onTouchEvent(MotionEvent event, MapView pMapView);

    boolean onTrackballEvent(MotionEvent event, MapView pMapView);

    boolean onSnapToItem(int x, int y, Point snapPoint, IMapView pMapView);

    boolean onDoubleTap(MotionEvent e, MapView pMapView);

    boolean onDoubleTapEvent(MotionEvent e, MapView pMapView);

    boolean onSingleTapConfirmed(MotionEvent e, MapView pMapView);

    boolean onDown(MotionEvent pEvent, MapView pMapView);

    boolean onFling(MotionEvent pEvent1, MotionEvent pEvent2,
                    float pVelocityX, float pVelocityY, MapView pMapView);

    boolean onLongPress(MotionEvent pEvent, MapView pMapView);

    boolean onScroll(MotionEvent pEvent1, MotionEvent pEvent2,
                     float pDistanceX, float pDistanceY, MapView pMapView);

    void onShowPress(MotionEvent pEvent, MapView pMapView);

    boolean onSingleTapUp(MotionEvent pEvent, MapView pMapView);

    void setOptionsMenusEnabled(boolean pEnabled);

    boolean onCreateOptionsMenu(Menu pMenu, int menuIdOffset, MapView mapView);

    boolean onPrepareOptionsMenu(Menu pMenu, int menuIdOffset, MapView mapView);

    boolean onOptionsItemSelected(MenuItem item, int menuIdOffset, MapView mapView);

    /**
     * @since 6.0.0
     */
    void onPause();

    /**
     * @since 6.0.0
     */
    void onResume();

}
