package org.osmdroid.views.overlay.mocks;

import org.osmdroid.views.overlay.OverlayWithIW;

/**
 * A test implementation of {@link OverlayWithIW}
 *
 * @author Daniil Timofeev (hestonic)
 */
public class TestOverlayWithIW extends OverlayWithIW {
    private boolean isClosed = false;

    /**
     * Simulates closing the information window by setting the isClosed flag to true.
     */
    @Override
    public void closeInfoWindow() {
        isClosed = true;
    }

    /**
     * Returns whether the information window has been closed.
     *
     * @return true if the information window is closed, false otherwise.
     */
    public boolean isClosed() {
        return isClosed;
    }
}
