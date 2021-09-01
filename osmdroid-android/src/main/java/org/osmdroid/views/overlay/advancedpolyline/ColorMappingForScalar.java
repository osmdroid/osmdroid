package org.osmdroid.views.overlay.advancedpolyline;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract {@link ColorMapping} populated by scalar data
 *
 * @author Fabrice Fontaine
 * @since 6.2.0
 */
public abstract class ColorMappingForScalar implements ColorMapping {

    private final List<Integer> mColors = new ArrayList<>();

    @Override
    public int getColorForIndex(int pSegmentIndex) {
        return mColors.get(pSegmentIndex);
    }

    public void add(final float pScalar) {
        mColors.add(computeColor(pScalar));
    }

    protected abstract int computeColor(final float pScalar);

    /**
     * @since 6.1.7
     */
    protected void set(final int pIndex, final float pScalar) {
        mColors.set(pIndex, computeColor(pScalar));
    }
}
