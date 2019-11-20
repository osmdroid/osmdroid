package org.osmdroid.util;

/**
 * Created by Fabrice on 03/01/2018.
 * @since 6.0.0
 */

public abstract class LineBuilder implements PointAccepter {

    private final float[] mLines;
    private final int[] mLinesIndex;
    private int mIndex;

    public LineBuilder(final int pMaxSize) {
        mLines = new float[pMaxSize];
        mLinesIndex = new int[pMaxSize];
    }

    @Override
    public void init() {
        mIndex = 0;
    }

    @Override
    public void add(final long pX, final long pY, int index) {

        // check for multiple of four
        if(mIndex % 4 == 0) {
            mLinesIndex[mIndex / 4] = (index / 2) - 1;
        }

        mLines[mIndex ++] = pX;
        mLines[mIndex ++] = pY;
        if (mIndex >= mLines.length) {
            innerFlush();
        }
    }

    public int[] getColorIndexes() {
        return  mLinesIndex;
    }

    @Override
    public void end() {
        innerFlush();
    }

    public float[] getLines() {
        return mLines;
    }

    public int getSize() {
        return mIndex;
    }

    private void innerFlush() {
        if (mIndex > 0) {
            flush();
        }
        mIndex = 0;
    }

    public abstract void flush();
}
