package org.osmdroid.views.overlay;


/**
 * @author Fabrice Fontaine
 * @since 6.0.0
 */

public class OverlayLayoutParams {

    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int CENTER_HORIZONTAL = 4;
    public static final int TOP = 8;
    public static final int BOTTOM = 16;
    public static final int CENTER_VERTICAL = 32;

    public static int getMaskedValue(final int pValue, final int pDefault, final int[] pMasks) {
        for (int mask : pMasks) {
            if ((pValue & mask) == mask) {
                return mask;
            }
        }
        return pDefault;
    }
}
