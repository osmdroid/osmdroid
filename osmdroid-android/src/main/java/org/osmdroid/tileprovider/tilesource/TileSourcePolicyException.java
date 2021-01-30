package org.osmdroid.tileprovider.tilesource;

/**
 * Exception dedicated to the enforcement of online tile source usage policies
 *
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class TileSourcePolicyException extends RuntimeException {

    public TileSourcePolicyException(final String pMessage) {
        super(pMessage);
    }
}
