// Created by plusminus on 19:14:08 - 20.10.2008
package org.osmdroid.tileprovider.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * don't use android classes here, since this class is used outside of android
 */
public class StreamUtils {

    // ===========================================================
    // Constants
    // ===========================================================

    public static final int IO_BUFFER_SIZE = 8 * 1024;

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * This is a utility class with only static members.
     */
    private StreamUtils() {
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * Copy the content of the input stream into the output stream, using a temporary byte array
     * buffer whose size is defined by {@link #IO_BUFFER_SIZE}.
     *
     * @param in  The input stream to copy from.
     * @param out The output stream to copy to.
     * @return the total length copied
     * @throws IOException If any error occurs during the copy.
     */
    public static long copy(final InputStream in, final OutputStream out) throws IOException {
        long length = 0;
        final byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
            length += read;
        }
        return length;
    }

    /**
     * Closes the specified stream.
     *
     * @param stream The stream to close.
     */
    public static void closeStream(final Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (final IOException e) {
                //don't use android classes here, since this class is used outside of android
                e.printStackTrace();
            }
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
