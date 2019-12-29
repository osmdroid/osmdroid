package org.osmdroid.views.overlay.advancedpolyline;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract {@link ColorMapping} populated by scalar data
 * @since 6.2.0
 * @author Fabrice Fontaine
 */
public abstract class ColorMappingForScalar implements ColorMapping{

    private final List<Integer> mColors = new ArrayList<>();

    @Override
    public int getColorForIndex(int pSegmentIndex) {
        return mColors.get(pSegmentIndex);
    }

    public void add(final float pScalar) {
        mColors.add(computeColor(pScalar));
    }

    protected abstract int computeColor(final float pScalar);
}
