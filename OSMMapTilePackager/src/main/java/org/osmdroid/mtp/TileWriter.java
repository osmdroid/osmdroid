package org.osmdroid.mtp;

/**
 * created on 9/7/2024.
 *
 * @author Alex O'Ree
 */
public abstract class TileWriter {
    public abstract void open() throws Exception;
    public abstract void close()throws Exception;
    public abstract void write(long z,
 long x, long  y, String name, byte[] bits)throws Exception;
}
