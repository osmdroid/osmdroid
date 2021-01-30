package org.osmdroid.server.jdk;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a simple command list web server (jetty based) that starts up a rest endpoint that serves map tiles.
 * <p>
 * By default it will attempt to start up on port 80.
 *
 * @author <a href="mailto:alexoree@apache.org">Alex O'Ree</a>
 */
public class TileServer {

    static int port = 80;
    static String ENDPOINT_ADDRESS;

    public static void main(String[] args) throws Exception {
        System.out.println("This will listen on port 80 by default for web traffic (on all IP addresses)");
        System.out.println("Usage");
        System.out.println("jar -jar <...with-dependencies.jar> <port>");
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        ENDPOINT_ADDRESS = "http://0.0.0.0:" + port + "/";

        System.out.println("Attempting to bind to " + ENDPOINT_ADDRESS);
        startServer();


        if (System.console() != null) {
            System.out.println("Server started at " + ENDPOINT_ADDRESS + " press enter to stop.");
            System.console().readLine();
        } else {
            System.out.println("Server started at " + ENDPOINT_ADDRESS + " press Ctrl-C to stop.");
            while (true) {
                Thread.sleep(5000);
            }
        }
        server.stop();
        server.destroy();

    }

    private static Server server;
    static TileFetcher instance = null;

    /**
     * this files up a CXF based Jetty server to host tile rest service
     *
     * @throws Exception
     */
    private static void startServer() throws Exception {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(TileFetcher.class);

        List<Object> providers = new ArrayList<Object>();
        // add custom providers if any
        providers.add(new org.apache.cxf.jaxrs.provider.JAXBElementProvider());
        providers.add(new org.apache.cxf.jaxrs.provider.json.JSONProvider());
        sf.setProviders(providers);

        sf.setResourceProvider(TileFetcher.class,
                new SingletonResourceProvider(new TileFetcher(), true));
        sf.setAddress(ENDPOINT_ADDRESS);

        server = sf.create();

    }
}
