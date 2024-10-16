package org.osmdroid.views.overlay;

import android.content.Context;

/**
 * A {@link org.osmdroid.views.overlay.FolderOverlay} is just a group of other {@link org.osmdroid.views.overlay.Overlay}s.
 *
 * <img alt="Class diagram around Marker class" width="686" height="413" src='./doc-files/marker-classes.png' />
 *
 * @author M.Kergall
 */
public class FolderOverlay extends BaseFolderOverlay<Overlay> {

    /**
     * Use {@link #FolderOverlay()} instead
     */
    @Deprecated
    public FolderOverlay(Context ctx) {
        this();
    }

    public FolderOverlay() {
        super(Overlay.class);
    }
}