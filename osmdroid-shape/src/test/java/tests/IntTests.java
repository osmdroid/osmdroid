package tests;

import org.junit.Test;
import org.nocrala.tools.gis.data.esri.shapefile.util.BAUtil;
import org.nocrala.tools.gis.data.esri.shapefile.util.IntSerializer;

import java.nio.ByteBuffer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class IntTests {

    @Test
    public void testBeInt() {

        assertBe(new byte[]{0, 0, 0, 0}, 0);
        assertBe(new byte[]{0, 0, 0, 1}, 1);
        assertBe(new byte[]{0, 0, 0, 2}, 2);
        assertBe(new byte[]{0, 0, 0, 10}, 10);

        assertBe(new byte[]{0, 0, 0, 127}, 127);
        assertBe(new byte[]{0, 0, 0, -128}, 128);

        assertBe(new byte[]{0, 0, 0, -1}, 255);

        assertBe(new byte[]{0, 0, 1, 0}, 256);
        assertBe(new byte[]{0, 0, 1, 1}, 257);

        assertBe(new byte[]{127, -1, -1, -1}, Integer.MAX_VALUE);
        assertBe(new byte[]{-128, 0, 0, 0}, Integer.MIN_VALUE);

        assertBe(new byte[]{-1, -1, -1, -1}, -1);

        assertBe(new byte[]{-1, -1, -1, 0}, -256);
        assertBe(new byte[]{-1, -1, -1, 1}, -255);

        // Special deserialize cases

        try {
            assertEquals(0, deserBeInt(null));
            fail();
        } catch (Exception e) {
            // ok
        }

        try {
            assertEquals(0, deserBeInt(new byte[]{0, 0, 0}));
            fail();
        } catch (Exception e) {
            // ok
        }

        assertEquals(0, deserBeInt(new byte[]{0, 0, 0, 0, 0}));

    }

    @Test
    public void testLeInt() {

        assertLe(new byte[]{0, 0, 0, 0}, 0);
        assertLe(new byte[]{1, 0, 0, 0}, 1);
        assertLe(new byte[]{2, 0, 0, 0}, 2);
        assertLe(new byte[]{10, 0, 0, 0}, 10);

        assertLe(new byte[]{127, 0, 0, 0}, 127);
        assertLe(new byte[]{-128, 0, 0, 0}, 128);

        assertLe(new byte[]{-1, 0, 0, 0}, 255);

        assertLe(new byte[]{0, 1, 0, 0}, 256);
        assertLe(new byte[]{1, 1, 0, 0}, 257);

        assertLe(new byte[]{-1, -1, -1, 127}, Integer.MAX_VALUE);
        assertLe(new byte[]{0, 0, 0, -128}, Integer.MIN_VALUE);

        assertLe(new byte[]{-1, -1, -1, -1}, -1);

        assertLe(new byte[]{0, -1, -1, -1}, -256);
        assertLe(new byte[]{1, -1, -1, -1}, -255);

        // Special deserialize cases

        try {
            assertEquals(0, deserLeInt(null));
            fail();
        } catch (Exception e) {
            // ok
        }

        try {
            assertEquals(0, deserLeInt(new byte[]{0, 0, 0}));
            fail();
        } catch (Exception e) {
            // ok
        }

        assertEquals(0, deserLeInt(new byte[]{0, 0, 0, 0, 0}));

    }

    // Utils Be

    private byte[] serBeInt(final int value) {
        byte[] result = new byte[4];
        IntSerializer.serializeBigEndian(value, ByteBuffer.wrap(result));
        return result;
    }

    private int deserBeInt(final byte[] b) {
        return IntSerializer.deserializeBigEndian(ByteBuffer.wrap(b));
    }

    private void assertBe(final byte[] serialized, final int value) {
        assertTrue(equals(serialized, serBeInt(value)));
        assertEquals(value, deserBeInt(serialized));
    }

    // Utils Le

    private byte[] serLeInt(final int value) {
        byte[] result = new byte[4];
        IntSerializer.serializeLittleEndian(value, ByteBuffer.wrap(result));
        return result;
    }

    private int deserLeInt(final byte[] b) {
        return IntSerializer.deserializeLittleEndian(ByteBuffer.wrap(b));
    }

    private void assertLe(final byte[] serialized, final int value) {
        assertTrue(equals(serialized, serLeInt(value)));
        assertEquals(value, deserLeInt(serialized));
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

}
