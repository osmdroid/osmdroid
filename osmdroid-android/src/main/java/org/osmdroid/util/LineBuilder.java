package org.osmdroid.util;

/**
 * Created by Fabrice on 03/01/2018.
 * @since 6.0.0
 */

public class LineBuilder implements PointAccepter {

    private final ListPointL mPoints = new ListPointL();
    private float[] mLines;
    private int mSize;

    @Override
    public void init() {
        mPoints.clear();
        mSize = 0;
    }

    @Override
    public void add(final long pX, final long pY) {
        mPoints.add(pX, pY);
    }

    @Override
    public void end() {
        mSize = mPoints.size() * 2;
        if (mSize < 4) {
            return;
        }
        if (mLines == null || mLines.length < mSize) {
            mLines = new float[mSize];
        }
        int index = 0;
        for (final PointL point : mPoints) {
            mLines[index ++] = point.x;
            mLines[index ++] = point.y;
        }
    }

    public float[] getLines() {
        return mLines;
    }

    public int getSize() {
        return mSize;
    }
}
