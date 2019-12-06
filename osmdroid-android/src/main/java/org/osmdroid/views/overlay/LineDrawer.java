package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;

import org.osmdroid.util.LineBuilder;
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingPlain;
import org.osmdroid.views.overlay.advancedpolyline.PolylineStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fabrice on 04/01/2018.
 * @since 6.0.0
 */

public class LineDrawer extends LineBuilder{

    private Canvas mCanvas;
    /**
     * A list holding all Paints to draw in the list order over each other.
     * Normal case is only one item in list.
     */
    private List<UniquePaintList> mPaintLists;

    public LineDrawer(int pMaxSize) {
        super(pMaxSize);
        mPaintLists = new ArrayList<>();
    }

    public void setCanvas(final Canvas pCanvas) {
        mCanvas = pCanvas;
    }

    public void setPaintList(final ArrayList<UniquePaintList> pArray) {
        mPaintLists = pArray;
    }

    @Override
    public void flush() {
        if(getSize() < 4) {
            // nothing to draw, just return
            return;
        }
        //  iterate over all paint list items
        for(PaintList item: mPaintLists) {
            // check for monochromatic
            if(item.isMonochromatic()) {
                // draw complete line with one paint
                mCanvas.drawLines(getLines(), 0, getSize(), item.getPaint(0));
            } else {
                // set references to color indexes and lines before loop
                item.setReferencesForAdvancedStyling(getColorIndexes(), getLines());
                // draw color mapping line segment by segment
                for (int i = 0; i < getSize() / 4; i++) {
                    mCanvas.drawLines(getLines(), i * 4, 4, item.getPaint(i));
                }
            }
        }
    }
}
