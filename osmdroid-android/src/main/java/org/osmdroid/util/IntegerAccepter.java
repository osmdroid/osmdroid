package org.osmdroid.util;

/**
 * A repository for integers
 * @since 6.2.0
 * @author Fabrice Fontaine
 */
public class IntegerAccepter {

    private final int[] mValues;
    private int mIndex;
    private int mFirstValue;
    private boolean mIsEnded;

    public IntegerAccepter(final int pSize) {
        mValues = new int[pSize];
    }

    public void init() {
        mIndex = 0;
        mIsEnded = false;
    }

    public void add(final int pInteger) {
        if (mIndex == 0) {
            mFirstValue = pInteger;
        }
        mValues[mIndex++] = pInteger;
    }

    public int getValue(final int pIndex) {
        //if (pIndex < mIndex) {
        return mValues[pIndex];
        //}
        // TODO handle the very last segment+1 case
    }

    public void end() {
        mIsEnded = true;
    }

    public void flush() {
        mIndex = 0;
    }
}
