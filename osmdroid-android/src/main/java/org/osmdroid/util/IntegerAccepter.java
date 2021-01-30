package org.osmdroid.util;

/**
 * A repository for integers
 *
 * @author Fabrice Fontaine
 * @since 6.2.0
 */
public class IntegerAccepter {

    private final int[] mValues;
    private int mIndex;

    public IntegerAccepter(final int pSize) {
        mValues = new int[pSize];
    }

    public void init() {
        mIndex = 0;
    }

    public void add(final int pInteger) {
        mValues[mIndex++] = pInteger;
    }

    public int getValue(final int pIndex) {
        return mValues[pIndex];
    }

    public void end() {
    }

    public void flush() {
        mIndex = 0;
    }
}
