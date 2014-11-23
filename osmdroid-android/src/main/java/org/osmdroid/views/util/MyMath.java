// Created by plusminus on 20:36:01 - 26.09.2008
package org.osmdroid.views.util;

/**
 * @author Nicolas Gramlich
 */
public class MyMath {
    /**
     * @param x
     * @return exponent of next higher power of 2 for x
     */
    public static final int nextHigherPow2Exp(final float x) {
        return (int) Math.ceil(Math.log(x) / Math.log(2.0));
    }
}
