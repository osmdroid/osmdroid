package org.osmdroid.views.overlay.mocks;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.Overlay;

/**
 * A test implementation of {@link Overlay} used to simulate polygon overlays for testing purposes.
 * This mock class allows for setting and retrieving geographical bounds using a {@link BoundingBox}.
 *
 * @author Daniil Timofeev (hestonic)
 */
public class TestPolygon extends Overlay {
    private final BoundingBox bounds;

    /**
     * Constructs a {@link TestPolygon} with the specified {@link BoundingBox}.
     *
     * @param bounds The geographical bounds to be associated with this polygon.
     */
    public TestPolygon(BoundingBox bounds) {
        this.bounds = bounds;
    }

    /**
     * Returns the {@link BoundingBox} associated with this polygon.
     *
     * @return The {@link BoundingBox} representing the geographical bounds of this polygon.
     */
    @Override
    public BoundingBox getBounds() {
        return this.bounds;
    }
}
