package org.osmdroid.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Compute a map tile area from a map tile area source
 *
 * @author Fabrice Fontaine
 * @since 6.0.3
 */

public interface MapTileAreaComputer {

    MapTileArea computeFromSource(@NonNull MapTileArea pSource, @Nullable MapTileArea pReuse);
}
