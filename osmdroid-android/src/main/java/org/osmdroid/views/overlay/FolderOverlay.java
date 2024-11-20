package org.osmdroid.views.overlay;

import android.content.Context;

/**
 * A {@link org.osmdroid.views.overlay.FolderOverlay} is a group of other {@link org.osmdroid.views.overlay.Overlay}s,
 * allowing for easier management of multiple overlays.
 *
 * <p>This class inherits from {@link BaseFolderOverlay} and serves as a container for multiple overlays, such as markers,
 * polygons, and polylines, enabling their combined management (e.g., drawing, interaction, visibility).</p>
 *
 * <img alt="Class diagram around Marker class" width="686" height="413" src='./doc-files/marker-classes.png' />
 *
 * @author M.Kergall
 */
public class FolderOverlay extends BaseFolderOverlay<Overlay> {

    /**
     * Deprecated constructor that accepts a {@link Context}.
     *
     * <p>Use {@link #FolderOverlay()} instead. This constructor is kept for backward compatibility.</p>
     *
     * @param ctx the context in which this overlay operates.
     * @deprecated Use {@link #FolderOverlay()} instead.
     */
    @Deprecated
    public FolderOverlay(Context ctx) {
        this();
    }

    /**
     * Default constructor for creating a {@link FolderOverlay}.
     *
     * <p>This constructor initializes an empty folder overlay that can contain other {@link Overlay} objects.</p>
     *
     * <p>Example of initialization:</p>
     * <pre>
     * // Creating a FolderOverlay
     * FolderOverlay folderOverlay = new FolderOverlay();
     *
     * // Creating and adding various overlays (e.g., markers, polygons)
     * Marker marker = new Marker(mapView);
     * marker.setPosition(new GeoPoint(48.8588443, 2.2943506)); // Set marker to Eiffel Tower
     * folderOverlay.add(marker);
     *
     * // Add the folder overlay to the map
     * mapView.getOverlays().add(folderOverlay);
     * </pre>
     */
    public FolderOverlay() {
        super(Overlay.class);
    }
}