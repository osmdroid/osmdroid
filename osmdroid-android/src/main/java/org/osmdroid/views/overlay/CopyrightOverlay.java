package org.osmdroid.views.overlay;

////////////////////////////////////////////////////////////////////////////////
//
//  Location - An Android location app.
//
//  Copyright (C) 2015	Bill Farmer
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//  
//       http://www.apache.org/licenses/LICENSE-2.0

//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//  Bill Farmer	 william j farmer [at] yahoo [dot] co [dot] uk.
//
///////////////////////////////////////////////////////////////////////////////


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;


/**
 * CopyrightOverlay - uses the {@link ITileSource#getCopyrightNotice()} text to paint on the screen
 *
 * <a href="https://github.com/billthefarmer/location/blob/master/src/main/java/org/billthefarmer/location/CopyrightOverlay.java">Original source</a>
 *
 * <a href="https://github.com/osmdroid/osmdroid/issues/501">Issue 501</a>
 *
 * <a href="http://www.openstreetmap.org/copyright/en">Open Street Map's guidance on attribution</a>
 * created on 1/2/2017.
 *
 * @author billthefarmer@github
 * @author Alex O'Ree
 * @since 5.6.3
 */
public class CopyrightOverlay extends Overlay {
    private Paint paint;
    int xOffset = 10;
    int yOffset = 10;
    protected boolean alignBottom = true;
    protected boolean alignRight = false;
    final DisplayMetrics dm;
    private String mCopyrightNotice;
    // Constructor

    public CopyrightOverlay(Context context) {
        super();

        // Get the string
        Resources resources = context.getResources();

        // Get the display metrics
        dm = resources.getDisplayMetrics();

        // Get paint
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(dm.density * 12);
    }

    public void setTextSize(int fontSize) {
        paint.setTextSize(dm.density * fontSize);
    }

    public void setTextColor(int color) {
        paint.setColor(color);
    }
    // Set alignBottom

    public void setAlignBottom(boolean alignBottom) {
        this.alignBottom = alignBottom;
    }

    // Set alignRight

    public void setAlignRight(boolean alignRight) {
        this.alignRight = alignRight;
    }

    /**
     * Sets the screen offset. Values are in real pixels, not dip
     *
     * @param x horizontal screen offset, if aligh right is set, the offset is from the right, otherwise lift
     * @param y vertical screen offset, if align bottom is set, the offset is pixels from the bottom (not the top)
     */
    public void setOffset(final int x, final int y) {
        xOffset = x;
        yOffset = y;
    }

    @Override
    public void draw(Canvas canvas, MapView map, boolean shadow) {
        setCopyrightNotice(map.getTileProvider().getTileSource().getCopyrightNotice());
        draw(canvas, map.getProjection());
    }

    /**
     * @since 6.1.0
     */
    @Override
    public void draw(final Canvas canvas, final Projection pProjection) {
        if (mCopyrightNotice == null || mCopyrightNotice.length() == 0)
            return;

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        float x = 0;
        float y = 0;

        if (alignRight) {
            x = width - xOffset;
            paint.setTextAlign(Paint.Align.RIGHT);
        } else {
            x = xOffset;
            paint.setTextAlign(Paint.Align.LEFT);
        }

        if (alignBottom)
            y = height - yOffset;
        else
            y = paint.getTextSize() + yOffset;

        // Draw the text
        pProjection.save(canvas, false, false);
        canvas.drawText(mCopyrightNotice, x, y, paint);
        pProjection.restore(canvas, false);
    }

    /**
     * @since 6.1.0
     */
    public void setCopyrightNotice(final String pCopyrightNotice) {
        mCopyrightNotice = pCopyrightNotice;
    }
}
