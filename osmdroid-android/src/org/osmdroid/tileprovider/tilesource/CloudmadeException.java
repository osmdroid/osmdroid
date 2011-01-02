package org.osmdroid.tileprovider.tilesource;

public class CloudmadeException extends Exception {

	private static final long serialVersionUID = 7375226751898110919L;

	public CloudmadeException() {
		super();
	}

	public CloudmadeException(final String aDetailMessage, final Throwable aThrowable) {
		super(aDetailMessage, aThrowable);
	}

	public CloudmadeException(final String aDetailMessage) {
		super(aDetailMessage);
	}

	public CloudmadeException(final Throwable aThrowable) {
		super(aThrowable);
	}
}
