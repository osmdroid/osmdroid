package org.osmdroid.views.overlay;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link org.osmdroid.views.overlay.FolderOverlay} is just a group of other {@link org.osmdroid.views.overlay.Overlay}s.
 *
 * <img alt="Class diagram around Marker class" width="686" height="413" src='./doc-files/marker-classes.png' />
 *
 * @author Daniil Timofeev (hestonic)
 */
public class TypedFolderOverlay<T extends Overlay> extends BaseFolderOverlay<T> {

    public TypedFolderOverlay(Class<T> clazz) {
        super(clazz);
    }
}