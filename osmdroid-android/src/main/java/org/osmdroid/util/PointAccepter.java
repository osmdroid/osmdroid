package org.osmdroid.util;

/**
 * Created by Fabrice on 23/12/2017.
 *
 * @since 6.0.0
 */

public interface PointAccepter {

    void init();

    void add(final long pX, final long pY);

    void end();
}
