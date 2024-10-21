package org.osmdroid.views.overlay.mocks;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.Overlay;

/**
 * A test implementation of {@link Overlay}
 *
 * @author Daniil Timofeev (hestonic)
 */
public class TestOverlay extends Overlay {
    private final BoundingBox bounds;

    /**
     * Constructs a {@link TestOverlay} with the specified {@link BoundingBox}.
     *
     * @param bounds The geographical bounds to associate with this overlay.
     */
    public TestOverlay(BoundingBox bounds) {
        this.bounds = bounds;
    }

    /**
     * Returns the {@link BoundingBox} associated with this overlay.
     *
     * @return The {@link BoundingBox} representing the geographical bounds of this overlay.
     */
    @Override
    public BoundingBox getBounds() {
        return this.bounds;
    }
}
