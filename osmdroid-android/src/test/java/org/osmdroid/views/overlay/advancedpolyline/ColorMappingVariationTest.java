package org.osmdroid.views.overlay.advancedpolyline;

import org.junit.Assert;
import org.junit.Test;

/**
 * Simple tests for class ColorMappingVariation.
 *
 * @author Matthias Dittmer
 */
public class ColorMappingVariationTest {

    private static final double delta = 1E-3;
    private static final ColorMappingVariation objTest = new ColorMappingVariation() {
        @Override
        protected float getHue(float pScalar) {
            return 0;
        }

        @Override
        protected float getSaturation(float pScalar) {
            return 0;
        }

        @Override
        protected float getLuminance(float pScalar) {
            return 0;
        }
    };

    @Test
    public void testAllZero() {
        objTest.init(0, 0, 0, 0);
        Assert.assertEquals(0, objTest.mapScalar(0), delta);
        Assert.assertEquals(0, objTest.mapScalar(-1), delta);
        Assert.assertEquals(0, objTest.mapScalar(5), delta);
    }

    @Test
    public void testSimple() {
        // linear mapping 0 to 100
        objTest.init(0, 100, 0, 100);
        Assert.assertEquals(0, objTest.mapScalar(0), delta);
        Assert.assertEquals(50, objTest.mapScalar(50), delta);
        Assert.assertEquals(100, objTest.mapScalar(100), delta);
        Assert.assertEquals(0, objTest.mapScalar(-4), delta);
        Assert.assertEquals(100, objTest.mapScalar(400), delta);
    }

    @Test
    public void testScalarOffset() {
        objTest.init(20, 100, 0, 100);
        Assert.assertEquals(0, objTest.mapScalar(0), delta);
        Assert.assertEquals(37.5, objTest.mapScalar(50), delta);
        Assert.assertEquals(100, objTest.mapScalar(100), delta);
        Assert.assertEquals(0, objTest.mapScalar(-4), delta);
        Assert.assertEquals(100, objTest.mapScalar(400), delta);
    }

    @Test
    public void testScalarNegative() {
        objTest.init(-200, 1000, 0, 100);
        Assert.assertEquals(16.6667, objTest.mapScalar(0), delta);
        Assert.assertEquals(58.3333, objTest.mapScalar(500), delta);
        Assert.assertEquals(100, objTest.mapScalar(1000), delta);
        Assert.assertEquals(100, objTest.mapScalar(1010), delta);
        Assert.assertEquals(0, objTest.mapScalar(-300), delta);
    }

    @Test
    public void testInverse() {
        objTest.init(-200, 1000, 300, -100);
        Assert.assertEquals(300, objTest.mapScalar(-200), delta);
        Assert.assertEquals(233.333, objTest.mapScalar(0), delta);
        Assert.assertEquals(-100, objTest.mapScalar(1000), delta);
        Assert.assertEquals(-100, objTest.mapScalar(1010), delta);
        Assert.assertEquals(300, objTest.mapScalar(-300), delta);
    }
}