// Created by plusminus on 19:14:08 - 20.10.2008
package org.osmdroid.tileprovider.util;

import android.util.Log;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.osmdroid.api.IMapView;

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
	 * @param in
	 *            The input stream to copy from.
	 * @param out
	 *            The output stream to copy to.
	 * @return the total length copied
	 * 
	 * @throws IOException
	 *             If any error occurs during the copy.
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
	 * @param stream
	 *            The stream to close.
	 */
	public static void closeStream(final Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (final IOException e) {
				Log.e(IMapView.LOGTAG,"Could not close stream", e);
			}
		}
	}

	/**
	 * Simple wrapper around {@link InputStream#read()} that throws EOFException
	 * instead of returning -1.
	 */
	private static int read(InputStream is) throws IOException {
		int b = is.read();
		if (b == -1) {
			throw new EOFException();
		}
		return b;
	}

	public static void writeInt(OutputStream os, int n) throws IOException {
		os.write((n >> 0) & 0xff);
		os.write((n >> 8) & 0xff);
		os.write((n >> 16) & 0xff);
		os.write((n >> 24) & 0xff);
	}

	public static int readInt(InputStream is) throws IOException {
		int n = 0;
		n |= (read(is) << 0);
		n |= (read(is) << 8);
		n |= (read(is) << 16);
		n |= (read(is) << 24);
		return n;
	}

	public static void writeLong(OutputStream os, long n) throws IOException {
		os.write((byte)(n >>> 0));
		os.write((byte)(n >>> 8));
		os.write((byte)(n >>> 16));
		os.write((byte)(n >>> 24));
		os.write((byte)(n >>> 32));
		os.write((byte)(n >>> 40));
		os.write((byte)(n >>> 48));
		os.write((byte)(n >>> 56));
	}

	public static long readLong(InputStream is) throws IOException {
		long n = 0;
		n |= ((read(is) & 0xFFL) << 0);
		n |= ((read(is) & 0xFFL) << 8);
		n |= ((read(is) & 0xFFL) << 16);
		n |= ((read(is) & 0xFFL) << 24);
		n |= ((read(is) & 0xFFL) << 32);
		n |= ((read(is) & 0xFFL) << 40);
		n |= ((read(is) & 0xFFL) << 48);
		n |= ((read(is) & 0xFFL) << 56);
		return n;
	}

	public static void writeString(OutputStream os, String s) throws IOException {
		byte[] b = s.getBytes("UTF-8");
		writeLong(os, b.length);
		os.write(b, 0, b.length);
	}

	public static String readString(InputStream is) throws IOException {
		int n = (int) readLong(is);
		byte[] b = streamToBytes(is, n);
		return new String(b, "UTF-8");
	}

	public static void writeStringStringMap(Map<String, String> map, OutputStream os) throws IOException {
		if (map != null) {
			writeInt(os, map.size());
			for (Map.Entry<String, String> entry : map.entrySet()) {
				writeString(os, entry.getKey());
				writeString(os, entry.getValue());
			}
		} else {
			writeInt(os, 0);
		}
	}

	public static Map<String, String> readStringStringMap(InputStream is) throws IOException {
		int size = readInt(is);
		Map<String, String> result = (size == 0)
			? Collections.<String, String>emptyMap()
			: new HashMap<String, String>(size);
		for (int i = 0; i < size; i++) {
			String key = readString(is).intern();
			String value = readString(is).intern();
			result.put(key, value);
		}
		return result;
	}

	/**
	 * Reads the contents of an InputStream into a byte[].
	 * */
	private static byte[] streamToBytes(InputStream in, int length) throws IOException {
		byte[] bytes = new byte[length];
		int count;
		int pos = 0;
		while (pos < length && ((count = in.read(bytes, pos, length - pos)) != -1)) {
			pos += count;
		}
		if (pos != length) {
			throw new IOException("Expected " + length + " bytes, read " + pos + " bytes");
		}
		return bytes;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
