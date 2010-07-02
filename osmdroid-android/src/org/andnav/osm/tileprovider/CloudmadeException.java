package org.andnav.osm.tileprovider;

public class CloudmadeException extends Exception {

	private static final long serialVersionUID = 7375226751898110919L;

	public CloudmadeException() {
		super();
	}

	public CloudmadeException(String aDetailMessage, Throwable aThrowable) {
		super(aDetailMessage, aThrowable);
	}

	public CloudmadeException(String aDetailMessage) {
		super(aDetailMessage);
	}

	public CloudmadeException(Throwable aThrowable) {
		super(aThrowable);
	}
}
