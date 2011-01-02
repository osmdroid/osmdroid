package org.osmdroid.tileprovider;

public interface INetworkAvailablityCheck {

	boolean getNetworkAvailable();

	boolean getWiFiNetworkAvailable();

	boolean getCellularDataNetworkAvailable();

	boolean getRouteToPathExists(int hostAddress);
}
