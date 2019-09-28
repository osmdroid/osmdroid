package org.osmdroid.tileprovider.modules;

/**
 * @since 6.0.2
 * @author Fabrice Fontaine
 * Used to be embedded in MapTileModuleProviderBase
 *
 * Thrown by a tile provider module in TileLoader.loadTile() to signal that it can no longer
 * function properly. This will typically clear the pending queue.
 */
public class CantContinueException extends Exception {
    private static final long serialVersionUID = 146526524087765133L;

    public CantContinueException(final String pDetailMessage) {
        super(pDetailMessage);
    }

    public CantContinueException(final Throwable pThrowable) {
        super(pThrowable);
    }
}
