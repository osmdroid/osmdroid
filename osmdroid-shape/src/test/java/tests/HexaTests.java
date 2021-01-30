package tests;

import org.junit.Test;
import org.nocrala.tools.gis.data.esri.shapefile.util.HexaUtil;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class HexaTests {
    @Test
    public void testOneByte() {

        byte[] serialized = HexaUtil.stringToByteArray("12");
        assertNotNull(serialized);
        assertEquals(1, serialized.length);
        assertEquals(18, serialized[0]);

        String back = HexaUtil.byteArrayToString(serialized);
        // System.out.println("back='" + back + "'");
        assertEquals("12", back);

    }

    @Test
    public void testTwoBytes() {

        byte[] serialized = HexaUtil.stringToByteArray("cdef");
        assertNotNull(serialized);
        assertEquals(2, serialized.length);
        assertEquals(205 - 256, serialized[0]);
        assertEquals(239 - 256, serialized[1]);

        String back = HexaUtil.byteArrayToString(serialized);
        // System.out.println("back='" + back + "'");
        assertEquals("cdef", back);

    }

    @Test
    public void testTwoBytesUppercase() {

        byte[] serialized = HexaUtil.stringToByteArray("CDEF");
        assertNotNull(serialized);
        assertEquals(2, serialized.length);
        assertEquals(205 - 256, serialized[0]);
        assertEquals(239 - 256, serialized[1]);

        String back = HexaUtil.byteArrayToString(serialized);
        // System.out.println("back='" + back + "'");
        assertEquals("cdef", back);

    }

}
