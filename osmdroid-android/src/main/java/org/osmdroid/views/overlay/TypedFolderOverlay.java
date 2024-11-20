package org.osmdroid.views.overlay;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic folder overlay that groups multiple overlays of a specific type.
 *
 * <p>{@link TypedFolderOverlay} extends {@link BaseFolderOverlay} and allows for type-specific
 * management of overlays. This class ensures that only overlays of type {@code T} can be added
 * to the folder, providing type safety and better control over the contained overlays.</p>
 *
 * @param <T> the specific type of {@link Overlay} that this folder manages.
 *
 * @author Daniil Timofeev (hestonic)
 */
public class TypedFolderOverlay<T extends Overlay> extends BaseFolderOverlay<T> {

    /**
     * Constructs a new {@link TypedFolderOverlay} with the specified overlay type.
     *
     * <p>This constructor ensures that only overlays of the specified type can be added to this folder overlay.</p>
     *
     * <p>Example of initialization:</p>
     * <pre>
     * // Creating a TypedFolderOverlay for Marker overlays
     * TypedFolderOverlay<Marker> markerFolder = new TypedFolderOverlay<>(Marker.class);
     *
     * // Adding a marker to the folder
     * Marker marker = new Marker(mapView);
     * marker.setPosition(new GeoPoint(48.8588443, 2.2943506)); // Set marker to Eiffel Tower
     * markerFolder.add(marker);
     *
     * // Adding the folder to the map
     * mapView.getOverlays().add(markerFolder);
     * </pre>
     *
     * @param clazz the class of the overlay type {@code T}.
     */
    public TypedFolderOverlay(Class<T> clazz) {
        super(clazz);
    }
}