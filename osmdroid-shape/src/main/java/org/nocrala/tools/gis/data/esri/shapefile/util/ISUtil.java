package org.nocrala.tools.gis.data.esri.shapefile.util;

import org.nocrala.tools.gis.data.esri.shapefile.exception.DataStreamEOFException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ISUtil {

    private static final byte[] BUFFER = new byte[8];
    private static final ByteBuffer BYTE_BUFFER = ByteBuffer.wrap(BUFFER);

    // Big endian int

    public static synchronized int readBeIntMaybeEOF(final InputStream is)
            throws DataStreamEOFException, IOException {
        readIntoBufferMaybeEOF(is, 4);
        return IntSerializer.deserializeBigEndian(BYTE_BUFFER);
    }

    public static synchronized int readBeInt(final InputStream is)
            throws IOException {
        readIntoBuffer(is, 4);
        return IntSerializer.deserializeBigEndian(BYTE_BUFFER);
    }

    // Big endian double

    public static synchronized double readBeDoubleMaybeEOF(final InputStream is)
            throws DataStreamEOFException, IOException {
        readIntoBufferMaybeEOF(is, 8);
        return DoubleSerializer.deserializeBigEndian(BYTE_BUFFER);
    }

    public static synchronized double readBeDouble(final InputStream is)
            throws IOException {
        readIntoBuffer(is, 8);
        return DoubleSerializer.deserializeBigEndian(BYTE_BUFFER);
    }

    // Little endian int

    public static synchronized int readLeIntMaybeEOF(final InputStream is)
            throws DataStreamEOFException, IOException {
        readIntoBufferMaybeEOF(is, 4);
        return IntSerializer.deserializeLittleEndian(BYTE_BUFFER);
    }

    public static synchronized int readLeInt(final InputStream is)
            throws IOException {
        readIntoBuffer(is, 4);
        // System.out.println("--> " + HexaUtil.byteArrayToString(BUFFER));
        return IntSerializer.deserializeLittleEndian(BYTE_BUFFER);
    }

    // Little endian double

    public static synchronized double readLeDoubleMaybeEOF(final InputStream is)
            throws DataStreamEOFException, IOException {
        readIntoBufferMaybeEOF(is, 8);
        return DoubleSerializer.deserializeLittleEndian(BYTE_BUFFER);
    }

    public static synchronized double readLeDouble(final InputStream is)
            throws IOException {
        readIntoBuffer(is, 8);
        return DoubleSerializer.deserializeLittleEndian(BYTE_BUFFER);
    }

    // Utils

    private static void readIntoBufferMaybeEOF(final InputStream is,
                                               final int length) throws DataStreamEOFException, IOException {
        try {
            int read = is.read(BUFFER, 0, length);
            if (read != length) {
                throw new DataStreamEOFException();
            }
        } catch (EOFException e) {
            throw new DataStreamEOFException();
        }
    }

    private static void readIntoBuffer(final InputStream is, final int length)
            throws IOException {
        int read = is.read(BUFFER, 0, length);
        if (read != length) {
            throw new EOFException();
        }
    }

}
