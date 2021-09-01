package org.osmdroid.views.overlay.advancedpolyline;

/**
 * Abstract base class for all implemented color mappings.
 *
 * @author Matthias Dittmer
 */
public interface ColorMapping {
    int getColorForIndex(final int pSegmentIndex);
}
