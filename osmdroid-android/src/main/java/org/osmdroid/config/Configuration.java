package org.osmdroid.config;

/**
 * Singleton class to get/set a configuration provider for osmdroid
 * <a href="https://github.com/osmdroid/osmdroid/issues/481">Issue 481</a>
 * Created on 11/29/2016.
 *
 * @author Alex O'Ree
 * @see org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants
 * @since 5.6
 */

public class Configuration {
    private static IConfigurationProvider ref;


    /**
     * gets the current reference to the config provider.
     * if one hasn't been set yet, the default provider and default configuration will be used
     *
     * @return
     */
    public static synchronized IConfigurationProvider getInstance() {
        if (ref == null)
            ref = new DefaultConfigurationProvider();
        return ref;
    }

    /**
     * Note, this should be called before any instances of MapView are created (either programmatically
     * or via android's inflater
     *
     * @param instance
     * @see android.view.LayoutInflater
     */
    public static void setConfigurationProvider(IConfigurationProvider instance) {
        ref = instance;
    }
}
