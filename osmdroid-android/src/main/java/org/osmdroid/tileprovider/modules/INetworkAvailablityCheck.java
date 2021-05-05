package org.osmdroid.tileprovider.modules;

public interface INetworkAvailablityCheck {

    boolean getNetworkAvailable();

    boolean getWiFiNetworkAvailable();

    boolean getCellularDataNetworkAvailable();

    /**
     * this method calls a method that was removed API26
     * and this method will be removed from osmdroid sometime after
     * v6.0.0.
     *
     * @param hostAddress
     * @return
     */
    @Deprecated
    boolean getRouteToPathExists(int hostAddress);
}
