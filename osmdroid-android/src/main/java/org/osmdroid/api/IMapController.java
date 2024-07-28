package org.osmdroid.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An interface that resembles the Google Maps API MapController class and is implemented by the
 * osmdroid {@link org.osmdroid.views.MapController} class.
 *
 * @author Neil Boyd
 */
public interface IMapController {

    void animateTo(@NonNull IGeoPoint geoPoint);
    /** Start animating the map towards the given point */
    void animateTo(double lat, double lon);

    void animateTo(int x, int y);

    void scrollBy(int x, int y);

    void setCenter(@NonNull IGeoPoint point);
    void setCenter(double lat, double lon);

    @Deprecated
    int setZoom(int zoomLevel);

    /**
     * @since 6.0
     */
    double setZoom(double pZoomLevel);

    void stopAnimation(boolean jumpToFinish);

    void stopPanning();

    /**
     * zooms in 1 whole map zoom level
     *
     * @return
     */
    boolean zoomIn();

    /**
     * zooms in 1 whole map zoom level with an adjustable zoom in animation speed
     *
     * @param animationSpeed in ms, if null the default is used
     * @return
     * @since 6.0
     */
    boolean zoomIn(@Nullable Long animationSpeed);

    boolean zoomInFixing(int xPixel, int yPixel, @Nullable Long zoomAnimation);

    /**
     * zooms in and centers the map to the given canvas coordinates
     *
     * @param xPixel
     * @param yPixel
     * @return
     */
    boolean zoomInFixing(int xPixel, int yPixel);

    /**
     * zooms out 1 whole  map zoom level with adjustable zoom speed
     *
     * @param animationSpeed in ms, if null the default is used
     * @return
     * @since 6.0
     */
    boolean zoomOut(@Nullable Long animationSpeed);

    /**
     * zooms out 1 whole map zoom level
     *
     * @return
     */
    boolean zoomOut();

    /**
     * zooms out while centering the map canvas coordinates
     *
     * @param xPixel
     * @param yPixel
     * @return
     */
    boolean zoomOutFixing(int xPixel, int yPixel);

    /**
     * zooms to the given zoom level (whole number) and animates the zoom motion
     *
     * @param zoomLevel 0-Max zoom of the current map tile source, typically 22 or less
     * @return
     */
    @Deprecated
    boolean zoomTo(int zoomLevel);

    /**
     * zooms to the given zoom level (whole number) and animates the zoom motion with adjustable zoom speed
     *
     * @param zoomLevel      0-Max zoom of the current map tile source, typically 22 or less
     * @param animationSpeed if null, the default is used
     * @return
     * @since 6.0
     */
    boolean zoomTo(int zoomLevel, @Nullable Long animationSpeed);

    boolean zoomToFixing(int zoomLevel, int xPixel, int yPixel, @Nullable Long zoomAnimationSpeed);

    boolean zoomTo(double pZoomLevel, @Nullable Long animationSpeed);

    /**
     * zooms to the given zoom level
     *
     * @param pZoomLevel any real number between 0 and max zoom of the current tile source, typically 22 or less
     * @return
     */
    boolean zoomTo(double pZoomLevel);

    @Deprecated
    boolean zoomToFixing(int zoomLevel, int xPixel, int yPixel);

    boolean zoomToFixing(double zoomLevel, int xPixel, int yPixel, @Nullable Long zoomAnimationSpeed);

    /**
     * @since 6.0
     */
    boolean zoomToFixing(double pZoomLevel, int pXPixel, int pYPixel);

    @Deprecated
    void zoomToSpan(int latSpanE6, int lonSpanE6);

    void zoomToSpan(double latSpan, double lonSpan);

    /**
     * @param point
     * @param pZoom
     * @param pSpeed
     * @since 6.0.2
     */
    void animateTo(@NonNull IGeoPoint point, Double pZoom, Long pSpeed);

    void animateTo(@NonNull Double pointLat, @NonNull Double pointLon, Double pZoom, Long pSpeed);

    /**
     * @since 6.0.3
     */
    void animateTo(@NonNull IGeoPoint point, Double pZoom, Long pSpeed, Float pOrientation);

    void animateTo(@NonNull Double pointLat, @NonNull Double pointLon, Double pZoom, Long pSpeed, Float pOrientation);

    /**
     * @since 6.1.0
     */
    void animateTo(@NonNull IGeoPoint point, Double pZoom, Long pSpeed, Float pOrientation, Boolean pClockwise);

    /** @since 6.1.18 */
    void animateTo(double pointLat, double pointLon, Double pZoom, Long pSpeed, Float pOrientation, Boolean pClockwise);
}
