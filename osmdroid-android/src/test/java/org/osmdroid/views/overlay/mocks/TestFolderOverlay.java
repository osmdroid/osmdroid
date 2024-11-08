package org.osmdroid.views.overlay.mocks;

import org.osmdroid.views.overlay.BaseFolderOverlay;
import org.osmdroid.views.overlay.Overlay;

/**
 * A test implementation of {@link BaseFolderOverlay}
 *
 * @author Daniil Timofeev (hestonic)
 */
public class TestFolderOverlay extends BaseFolderOverlay<Overlay> {
    public TestFolderOverlay() {
        super(Overlay.class);
    }
}
