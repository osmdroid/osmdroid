package org.osmdroid.views.overlay.advancedpolyline;

import android.graphics.Color;

/**
 * Class with color helper functions.
 * Please note the functions were copied over from:
 * https://developer.android.com/reference/android/support/v4/graphics/ColorUtils (old support lib)
 * https://developer.android.com/reference/kotlin/androidx/core/graphics/ColorUtils (new Androidx lib)
 * Maybe include one lib directly.
 * @author Matthias Dittmer
 */
public class ColorHelper {

    /**
     * Convert HSL to color value.
     * @param hsl float array for HSL value
     * @return int color value
     */
    public static int HSLToColor(float[] hsl) {
        final float h = hsl[0];
        final float s = hsl[1];
        final float l = hsl[2];

        final float c = (1f - Math.abs(2 * l - 1f)) * s;
        final float m = l - 0.5f * c;
        final float x = c * (1f - Math.abs((h / 60f % 2f) - 1f));

        final int hueSegment = (int) h / 60;

        int r = 0, g = 0, b = 0;

        switch (hueSegment) {
            case 0:
                r = Math.round(255 * (c + m));
                g = Math.round(255 * (x + m));
                b = Math.round(255 * m);
                break;
            case 1:
                r = Math.round(255 * (x + m));
                g = Math.round(255 * (c + m));
                b = Math.round(255 * m);
                break;
            case 2:
                r = Math.round(255 * m);
                g = Math.round(255 * (c + m));
                b = Math.round(255 * (x + m));
                break;
            case 3:
                r = Math.round(255 * m);
                g = Math.round(255 * (x + m));
                b = Math.round(255 * (c + m));
                break;
            case 4:
                r = Math.round(255 * (x + m));
                g = Math.round(255 * m);
                b = Math.round(255 * (c + m));
                break;
            case 5:
            case 6:
                r = Math.round(255 * (c + m));
                g = Math.round(255 * m);
                b = Math.round(255 * (x + m));
                break;
        }

        r = constrain(r, 0, 255);
        g = constrain(g, 0, 255);
        b = constrain(b, 0, 255);

        return Color.rgb(r, g, b);
    }

    /**
     * Constriant int value.
     * @param amount input value
     * @param low lower bound
     * @param high upper bound
     * @return
     */
    private static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    /**
     * Constrain float value.
     * @param amount input value
     * @param low lower bound
     * @param high upper bound
     * @return
     */
    public static float constrain(float amount, float low, float high) {
        return amount < low ? low : (amount > high ? high : amount);
    }
}
