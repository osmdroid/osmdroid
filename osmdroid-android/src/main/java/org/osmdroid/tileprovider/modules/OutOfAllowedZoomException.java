package org.osmdroid.tileprovider.modules;

public class OutOfAllowedZoomException extends Exception {

    public OutOfAllowedZoomException(final String pDetailMessage) {
        super(pDetailMessage);
    }

    public OutOfAllowedZoomException(final Throwable pThrowable) {
        super(pThrowable);
    }

}
