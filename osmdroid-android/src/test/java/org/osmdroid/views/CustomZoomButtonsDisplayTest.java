package org.osmdroid.views;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Random;

/**
 * @author Fabrice Fontaine
 * @since 6.1.0
 */

public class CustomZoomButtonsDisplayTest {

    private final Random mRandom = new Random();

    @Test
    public void testTopLeft() {
        final int randomMargin = 20;
        final int bitmapSize = 50 + mRandom.nextInt(randomMargin);
        final int margin = 25 + mRandom.nextInt(randomMargin);
        final int padding = 16 + mRandom.nextInt(randomMargin);
        final float strokeWidth = bitmapSize * mRandom.nextFloat();
        final int segmentSize = mRandom.nextInt(bitmapSize);
        final int mapViewWidth = 600 + mRandom.nextInt(randomMargin);
        final int mapViewHeight = 800 + mRandom.nextInt(randomMargin);
        final boolean[] horizontalOrVerticals = new boolean[] {true, false};
        final CustomZoomButtonsDisplay.HorizontalPosition[] horizontalPositions = new CustomZoomButtonsDisplay.HorizontalPosition[] {
                CustomZoomButtonsDisplay.HorizontalPosition.LEFT,
                CustomZoomButtonsDisplay.HorizontalPosition.CENTER,
                CustomZoomButtonsDisplay.HorizontalPosition.RIGHT,
        };
        final CustomZoomButtonsDisplay.VerticalPosition[] verticalPositions = new CustomZoomButtonsDisplay.VerticalPosition[] {
                CustomZoomButtonsDisplay.VerticalPosition.TOP,
                CustomZoomButtonsDisplay.VerticalPosition.CENTER,
                CustomZoomButtonsDisplay.VerticalPosition.BOTTOM,
        };
	    final CustomZoomButtonsDisplay display = new CustomZoomButtonsDisplay(null);
        display.setDrawingSizes(bitmapSize, strokeWidth, segmentSize);
        display.setMarginPadding(margin, padding);

        for (final boolean horizontalOrVertical : horizontalOrVerticals) {
            for (final CustomZoomButtonsDisplay.HorizontalPosition horizontalPosition : horizontalPositions) {
                for (final CustomZoomButtonsDisplay.VerticalPosition verticalPosition : verticalPositions) {
                    display.setPositions(horizontalOrVertical, horizontalPosition, verticalPosition);
                    if (horizontalPosition == CustomZoomButtonsDisplay.HorizontalPosition.LEFT) {
                        Assert.assertEquals(margin, display.getLeftForZoomIn(mapViewWidth));
                    }
                    if (verticalPosition == CustomZoomButtonsDisplay.VerticalPosition.TOP) {
                        Assert.assertEquals(margin, display.getTopForZoomIn(mapViewHeight));
                    }
                    if (horizontalOrVertical) {
                        switch(horizontalPosition) {
                            case CENTER:
                                Assert.assertEquals(mapViewWidth / 2 - bitmapSize - padding / 2, display.getLeftForZoomIn(mapViewWidth)); break;
                            case RIGHT:
                                Assert.assertEquals(mapViewWidth - 2 * bitmapSize - margin - padding, display.getLeftForZoomIn(mapViewWidth)); break;
                        }
                        switch(verticalPosition) {
                            case CENTER:
                                Assert.assertEquals(mapViewHeight / 2 - bitmapSize / 2, display.getTopForZoomIn(mapViewHeight)); break;
                            case BOTTOM:
                                Assert.assertEquals(mapViewHeight - bitmapSize - margin, display.getTopForZoomIn(mapViewHeight)); break;
                        }
                    } else {
                        switch(horizontalPosition) {
                            case CENTER:
                                Assert.assertEquals(mapViewWidth / 2 - bitmapSize / 2, display.getLeftForZoomIn(mapViewWidth)); break;
                            case RIGHT:
                                Assert.assertEquals(mapViewWidth - bitmapSize - margin, display.getLeftForZoomIn(mapViewWidth)); break;
                        }
                        switch(verticalPosition) {
                            case CENTER:
                                Assert.assertEquals(mapViewHeight / 2 - bitmapSize - padding / 2, display.getTopForZoomIn(mapViewHeight)); break;
                            case BOTTOM:
                                Assert.assertEquals(mapViewHeight - 2 * bitmapSize - margin - padding, display.getTopForZoomIn(mapViewHeight)); break;
                        }
                    }
                }
            }
        }
    }
}
