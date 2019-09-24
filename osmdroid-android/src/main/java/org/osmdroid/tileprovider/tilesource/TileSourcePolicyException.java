package org.osmdroid.tileprovider.tilesource;

/**
 * Exception dedicated to the enforcement of online tile source usage policies
 * @since 6.1.0
 * @author Fabrice Fontaine
 */
public class TileSourcePolicyException extends RuntimeException{

    public TileSourcePolicyException(final String pMessage) {
        super(pMessage);
    }
}
