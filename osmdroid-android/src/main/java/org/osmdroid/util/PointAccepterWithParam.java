package org.osmdroid.util;

/**
 * Class PointAccepterWithParam adds additional add function with index.
 */
public interface PointAccepterWithParam extends PointAccepter {

    void add(final long pX, final long pY, final int index);
}
