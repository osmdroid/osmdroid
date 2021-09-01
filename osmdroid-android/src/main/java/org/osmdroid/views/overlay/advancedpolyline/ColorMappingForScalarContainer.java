package org.osmdroid.views.overlay.advancedpolyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Scalar container on top of any ColorMappingForScalar
 * Typical use:
 * * create the {@link ColorMappingForScalar} you need
 * * create a ColorMappingForScalarContainer on top of it
 * * add your scalars to the container
 * If you need to adjust your color mapping because you have a better idea of the actual scalar range
 * * first adjust the settings
 * (e.g. with {@link ColorMappingVariation#init(float, float, float, float)})
 * * then call {@link #refresh()} and the colors ({@link ColorMapping#getColorForIndex(int)})
 * will reflect the new set-up
 * cf. https://github.com/osmdroid/osmdroid/issues/1551
 *
 * @author Fabrice Fontaine
 * @since 6.1.7
 */
public class ColorMappingForScalarContainer {

    private final ColorMappingForScalar mInnerMapping;
    private final List<Float> mScalars = new ArrayList<>();
    private float mScalarMin = Float.MAX_VALUE;
    private float mScalarMax = Float.MIN_VALUE;

    public ColorMappingForScalarContainer(final ColorMappingForScalar pInnerMapping) {
        mInnerMapping = pInnerMapping;
    }

    public ColorMappingForScalar getMappingForScalar() {
        return mInnerMapping;
    }

    public int size() {
        return mScalars.size();
    }

    public float getScalarMin() {
        return mScalarMin;
    }

    public float getScalarMax() {
        return mScalarMax;
    }

    public void add(final float pScalar) {
        mInnerMapping.add(pScalar);
        mScalars.add(pScalar);
        if (mScalarMin > pScalar) {
            mScalarMin = pScalar;
        }
        if (mScalarMax < pScalar) {
            mScalarMax = pScalar;
        }
    }

    public void refresh() {
        int i = 0;
        for (final float scalar : mScalars) {
            mInnerMapping.set(i, scalar);
            i++;
        }
    }
}
