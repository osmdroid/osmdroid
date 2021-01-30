package tests;

import org.junit.Test;
import org.nocrala.tools.gis.data.esri.shapefile.util.BAUtil;
import org.nocrala.tools.gis.data.esri.shapefile.util.DoubleSerializer;
import org.nocrala.tools.gis.data.esri.shapefile.util.HexaUtil;

import java.nio.ByteBuffer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


public class DoubleTests {


    @Test
    public void testBigEndian() {

        assertBe(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, 0);
        assertBe(new byte[]{63, -16, 0, 0, 0, 0, 0, 0}, 1);
        assertBe(new byte[]{64, 0, 0, 0, 0, 0, 0, 0}, 2);
        assertBe(new byte[]{-65, -16, 0, 0, 0, 0, 0, 0}, -1);

        assertBe(new byte[]{63, -32, 0, 0, 0, 0, 0, 0}, 0.5);
        assertBe(new byte[]{-65, -32, 0, 0, 0, 0, 0, 0}, -0.5);

        assertBe(new byte[]{64, 36, 0, 0, 0, 0, 0, 0}, 10);

        assertBe(new byte[]{64, 95, -64, 0, 0, 0, 0, 0}, 127);
        assertBe(new byte[]{64, 96, 0, 0, 0, 0, 0, 0}, 128);

        assertBe(new byte[]{64, 111, -32, 0, 0, 0, 0, 0}, 255);

        assertBe(new byte[]{64, 112, 0, 0, 0, 0, 0, 0}, 256);
        assertBe(new byte[]{64, 112, 16, 0, 0, 0, 0, 0}, 257);

        assertBe(new byte[]{65, -33, -1, -1, -1, -64, 0, 0}, Integer.MAX_VALUE);
        assertBe(new byte[]{-63, -32, 0, 0, 0, 0, 0, 0}, Integer.MIN_VALUE);

        assertBe(new byte[]{-65, -16, 0, 0, 0, 0, 0, 0}, -1);

        assertBe(new byte[]{-64, 112, 0, 0, 0, 0, 0, 0}, -256);
        assertBe(new byte[]{-64, 111, -32, 0, 0, 0, 0, 0}, -255);

        // Special deserialize cases

        try {
            assertEquals(0, deserBe(null));
            fail();
        } catch (Exception e) {
            // ok
        }

        try {
            assertEquals(0, deserBe(new byte[]{0, 0, 0}));
            fail();
        } catch (Exception e) {
            // ok
        }

        assertEquals(0.0, deserBe(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0}));

    }

    @Test
    public void testLittleEndian() {

        assertLe(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, 0);
        assertLe(new byte[]{0, 0, 0, 0, 0, 0, -16, 63}, 1);
        assertLe(new byte[]{0, 0, 0, 0, 0, 0, 0, 64}, 2);

        assertLe(new byte[]{0, 0, 0, 0, 0, 0, -16, -65}, -1);
        assertLe(new byte[]{0, 0, 0, 0, 0, 0, -32, 63}, 0.5);
        assertLe(new byte[]{0, 0, 0, 0, 0, 0, -32, -65}, -0.5);

        assertLe(new byte[]{0, 0, 0, 0, 0, 0, 36, 64}, 10);

        assertLe(new byte[]{0, 0, 0, 0, 0, -64, 95, 64}, 127);
        assertLe(new byte[]{0, 0, 0, 0, 0, 0, 96, 64}, 128);

        assertLe(new byte[]{0, 0, 0, 0, 0, -32, 111, 64}, 255);

        assertLe(new byte[]{0, 0, 0, 0, 0, 0, 112, 64}, 256);
        assertLe(new byte[]{0, 0, 0, 0, 0, 16, 112, 64}, 257);

        assertLe(new byte[]{0, 0, -64, -1, -1, -1, -33, 65}, Integer.MAX_VALUE);
        assertLe(new byte[]{0, 0, 0, 0, 0, 0, -32, -63}, Integer.MIN_VALUE);

        assertLe(new byte[]{0, 0, 0, 0, 0, 0, -16, -65}, -1);

        assertLe(new byte[]{0, 0, 0, 0, 0, 0, 112, -64}, -256);
        assertLe(new byte[]{0, 0, 0, 0, 0, -32, 111, -64}, -255);

        assertLe("A5 3E BE E9 FF 7F 66 C0", -179.999989387104, 0.000000001);

        // Special deserialize cases

        try {
            assertEquals(0, deserLe(null));
            fail();
        } catch (Exception e) {
            // ok
        }

        try {
            assertEquals(0, deserLe(new byte[]{0, 0, 0}));
            fail();
        } catch (Exception e) {
            // ok
        }

        assertEquals(0.0, deserLe(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0}));

    }

    // Utils Be

    private byte[] serBe(final double value) {
        byte[] result = new byte[8];
        DoubleSerializer.serializeBigEndian(value, ByteBuffer.wrap(result));
        return result;
    }

    private double deserBe(final byte[] b) {
        return DoubleSerializer.deserializeBigEndian(ByteBuffer.wrap(b));
    }

    private void assertBe(final byte[] serialized, final double value) {
        assertTrue(equals(serialized, serBe(value)));
        assertEquals(value, deserBe(serialized));
    }

    // Utils Le

    private byte[] serLe(final double value) {
        byte[] result = new byte[8];
        DoubleSerializer.serializeLittleEndian(value, ByteBuffer.wrap(result));
        return result;
    }

    private double deserLe(final byte[] b) {
        return DoubleSerializer.deserializeLittleEndian(ByteBuffer.wrap(b));
    }

    private void assertLe(final byte[] serialized, final double value) {
        assertTrue(equals(serialized, serLe(value)));
        assertEquals(value, deserLe(serialized));
    }

    private void assertLe(final String hexaSerialized, final double value,
                          final double delta) {
        byte[] serialized = HexaUtil.stringToByteArray(hexaSerialized);
        display(hexaSerialized + " --> " + HexaUtil.byteArrayToString(serialized));
        assertEquals(value, deserLe(serialized), delta);
    }

    // Helper

    private boolean equals(final byte[] expected, final byte[] actual) {
        if (expected == null) {
            BAUtil.displayByteArray("null", expected);
            return false;
        }
        if (actual == null || actual.length < expected.length) {
            BAUtil.displayByteArray("other", expected);
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                BAUtil.displayByteArray("exp", expected);
                BAUtil.displayByteArray("act", actual);
                return false;
            }
        }
        return true;
    }

    private static void display(final String txt) {
        // System.out.println(txt);
    }

}
